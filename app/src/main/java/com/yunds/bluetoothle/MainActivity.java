package com.yunds.bluetoothle;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.bluetoothle.base.BLESDKLibrary;
import com.bluetoothle.core.listener.OnBLEScanListener;
import com.bluetoothle.core.manage.BLEManage;
import com.bluetoothle.util.log.LogUtil;
import com.bluetoothle.util.permisstion.OnPermissionResult;
import com.bluetoothle.util.permisstion.PermisstionUtil;

import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermisstionUtil.requestBLELocationPermission(this, "", new OnPermissionResult() {
            @Override
            public void granted(int requestCode) {
                String logPath = Environment.getExternalStorageDirectory() + "/" + getPackageName().substring(getPackageName().lastIndexOf(".") + 1) + "/Log/";
                String logFileName = "log.txt";
                String releaseLogPath = getFilesDir() + "data/" + getPackageName() + "/cache/Log/";
                BLESDKLibrary.init(getApplication(), true, true, logPath, logFileName, releaseLogPath);
                BLEManage bleManage = new BLEManage();
                bleManage.setListenterObject(new OnBLEScanListener() {
                    @Override
                    public void onFoundDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
                        LogUtil.i(TAG, "device=" + bluetoothDevice);
                    }

                    @Override
                    public void onScanFinish(List<Map<String, Object>> bluetoothDeviceList) {

                    }

                    @Override
                    public void onScanFail(int errorCode) {

                    }
                });
                bleManage.startScan();
            }

            @Override
            public void denied(int requestCode) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermisstionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
