package com.yunds.bluetoothle.presenter.scan;

import android.view.View;
import android.view.animation.Animation;

import com.yunds.bluetoothle.adapter.CommonRVAdapter;
import com.yunds.bluetoothle.entry.BluetoothDRB;

import java.util.List;

/**
 * Created by dccjll on 2017/6/27.
 * 扫描设备
 */

public interface ScanDeviceView {
    View getScanView();
    Animation getScanAnimation();
    CommonRVAdapter getAdapter();
    void onFoundDevice(BluetoothDRB bluetoothDRB);
    void onScanFinish(List<BluetoothDRB> bluetoothDRBList);
    void onScanFailure(String error);
}
