package com.bluetoothle.util.permisstion;

/**
 * 权限请求监听器
 */

public abstract class OnPermissionResult {
    public int requestCode;
    public String explainMsg;
    public String[] permissions;
    public String[] deniedPermissions;
    public String[] rationalePermissions;
    public int[] grantResults;

    //权限允许
    public abstract void granted(int requestCode);

    //权限拒绝
    public abstract void denied(int requestCode);
}
