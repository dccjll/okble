package com.bluetoothle.core.node;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;

import com.bluetoothle.base.BLESDKLibrary;
import com.bluetoothle.core.listener.OnBLEOpenNotificationListener;
import com.bluetoothle.core.manage.BLEManage;
import com.bluetoothle.util.ByteUtil;
import com.bluetoothle.util.log.LogUtil;

import java.util.List;
import java.util.UUID;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 13:28<br>
 * 功能描述：打开通知<br>
 */
public class BLEOpenNotification {

    private final static String TAG = BLEOpenNotification.class.getSimpleName();
    private List<BluetoothGattService> bluetoothGattServices;
    private UUID[] uuids;
    private BLEManage bleManage;

    /**
     * gatt服务器打开通知监听器
     */
    public interface OnGattBLEOpenNotificationListener {
        void onOpenNotificationSuccess(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);
        void onOpenNotificationFail(String errorMsg, int loglevel);
    }

    /**
     * 打开通知
     */
    public BLEOpenNotification(BLEManage bleManage) {
        this.bluetoothGattServices = bleManage.getBluetoothGatt().getServices();
        this.uuids = bleManage.getNotificationuuids();
        this.bleManage = bleManage;
    }

    /**
     * 开始打开通知
     */
    public void openNotification(){
        LogUtil.i(TAG, "准备开始打开通知");
        if (BLESDKLibrary.context == null) {
            throw new IllegalArgumentException("BLESDKLibrary.context == null");
        }
        if (!BLESDKLibrary.bluetoothAdapter.isEnabled()) {
            bleManage.handleError(-10015);
            return;
        }
        if(bleManage.getBluetoothGatt() == null){
            bleManage.handleError(-10021);
            return;
        }
        if(bluetoothGattServices == null || bluetoothGattServices.size() == 0){
            bleManage.handleError(-10023);
            return;
        }
        if(uuids == null || uuids.length != 3){
            bleManage.handleError(-10024);
            return;
        }
        if(bleManage.getBleGattCallback() == null){
            bleManage.handleError(-10022);
            return;
        }
        bleManage.getBleGattCallback().setUuidCharacteristicChange(uuids[1]);
        bleManage.getBleGattCallback().setUuidDescriptorWrite(uuids[2]);
        bleManage.getBleGattCallback().registerOnGattBLEOpenNotificationListener(
                new OnGattBLEOpenNotificationListener() {
                    @Override
                    public void onOpenNotificationSuccess(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                        afterOpenNotificationSuccess(gatt, descriptor, status);
                    }

                    @Override
                    public void onOpenNotificationFail(String errorMsg, int loglevel) {
                        LogUtil.i(TAG, "打开通知失败\nerrorMsg=" + errorMsg + "\nloglevel=" + LogUtil.getLogTag(loglevel));
                        bleManage.getBleConnect().connect();
                    }
                }
        );
        LogUtil.i(TAG, "开始打开通知");
        BluetoothGattService bluetoothGattService = null;
        for(BluetoothGattService bluetoothGattService_ : bluetoothGattServices){
            if(bluetoothGattService_.getUuid().toString().equalsIgnoreCase(uuids[0].toString())){
                bluetoothGattService = bluetoothGattService_;
                break;
            }
        }
        if(bluetoothGattService == null){
            bleManage.handleError(-10025);
            return;
        }
        final BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(uuids[1]);
        if(bluetoothGattCharacteristic == null){
            bleManage.handleError(-10026);
            return;
        }
        LogUtil.i(TAG, "通知的特征属性：" + bluetoothGattCharacteristic.getProperties() + ",need：" + BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
            bleManage.handleError(-10027);
            return;
        }
        final BluetoothGattDescriptor bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptor(uuids[2]);
        if(bluetoothGattDescriptor == null){
            bleManage.handleError(-10028);
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                    bleManage.handleError(-10029);
                    return;
                }
                if(!bleManage.getBluetoothGatt().setCharacteristicNotification(bluetoothGattCharacteristic, true)){
                    bleManage.handleError(-10030);
                    return;
                }
                if(!bleManage.getBluetoothGatt().writeDescriptor(bluetoothGattDescriptor)){
                    bleManage.handleError(-10031);
                }
            }
        });
    }

    private void afterOpenNotificationSuccess(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        LogUtil.i(TAG, "打开通知成功");
        if(bleManage.getListenterObject() instanceof OnBLEOpenNotificationListener){
            ((OnBLEOpenNotificationListener)bleManage.getListenterObject()).onOpenNotificationSuccess(gatt, descriptor, status, bleManage.getBleGattCallback());
            return;
        }
        LogUtil.i(TAG, "要写入的数据：" + ByteUtil.bytesToHexString(bleManage.getData()));
        if (!bleManage.getRunning()) {
            LogUtil.e(TAG, "准备写入数据时任务已停止");
            return;
        }
        BLEWriteData bleWriteData = new BLEWriteData(bleManage);
        bleWriteData.writeData();
    }
}
