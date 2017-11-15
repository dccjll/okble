package com.bluetoothle.util;

import com.bluetoothle.util.log.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dessmann on 16/7/27.
 * BLE字节数据辅助工具
 */
public class ByteUtil {
    /**
     * 截取一部分字节
     * @param bytes
     * @param start
     * @param len
     * @return
     */
    public static byte[] getSubbytes(byte[] bytes, int start, int len) {
        if (bytes.length < len || len == 0) {
            return null;
        }else if (bytes.length == len){
            return bytes;
        }
        byte[] bs = new byte[len];
        for (int i = 0; i < len; i++) {
            bs[i] = bytes[start++];
        }
        return bs;
    }

    /**
     * 以十六进制字符输出字节数组到日志,每两位字符进行分隔
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            if (i == src.length - 1) {
                stringBuilder.append(hv.toUpperCase(Locale.US));
            }else{
                stringBuilder.append(hv.toUpperCase(Locale.US)).append(" ");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 比较两个字节数组值是否相等
     * @param buffer1
     * @param buffer2
     * @return
     */
    public static boolean compareTwoBytes(byte[] buffer1, byte[] buffer2){
        if(buffer1 == null || buffer2 == null || buffer1.length != buffer2.length){
            return false;
        }
        for(int i=0;i<buffer1.length;i++){
            if(buffer1[i] != buffer2[i]){
                return false;
            }
        }
        return true;
    }

    /**
     * 正则通用验证
     * @param res   验证字符串
     * @param regex 验证规则
     * @return
     */
    private static boolean regexCheck(String res, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(res);
        return matcher.matches();
    }

