package com.bluetoothle.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;

import com.bluetoothle.core.manage.BLEService;
import com.dsm.platform.DsmLibrary;
import com.dsm.platform.util.ToastUtil;
import com.dsm.platform.util.log.LogUtil;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 11:25<br>
 * 功能描述：蓝牙初始化<br>
 */
public class BLESDKLibrary {

    private static final String TAG = "BLESDKLibrary";
    public static BluetoothAdapter bluetoothAdapter;
    public static boolean hasBLEFeature = false;
    public static boolean status = false;//蓝牙环境初始化状态
    public static BluetoothManager bluetoothManager;
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private static boolean inited = false;//是否已经初始化

    public static synchronized void init(Application application, boolean enableConsoleLog, boolean enableFileLog) {
        String logPath = Environment.getExternalStorageDirectory() + "/" + application.getPackageName().substring(application.getPackageName().lastIndexOf(".") + 1) + "/Log/";
        String logFileName = "log.txt";
        String releaseLogPath = application.getFilesDir() + "data/" + application.getPackageName() + "/cache/Log/";
        init(application, enableConsoleLog, enableFileLog, logPath, logFileName, releaseLogPath);
    }

    public static synchronized void init(Application application, boolean enableConsoleLog, boolean enableFileLog, String logPath, String logFileName, String releaseLogPath) {
        if (!inited) {//是否已经初始化作同步管理，避免多次被初始化
            if (application == null) {
                throw new IllegalArgumentException("context == null");
            }
            inited = true;
            DsmLibrary.getInstance().init(application,null,enableConsoleLog, enableFileLog, logPath, logFileName, releaseLogPath);
            context = application.getApplicationContext();
            new BLEMsgCode();
            //初始化Base库
            //        LibraryInit.init(BLESDKLibrary.context);
            //判断当前设备是否支持蓝牙ble功能
            hasBLEFeature = application.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
            if(!hasBLEFeature){
                ToastUtil.showToast(TAG, BLEMsgCode.parseBLECodeMessage(-10000), "hasBLEFeature == false");
                return;
            }
            //获取蓝牙管理服务
            bluetoothManager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null){
                ToastUtil.showToast(TAG, BLEMsgCode.parseBLECodeMessage(-10001), "bluetoothManager == false");
                return;
            }
            //获得蓝牙适配器
            bluetoothAdapter = bluetoothManager.getAdapter();
            if(bluetoothAdapter == null){
                ToastUtil.showToast(TAG, BLEMsgCode.parseBLECodeMessage(-10002), "bluetoothAdapter == false");
                return;
            }
            //启动蓝牙环境服务，该服务只是一个无限循环的触发器，每隔几秒钟触发一次检测，根据条件决定是否删除过期缓存的蓝牙连接对象
            application.startService(new Intent(BLESDKLibrary.context, BLEService.class));
            //根据手机型号调整蓝牙参数
            adjustParamsByModel();
            LogUtil.e(TAG, "============蓝牙模块加载完成===============");
        }
    }

    /**
     * 根据手机型号调整蓝牙参数
     */
    private static void adjustParamsByModel() {
        if ("Redmi 3".equalsIgnoreCase(Build.MODEL)) {
            ShakeListener.SPEED_SHRESHOLD = 1400;
        } else if ("vivo Y51A".equalsIgnoreCase(Build.MODEL)) {
            ShakeListener.SPEED_SHRESHOLD = 500;
        } else if ("MI 4LTE".equalsIgnoreCase(Build.MODEL)) {
            ShakeListener.SPEED_SHRESHOLD = 600;
        } else if ("M578CA".equalsIgnoreCase(Build.MODEL)) {
            ShakeListener.SPEED_SHRESHOLD = 550;
        } else if ("ONEPLUS A3010".equalsIgnoreCase(Build.MODEL)) {
            ShakeListener.SPEED_SHRESHOLD = 1100;
        } else if ("vivo V3Max A".equalsIgnoreCase(Build.MODEL) || "SM-G9280".equalsIgnoreCase(Build.MODEL)) {
            ShakeListener.SPEED_SHRESHOLD = 800;
        }
    }

    /**
     * 检查ble扫描是否需要定位权限
     */
    private static boolean bleScanNeedLocation() {
        return Build.VERSION.SDK_INT >= 21;
    }

    /**
     * 检测定位功能是否开启
     */
    private static boolean locationIsEnable(Context context) {
        boolean flag = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        // 通过GPS卫星定位
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // GPS辅助定位,AGPS,借助网络
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            flag = true;
        }
        return flag;
    }

    /**
     * 位置功能可用性检测
     */
    public static boolean checkLocationAvailable(Context context) {
        //不需要位置权限，直接返回可用
        return !bleScanNeedLocation() || locationIsEnable(context);
    }
}
