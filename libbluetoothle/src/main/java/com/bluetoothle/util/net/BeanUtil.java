package com.bluetoothle.util.net;

import android.text.TextUtils;

import com.bluetoothle.util.log.LogUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ALL")
public class BeanUtil {

    /**
     * 反射封装网络数据包
     * 目前只能封装一个json数组的字符串，json数组中的每个元素以T对象来封装，最后以列表返回
     * 如果jsonArrayString是空的，则返回一个只有一个空元素的列表；如果T对象的字节码为空，则返回一个将jsonArrayString作为唯一元素的列表
     * @param jsonArrayString   网络访问数据包
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> antiSerializationJsonString(String jsonArrayString, Class<T> clazz) {
        List dataList = new ArrayList();
        if(TextUtils.isEmpty(jsonArrayString) || "null".equalsIgnoreCase(jsonArrayString)){
//            dataList.add(null);
            return dataList;
        }
        try {
            if(clazz == null){
                dataList.add(jsonArrayString);
                return dataList;
            }
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for (int index=0;index<jsonArray.length();index++) {
                JSONObject jsonObject = jsonArray.getJSONObject(index);
                Object object = clazz.newInstance();
                Map<String, Class> fieldList = reflectClassFieldNameAndTypeMap(object);
                for (Map.Entry<String, Class> entry : fieldList.entrySet()) {
                    String fieldName = entry.getKey();
                    if (jsonObject.toString().contains("\"" + fieldName + "\"")) {
                        String firstChar = fieldName.substring(0, 1);
                        String methodName = "set" + fieldName.replaceFirst(firstChar, firstChar.toUpperCase(Locale.US));
                        Class[] methodParamType = getMethodParamTypes(clazz.newInstance(), methodName);
                        Method method = clazz.getDeclaredMethod(methodName, methodParamType);
                        try {
                            String fieldValue = jsonObject.getString(fieldName);
                            // && !"null".equalsIgnoreCase(fieldValue)
                            if(fieldValue != null){
                                method.invoke(object, fieldValue);
                            }
                        } catch (Exception eString) {
                            //e.printStackTrace();
                            try {
                                method.invoke(object, jsonObject.getInt(fieldName));
                            } catch (Exception eInt) {
                                //eInt.printStackTrace();
                                try{
                                    method.invoke(object,jsonObject.getLong(fieldName));
                                }catch (Exception eLong) {
                                    //eLong.printStackTrace();
                                    try {
                                        method.invoke(object,jsonObject.getDouble(fieldName));
                                    }catch (Exception eDouble){
                                        //eDouble.printStackTrace();
                                        try{
                                            //Float 类型转换
                                            method.invoke(object,Float.parseFloat(jsonObject.get(fieldName).toString()));
                                        }catch (Exception ignored) {
                                            ignored.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                dataList.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dataList;
    }

    private static Map<String, Class> reflectClassFieldNameAndTypeMap(Object obj) throws ClassNotFoundException {
        Map<String, Class> fieldMap = new HashMap<>();
        Class clazz;
        if (obj instanceof String) {
            String obj_string = (String) obj;
            clazz = Class.forName(obj_string);
        } else {
            clazz = obj.getClass();
        }
        Field[] fieldList = clazz.getDeclaredFields();
        for (Field field : fieldList) {
            fieldMap.put(field.getName(), field.getType());
        }
        return fieldMap;
    }

    @SuppressWarnings("rawtypes")
    private static Class[] getMethodParamTypes(Object classInstance, String methodName) throws ClassNotFoundException {
        Class[] paramTypes;
        paramTypes = null;
        Method[] methods = classInstance.getClass().getMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                Class[] params = method.getParameterTypes();
                paramTypes = new Class[params.length];
                for (int j = 0; j < params.length; j++) {
                    paramTypes[j] = Class.forName(params[j].getName());
                }
                break;
            }
        }
        return paramTypes;
    }

    /**
     * 将一个对象转换成json格式良好的数据表现形式
     * 列表型的转换成json数组
     * 字符串、浮点型转换成带双引号的
     * 数字型的保持不变
     * boolean型的默认用1表示true，0表示false
     * 引用类型的转换成json数组
     */
    private static String parseJsonFieldValue(Object fieldValue) {
        String fieldValueValue;
        if (fieldValue instanceof Boolean) {
            boolean fieldValueBoolean = (boolean) fieldValue;
            fieldValueValue = fieldValueBoolean ? "1" : "0";
            fieldValue = fieldValueValue;
        }
        if (fieldValue instanceof List) {//列表类型
            fieldValueValue = loadObjectListJsonString((List) fieldValue);
        } else if (fieldValue instanceof String || fieldValue instanceof Float || fieldValue instanceof Double) {
            fieldValueValue = "\"" + fieldValue + "\"";
        } else if (fieldValue instanceof Byte || fieldValue instanceof Integer || fieldValue instanceof Long){//简单类型
            fieldValueValue = fieldValue + "";
        } else {//其他默认为引用类型
            fieldValueValue = loadObjectJsonString(fieldValue);
        }
        if (TextUtils.isEmpty(fieldValueValue)) {
            fieldValueValue = "\"\"";
        }
        return fieldValueValue;
    }

