package com.bluetoothle.base;

import com.bluetoothle.R;
import com.dsm.platform.base.BaseMsgCode;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/10 09 53 星期五<br>
 * 功能描述：<br>蓝牙栈消息码
 */

public class BLEMsgCode extends BaseMsgCode {

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
}
