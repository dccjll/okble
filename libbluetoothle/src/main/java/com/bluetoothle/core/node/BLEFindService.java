package com.bluetoothle.core.node;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.bluetoothle.base.BLECode;
import com.bluetoothle.base.BLEConfig;
import com.bluetoothle.base.BLESDKLibrary;
import com.bluetoothle.core.listener.OnBLEFindServiceListener;
import com.bluetoothle.core.manage.BLEManage;
import com.bluetoothle.util.log.LogUtil;

import java.util.List;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 13:28<br>
 * 功能描述：寻找服务<br>
 */
public class BLEFindService {

    private final static String TAG = BLEFindService.class.getSimpleName();
    private BLEManage bleManage;

    /**
     * gatt服务器找服务监听器
     */
    public interface OnGattBLEFindServiceListener{
        void onFindServiceSuccess(BluetoothGatt bluetoothGatt, int status, List<BluetoothGattService> bluetoothGattServices);
        void onFindServiceFail(String errorMsg, int loglevel);
    }

    /**
     * 找服务
     */
    public BLEFindService(BLEManage bleManage) {
        this.bleManage = bleManage;
    }

    /**
     * 开始找服务
     */
    public void findService(){
        LogUtil.i(TAG, "准备开始找服务");
        if (BLESDKLibrary.context == null) {
            throw new IllegalArgumentException("BLESDKLibrary.context == null");
        }
        if(bleManage.getBluetoothGatt() == null){
            bleManage.handleError(BLECode.on_bluetooth_gatt_empty);
            return;
        }
        if(bleManage.getBleGattCallback() == null){
            bleManage.handleError(BLECode.on_bluetooth_gatt_callback_empty);
            return;
        }
        if (!BLESDKLibrary.bluetoothAdapter.isEnabled()) {
            bleManage.handleError(BLECode.blutooth_is_closed);
            return;
        }
        bleManage.getBleGattCallback().registerOnGattBLEFindServiceListener(
                new OnGattBLEFindServiceListener() {
                    @Override
                    public void onFindServiceSuccess(BluetoothGatt bluetoothGatt, int status, List<BluetoothGattService> bluetoothGattServices) {
                        afterFindServiceSuccess(bluetoothGatt, status, bluetoothGattServices);
                    }

                    @Override
                    public void onFindServiceFail(String errorMsg, int loglevel) {
                        LogUtil.i(TAG, "找服务失败\nerrorMsg=" + errorMsg + "\nloglevel=" + LogUtil.getLogTag(loglevel));
                        bleManage.getBleConnect().connect();
                    }
                }
        );
        LogUtil.i(TAG, "开始找服务");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!bleManage.getBluetoothGatt().discoverServices()) {
                    bleManage.getBleConnect().connect();
                }
            }
        });
    }

    private void afterFindServiceSuccess(BluetoothGatt bluetoothGatt, int status, List<BluetoothGattService> bluetoothGattServices) {
        LogUtil.i(TAG, "已找到服务");
        if (bluetoothGattServices == null || bluetoothGattServices.size() == 0) {
            LogUtil.e(TAG, "服务列表为空");
            bleManage.getBleConnect().connect();
            return;
        }
        //遍历服务
        for(BluetoothGattService bluetoothGattService : bluetoothGattServices){
            LogUtil.i(TAG, "++++service uuid:" + bluetoothGattService.getUuid());
            for(BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()){
                LogUtil.i(TAG, "--------characteristics uuid:" + bluetoothGattCharacteristic.getUuid());
                for(BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattCharacteristic.getDescriptors()){
                    LogUtil.i(TAG, "------------descriptor uuid:" + bluetoothGattDescriptor.getUuid());
                }
            }
        }
        if(bleManage.getListenterObject() instanceof OnBLEFindServiceListener){
            ((OnBLEFindServiceListener)bleManage.getListenterObject()).onFindServiceSuccess(bluetoothGatt, status, bluetoothGattServices, bleManage.getBleGattCallback());
        }
        if(bleManage.getNeedOpenNotification()){
            if (BLEConfig.START_OPEN_NOTIFICATION_INTERVAL > 0) {
                LogUtil.i(TAG, "找服务成功,休眠" + BLEConfig.START_OPEN_NOTIFICATION_INTERVAL + "ms开始打开通知,当前手机型号:" + Build.MODEL);
                SystemClock.sleep(BLEConfig.START_OPEN_NOTIFICATION_INTERVAL);
                if (!bleManage.getRunning()) {
                    LogUtil.e(TAG, "准备打开通知时任务已停止");
                    return;
                }
            }
            BLEOpenNotification bleOpenNotification = new BLEOpenNotification(bleManage);
            bleOpenNotification.openNotification();
        }
    }
}