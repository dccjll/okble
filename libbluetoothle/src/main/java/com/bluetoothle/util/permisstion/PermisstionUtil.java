package com.bluetoothle.util.permisstion;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.bluetoothle.base.PhoneConfigManager;
import com.bluetoothle.util.SystemUtil;
import com.bluetoothle.util.log.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yanfa on 2016/11/3.
 */

@TargetApi(Build.VERSION_CODES.M)
public class PermisstionUtil {
    private static final String TAG = "PermisstionUtil";

    //日历
    public static String[] CALENDAR = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
    public static final int CALENDAR_CODE = 0x1101;
    //相机
    public static String[] CAMERA = {Manifest.permission.CAMERA};
    public static final int CAMERA_CODE = 0x1102;
    //联系人, Manifest.permission.GET_ACCOUNTS
    public static String[] CONTACTS = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
    public static final int CONTACTS_CODE = 0x1103;
    //位置
    public static String[] LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final int LOCATION_CODE = 0x1104;
    //麦克风
    public static String[] MICROPHONE = {Manifest.permission.RECORD_AUDIO};
    public static final int MICROPHONE_CODE = 0x1105;
    //手机
    public static String[] PHONE = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.ADD_VOICEMAIL, Manifest.permission.USE_SIP, Manifest.permission.PROCESS_OUTGOING_CALLS};
    public static final int PHONE_CODE = 0x1106;
    //传感器
    public static String[] SENSORS = {Manifest.permission.BODY_SENSORS};
    public static final int SENSORS_CODE = 0x1107;
    //短信
    public static String[] SMS = {Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_WAP_PUSH, Manifest.permission.RECEIVE_MMS};
    public static final int SMS_CODE = 0x1108;
    //文件读写
    public static String[] STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int STORAGE_CODE = 0x1109;


    //拍照权限
//    public static final String CAMERA = Manifest.permission.CAMERA;
    //读取联系人权限
//    public static final String CONTACTS = Manifest.permission.READ_CONTACTS;
    //读写权限