    /**
     * 将指定进制字(10进制或16进制)符串转换成字节数组
     */
    public static byte[] parseRadixStringToBytes(String src, int radix){
        try {
            if (src == null || src.length() <= 0) {
                return null;
            }
            src = src.replace(" ", "").replace("-", "").replace("_", "");
            StringBuilder stringBuffer = new StringBuilder();
            for(int index=0;index<src.length();index+=2){
                if(index == src.length() - 1){
                    stringBuffer.append(src.substring(index, index + 1));
                }else if(index == src.length() - 2){
                    stringBuffer.append(src.substring(index, index + 2));
                }else{
                    stringBuffer.append(src.substring(index, index + 2)).append(" ");
                }
            }
            return radixStringToBytes(stringBuffer.toString(), radix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将以空格分割的每两位相连的指定进制字(10进制或16进制)符串转换成字节数组
     */
    private static byte[] radixStringToBytes(String src, int radix){
        if (src == null || src.length() <= 0) {
            return null;
        }
        String[] srcarray;
        try {
            srcarray = src.split(" ");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if(srcarray.length == 0){
            return null;
        }
        for(int i=0;i<srcarray.length;i++){
            if(srcarray[i].length() > 2){
                return null;
            }
            if(srcarray[i].length() == 1){
                srcarray[i] = 0 + srcarray[i];
            }
            if(radix == 16 && !regexCheck(srcarray[i], "^[0-9a-fA-F]{2}$")){
                return null;
            }else if(radix == 10 && !regexCheck(srcarray[i], "^[0-9]{2}$")){
                return null;
            }
        }
        byte[] srcbytes = new byte[srcarray.length];
        for(int i=0;i<srcarray.length;i++){
            if(radix == 16){
                srcbytes[i] = (byte) Integer.parseInt(srcarray[i], 16);
            }else if(radix == 10){
                srcbytes[i] = (byte) Integer.parseInt(srcarray[i]);
            }else{
                LogUtil.i("hexStringToBytes,radix error");
                return null;
            }
        }
        return srcbytes;
    }

    /**
     * 将一个小于8个字节的字节数组转换成长整形
     * @param src   源字节数组
     * @return
     */
    public static Long lessThan8bytesToLongInt(byte[] src){
        Long target = 0L;
        if(src == null || src.length <= 0 || src.length > 8){
            return null;
        }
        for(int i=0; i < src.length; i++){
            target += (src[i] << (src.length - i - 1) * 8);
        }
        return target;
    }

    /**
     * 将一个小于4个字节的字节数组转换成整形
     * @param src   源字节数组
     * @return
     */
    public static int lessThan4bytesInt(byte[] src){
        int target = 0;
        if(src == null || src.length <= 0 || src.length > 4){
            return -1;
        }
        for(int i=0; i < src.length; i++){
            if(src[i] < 0){
                int temp = Integer.parseInt(bytesToHexString(new byte[]{src[i]}).trim(), 16);
                target += (temp << (src.length - i - 1) * 8);
                continue;
            }
            target += (src[i] << (src.length - i - 1) * 8);
        }
        return target;
    }

    /**
     * 将一个两位的十进制字面量数据转换成对应字面量的字节显示，即十进制的16转换为0x16
     * @param tendesc   源数据
     * @return
     */
    public static Byte parseTenDescToDescByte(String tendesc){
        if(tendesc == null || tendesc.length() != 2){
            return null;
        }
        return (byte) ((byte) ((Byte.parseByte(tendesc.substring(0, 1))) << 4) | (Byte.parseByte(tendesc.substring(1, 2))));
    }

    /**
     * 将一个字节数组转换成等长的字节数组列表
     * @param srcBytes  源字节数组
     * @param maxLength 长度
     * @return  字节数组列表
     */
    public static List<byte[]> paeseByteArrayToByteList(byte[] srcBytes, Integer maxLength){
        if(srcBytes == null || srcBytes.length == 0 || maxLength == null || maxLength == 0){
            return null;
        }
        List<byte[]> byteList = new ArrayList<>();
        if(maxLength >= srcBytes.length){
            byteList.add(srcBytes);
            return byteList;
        }
        int num = srcBytes.length/maxLength;
        int level = srcBytes.length%maxLength;
        for(int index=0;index<num;index++){
            byteList.add(getSubbytes(srcBytes, index * maxLength, maxLength));
        }
        byteList.add(getSubbytes(srcBytes, maxLength * num, level));
        return byteList;
    }

    public static void main(String[] args){
        List<byte[]> bytes = paeseByteArrayToByteList(new byte[]{(byte) 0xFE,0x01,0x39, 0, 0x18, 65, 0x6A, 0x3F, 0x59, 10, 0x2A, (byte) 0xCB, 0x16, 0x22, 0x3A, 0x23, 0x33, 0x2E, 0x29, (byte) 0xCB, 0x10, 0x14, 0x14, 0x02, 0x21, 0x1A, 0x0E, (byte) 0xF3, 0x00, 0x06, 0x1E,(byte) 0xFE,0x01,0x39, 0, 0x18, 65, 0x6A, 0x3F, 0x59, 10, 0x2A, (byte) 0xCB, 0x16, 0x22, 0x3A, 0x23, 0x33, 0x2E, 0x29, (byte) 0xCB, 0x10, 0x14, 0x14, 0x02, 0x21, 0x1A, 0x0E, (byte) 0xF3, 0x00, 0x06, 0x1E}, 20);
        for(int i=0;i<bytes.size();i++){
            System.out.println(bytesToHexString(bytes.get(i)));
        }
//        System.out.println(bytesToHexString(parseStringToBytesWithRadix("20161026133603118668165280", 16)));
    }

    /**
     * 两个字节数组按位异或
     * @param srcbytes	源字节数组
     * @param xorbytes	进行异或操作的字节数组
     * @return 异或后的字节数组
     */
    public static byte[] bytesXor(byte[] srcbytes, byte[] xorbytes){
        if(srcbytes == null || xorbytes == null){
            return null;
        }
        byte[] data = new byte[srcbytes.length];
        byte[] xortempbytes = new byte[srcbytes.length];
        if (srcbytes.length < xorbytes.length) {
            System.arraycopy(xorbytes, 0, xortempbytes, 0, xortempbytes.length);
        } else {
            Integer num = srcbytes.length / xorbytes.length;
            Integer levelNum = srcbytes.length % xorbytes.length;
            byte[] subbytes = getSubbytes(xorbytes, 0, levelNum);
            for(int i=0;i<num;i++){
                System.arraycopy(xorbytes, 0, xortempbytes, i * xorbytes.length, xorbytes.length);
            }
            if(levelNum > 0 && subbytes != null && subbytes.length > 0){
                System.arraycopy(subbytes, 0, xortempbytes, num * xorbytes.length, levelNum);
            }
        }
        for(int i=0;i<srcbytes.length;i++){
            data[i] = (byte) (srcbytes[i] ^ xortempbytes[i]);
        }
        return data;
    }
}
