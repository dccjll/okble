package com.bluetoothle.core.node;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.bluetoothle.base.BLECode;
import com.bluetoothle.base.BLEConfig;
import com.bluetoothle.base.BLESDKLibrary;
import com.bluetoothle.core.listener.OnBLEConnectListener;
import com.bluetoothle.core.listener.OnBLEResponse;
import com.bluetoothle.core.manage.BLEGattCallback;
import com.bluetoothle.core.manage.BLEManage;
import com.bluetoothle.util.log.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 13:27<br>
 * 功能描述：连接设备<br>
 */
public class BLEConnect {

    private final static String TAG = BLEConnect.class.getSimpleName();
    private int currentConnectCount = 0;//当前连接次数
    private final Context context;//蓝牙连接的上下文对象
    private BluetoothDevice bluetoothDevice;//需要连接的蓝牙设备
    private String targetMacAddress;//远程蓝牙设备的mac地址
    private BLEManage bleManage;

    /**
     * 连接蓝牙服务器回调接口
     */
    public interface OnGattBLEConnectListener {
        void onConnectSuccss(BluetoothGatt bluetoothGatt, int status, int newState, BLEGattCallback bleGattCallback);
        void onConnectFail(String errorMsg, int loglevel, BluetoothGatt bluetoothGatt);
    }

    /**
     * 指定设备对象连接设备
     */
    public BLEConnect(BluetoothDevice bluetoothDevice, BLEManage bleManage) {
        this(bleManage);
        this.bluetoothDevice = bluetoothDevice;
    }

    /**
     * 指定设备MAC地址连接设备
     */
    public BLEConnect(String targetMacAddress, BLEManage bleManage) {
        this(bleManage);
        this.targetMacAddress = targetMacAddress;
    }

    /**
     * 私有构造器
     */
    private BLEConnect(BLEManage bleManage) {
        context = BLESDKLibrary.context;
        currentConnectCount = 0;
        this.bleManage = bleManage;
        if (bleManage.getBleGattCallback() == null) {
            bleManage.setBleGattCallback(new BLEGattCallback());
            bleManage.getBleGattCallback().setBLEResponseManager(bleManage.getBleResponseManager());
            if (bleManage.getListenterObject() instanceof OnBLEResponse) {
                bleManage.getBleGattCallback().registerOnBLEResponse((OnBLEResponse) bleManage.getListenterObject());
            }
        }
    }

