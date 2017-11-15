package com.bluetoothle.util.net;

import android.text.TextUtils;
import android.util.Base64;

import com.bluetoothle.util.log.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class EncryptUtil {
    private static final String tag = EncryptUtil.class.getSimpleName();

    public static ArrayList<Map<String, String>> encryptArrayList(
            ArrayList<Map<String, String>> list, Boolean isEncropt) {
        ArrayList<Map<String, String>> resultList = new ArrayList<>();
        for (Map<String, String> aList : list) {
            resultList.add(encryptMap(aList));
        }
        return resultList;
    }

    public static Map<String, String> encryptMap(Map<String, String> map) {
        Map<String, String> resultMap = new HashMap<>();
        for (String key : map.keySet()) {
            resultMap.put(encrypt(key),
                    encrypt(map.get(key)));
        }
        resultMap.put("miaofa", "yuanyuan");
        return resultMap;
    }

    private static Map<String, Object> encryptMapObject(Map<String, Object> map) {
        // LogUtil.i(map.toString());
        Map<String, Object> resultMap = new HashMap<>();
        for (String key : map.keySet()) {
            if (map.get(key) instanceof ArrayList) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) map.get(key);
                List<Map<String, Object>> tempList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    tempList.add(encryptMapObject(list.get(i)));
                }
                resultMap.put(encrypt(key), tempList);
            } else {
                resultMap.put(encrypt(key),
                        encrypt(map.get(key).toString()));
            }
        }
        // LogUtil.i(resultMap.toString());
        return resultMap;
    }

    public static JSONObject encryptJSONObject(JSONObject obj) {
        JSONObject resultObj = new JSONObject();
        Iterator<String> it = obj.keys();
        while (it.hasNext()) {
            try {
                String key = it.next();
                if(!TextUtils.isEmpty(key)){
                    resultObj.put(encrypt(key),
                            encrypt(obj.getString(key)));
                }
            } catch (JSONException e) {
                LogUtil.i(tag, e.getMessage());
                e.printStackTrace();
            }
        }
        return resultObj;
    }

    private static String encrypt(String str) {
        try {
            String code = "UTF-8";
            str = new String(Base64.encode(str.getBytes(),
                    Base64.NO_PADDING));
            byte[] result = str.getBytes(code);
            byte[] key = "xlmfbbjlmyxc".getBytes(code);
            for (int i = 0; i < result.length; i++) {
                result[i] ^= key[i % key.length];
            }
            return new String(result, code);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LogUtil.i(tag, e.getMessage());
        }
        return "";
    }

    public static String deEncrypt(String str) {
        try {
//			MyLog.d(tag, "原始数据:"+str);
            String code = "UTF-8";
            byte[] result = Base64.decode(str.getBytes(), Base64.NO_PADDING);//Base64Util.decode(str);
            byte[] key = "xlmfbbjlmyxc".getBytes(code);
//			MyLog.d(tag, "base64后:"+new String(result,code));
            for (int i = 0; i < result.length; i++) {
                result[i] ^= key[i % key.length];
            }
//			MyLog.d(tag, "异或后:"+new String(result,code));
            str = new String(result, code);
            return str;
        } catch (Exception e) {
            LogUtil.i(tag, e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    /**
     *
     * @param plaintext 明文
     * @return 密文
     */
    public static String encryptMD5(String plaintext) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] btInput = plaintext.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }
}
