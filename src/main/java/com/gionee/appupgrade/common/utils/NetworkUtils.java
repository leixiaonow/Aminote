package com.gionee.appupgrade.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import com.gionee.feedback.config.NetConfig.NetType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    public enum ConnectionType {
        CONNECTION_TYPE_IDLE,
        CONNECTION_TYPE_WIFI,
        CONNECTION_TYPE_3G,
        CONNECTION_TYPE_2G,
        CONNECTION_TYPE_4G
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean flag;
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            flag = false;
        } else {
            flag = true;
        }
        LogUtils.logd(TAG, LogUtils.getThreadName() + "flag = " + flag);
        return flag;
    }

    public static void getNetworkTypeUrl(StringBuffer url, Context context) {
        if (url.indexOf("?") != -1) {
            url.append("&");
        } else {
            url.append("?");
        }
        url.append("nt=");
        switch (getConnectionType(context)) {
            case CONNECTION_TYPE_2G:
                appene2GToUrl(url, isWapConnection(context));
                return;
            case CONNECTION_TYPE_3G:
                url.append(NetType.NET_TYPE_3G);
                return;
            case CONNECTION_TYPE_4G:
                url.append(NetType.NET_TYPE_4G);
                return;
            case CONNECTION_TYPE_WIFI:
                url.append("WF");
                return;
            default:
                return;
        }
    }

    private static int getNetworkTypeGemini(Context context, TelephonyManager telephonyManager) {
        if (Config.IS_MTK_PLATFORM) {
            return getNetworkTypeGeminiForMTK(context, telephonyManager);
        }
        return getNetworkTypeGeminiForQcom(context, telephonyManager);
    }

    private static int getNetworkTypeGeminiForQcom(Context context, TelephonyManager telephonyManager) {
        try {
            Class<?> mSimTelephonyManagerClass = Class.forName("android.telephony.MSimTelephonyManager");
            Method getNetworkTypeMethod = mSimTelephonyManagerClass.getMethod("getNetworkType", new Class[]{Integer.TYPE});
            Method getPreferredDataSubscriptionMethod = mSimTelephonyManagerClass.getMethod("getPreferredDataSubscription", new Class[0]);
            Object mSimTelephonyManager = mSimTelephonyManagerClass.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
            int simId = ((Integer) getPreferredDataSubscriptionMethod.invoke(mSimTelephonyManager, new Object[0])).intValue();
            int type = ((Integer) getNetworkTypeMethod.invoke(mSimTelephonyManager, new Object[]{Integer.valueOf(simId)})).intValue();
            LogUtils.logd(TAG, "getNetworkTypeGeminiForQcom = " + type);
            return type;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return telephonyManager.getNetworkType();
        } catch (InstantiationException e2) {
            e2.printStackTrace();
            return telephonyManager.getNetworkType();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            return telephonyManager.getNetworkType();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            return telephonyManager.getNetworkType();
        } catch (IllegalAccessException e5) {
            e5.printStackTrace();
            return telephonyManager.getNetworkType();
        } catch (InvocationTargetException e6) {
            e6.printStackTrace();
            return telephonyManager.getNetworkType();
        }
    }

    private static int getNetworkTypeGeminiForMTK(Context context, TelephonyManager telephonyManager) {
        try {
            Class<?> simInfoClass = Class.forName("android.provider.Telephony$SIMInfo");
            String gprsConnectionSimSettingString = (String) System.class.getField("GPRS_CONNECTION_SIM_SETTING").get(null);
            Method getNetworkTypeGemini = TelephonyManager.class.getMethod("getNetworkTypeGemini", new Class[]{Integer.TYPE});
            int soltId = ((Integer) simInfoClass.getMethod("getSlotById", new Class[]{Context.class, Long.TYPE}).invoke(null, new Object[]{context, Long.valueOf(System.getLong(context.getContentResolver(), gprsConnectionSimSettingString, 0))})).intValue();
            LogUtils.logd(TAG, "gprsConnectionSimId = " + gprsConnectionSimId);
            LogUtils.logd(TAG, "soltId = " + soltId);
            int type = ((Integer) getNetworkTypeGemini.invoke(telephonyManager, new Object[]{Integer.valueOf(soltId)})).intValue();
            LogUtils.logd(TAG, "getNetworkTypeGeminiForMTK ( " + soltId + ")= " + type);
            return type;
        } catch (Exception e) {
            e.printStackTrace();
            return telephonyManager.getNetworkType();
        }
    }

    public static boolean isWIFIConnection(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            int netWorkType = activeNetworkInfo.getType();
            if ((1 == netWorkType || 6 == netWorkType) && activeNetworkInfo.getState() == State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public static ConnectionType getConnectionTypeByNetworkType(int networkType) {
        switch (networkType) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
                return ConnectionType.CONNECTION_TYPE_2G;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
                return ConnectionType.CONNECTION_TYPE_3G;
            case 13:
                return ConnectionType.CONNECTION_TYPE_4G;
            default:
                return ConnectionType.CONNECTION_TYPE_IDLE;
        }
    }

    private static int getNetworkSingleSim(Context context, TelephonyManager telephonyManager) {
        int type = telephonyManager.getNetworkType();
        LogUtils.logd(TAG, "getNetworkSingleSim  type =  " + type);
        return type;
    }

    private static ConnectionType getMobileConnectionType(Context context) {
        ConnectionType type = ConnectionType.CONNECTION_TYPE_IDLE;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager == null) {
            return type;
        }
        int networkType;
        if (Config.GEMINI_SUPPORT) {
            networkType = getNetworkTypeGemini(context, telephonyManager);
        } else {
            networkType = getNetworkSingleSim(context, telephonyManager);
        }
        return getConnectionTypeByNetworkType(networkType);
    }

    public static ConnectionType getConnectionType(Context context) {
        ConnectionType type = ConnectionType.CONNECTION_TYPE_IDLE;
        if (!isNetworkAvailable(context)) {
            return type;
        }
        if (isWIFIConnection(context)) {
            return ConnectionType.CONNECTION_TYPE_WIFI;
        }
        return getMobileConnectionType(context);
    }

    public static boolean isWapConnection(Context context) {
        ConnectivityManager lcm = (ConnectivityManager) context.getSystemService("connectivity");
        if (lcm.getNetworkInfo(0).getState() != State.CONNECTED) {
            return false;
        }
        String currentAPN = lcm.getNetworkInfo(0).getExtraInfo();
        if (currentAPN == null || !currentAPN.contains(NetType.NET_TYPE_WAP)) {
            return false;
        }
        return true;
    }

    public static boolean isMobileNetwork(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            LogUtils.logd(TAG, "isMobileNetwork  getType: " + activeNetworkInfo.getType() + " activeNetworkInfo.getState(): " + activeNetworkInfo.getState());
            if (activeNetworkInfo.getType() == 0 && activeNetworkInfo.getState() == State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    private static void appene2GToUrl(StringBuffer url, boolean isWapNetwork) {
        url.append(NetType.NET_TYPE_2G);
        if (isWapNetwork) {
            url.append("&wap");
        }
    }
}