//    public static final String STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    //WIFI、位置权限
//    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    // 打电话权限
//    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;

    private static final HashMap<String, Object> map = new HashMap<>();

    private static boolean checkSDK() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 请求读写sd卡权限
     */
    public static void requestStoragePermisstion(@NonNull final Context context, final OnPermissionResult onPermissionResult) {
        requestPermissions(context, STORAGE, STORAGE_CODE, "需要读写sd卡权限", onPermissionResult);
    }

    /**
     * 请求拍照权限
     */
    public static void requestCamaraPermission(@NonNull final Context context, final OnPermissionResult onPermissionResult) {
        requestPermissions(context, CAMERA, CAMERA_CODE, "需要拍照权限", onPermissionResult);
    }

    /**
     * 请求蓝牙低功耗需要的位置权限
     */
    public static void requestBLELocationPermission(@NonNull final Context context, String explainMsg, final OnPermissionResult onPermissionResult) {
        if (Build.VERSION.SDK_INT < 21) {//5.0之前的蓝牙不需要位置权限
            onPermissionResult.granted(LOCATION_CODE);
            return;
        }
        //5.0之后需要位置权限，请求位置权限
        requestLocationPermission(context, explainMsg, onPermissionResult);
    }

    /**
     * 请求位置权限
     */
    public static void requestLocationPermission(@NonNull final Context context, String explainMsg, final OnPermissionResult onPermissionResult) {
        //请求位置权限
        requestPermissions(context, LOCATION, LOCATION_CODE, explainMsg, new OnPermissionResult() {
            @Override
            public void granted(int requestCode) {//位置权限允许
                //再次检查定位功能是否开启
                if (SystemUtil.locationIsEnable(context)) {//定位已开启，则检查成功
                    onPermissionResult.granted(LOCATION_CODE);
                    return;
                }
                //定位未开启，跳转到系统定位请求界面或系统设置界面，提示用户手动开启定位功能
                try {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                onPermissionResult.denied(LOCATION_CODE);
            }

            @Override
            public void denied(int requestCode) {//位置权限不允许
                //打开当前应用的权限管理界面或系统设置界面，提示用户手动开启位置权限
                onPermissionResult.denied(LOCATION_CODE);
                startAppPermissionDetailSettingActivity(context);
            }
        });
    }

    /**
     * 请求权限
     *
     * @param context
     * @param permissions
     * @param requestCode
     * @param explainMsg
     * @param onPermissionResult
     */
    public static void requestPermissions(@NonNull Context context, @NonNull String[] permissions, int requestCode, String explainMsg, OnPermissionResult onPermissionResult) {
        onPermissionResult = initOnPermissionResult(onPermissionResult, permissions, requestCode, explainMsg);
        if (permissions.length == 0) {
            invokeOnRequestPermissionsResult(context, onPermissionResult);
        } else if (context instanceof Activity || (Object) context instanceof Fragment) {
            if (checkSDK()) {
                onPermissionResult.deniedPermissions = getDeniedPermissions(context, permissions);
                if (onPermissionResult.deniedPermissions.length > 0) {//存在被拒绝的权限
                    onPermissionResult.rationalePermissions = getRationalePermissions(context, onPermissionResult.deniedPermissions);
                    if (onPermissionResult.rationalePermissions.length > 0) {//向用户解释请求权限的理由
                        shouldShowRequestPermissionRationale(context, onPermissionResult);
                    } else {
                        invokeRequestPermissions(context, onPermissionResult);
                    }
                } else {//所有权限允许
                    onPermissionResult.grantResults = new int[permissions.length];
                    for (int i = 0; i < onPermissionResult.grantResults.length; i++) {
                        onPermissionResult.grantResults[i] = PackageManager.PERMISSION_GRANTED;
                    }
                    invokeOnRequestPermissionsResult(context, onPermissionResult);
                }
            } else {
                onPermissionResult.grantResults = getPermissionsResults(context, permissions);
                invokeOnRequestPermissionsResult(context, onPermissionResult);
            }
        }
    }
    private static boolean hasRecordPermission() {
        int minBuffer = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        short[] point = new short[minBuffer];
        int readSize = 0;
        AudioRecord audioRecord = null;
        try {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    (minBuffer * 100));
            // 开始录音
            audioRecord.startRecording();// 检测是否可以进入初始化状态
        } catch (Exception e) {
            LogUtil.e(TAG, "catch, 捕捉到异常, 无录音权限, e = " + e.getMessage());
            if (audioRecord != null) {
                audioRecord.release();
                audioRecord = null;
                LogUtil.i(TAG, "catch, 返回对象非空,释放资源");
            } else {
                LogUtil.i(TAG, "catch, 返回对象非空");
            }
            return false;
        }
        // 检测是否在录音中
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
        // 6.0以下机型都会返回此状态，故使用时需要判断bulid版本
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                LogUtil.e(TAG, "无法启动录音, 无法录音");
            }
            return false;
        } else {// 正在录音
            readSize = audioRecord.read(point, 0, point.length);
            // 检测是否可以获取录音结果
            if (readSize <= 0) {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
                LogUtil.e(TAG, "没有获取到录音数据，无录音权限");
                return false;
            } else {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
                LogUtil.i(TAG, "获取到录音数据, 有录音权限");
                return true;
            }
        }
    }

    /**
     * 获取被拒绝的权限
     *
     * @param context
     * @param permissions
     * @return
     */
    private static String[] getDeniedPermissions(Context context, String[] permissions) {
        List<String> list = new ArrayList<>();
        for (String permission : permissions) {
            if (checkPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                list.add(permission);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 获取权限请求结果
     *
     * @param context
     * @param permissions
     * @return
     */
    private static int[] getPermissionsResults(Context context, String[] permissions) {
        int[] results = new int[permissions.length];
        for (int i = 0; i < results.length; i++)
            results[i] = checkPermission(context, permissions[i]);
        return results;
    }

    private static String[] getRationalePermissions(Context context, String[] deniedPermissions) {
        List<String> list = new ArrayList<>();
        for (String permission : deniedPermissions) {
            if (context instanceof Activity) {
                if (((Activity) context).shouldShowRequestPermissionRationale(permission)) {
                    list.add(permission);
                }
            } else {
                throw new IllegalArgumentException("context 只能是Activity或Fragment");
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 调用权限请求方法
     *
     * @param context
     * @param onPermissionResult
     */
    private static void invokeRequestPermissions(Context context, OnPermissionResult onPermissionResult) {
        if (context instanceof Activity)
            ((Activity) context).requestPermissions(onPermissionResult.deniedPermissions, onPermissionResult.requestCode);
    }

    /**
     * 调用权限请求结果回调
     *
     * @param context
     * @param onPermissionResult
     */
    private static void invokeOnRequestPermissionsResult(Context context, OnPermissionResult onPermissionResult) {
        if (context instanceof Activity) {
            if (checkSDK()) {
                ((Activity) context).onRequestPermissionsResult(onPermissionResult.requestCode, onPermissionResult.permissions, onPermissionResult.grantResults);
            } else if (context instanceof ActivityCompat.OnRequestPermissionsResultCallback) {
                ((ActivityCompat.OnRequestPermissionsResultCallback) context).onRequestPermissionsResult(onPermissionResult.requestCode, onPermissionResult.permissions, onPermissionResult.grantResults);
            } else {
                onRequestPermissionsResult(onPermissionResult.requestCode, onPermissionResult.permissions, onPermissionResult.grantResults);
            }
        }
    }

    /**
     * 显示权限解释
     *
     * @param context
     * @param onPermissionResult
     */
    private static void shouldShowRequestPermissionRationale(final Context context, final OnPermissionResult onPermissionResult) {
//        new AlertDialog.Builder(context instanceof Activity?context:((Fragment)(Object)context).getActivity())
//                .setTitle("提示")
//                .setMessage(onPermissionResult.explainMsg)
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                        invokeRequestPermissions(context,onPermissionResult);
//                    }
//                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                        onPermissionResult.grantResults = getPermissionsResults(context,onPermissionResult.permissions);
//                        invokeOnRequestPermissionsResult(context,onPermissionResult);
//                    }
//                }).show();
//        DialogUtil.showConfirm(context, null, onPermissionResult.explainMsg, null, null, new BaseListener() {
//            @Override
//            public void baseListener(View v, String msg) {
//                invokeRequestPermissions(context, onPermissionResult);
//            }
//        }, new BaseListener() {
//            @Override
//            public void baseListener(View v, String msg) {
//                onPermissionResult.grantResults = getPermissionsResults(context,onPermissionResult.permissions);
//                invokeOnRequestPermissionsResult(context,onPermissionResult);
//            }
//        },true);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(onPermissionResult.explainMsg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                invokeRequestPermissions(context, onPermissionResult);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onPermissionResult.grantResults = getPermissionsResults(context, onPermissionResult.permissions);
                invokeOnRequestPermissionsResult(context, onPermissionResult);
            }
        });
        builder.show();
    }

    /**
     * 检查权限
     *
     * @param context
     * @param permission
     * @return
     */
    private static int checkPermission(Context context, String permission) {
        int result = context.checkPermission(permission, Process.myPid(), Process.myUid());
        if(MICROPHONE[0].equalsIgnoreCase(permission)&&checkSDK()==false&&result==PackageManager.PERMISSION_GRANTED){
            //录音权限
            result = hasRecordPermission()?PackageManager.PERMISSION_GRANTED:PackageManager.PERMISSION_DENIED;
        }
        return result;
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        synchronized (PermisstionUtil.class) {
            OnPermissionResult onPermissionResult = (OnPermissionResult) map.get(String.valueOf(requestCode));
            if (onPermissionResult != null) {
                List<String> deniedPermissions = new ArrayList<>();
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i]);
                    }
                }
                if (deniedPermissions.size() > 0) {
                    onPermissionResult.denied(requestCode);
                } else {
                    onPermissionResult.granted(requestCode);
                }
                map.remove(String.valueOf(requestCode));
            }
        }
    }

    /**
     * 初始化权限请求回调
     *
     * @param onPermissionResult
     * @param permissions
     * @param requestCode
     * @param explainMsg         @return
     */
    private static OnPermissionResult initOnPermissionResult(OnPermissionResult onPermissionResult, String[] permissions, int requestCode, String explainMsg) {
        synchronized (PermisstionUtil.class) {
            if (onPermissionResult == null) {
                onPermissionResult = new OnPermissionResult() {
                    @Override
                    public void granted(int requestCode) {

                    }

                    @Override
                    public void denied(int requestCode) {

                    }
                };
            }
            onPermissionResult.permissions = permissions;
            onPermissionResult.requestCode = requestCode;
            onPermissionResult.explainMsg = explainMsg;
            onPermissionResult.grantResults = new int[0];
            map.put(String.valueOf(requestCode), onPermissionResult);
            return onPermissionResult;
        }
    }

    /**
     * 跳转到miui的权限管理页面
     */
    private static void gotoMiuiPermission(Context context) {
        Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
        try {
            ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            i.setComponent(componentName);
            i.putExtra("extra_pkgname", context.getPackageName());
            context.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                i.setComponent(componentName);
                i.putExtra("extra_pkgname", context.getPackageName());
                context.startActivity(i);
            } catch (Exception e1) {
                e1.printStackTrace();
                startAppDetailSettingActivity(context);
            }
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private static void gotoMeizuPermission(Context context) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", context.getPackageName());
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startAppDetailSettingActivity(context);
        }
    }

    /**
     * 华为的权限管理页面
     */
    private static void gotoHuaweiPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startAppDetailSettingActivity(context);
        }

    }

    /**
     * 打开当前应用的设置详情
     */
    private static void startAppDetailSettingActivity(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(localIntent);
    }

    /**
     * 打开当前应用的权限设置详情
     */
    private static void startAppPermissionDetailSettingActivity(Context context) {
        if (PhoneConfigManager.isHuaweiBrand(Build.BRAND)) {
            gotoHuaweiPermission(context);
        } else if (PhoneConfigManager.isXiaomiBrand(Build.BRAND)) {
            gotoMiuiPermission(context);
        } else if (PhoneConfigManager.isMeizuBrand(Build.BRAND)) {
            gotoMeizuPermission(context);
        } else {
            startAppDetailSettingActivity(context);
        }
    }

    /**
     * 多组权限合并
     *
     * @param items
     * @return
     */
    public static String[] getPermissions(String[]... items) {
        int length = 0;
        for (String[] item : items) {
            length += item.length;
        }
        String[] result = new String[length];
        int i = 0;
        for (String[] item : items) {
            for (String itemIn : item) {
                result[i] = itemIn;
                i++;
            }
        }
        return result;
    }

}
