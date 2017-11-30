package com.bluetoothle.core.listener;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import com.bluetoothle.core.manage.BLEGattCallback;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 11:31<br>
 * 功能描述：打开通知监听器<br>
 */
public interface OnBLEOpenNotificationListener {
    void onOpenNotificationSuccess(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status, BLEGattCallback bleGattCallback);
    void onOpenNotificationFail(int errorCode);
}
