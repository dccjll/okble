package com.bluetoothle.core.listener;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import com.bluetoothle.core.manage.BLEGattCallback;

import java.util.List;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 11:30<br>
 * 功能描述：找服务监听<br>
 */
public interface OnBLEFindServiceListener {
    void onFindServiceSuccess(BluetoothGatt bluetoothGatt, int status, List<BluetoothGattService> bluetoothGattServices, BLEGattCallback bleGattCallback);
    void onFindServiceFail(int errorCode);
}
