package com.bluetoothle.base;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.bluetoothle.R;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/10 09 53 星期五<br>
 * 功能描述：<br>蓝牙栈消息码
 */

public class BLECode {

    public final static SparseArray<Integer> codeMap = new SparseArray<>();//消息码集合

    public final static int not_support_ble = -10000;//手机不支持蓝牙低功耗
    public final static int can_not_get_ble_manager = -10001;//无法获取蓝牙管理服务
    public final static int can_not_get_ble_adapter = -10002;//无法获取蓝牙适配器
    public final static int please_check_device_mac_or_name = -10003;//请检查设备mac地址或名称
    public final static int can_not_get_accelerometer_sensor = -10004;//无法获取重力传感器
    public final static int device_connected = 10000;//设备已连接
    public final static int device_disconnected = -10005;//设备已断开连接
    public final static int on_receive_ble_error_code = -10006;//收到蓝牙底层协议栈异常消息
    public final static int not_found_device = -10007;//未发现设备，请确保设备蓝牙开启并在一定范围内
    public final static int the_mac_address_for_dfu_parse_failure = -10008;//进入固件更新的mac地址转换失败
    public final static int on_file_format_exception = -10009;//文件格式检测失败
    public final static int on_start_scan_exception = -10010;//启动扫描异常
    public final static int scan_need_location_permission = -10011;//扫描蓝牙设备需要位置权限
    public final static int location_permission_forbidden = -10012;//位置权限被禁止
    public final static int blutooth_is_closed = -10013;//蓝牙未开启，请前往设置中心打开蓝牙
    public final static int need_location_permission = -10014;//当前操作需要定位权限与定位功能，请允许该应用访问手机定位权限并打开手机的定位功能后重试
    public final static int scanning = -10015;//正在扫描
    public final static int connect_fail_reach_to_max_count = -10016;//连接失败,已尝试最大连接次数
    public final static int on_bluetooth_device_or_adapter_or_mac_validate_failure = -10017;//蓝牙设备(或蓝牙适配器或目标设备mac地址)校验失败
    public final static int already_connect_max_count_device_can_not_connect_more = -10018;//已连接最大数量的蓝牙设备，无法再连接更多的设备
    public final static int on_bluetooth_gatt_empty = -10019;//未连接
    public final static int on_bluetooth_gatt_callback_empty = -10020;//连接状态异常
    public final static int empty_ble_service_list = -10021;//服务列表为空
    public final static int on_notification_uuids_validate_failure = -10022;//通知uuid列表校验失败
    public final static int not_found_specified_notification_service_uuid = -10023;//没有找到匹配的通知服务
    public final static int not_found_specified_notification_characteristics_uuid = -10024;//没有找到匹配的通知特征
    public final static int not_notification_function = -10025;//指定的通知没有通知功能
    public final static int not_found_specified_notification_characteristics_descriptor = -10026;//没有找到匹配的通知特征描述符
    public final static int on_bluetooth_gatt_descriptor_set_enable_notification_value_failure = -10027;//蓝牙底层启用通知失败
    public final static int on_bluetooth_gatt_set_characteristics_notification_failure = -10028;//蓝牙底层设置通知开启状态失败
    public final static int on_bluetooth_gatt_write_descriptor_failure = -10029;//蓝牙底层写通知描述符失败
    public final static int on_write_data_uuids_validate_failure = -10030;//写数据uuid列表校验失败
    public final static int on_empty_write_data = -10031;//写入的数据为空
    public final static int on_data_sub_package_exception = -10032;//数据分包异常
    public final static int not_found_specified_write_data_service_uuid = -10033;//没有找到匹配的写数据服务
    public final static int not_found_specified_write_data_characteristics_uuid = -10034;//没有找到匹配的写数据特征
    public final static int not_write_data_function = -10035;//指定的服务没有写数据功能
    public final static int on_bluetooth_gatt_characteristics_set_value_failure = -10036;//蓝牙底层设置写入数据失败
    public final static int on_bluetooth_gatt_write_characteristics_failure= -10037;//蓝牙底层写数据失败
    public final static int ble_timeout = -10038;//蓝牙超时
    public final static int disconnected_on_timeout_for_disconnect = -10039;//已超时断开
    public final static int on_ble_uuid_list_validate_failure = -10040;//通讯uuid组校验失败

