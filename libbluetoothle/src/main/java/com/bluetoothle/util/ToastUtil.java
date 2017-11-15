package com.bluetoothle.util;

import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.bluetoothle.base.BLESDKLibrary;
import com.bluetoothle.util.log.LogUtil;

/**
 * Created by sjl on 2015/8/11.
 */
public class ToastUtil {
    private static final String tag = ToastUtil.class.getSimpleName();
    private static Toast toast;

    /**
     * 显示提示文本
     *
     * @param text
     */
    public static void showToast(String text) {
        if (text == null || text.equals("")) {
            text = "系统异常";
        }
        if (toast == null) {
            toast = Toast.makeText(BLESDKLibrary.context, text, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM,0,200);
        } else {
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM,0,200);
            toast.setText(text);
        }
        LogUtil.i(tag,text);
        toast.show();
    }

    public static void showToast(String tag, String text, String log) {
        if (text == null || text.equals("")) {
            text = "系统异常";
        }
        if (toast == null) {
            toast = Toast.makeText(BLESDKLibrary.context, text, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM,0,200);
        } else {
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM,0,200);
            toast.setText(text);
        }
        LogUtil.i(tag,text + "\n" + log);
        toast.show();
    }

    public static void showToastLong(String text) {
        showToastLong(text, null);
    }

    public static void showToastLong(String tag, String text) {
        if (text == null || text.equals("")) {
            text = "系统异常";
        }
        if (toast == null) {
            toast = Toast.makeText(BLESDKLibrary.context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM,0,200);
        } else {
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM,0,200);
            toast.setText(text);
        }
        if (TextUtils.isEmpty(tag)) {
            tag = ToastUtil.tag;
        }
        LogUtil.i(tag,text);
        toast.show();
    }

    /**
     * 显示提示文本,可自定义方向
     *
     * @param text
     * @param gravity
     */
    public static void showToast(String text, int gravity) {
        if (text == null || text.equals("")) {
            text = "系统异常";
        }
        if (toast == null) {
            toast = Toast.makeText(BLESDKLibrary.context, text, Toast.LENGTH_LONG);
            toast.setGravity(gravity, 0, 0);
        } else {
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setText(text);
            toast.setGravity(gravity, 0, 0);
        }
        toast.show();
        LogUtil.i(tag,text);
        LogUtil.i("toast", text);
    }

    public static void showToast(int resId) {
        showToast(BLESDKLibrary.context.getResources().getText(resId) + "");
    }
}
