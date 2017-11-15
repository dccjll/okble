package com.bluetoothle.core.node;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.text.TextUtils;

import com.bluetoothle.base.BLECode;
import com.bluetoothle.base.BLEConfig;
import com.bluetoothle.base.BLESDKLibrary;
import com.bluetoothle.core.listener.OnBLEScanListener;
import com.bluetoothle.core.manage.BLEGattCallback;
import com.bluetoothle.core.manage.BLEManage;
import com.bluetoothle.util.BLEUtil;
import com.bluetoothle.util.log.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 13:33<br>
 * 功能描述：蓝牙扫描<br>
 */
@SuppressWarnings("ALL")
public class BLEScan {

    private BLEManage bleManage;
    private BLEConnect bleConnect;

    private int currentScanCount = 0;//当前扫描次数
    private final static String TAG = BLEScan.class.getSimpleName();
    private final List<Map<String, Object>> foundDeviceList = new ArrayList<>();
    private Boolean isScaning = false;
    private Handler scanHandler;
    private Runnable scanRunnable;
    private boolean foundDevice = false;//是否已找到设备
    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            LogUtil.i(TAG, "扫描到设备,deviceMac=" + device);
            if(foundDevice){
                LogUtil.i(TAG, "已找到设备，略过停止扫描间隙扫描到的设备");
                return;
            }
            boolean containsDevice = false;
            for (Map<String, Object> bluetoothDeviceMap : BLEManage.bluetoothDeviceList) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) bluetoothDeviceMap.get("bluetoothDevice");
                if (bluetoothDevice.equals(device)) {
                    containsDevice = true;
                    break;
                }
            }
            if (!containsDevice) {
                Map<String, Object> bluetoothDeviceMap = new HashMap<>();
                bluetoothDeviceMap.put("foundTime", System.currentTimeMillis());
                bluetoothDeviceMap.put("bluetoothDevice", device);
                BLEManage.bluetoothDeviceList.add(bluetoothDeviceMap);
            }
            synchronized (TAG) {
                Map<String, Object> existDevice = null;
                for(Map<String, Object> entry : foundDeviceList){
                    if(((BluetoothDevice)entry.get("device")).getAddress().equalsIgnoreCase(device.getAddress())){
                        existDevice = entry;
                        break;
                    }
                }
                if(existDevice == null){//已扫描到的列表中没有这个设备，新加
                    Map<String, Object> deviceAttrMap = new HashMap<>();
                    deviceAttrMap.put("device", device);
                    deviceAttrMap.put("rssi", rssi);
                    deviceAttrMap.put("scanRecord", scanRecord);
                    foundDeviceList.add(deviceAttrMap);
                } else {//已扫描到的列表中有这个设备，更新
                    existDevice.put("device", device);
                    existDevice.put("rssi", rssi);
                    existDevice.put("scanRecord", scanRecord);
                }
                if(!TextUtils.isEmpty(bleManage.getTargetDeviceAddress())){
                    if(device.getAddress().equalsIgnoreCase(bleManage.getTargetDeviceAddress())){
                        onFoundDevice(device, rssi, scanRecord);
                    }
                }else if(bleManage.getTargetDeviceAddressList() != null && bleManage.getTargetDeviceAddressList().size() > 0){
                    if(bleManage.getTargetDeviceAddressList().contains(device.getAddress())){
                        onFoundDevice(device, rssi, scanRecord);
                    }
                }else if(!TextUtils.isEmpty(bleManage.getTargetDeviceName())){
                    if(device.getName() != null && device.getName().equalsIgnoreCase(bleManage.getTargetDeviceName())){
                        onFoundDevice(device, rssi, scanRecord);
                    }
                }else{
                    onFoundDevice(device, rssi, scanRecord);
                }
            }
        }
    };

    public BLEScan(BLEManage bleManage) {
        this.bleManage = bleManage;
    }

    /**
     * 扫描设备
     */
    public void startScan(){
        LogUtil.i(TAG, "准备开始扫描");
        if(BLESDKLibrary.bluetoothAdapter == null){
            bleManage.handleError(BLECode.can_not_get_ble_adapter);
            return;
        }
        if (!BLESDKLibrary.bluetoothAdapter.isEnabled()) {
            bleManage.handleError(BLECode.blutooth_is_closed);
            return;
        }
        if (!BLESDKLibrary.checkLocationAvailable(BLESDKLibrary.context)) {
            bleManage.handleError(BLECode.need_location_permission);
            return;
        }
        if (++currentScanCount > BLEConfig.MAX_SCAN_COUNT) {
            LogUtil.i(TAG, "扫描失败,已尝试最大扫描次数");
            if (!(bleManage.getListenterObject() instanceof OnBLEScanListener) && BLEUtil.checkAddress(bleManage.getTargetDeviceAddress())) {//总体任务不是执行扫描任务，并且扫描的是指定的单个设备，在扫描失败后发起直连请求
                bleConnect = new BLEConnect(bleManage.getTargetDeviceAddress(), bleManage);
                bleConnect.connect();
            } else {
                bleManage.handleError(BLECode.not_found_device);
            }
            return;
        }
        scanHandler = new Handler(BLESDKLibrary.context.getMainLooper());
        scanRunnable = new Runnable() {
            @Override
            public void run() {
                scanControl(false);
                startScan();//重新开始扫描
            }
        };
        if (!BLEConfig.LONG_SCAN_FALG) {//长扫描标记会一直扫描
            scanHandler.postDelayed(scanRunnable, bleManage.getTimeoutScanBLE());
        }
        foundDeviceList.clear();
        BLEManage.disconnect(BLEGattCallback.lastBluetoothGatt);
        LogUtil.i(TAG, "第" + currentScanCount + "次开始扫描");
        try {
            scanControl(true);
        } catch (Exception e) {
            e.printStackTrace();
            bleManage.handleError(BLECode.on_start_scan_exception);
        }
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        scanControl(false);
    }

    /**
     * 扫描控制,scanFlag=true 开始扫描 scanFlag=false 停止扫描
     */
    private void scanControl(Boolean scanFlag){
        if(scanFlag){
            if(isScaning){
                bleManage.handleError(BLECode.scanning);
                return;
            }
            isScaning = true;
            foundDevice = false;
            BLESDKLibrary.bluetoothAdapter.startLeScan(leScanCallback);
        }else{
            BLESDKLibrary.bluetoothAdapter.stopLeScan(leScanCallback);
            if (scanHandler != null) {
                scanHandler.removeCallbacks(scanRunnable);
            }
            isScaning = false;
        }
    }

    /**
     * 发现设备
     */
    private void onFoundDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        LogUtil.i(TAG, "已找到设备,mac=" + device);
        if (bleManage.getTargetDeviceAddressList() != null && bleManage.getTargetDeviceAddressList().size() > 0 || !TextUtils.isEmpty(bleManage.getTargetDeviceAddress()) || !TextUtils.isEmpty(bleManage.getTargetDeviceName())) {
            foundDevice = true;
            scanControl(false);
        }
        bleManage.setBluetoothDevice(device);
        bleManage.setTargetDeviceAddress(device.getAddress());
        if (bleManage.getListenterObject() instanceof OnBLEScanListener) {
            bleManage.getBleResponseManager().onFoundDevice(device, rssi, scanRecord);
            return;
        }
        if (!bleManage.getRunning()) {
            LogUtil.e(TAG, "准备连接时任务已停止");
            return;
        }
        bleConnect = new BLEConnect(device, bleManage);
        bleConnect.connect();
    }
}