    static {
        codeMap.put(not_support_ble, R.string.not_support_ble);
        codeMap.put(can_not_get_ble_manager, R.string.can_not_get_ble_manager);
        codeMap.put(can_not_get_ble_adapter, R.string.can_not_get_ble_adapter);
        codeMap.put(please_check_device_mac_or_name, R.string.please_check_device_mac_or_name);
        codeMap.put(can_not_get_accelerometer_sensor, R.string.can_not_get_accelerometer_sensor);
        codeMap.put(device_connected, R.string.device_connected);
        codeMap.put(device_disconnected, R.string.device_disconnected);
        codeMap.put(on_receive_ble_error_code, R.string.on_receive_ble_error_code);
        codeMap.put(not_found_device, R.string.not_found_device);
        codeMap.put(the_mac_address_for_dfu_parse_failure, R.string.the_mac_address_for_dfu_parse_failure);
        codeMap.put(on_file_format_exception, R.string.on_file_format_exception);
        codeMap.put(on_start_scan_exception, R.string.on_start_scan_exception);
        codeMap.put(scan_need_location_permission, R.string.scan_need_location_permission);
        codeMap.put(location_permission_forbidden, R.string.location_permission_forbidden);
        codeMap.put(blutooth_is_closed, R.string.blutooth_is_closed);
        codeMap.put(need_location_permission, R.string.need_location_permission);
        codeMap.put(scanning, R.string.scanning);
        codeMap.put(connect_fail_reach_to_max_count, R.string.connect_fail_reach_to_max_count);
        codeMap.put(on_bluetooth_device_or_adapter_or_mac_validate_failure, R.string.on_bluetooth_device_or_adapter_or_mac_validate_failure);
        codeMap.put(already_connect_max_count_device_can_not_connect_more, R.string.already_connect_max_count_device_can_not_connect_more);
        codeMap.put(on_bluetooth_gatt_empty, R.string.on_bluetooth_gatt_empty);
        codeMap.put(on_bluetooth_gatt_callback_empty, R.string.on_bluetooth_gatt_callback_empty);
        codeMap.put(empty_ble_service_list, R.string.empty_ble_service_list);
        codeMap.put(on_notification_uuids_validate_failure, R.string.on_notification_uuids_validate_failure);
        codeMap.put(not_found_specified_notification_service_uuid, R.string.not_found_specified_notification_service_uuid);
        codeMap.put(not_found_specified_notification_characteristics_uuid, R.string.not_found_specified_notification_characteristics_uuid);
        codeMap.put(not_notification_function, R.string.not_notification_function);
        codeMap.put(not_found_specified_notification_characteristics_descriptor, R.string.not_found_specified_notification_characteristics_descriptor);
        codeMap.put(on_bluetooth_gatt_descriptor_set_enable_notification_value_failure, R.string.on_bluetooth_gatt_descriptor_set_enable_notification_value_failure);
        codeMap.put(on_bluetooth_gatt_set_characteristics_notification_failure, R.string.on_bluetooth_gatt_set_characteristics_notification_failure);
        codeMap.put(on_bluetooth_gatt_write_descriptor_failure, R.string.on_bluetooth_gatt_write_descriptor_failure);
        codeMap.put(on_write_data_uuids_validate_failure, R.string.on_write_data_uuids_validate_failure);
        codeMap.put(on_empty_write_data, R.string.on_empty_write_data);
        codeMap.put(on_data_sub_package_exception, R.string.on_data_sub_package_exception);
        codeMap.put(not_found_specified_write_data_service_uuid, R.string.not_found_specified_write_data_service_uuid);
        codeMap.put(not_found_specified_write_data_characteristics_uuid, R.string.not_found_specified_write_data_characteristics_uuid);
        codeMap.put(not_write_data_function, R.string.not_write_data_function);
        codeMap.put(on_bluetooth_gatt_characteristics_set_value_failure, R.string.on_bluetooth_gatt_characteristics_set_value_failure);
        codeMap.put(on_bluetooth_gatt_write_characteristics_failure, R.string.on_bluetooth_gatt_write_characteristics_failure);
        codeMap.put(ble_timeout, R.string.ble_timeout);
        codeMap.put(disconnected_on_timeout_for_disconnect, R.string.disconnected_on_timeout_for_disconnect);
        codeMap.put(on_ble_uuid_list_validate_failure, R.string.on_ble_uuid_list_validate_failure);
    }

    /**
     * 获取消息码对应的描述信息
     */
    public static String getBLECodeMessage(Context context, int bleCode) {
        return context.getString(codeMap.get(bleCode));
    }

    /**
     * 获取消息码对应的描述信息等级
     */
    public static int getBLECodeMessageLevel(Context context, int bleCode) {
        return Log.INFO;
    }
}
