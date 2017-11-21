package com.yunds.bluetoothle;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluetoothle.base.BLESDKLibrary;
import com.bluetoothle.util.ByteUtil;
import com.bluetoothle.util.ScreenUtils;
import com.bluetoothle.util.ToastUtil;
import com.bluetoothle.util.permisstion.PermisstionUtil;
import com.yunds.bluetoothle.adapter.CommonRVAdapter;
import com.yunds.bluetoothle.entry.BluetoothDRB;
import com.yunds.bluetoothle.presenter.scan.ScanDevice;
import com.yunds.bluetoothle.presenter.scan.ScanDeviceImpl;
import com.yunds.bluetoothle.presenter.scan.ScanDeviceView;

import java.util.List;

@SuppressWarnings("ALL")
public class MainActivity extends Activity implements View.OnClickListener, ScanDeviceView {

    private ImageView scanIv;
    private Animation animation;
    private RecyclerView listRv;
    private ScanDevice scanDevice;
    private final CommonRVAdapter<BluetoothDRB> bluetoothDRBCommonRVAdapter = new CommonRVAdapter<BluetoothDRB>(this, null, R.layout.item_scan, R.layout.item_null) {
        @Override
        public void onBindNullDataViewHolder(RecyclerView.Adapter adapter, RVViewHolder rvViewHolder, int position, BluetoothDRB entry, List<BluetoothDRB> data) {
            int minimumHeight = ScreenUtils.getScreenHeight(MainActivity.this) - ScreenUtils.getStatusBarHeight(MainActivity.this) - findViewById(R.id.titleLayout).getHeight();
            rvViewHolder.findViewById(R.id.nullTv).setMinimumHeight(minimumHeight);
        }

        @Override
        public void onBindViewHolder(RecyclerView.Adapter adapter, RVViewHolder rvViewHolder, int position, BluetoothDRB entry, List<BluetoothDRB> data) {
            if (position == 0) {
                rvViewHolder.findViewById(R.id.topPaddingLayout).setVisibility(View.GONE);
            } else {
                rvViewHolder.findViewById(R.id.topPaddingLayout).setVisibility(View.VISIBLE);
            }
            BluetoothDevice bluetoothDevice = entry.getBluetoothDevice();
            int rssi = entry.getRssi();
            byte[] bdctData = entry.getBroadcastPackageData();
            String deviceName = bluetoothDevice.getName();
            String deviceMac = bluetoothDevice.getAddress();
            ((TextView)rvViewHolder.findViewById(R.id.deviceNameTv)).setText(deviceName);
            ((TextView)rvViewHolder.findViewById(R.id.deviceMacTv)).setText(deviceMac);
            ((TextView)rvViewHolder.findViewById(R.id.rssiTv)).setText(String.valueOf(rssi));
            TextView broadcastDataTv = ((TextView)rvViewHolder.findViewById(R.id.broadcastDataTv));
            broadcastDataTv.setText(ByteUtil.bytesToHexString(bdctData));
            ImageView rssiIv = (ImageView) rvViewHolder.findViewById(R.id.rssiIv);
            int rssiResId;
            int rssiAbs = Math.abs(rssi);
            if (rssiAbs >= 80) {
                rssiResId = R.mipmap.rssi1;
            } else if (rssiAbs >= 70) {
                rssiResId = R.mipmap.rssi2;
            } else if (rssiAbs >= 60) {
                rssiResId = R.mipmap.rssi3;
            } else if (rssiAbs >= 50) {
                rssiResId = R.mipmap.rssi4;
            } else {
                rssiResId = R.mipmap.rssi5;
            }
            rssiIv.setImageResource(rssiResId);
        }

        @Override
        public BluetoothDRB getSelectEntry() {
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLibrary();
        initPresenter();
        initView();
    }

    private void initLibrary() {
        BLESDKLibrary.init(getApplication(), true, true, null, null, null);
    }

    private void initPresenter() {
        scanDevice = new ScanDeviceImpl(this, this);
    }

    private void initView() {
        findViewById(R.id.backIv).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.titleTv)).setText(R.string.scan);
        scanIv = (ImageView) findViewById(R.id.scanIv);
        scanIv.setOnClickListener(this);
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_drawable);
        findViewById(R.id.settingIv).setOnClickListener(this);
        listRv = (RecyclerView) findViewById(R.id.listRv);
        listRv.setLayoutManager(new LinearLayoutManager(this));
        listRv.setAdapter(bluetoothDRBCommonRVAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        scanDevice.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanDevice.stopScan();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scanIv) {
            if ((Boolean)scanIv.getTag()) {
                scanDevice.stopScan();
            } else {
                scanDevice.startScan();
            }
        } else if (v.getId() == R.id.settingIv) {

        }
    }

    @Override
    public View getScanView() {
        return scanIv;
    }

    @Override
    public Animation getScanAnimation() {
        return animation;
    }

    @Override
    public CommonRVAdapter getAdapter() {
        return bluetoothDRBCommonRVAdapter;
    }

    @Override
    public void onFoundDevice(BluetoothDRB bluetoothDRB) {
        bluetoothDRBCommonRVAdapter.flush(bluetoothDRB);
    }

    @Override
    public void onScanFinish(List<BluetoothDRB> bluetoothDRBList) {

    }

    @Override
    public void onScanFailure(String error) {
        ToastUtil.showToast(error);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermisstionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
