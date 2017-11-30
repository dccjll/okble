package com.bluetoothle.core.listener;

import android.bluetooth.BluetoothDevice;

import java.util.List;
import java.util.Map;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 11:31<br>
 * 功能描述：扫描监听器<br>
 */
public interface OnBLEScanListener {
    void onFoundDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord);
    void onScanFinish(List<Map<String, Object>> bluetoothDeviceList);
    void onScanFail(int errorCode);
}