    /**
     * 连接设备
     */
    public void connect(){
        LogUtil.i(TAG, "准备开始连接");
        if(context == null){
            throw new IllegalArgumentException("context == null");
        }
        if (BLESDKLibrary.context == null) {
            throw new IllegalArgumentException("BLESDKLibrary.context == null");
        }
        if (BLESDKLibrary.bluetoothAdapter == null) {
            throw new IllegalArgumentException("BLESDKLibrary.bluetoothAdapter == null");
        }
        if (!BLESDKLibrary.bluetoothAdapter.isEnabled()) {
            bleManage.handleError(BLECode.blutooth_is_closed);
            return;
        }
        LogUtil.i(TAG, "bluetoothDevice=" + bluetoothDevice + "\nbluetoothAdapter=" + BLESDKLibrary.bluetoothAdapter + "\ntargetMacAddress=" + targetMacAddress);
        if(++currentConnectCount > BLEConfig.MAX_CONNECT_COUNT){
            bleManage.handleError(BLECode.connect_fail_reach_to_max_count);
            return;
        }
        if(bluetoothDevice == null && (BLESDKLibrary.bluetoothAdapter == null || targetMacAddress == null || targetMacAddress.split(":").length != 6)){
            bleManage.handleError(BLECode.on_bluetooth_device_or_adapter_or_mac_validate_failure);
            return;
        }
        List<BluetoothDevice> bluetoothDevices = BLESDKLibrary.bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        if (bluetoothDevices != null && bluetoothDevices.size() > 0) {
            LogUtil.i(TAG, "有连接的设备列表=" + bluetoothDevices);
            if (bluetoothDevices.size() >= BLEConfig.MaxConnectDeviceNum) {
                bleManage.handleError(BLECode.already_connect_max_count_device_can_not_connect_more);
                return;
            }
            for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                if (BLEConnect.this.bluetoothDevice == bluetoothDevice) {
                    LogUtil.i(TAG, "根据设备对象匹配到已连接的设备,device=" + bluetoothDevice);
                    onConnectDeivceSuccss(BLEManage.getBluetoothGatt(bluetoothDevice.getAddress()), BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);
                    return;
                }
                if (bluetoothDevice.getAddress().equalsIgnoreCase(bleManage.getTargetDeviceAddress())) {
                    LogUtil.i(TAG, "根据mac地址匹配到已连接的设备,mac=" + bluetoothDevice.getAddress());
                    onConnectDeivceSuccss(BLEManage.getBluetoothGatt(bleManage.getTargetDeviceAddress()), BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);
                    return;
                }
            }
        }
        bleManage.getBleGattCallback().registerOnGattConnectListener(
                new OnGattBLEConnectListener() {
                    @Override
                    public void onConnectSuccss(BluetoothGatt bluetoothGatt, int status, int newState, BLEGattCallback bleGattCallback) {
                        onConnectDeivceSuccss(bluetoothGatt, status, newState);
                    }

                    @Override
                    public void onConnectFail(String errorMsg, int loglevel, BluetoothGatt bluetoothGatt) {
                        LogUtil.i(TAG, "第" + currentConnectCount + "次连接失败\nerrorMsg=" + errorMsg + "\nloglevel=" + LogUtil.getLogTag(loglevel));
                        connect();
                    }
                }
        );
        LogUtil.i(TAG, "第" + currentConnectCount + "次开始连接");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (bluetoothDevice == null) {
                    bluetoothDevice = BLESDKLibrary.bluetoothAdapter.getRemoteDevice(targetMacAddress);
                }
                BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(context, false, bleManage.getBleGattCallback());
                if (bluetoothGatt == null) {
                    LogUtil.e(TAG, "连接失败");
                    connect();
                    return;
                }
                LogUtil.i(TAG, "请求连接成功,gatt=" + bluetoothGatt);
            }
        });
    }

    /**
     * 连接成功
     */
    private void onConnectDeivceSuccss(BluetoothGatt bluetoothGatt, int status, int newState) {
        LogUtil.i(TAG, "已连接成功");
        bleManage.setBluetoothGatt(bluetoothGatt);
        if (BLEManage.getBluetoothGatt(bluetoothGatt.getDevice().getAddress()) == null && BLEManage.getBluetoothGatt(bluetoothGatt.getDevice().getName()) == null) {
            Map<String, Object> bluetoothGattMap = new HashMap<>();
            bluetoothGattMap.put("bluetoothGatt", bluetoothGatt);
            bluetoothGattMap.put("connectedTime", System.currentTimeMillis());
            bluetoothGattMap.put("bluetoothGattCallback", bleManage.getBleGattCallback());
            BLEManage.connectedBluetoothGattList.add(bluetoothGattMap);
        }
        if (bleManage.getListenterObject() instanceof OnBLEConnectListener) {
            ((OnBLEConnectListener)bleManage.getListenterObject()).onConnectSuccess(bluetoothGatt, status, newState, bleManage.getBleGattCallback());
            return;
        }
        if (BLEConfig.START_FIND_SERVICE_INTERVAL > 0) {
            LogUtil.i(TAG, "休眠" + BLEConfig.START_FIND_SERVICE_INTERVAL + "ms开始找服务，手机型号:" + Build.MODEL);
            SystemClock.sleep(BLEConfig.START_FIND_SERVICE_INTERVAL);
        }
        if (!bleManage.getRunning()) {
            LogUtil.e(TAG, "准备找服务时任务已停止");
            return;
        }
        bleManage.setBleConnect(this);
        BLEFindService bleFindService = new BLEFindService(bleManage);
        bleFindService.findService();
    }
}
