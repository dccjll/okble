package com.yunds.bluetoothle.entry;

import android.bluetooth.BluetoothDevice;

import java.util.Arrays;

/**
 * Created by dccjll on 2017/6/27.
 * 封装了蓝牙设备(Device)、信号强度(Rssi)、广播包(Broadcast)的实体类
 */

public class BluetoothDRB {
    private BluetoothDevice bluetoothDevice;
    private int rssi;
    private byte[] broadcastPackageData;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public BluetoothDRB setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        return this;
    }

    public int getRssi() {
        return rssi;
    }

    public BluetoothDRB setRssi(int rssi) {
        this.rssi = rssi;
        return this;
    }

    public byte[] getBroadcastPackageData() {
        return broadcastPackageData;
    }

    public BluetoothDRB setBroadcastPackageData(byte[] broadcastPackageData) {
        this.broadcastPackageData = broadcastPackageData;
        return this;
    }

    @Override
    public String toString() {
        return "BluetoothDRB{" + "bluetoothDevice=" + bluetoothDevice + ", rssi=" + rssi + ", broadcastPackageData=" + Arrays.toString(broadcastPackageData) + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BluetoothDRB that = (BluetoothDRB) o;
        return bluetoothDevice.equals(that.bluetoothDevice);

    }

    @Override
    public int hashCode() {
        int result = bluetoothDevice.hashCode();
        result = 31 * result + rssi;
        result = 31 * result + Arrays.hashCode(broadcastPackageData);
        return result;
    }
}
