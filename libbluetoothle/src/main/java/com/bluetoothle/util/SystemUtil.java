package com.bluetoothle.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

@SuppressWarnings("ALL")
public class SystemUtil {

    private SystemUtil() {
        throw new UnsupportedOperationException("Cannot be instantiated");
    }

    /**
     * 获取使用国家
     * 中国zh
     */
    public static String getLanguage(Context context){
        String language = context.getResources().getConfiguration().locale.getLanguage();
        language = language.contains("en")?"en":"zh";
        return language;
    }

    /**
     * 改变背景透明度
     * @param context
     * @param alpha
     */
    public static void changeAlpha(Context context,float alpha) {
        WindowManager.LayoutParams params = ((Activity) context).getWindow().getAttributes();
        params.alpha = alpha;
        ((Activity) context).getWindow().setAttributes(params);
    }

    /**
     * 隐藏虚拟键盘
     */
    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE );
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    /**
     * 显示虚拟键盘
     */
    public static void showKeyboard(final View view) {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE );
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        }, 50);
    }

    /**
     * 获得状态栏的高度
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    @SuppressWarnings("unchecked")
    public static <T> T castList(Object obj) {
        return (T) obj;
    }

    /**
     * 获取应用程序包名
     */
    public static String getAppCurrentVersion(Context context) {
        String appVersion = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            appVersion = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersion;
    }

    /**
     * 检查网络是否已连接
     */
    public static boolean checkNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = connectivity.getAllNetworkInfo();
        if (info != null) {
            for (NetworkInfo anInfo : info) {
                if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                    if (anInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        return true;
                    } else if (anInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 打开通讯录选择一个联系人
     */
    public static void selectContactForResult(Activity activity, int requestCode) {
        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setClassName("com.android.contacts","com.android.contacts.activities.ContactSelectionActivity");
        activity.startActivityForResult(intent, requestCode);*/

        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * onActivityResult方法中获取号码
     */
    public static String getPhoneNumberFromIntent(Activity activity, Intent intent){
        Cursor cursor = null;
        Cursor phone = null;
        try{
            String[] projections = {ContactsContract.Contacts._ID,ContactsContract.Contacts.HAS_PHONE_NUMBER};
            cursor = activity.getContentResolver().query(intent.getData(),projections, null, null, null);
            if ((cursor == null) || (!cursor.moveToFirst())){
                return null;
            }
            int _id = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID);
            String id = cursor.getString(_id);
            int has_phone_number = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER);
            int hasPhoneNumber = cursor.getInt(has_phone_number);
            String phoneNumber = null;
            if(hasPhoneNumber>0){
                phone = activity.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                if (phone == null) {
                    return null;
                }
                while(phone.moveToNext()){
                    int index = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    phoneNumber = phone.getString(index);
                    phoneNumber = phoneNumber.replace(" ", "").replace("-", "").replace("+86", "");
                }
            }
            return phoneNumber;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (cursor != null) cursor.close();
            if(phone != null) phone.close();
        }
        return null;
    }

    /**
     * 检测定位功能是否开启
     */
    public static boolean locationIsEnable(Context context) {
        boolean flag = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // GPS辅助定位,AGPS,借助网络
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            flag = true;
        }
        return flag;
    }
}
