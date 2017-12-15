package com.yunds.bluetoothle.presenter.scan;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.bluetoothle.base.BLECode;
import com.bluetoothle.core.listener.OnBLEScanListener;
import com.bluetoothle.core.manage.BLEManage;
import com.bluetoothle.util.permisstion.OnPermissionResult;
import com.bluetoothle.util.permisstion.PermisstionUtil;
import com.yunds.bluetoothle.R;
import com.yunds.bluetoothle.entry.BluetoothDRB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScanDeviceImpl implements ScanDevice {

    private final Context context;
    private final ScanDeviceView scanDeviceView;

    public ScanDeviceImpl(Context context, ScanDeviceView scanDeviceView) {
        this.context = context;
        this.scanDeviceView = scanDeviceView;
    }

    private BLEManage bleManage;
    private final OnBLEScanListener onBLEScanListener = new OnBLEScanListener() {
        @Override
        public void onFoundDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            scanDeviceView.onFoundDevice(new BluetoothDRB().setBluetoothDevice(bluetoothDevice).setRssi(rssi).setBroadcastPackageData(scanRecord));
        }

        @Override
        public void onScanFinish(List<Map<String, Object>> bluetoothDeviceList) {
            List<BluetoothDRB> bluetoothDRBList = new ArrayList<>();
            for (Map<String, Object> map : bluetoothDeviceList) {
                bluetoothDRBList.add(new BluetoothDRB().setBluetoothDevice((BluetoothDevice) map.get("device")).setRssi((Integer) map.get("rssi")).setBroadcastPackageData((byte[]) map.get("scanRecord")));
            }
            scanDeviceView.onScanFinish(bluetoothDRBList);
        }

        @Override
        public void onScanFail(int errorCode) {
            stopScan();
            scanDeviceView.onScanFailure(BLECode.parseBLECodeMessage(errorCode));
        }
    };

    @Override
    public void startScan() {
        if (bleManage == null) {
            bleManage = new BLEManage();
            bleManage.setLongScanFlag();
            bleManage.setListenterObject(onBLEScanListener);
        }
        bleManage.stopScan();
        PermisstionUtil.requestBLELocationPermission(context, context.getString(R.string.scan_need_location_permission), new OnPermissionResult() {
            @Override
            public void granted(int requestCode) {
                scanDeviceView.getScanView().startAnimation(scanDeviceView.getScanAnimation());
                scanDeviceView.getScanView().setTag(true);
                scanDeviceView.getAdapter().clear();
                bleManage.startScan();
            }

            @Override
            public void denied(int requestCode) {
                scanDeviceView.onScanFailure(context.getString(R.string.scan_need_location_permission));
            }
        });
    }

    @Override
    public void stopScan() {
        if (bleManage == null) {
            return;
        }
        scanDeviceView.getScanView().clearAnimation();
        scanDeviceView.getScanView().setTag(false);
        bleManage.stopScan();
    }
}
