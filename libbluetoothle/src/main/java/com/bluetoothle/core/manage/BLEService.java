package com.bluetoothle.core.manage;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.IBinder;

import com.bluetoothle.base.BLEConfig;
import com.bluetoothle.util.log.LogUtil;

import java.util.Map;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 13:27<br>
 * 功能描述：底层蓝牙服务<br>
 */
public class BLEService extends Service {

	private final static String TAG = BLEService.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();

		//启动一个子线程，不停扫描蓝牙连接池，如果某一个连接超过规定的时间仍然没有通讯的话，主动断开连接
		new Thread(
				new Runnable() {
					@Override
					public void run() {
						while(BLEConfig.AUTO_DISCONNECT_WHEN_NO_DATA_INTERACTION){
							try {
								Thread.sleep(5000);
								synchronized (TAG) {
									if(BLEManage.connectedBluetoothGattList != null && BLEManage.connectedBluetoothGattList.size() > 0){
										for (Map<String,Object> map: BLEManage.connectedBluetoothGattList) {
											Long timeInterval = System.currentTimeMillis() - (Long)map.get("connectedTime");
											if(timeInterval >= BLEConfig.AUTO_DISCONNECT_INTERVAL_WHEN_NO_DATA_INTERACTION){
												BluetoothGatt bluetoothGatt = (BluetoothGatt) map.get("bluetoothGatt");
												LogUtil.i(TAG, "连接" + bluetoothGatt.getDevice().getAddress() + "超过规定的时间仍然没有通讯，主动断开并关闭连接");
												BLEManage.disconnect(bluetoothGatt);
											}
										}
                                    }
									if(BLEManage.bluetoothDeviceList != null && BLEManage.bluetoothDeviceList.size() > 0){
										for (Map<String, Object> map : BLEManage.bluetoothDeviceList) {
											Long timeInterval = System.currentTimeMillis() - (Long) map.get("foundTime");
											if (timeInterval >= BLEConfig.DEVICE_MAX_CACHED_TIME) {
												String deviceMac = ((BluetoothDevice) map.get("bluetoothDevice")).getAddress();
												LogUtil.i(TAG, "设备" + deviceMac + "超过规定的缓存时间，自动清除");
												BLEManage.bluetoothDeviceList.remove(map);
											}
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
		).start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}