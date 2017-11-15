package com.bluetoothle.util;

import android.text.TextUtils;

import com.bluetoothle.R;
import com.bluetoothle.base.BLESDKLibrary;

import java.util.List;


/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 11:29<br>
 * 功能描述：蓝牙简单工具类<br>
 */
public class BLEUtil {

    private static final String TAG = "BLEUtil";

    /**
     * 验证设备mac地址
     */
    public static String checkDeviceMac(String deviceMac){
        return BLEUtil.checkAddress(deviceMac) ? null : BLESDKLibrary.context.getString(R.string.please_check_device_mac_or_name);
    }

    /**
     * 验证设备mac地址
     * @param address   设备mac地址
     * @return  验证结果
     */
    public static boolean checkAddress(String address){
        if(TextUtils.isEmpty(address)){
            return false;
        }
        if(address.split(":").length != 6){
            return false;
        }
        char[] macChars = address.replace(":", "").toCharArray();
        String regexChars = "0123456789ABCDEF";
        for(char c : macChars){
            if(!regexChars.contains(c + "")){
                return false;
            }
        }
        return true;
    }

    /**
     * 验证设备mac地址列表
     * @param targetAddressList 目标设备mac地址列表
     * @return  验证结果
     */
    public static boolean checkTargetAddressList(List<String> targetAddressList){
        if(targetAddressList == null || targetAddressList.size() == 0){
            return false;
        }
        for(String mac : targetAddressList){
            if(!checkAddress(mac)){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args){
        System.out.println(checkAddress("DC:B1:1F:80:69:05"));
    }
}
