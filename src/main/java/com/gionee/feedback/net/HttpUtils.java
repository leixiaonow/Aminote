package com.gionee.feedback.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.gionee.appupgrade.common.NewVersion.VersionType;
import com.gionee.feedback.config.EnvConfig;
import com.gionee.feedback.config.NetConfig;
import com.gionee.feedback.exception.FeedBackException;
import com.gionee.feedback.logic.vo.Message;
import com.gionee.feedback.utils.BitmapUtils;
import com.gionee.feedback.utils.Log;
import com.gionee.feedback.utils.SystemPropertiesUtil;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import amigoui.changecolors.ColorConfigConstants;

public class HttpUtils {
    private static final boolean DEBUG = true;
    private static final String TAG = "HttpUtils";

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (networkInfo == null || !networkInfo.isConnected()) ? false : true;
    }

    public static String getServerHost() {
        if (EnvConfig.isTestEnv()) {
            return NetConfig.TEST_HOST;
        }
        return NetConfig.NORMAL_HOST;
    }

    public static String login(Context context, IAppData appData) throws FeedBackException {
        List<NameValuePair> pairs = new ArrayList<>();
        assembleNameValuePair("source", VersionType.FORCED_VERSION, pairs);
        assembleNameValuePair("appId", appData.getAppKey(), pairs);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ColorConfigConstants.ID, "123123123");
            jsonObject.put("t", VersionType.FORCED_VERSION);
            jsonObject.put("p", "13512459874");
            jsonObject.put("nk", "nickname");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "jsonObject = " + jsonObject.toString());
        String uri = getServerHost() + NetConfig.LOGIN_URI;
        Log.d(TAG, "login uri = " + uri);
        return HttpHelper.executeHttpPost(context, uri, pairs, false, "UTF-8", appData);
    }

    public static String sendMessage(Message message, Context context, IAppData appData) throws FeedBackException {
        Log.d(TAG, "sendMessage model = " + SystemPropertiesUtil.getModel() + "  message = " + message.getMessage() + "\nromVer = " + SystemPropertiesUtil.getRomVersion() + "\nappVer = " + SystemPropertiesUtil.getAppVersion(context) + "\ncontact = " + message.getContact() + "\nimei = " + appData.getImei() + "\nosVer = " + SystemPropertiesUtil.getOsVersion() + "\nnet = " + SystemPropertiesUtil.getNetType(context) + "\next = " + message.getEntity() + "\nattachs = " + message.getAttachs());
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        try {
            assembleMultiBody(MIME.CONTENT_TYPE, "multipart/form-data", entity);
            assembleMultiBody("model", SystemPropertiesUtil.getModel(), entity);
            assembleMultiBody("message", message.getMessage(), entity);
            assembleMultiBody("romVer", SystemPropertiesUtil.getRomVersion(), entity);
            assembleMultiBody("appVer", SystemPropertiesUtil.getAppVersion(context), entity);
            assembleMultiBody("contact", message.getContact(), entity);
            assembleMultiBody("imei", appData.getImei(), entity);
            assembleMultiBody("osVer", SystemPropertiesUtil.getOsVersion(), entity);
            assembleMultiBody("ua", "1234567890", entity);
            assembleMultiBody("net", SystemPropertiesUtil.getNetType(context), entity);
            assembleMultiBody("ext", message.getEntity(), entity);
            List<String> attachs = message.getAttachs();
            if (attachs != null && attachs.size() > 0) {
                for (String attach : attachs) {
                    ByteArrayOutputStream bos = null;
                    try {
                        Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromUri(context, Uri.parse(attach));
                        bos = new ByteArrayOutputStream();
                        bitmap.compress(CompressFormat.JPEG, 70, bos);
                        assembleMultiBody(new ByteArrayBody(bos.toByteArray(), "pic.png"), entity);
                        if (bos != null) {
                            try {
                                bos.flush();
                                bos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (OutOfMemoryError e2) {
                        e2.printStackTrace();
                    } catch (Throwable th) {
                        if (bos != null) {
                            try {
                                bos.flush();
                                bos.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e4) {
            e4.printStackTrace();
            Log.d(TAG, "sendMessage UnsupportedEncodingException");
        }
        Log.d(TAG, "xxxxxx");
        return HttpHelper.invokeByPost(context, getServerHost() + NetConfig.SEND_URI, entity, DEBUG, "UTF-8", appData);
    }

    public static String queryUnread(Context context, String packageName, String imei, IAppData appData) throws FeedBackException {
        Log.d(TAG, "queryUnread imei = " + imei + "  package = " + packageName);
        List<NameValuePair> pairs = new ArrayList<>();
        assembleNameValuePair(MIME.CONTENT_TYPE, "multipart/form-data", pairs);
        assembleNameValuePair("imei", imei, pairs);//change
        assembleNameValuePair("package", packageName, pairs);
        assembleNameValuePair("autoCommit", "true", pairs);
        return HttpHelper.executeHttpPost(context, getServerHost() + NetConfig.QUERY_UNREAD_URI, pairs, DEBUG, "UTF-8", appData);
    }

    private static void assembleNameValuePair(String key, String value, List<NameValuePair> pairs) {
        if (value != null) {
            pairs.add(new BasicNameValuePair(key, value));
        }
    }

    private static void assembleMultiBody(ContentBody body, MultipartEntity entity) {
        if (body != null) {
            entity.addPart(body.getFilename(), body);
        }
    }

    private static void assembleMultiBody(String key, String value, MultipartEntity entity) throws UnsupportedEncodingException {
        if (value != null) {
            entity.addPart(key, new StringBody(value, Charset.forName("UTF-8")));
        }
    }
}
