package com.bluetoothle.core.listener;

import android.bluetooth.BluetoothGatt;

import com.bluetoothle.core.manage.BLEGattCallback;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 11:30<br>
 * 功能描述：连接监听器<br>
 */
public interface OnBLEConnectListener {
    void onConnectSuccess(BluetoothGatt bluetoothGatt, int status, int newState, BLEGattCallback bleGattCallback);
    void onConnectFail(int errorCode);
}
