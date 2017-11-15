package com.bluetoothle.core.listener;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.bluetoothle.core.manage.BLEGattCallback;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 13:26<br>
 * 功能描述：写数据监听器<br>
 */
public interface OnBLEWriteDataListener {
    void onWriteDataFinish();
    void onWriteDataSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status, BLEGattCallback bleGattCallback);
    void onWriteDataFail(int errorCode);
}
