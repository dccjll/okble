package com.bluetoothle.base;

/**
 * Created by dccjll on 2017/6/29.
 * 手机配置管理器
 */

public class PhoneConfigManager {

    public static class HUAWEI {
        public static final String brand = "HUAWEI";
    }

    public static class XIAOMI {
        public static final String brand = "Xiaomi";
    }

    public static class MEIZU {
        public static final String brand = "Meizu";
    }

    public static boolean isHuaweiBrand(String phoneBrand) {
        return HUAWEI.brand.equalsIgnoreCase(phoneBrand);
    }

    public static boolean isXiaomiBrand(String phoneBrand) {
        return XIAOMI.brand.equalsIgnoreCase(phoneBrand);
    }

    public static boolean isMeizuBrand(String phoneBrand) {
        return MEIZU.brand.equalsIgnoreCase(phoneBrand);
    }
}