    /**
     * 将JavaBean对象转换成json字符串
     */
    public static String loadObjectJsonString(Object object) {
        if (object instanceof List) {
            throw new IllegalArgumentException("请调用列表转换方法");
        }
        if (object == null) {
            return "";
        }
        String objectJsonString;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        Map<String, Class> fieldList = null;
        try {
            fieldList = reflectClassFieldNameAndTypeMap(object);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (fieldList == null) {
            return "";
        }
        for (Map.Entry<String, Class> entry : fieldList.entrySet()) {
            String fieldName = entry.getKey();
            String firstChar = fieldName.substring(0, 1);
            String methodName = "get" + fieldName.replaceFirst(firstChar, firstChar.toUpperCase(Locale.US));
            Class[] methodParamType;
            try {
                methodParamType = getMethodParamTypes(object, methodName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            Method method;
            try {
                method = object.getClass().getDeclaredMethod(methodName, methodParamType);
            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
                methodName = "is" + fieldName.replaceFirst(firstChar, firstChar.toUpperCase(Locale.US));
                try {
                    methodParamType = getMethodParamTypes(object, methodName);
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                    continue;
                }
                try {
                    method = object.getClass().getDeclaredMethod(methodName, methodParamType);
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                    continue;
                }
            }
            try {
                String fieldValueValue = parseJsonFieldValue(method.invoke(object));
                stringBuilder.append("\"").append(fieldName).append("\"").append(":").append(fieldValueValue).append(",");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        objectJsonString = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
        objectJsonString += "}";
        return objectJsonString;
    }

    /**
     * 将JavaBean对象列表转换成json字符串
     */
    public static String loadObjectListJsonString(List listObject) {
        if (listObject == null || listObject.size() == 0) {
            return "";
        }
        String objectListJsonString = "[";
        for (int i = 0; i < listObject.size(); i++) {
            objectListJsonString += loadObjectJsonString(listObject.get(i));
            if (i < listObject.size() - 1) {
                objectListJsonString += ",";
            }
//            if (i == 0) {
//                objectListJsonString += "[";
//                objectListJsonString += loadObjectJsonString(listObject.get(i));
//                objectListJsonString += ",";
//            } else if (i < listObject.size() - 1) {
//                objectListJsonString += loadObjectJsonString(listObject.get(i));
//                objectListJsonString += ",";
//            } else {
//                objectListJsonString += loadObjectJsonString(listObject.get(i));
//                objectListJsonString += "]";
//            }
        }
        return objectListJsonString + "]";
    }

    /**
     * 将一个Map对象转换成json字符串
     */
    public static String loadMapJsonString(Map<String, Object> objectMap){
        if (objectMap == null || objectMap.size() == 0) {
            return "";
        }
        String mapJsonString = "";
        mapJsonString += "{";
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String keyString = "\"" + key + "\"";
            String valueString;
            LogUtil.i("loadMapJsonString,key=" + key + ",value=" + value);
            if (value instanceof List) {//列表类型
                valueString = loadObjectListJsonString((List) value);
            } else if (value instanceof Byte || value instanceof Integer
                    || value instanceof Long || value instanceof Float
                    || value instanceof Double || value instanceof Boolean || value instanceof String){//简单类型
                valueString = parseJsonFieldValue(value);
            } else {//其他默认为引用类型
                if (value instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) value;
                    valueString = jsonArray.toString();
                } else {
                    valueString = loadObjectJsonString(value);
                }
            }
            mapJsonString += (keyString + ":" + valueString + ",");
        }
        mapJsonString = mapJsonString.substring(0, mapJsonString.length() - 1);
        mapJsonString += "}";
        return mapJsonString;
    }
}