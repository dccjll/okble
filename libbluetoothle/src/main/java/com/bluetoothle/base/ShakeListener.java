package com.bluetoothle.base;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.bluetoothle.util.log.LogUtil;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 11:30<br>
 * 功能描述：一个检测手机摇晃的监听器<br>
 */
@SuppressWarnings("ALL")
public class ShakeListener implements SensorEventListener {
    private static final String TAG = ShakeListener.class.getSimpleName();
    //手机摇一摇的速度阈值，当达到该值时响应摇一摇，具体的算法为：Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ* deltaZ)/ timeInterval * 10000(详情参考com.bluetoothle.core.ShakeListener监听器)
    public static int SPEED_SHRESHOLD = 1500;
    // 上次检测时间
    private long lastUpdateTime;
    // 两次检测的时间间隔
    private static final int UPDATE_INTERVAL_TIME = 70;
    private long lastResponseTime;
    // 两次响应的时间间隔
    private long responseIntervalTime = 0;
    // 传感器管理器
    private SensorManager sensorManager;
    // 重力感应监听器
    private OnShakeListener onShakeListener;
    // 上下文
    private final Context mContext;
    // 手机上一个位置时重力感应坐标
    private float lastX;
    private float lastY;
    private float lastZ;

    // 构造器
    public ShakeListener(Context context) {
        // 获得监听对象
        mContext = context;
    }

    // 开始
    public void start() {
        // 获得传感器管理器
        if (sensorManager == null) {
            sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        }
        if (sensorManager == null) {
            LogUtil.e(TAG, "sensorManager error");
            return;
        }
        // 获得重力传感器
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 注册
        if (sensor == null) {
            LogUtil.e(TAG, "sensor error");
            return;
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        LogUtil.e(TAG, "ShakeListener,start");
    }

    // 停止检测
    public void stop() {
        sensorManager.unregisterListener(this);
        LogUtil.e(TAG, "ShakeListener,stop");
    }

    // 设置重力感应监听器
    public void setOnShakeListener(OnShakeListener listener) {
        onShakeListener = listener;
    }

    // 重力感应器感应获得变化数据
    @Override
    public void onSensorChanged(SensorEvent event) {
        // 获得x,y,z坐标
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
//        LogUtil.i("ShakeListener", "      " + x + ",       " + y + ",      " + z);
//        if (Math.abs(x) > SPEED || Math.abs(y) > SPEED || Math.abs(z) > SPEED) {
//            //LogUtil.e("ShakeListener", "      " + x + ",       " + y + ",      " + z);
//            // 现在检测时间
//            long currentUpdateTime = System.currentTimeMillis();
//            if (currentUpdateTime - lastUpdateTime < updateIntervalTime)
//                return;
//            onShakeListener.onShake();
//            lastUpdateTime = currentUpdateTime;
//        }

//		LogUtil.i("ShakeListener", "onSensorChanged");
        // 现在检测时间
        long currentUpdateTime = System.currentTimeMillis();
        // 两次检测的时间间隔
        long timeInterval = currentUpdateTime - lastUpdateTime;
        // 判断是否达到了检测时间间隔
        if (timeInterval < UPDATE_INTERVAL_TIME)
            return;
        // 现在的时间变成last时间
        lastUpdateTime = currentUpdateTime;



        // 获得x,y,z的变化值
        float deltaX = x - lastX;
        float deltaY = y - lastY;
        float deltaZ = z - lastZ;

        // 将现在的坐标变成last坐标
        lastX = x;
        lastY = y;
        lastZ = z;



        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ* deltaZ)/ timeInterval * 10000;
        // 达到速度阀值，发出提示
        long rtimeInterval = System.currentTimeMillis() - lastResponseTime;
		if (speed > 500) {
//            LogUtil.e(TAG, "onSensorChanged,\nBuild.MODEL=" + Build.MODEL + "\nspeed=" + speed + "\nSPEED_SHRESHOLD=" + SPEED_SHRESHOLD + "\n响应时间间隔:" + rtimeInterval + "\n固定间隔:" + responseIntervalTime);
        } else {
//            LogUtil.i(TAG, "onSensorChanged,\nBuild.MODEL=" + Build.MODEL + "\nspeed=" + speed + "\nSPEED_SHRESHOLD=" + SPEED_SHRESHOLD + "\n响应时间间隔:" + rtimeInterval + "\n固定间隔:" + responseIntervalTime);
        }
        if (speed >= SPEED_SHRESHOLD && rtimeInterval > responseIntervalTime) {//速度达到响应阀值，并且响应间隔大于当前响应间隔才触发摇一摇
            lastResponseTime = System.currentTimeMillis();
            onShakeListener.onShake();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // 摇晃监听接口
    public interface OnShakeListener {
        void onShake();
    }

    public void setResponseIntervalTime(int responseIntervalTime) {
        this.responseIntervalTime = responseIntervalTime;
    }

    public void resetResponseIntervalTime(){
        this.responseIntervalTime = 0;
    }
}