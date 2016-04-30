package com.gionee.feedback.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.gionee.feedback.config.NetConfig.NetType;

public final class SystemPropertiesUtil {
    public static String getModel() {
        return Build.MODEL;
    }

    public static String getOsVersion() {
        return VERSION.RELEASE;
    }

    public static String getRomVersion() {
        String version = getProp("ro.gn.gnznvernumber", "");
        return TextUtils.isEmpty(version) ? Build.DISPLAY : version;
    }

    private static String getProp(String key, String def) {
        String value = "phone";
        try {
            return (String) Class.forName("android.os.SystemProperties").getMethod("get", new Class[]{String.class, String.class}).invoke(null, new Object[]{key, def});
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
    }

    public static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static String getNetType(Context context) {
        String netType = NetType.NET_TYPE_UNKNOW;
        NetworkInfo ni = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (ni == null || !ni.isConnectedOrConnecting()) {
            return netType;
        }
        switch (ni.getType()) {
            case 0:
                return getMobileNetType(ni);
            case 1:
                return NetType.NET_TYPE_WIFI;
            default:
                return NetType.NET_TYPE_UNKNOW;
        }
    }

    @TargetApi(3)
    private static String getMobileNetType(NetworkInfo ni) {
        if (!TextUtils.isEmpty(Proxy.getDefaultHost())) {
            return NetType.NET_TYPE_WAP;
        }
        String netType;
        switch (ni.getSubtype()) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
                netType = NetType.NET_TYPE_2G;
                break;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
                netType = NetType.NET_TYPE_3G;
                break;
            case 13:
                netType = NetType.NET_TYPE_4G;
                break;
            default:
                netType = NetType.NET_TYPE_UNKNOW;
                break;
        }
        return netType;
    }
}
