package com.bluetoothle.base;

import android.util.Log;
import android.util.SparseArray;

import com.bluetoothle.R;
import com.bluetoothle.util.log.LogUtil;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/10 09 53 星期五<br>
 * 功能描述：<br>蓝牙栈消息码
 */

public class BLECode {
    private static final String TAG = "BLECode";
    public final static SparseArray<String> codeMap = new SparseArray<>();//消息码集合

    static {
        String[] bleCodeArr = BLESDKLibrary.context.getResources().getStringArray(R.array.blecode);
        try {
            for (String bleCode: bleCodeArr) {
                String[] aar = bleCode.split("#");
                codeMap.put(Integer.parseInt(aar[0]), aar[1]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转换消息码
     */
    public static String parseBLECodeMessage(int bleCode) {
        String originBleMsg = null;
        try {
            String bleString = codeMap.get(bleCode);
            String[] bleStringArr = bleString.split("\\|");
            originBleMsg = bleStringArr[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return originBleMsg;
    }

    /**
     * 过滤转换的消息码
     */
    public static String getBLECodeMessage(int bleCode) {
        String bleString = codeMap.get(bleCode);
        try {
            bleString = parseBLECodeMessage(bleCode);
            int bleLogLevel = getBLECodeMessageLevel(bleCode);
            LogUtil.i(TAG, "Befor pass,getBLECodeMessage, msg=" + bleString + ",level=" + getBLECodeMessageLevelMessage(bleCode));
            if (bleLogLevel > Log.INFO) {
                bleString = BLESDKLibrary.context.getString(R.string.ble_error);
            }
            LogUtil.i(TAG, "After pass,getBLECodeMessage, msg=" + bleString + ",level=" + getBLECodeMessageLevelMessage(bleCode));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bleString;
    }

    /**
     * 获取消息码对应的描述信息等级
     */
    public static int getBLECodeMessageLevel(int bleCode) {
        int bleLevel = Log.INFO;
        try {
            String bleLogLevelString = getBLECodeMessageLevelMessage(bleCode);
            if ("VERBOSE".equalsIgnoreCase(bleLogLevelString)) {
                bleLevel = Log.VERBOSE;
            } else if ("DEBUG".equalsIgnoreCase(bleLogLevelString)) {
                bleLevel = Log.DEBUG;
            } else if ("INFO".equalsIgnoreCase(bleLogLevelString)) {
                bleLevel = Log.INFO;
            } else if ("WARN".equalsIgnoreCase(bleLogLevelString)) {
                bleLevel = Log.WARN;
            } else if ("ERROR".equalsIgnoreCase(bleLogLevelString)) {
                bleLevel = Log.ERROR;
            } else if ("ASSERT".equalsIgnoreCase(bleLogLevelString)) {
                bleLevel = Log.ASSERT;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bleLevel;
    }

    /**
     * 获取消息码对应的描述信息等级
     */
    public static String getBLECodeMessageLevelMessage(int bleCode) {
        String bleLevelMessage = "INFO";
        try {
            String bleString = codeMap.get(bleCode);
            String[] bleStringArr = bleString.split("\\|");
            bleLevelMessage = bleStringArr[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bleLevelMessage;
    }
}
