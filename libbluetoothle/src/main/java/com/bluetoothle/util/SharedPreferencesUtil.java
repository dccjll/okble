package com.bluetoothle.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>SharedPreferences工具类</h3>
 */
public final class SharedPreferencesUtil {

    private SharedPreferencesUtil() {

    }

    private static final String PREFERENCE_NAME = "dsmapp";

    /**
     * 保存String字符串
     *
     * @param context
     * @param key
     * @param value
     * @return 判断是否保存成功，保存成功返回true，反之返回false
     */
    public static boolean putString(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * 根据key获得保存的String字符串
     *
     * @param context
     * @param key
     * @return value（String类型，默认值为null，即当得不到值时返回null）
     */
    public static String getString(Context context, String key) {
        return getString(context, key, null);
    }

    /**
     * 根据key获得保存的String字符串
     *
     * @param context
     * @param key
     * @param defaultValue 当获取不到值时，用该值代替
     * @return String字符串
     */
    public static String getString(Context context, String key,
                                   String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getString(key, defaultValue);
    }

    /**
     * 保存int值
     *
     * @param context
     * @param key
     * @param value
     * @return 判断是否保存成功，保存成功返回true，反之返回false
     */
    public static boolean putInt(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * 获得保存的int值
     *
     * @param context
     * @param key
     * @return 默认值为-1
     */
    public static int getInt(Context context, String key) {
        return getInt(context, key, -1);
    }

    /**
     * 获得保存的int值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return 获得保存的int值
     */
    private static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key, defaultValue);
    }

    /**
     * 保存long值
     *
     * @param context
     * @param key
     * @param value
     * @return 判断是否保存成功，保存成功返回true，反之返回false
     */
    public static boolean putLong(Context context, String key, long value) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * 获得保存的long值
     *
     * @param context
     * @param key
     * @return 默认值为-1
     */
    public static long getLong(Context context, String key) {
        return getLong(context, key, -1);
    }

    /**
     * 获得保存的long值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return 获得保存的long值
     */
    private static long getLong(Context context, String key, long defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getLong(key, defaultValue);
    }

    /**
     * 保存float值
     *
     * @param context
     * @param key
     * @param value
     * @return 判断是否保存成功，保存成功返回true，反之返回false
     */
    public static boolean putFloat(Context context, String key, float value) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    /**
     * 获得保存的float值
     *
     * @param context
     * @param key
     * @return 默认值为-1.0f
     */
    public static float getFloat(Context context, String key) {
        return getFloat(context, key, -1.0f);
    }

    /**
     * 获得保存的float值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return 获得保存的float值
     */
    private static float getFloat(Context context, String key, float defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getFloat(key, defaultValue);
    }

    /**
     * 保存boolean值
     *
     * @param context
     * @param key
     * @param value
     * @return 判断是否保存成功，保存成功返回true，反之返回false
     */
    public static boolean putBoolean(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * 获得保存的int值
     *
     * @param context
     * @param key
     * @return 默认值为false
     */
    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    /**
     * 获得保存的boolean值
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return 获得保存的boolean值
     */
    private static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(key, defaultValue);
    }

    /**
     * 移除键值对
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (settings.contains(key)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(key);
            editor.apply();
        }
    }

    /**
     * 清空SharedPreference
     * @param context
     */
    private static void clear(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * 清空names除外的SharedPreference
     * @param context
     * @param names
     */
    public static void clearExcept(Context context, String[] names) {
        List<String> valueList = new ArrayList<>();
        for (String name : names) {
            valueList.add(getString(context, name, ""));
        }
        clear(context);
        for (int i = 0; i < names.length; i++) {
            putString(context, names[i], valueList.get(i));
        }
    }
}
