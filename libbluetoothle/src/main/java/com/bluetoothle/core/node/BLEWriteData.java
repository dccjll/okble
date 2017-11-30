package com.bluetoothle.core.node;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.SystemClock;

import com.bluetoothle.base.BLECode;
import com.bluetoothle.base.BLEConfig;
import com.bluetoothle.base.BLESDKLibrary;
import com.bluetoothle.core.manage.BLEManage;
import com.bluetoothle.util.ByteUtil;
import com.bluetoothle.util.log.LogUtil;

import java.util.List;
import java.util.UUID;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 13:33<br>
 * 功能描述：写数据<br>
 */
public class BLEWriteData {

    private final static String TAG = BLEWriteData.class.getSimpleName();
    private boolean dataWrittenStart = false;//是否已开始写数据标记(发现锁在有的时候还没有开始写数据就回了上一条写数据失败的数据，加此标记以便于在接收数据时过滤)
    private boolean dataWrittenFinish = false;
    private boolean dataWrittenState = true;
    private final UUID[] uuids;
    private final byte[] data;
    private List<byte[]> dataList;
    private Integer writtenDataLength;
    private BLEManage bleManage;
    private Integer index = 0;//当前发送的第几个数据包
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    public boolean getDataWrittenStart() {
        return dataWrittenStart;
    }

    public boolean isDataWrittenFinish() {
        return dataWrittenFinish;
    }

    public boolean isDataWrittenState() {
        return dataWrittenState;
    }

    public interface OnGattBLEWriteDataListener {
        void onWriteDataFinish();

        void onWriteDataSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onWriteDataFail(String errorMsg, int loglevel);
    }

    /**
     * 写数据构造器
     */
    public BLEWriteData(BLEManage bleManage) {
        this.uuids = bleManage.getWriteuuids();
        this.data = bleManage.getData();
        this.bleManage = bleManage;
        this.bleManage.setBleWriteData(this);
    }

    public void writeData() {
        LogUtil.i(TAG, "准备开始写数据");
        if (BLESDKLibrary.context == null) {
            throw new IllegalArgumentException("BLESDKLibrary.context == null");
        }
        dataWrittenFinish = false;
        dataWrittenState = true;
        if (bleManage.getBluetoothGatt() == null) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.on_bluetooth_gatt_empty);
            return;
        }
        if (uuids == null || uuids.length != 2) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.on_write_data_uuids_validate_failure);
            return;
        }
        if (data == null || data.length == 0) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.on_empty_write_data);
            return;
        }
        dataList = ByteUtil.paeseByteArrayToByteList(data, BLEConfig.MAX_BYTES);
        LogUtil.i(TAG, "要写入的总数据遍历,total data=" + ByteUtil.bytesToHexString(data));
        for(int i=0;dataList != null && i<dataList.size();i++) {
            LogUtil.i(TAG, "i=" + i + ",data=" + ByteUtil.bytesToHexString(dataList.get(i)));
        }
        if (dataList == null) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.on_data_sub_package_exception);
            return;
        }
        if (bleManage.getBleGattCallback() == null) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.on_bluetooth_gatt_callback_empty);
            return;
        }
        writtenDataLength = 0;
        bleManage.getBleGattCallback().setUuidCharacteristicWrite(uuids[1]);
        bleManage.getBleGattCallback().registerOnGattBLEWriteDataListener(
                new OnGattBLEWriteDataListener() {
                    @Override
                    public void onWriteDataFinish() {
                        LogUtil.i(TAG, "写入的所有数据为：" + ByteUtil.bytesToHexString(bleManage.getData()));
                        dataWrittenFinish = true;
                        bleManage.getBleResponseManager().onWriteDataFinish();
                    }

                    @Override
                    public void onWriteDataSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        LogUtil.i(TAG, "数据写入成功，写入的数据为:\t\t\t\t" + ByteUtil.bytesToHexString(characteristic.getValue()) + "\n当前第" + (index + 1) + "包" + "\n总共" + dataList.size() + "包");
                        bleManage.getBleResponseManager().onWriteDataSuccess(gatt, characteristic, status, bleManage.getBleGattCallback());
                        if ((writtenDataLength += characteristic.getValue().length) == data.length) {
                            LogUtil.i(TAG, "数据写入完成");
                            onWriteDataFinish();
                            return;
                        }
                        LogUtil.i(TAG, "间隔" + BLEConfig.SEND_NEXT_PACKAGE_INTERVAL + "ms再发送下一个数据包");
                        SystemClock.sleep(BLEConfig.SEND_NEXT_PACKAGE_INTERVAL);
                        int i = ++index;
                        if(i >= dataList.size()){
                            LogUtil.e(TAG,"写数据时下标越界(出现在底层写成功回调异常情况下，请忽略)");
                            return;
                        }
                        sendBLEData(dataList.get(i));
                    }

                    @Override
                    public void onWriteDataFail(String errorMsg, int loglevel) {
                        LogUtil.e(TAG, "onWriteDataFail\nerrorMsg=" + errorMsg + "\nloglevel=" + loglevel + "\nbleManage=" + bleManage);
                        dataWrittenState = false;
                        if (bleManage.getBleConnect() != null) {
                            bleManage.getBleConnect().connect();
                            return;
                        }
                        bleManage.handleError(BLECode.on_bluetooth_gatt_write_characteristics_failure);
                    }
                }
        );
        BluetoothGattService bluetoothGattService = bleManage.getBluetoothGatt().getService(uuids[0]);
        if (bluetoothGattService == null) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.not_found_specified_write_data_service_uuid);
            return;
        }
        bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(uuids[1]);
        if (bluetoothGattCharacteristic == null) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.not_found_specified_write_data_characteristics_uuid);
            return;
        }
        LogUtil.i(TAG, "写数据的特征属性：" + bluetoothGattCharacteristic.getProperties() + ",need：" + BluetoothGattCharacteristic.PROPERTY_WRITE);
        if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.not_write_data_function);
            return;
        }
        LogUtil.i(TAG, "开始写数据");
        dataWrittenStart = true;
        sendBLEData(dataList.get(index));
    }

    /**
     * 写数据
     *
     * @param value 写入的数据
     */
    private void sendBLEData(byte[] value) {
        if (!bluetoothGattCharacteristic.setValue(value)) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.on_bluetooth_gatt_characteristics_set_value_failure);
            return;
        }
        if (!bleManage.getBluetoothGatt().writeCharacteristic(bluetoothGattCharacteristic)) {
            dataWrittenState = false;
            bleManage.handleError(BLECode.on_bluetooth_gatt_write_characteristics_failure);
        }
    }
}