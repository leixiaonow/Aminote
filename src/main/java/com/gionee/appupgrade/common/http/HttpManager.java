package com.gionee.appupgrade.common.http;

import android.content.Context;
import com.gionee.appupgrade.common.utils.Config;
import com.gionee.appupgrade.common.utils.Constants;
import com.gionee.appupgrade.common.utils.LogUtils;
import com.gionee.appupgrade.common.utils.NetworkUtils;
import com.gionee.appupgrade.common.utils.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;

public class HttpManager implements HttpApi {
    private static final String TAG = "HttpManager";
    private int mTryTimes = 0;

    public HttpGet createHttpGet(String url, NameValuePair... nameValuePairs) {
        return null;
    }

    public HttpPost createHttpPost(String url, NameValuePair... nameValuePairs) {
        return null;
    }

    public HttpURLConnection createHttpURLConnectionPost(URL url, String boundary) throws IOException {
        return null;
    }

    public String executeHttpRequest(String checkurl, Context context) {
        LogUtils.log(TAG, LogUtils.getThreadName() + " checkurl = " + checkurl);
        try {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            BasicHttpParams localBasicHttpParams = new BasicHttpParams();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(localBasicHttpParams, schemeRegistry), localBasicHttpParams);
            if (NetworkUtils.isWapConnection(context)) {
                httpClient.getParams().setParameter("http.route.default-proxy", new HttpHost(Constants.CONNECTION_MOBILE_DEFAULT_HOST, Constants.CONNECTION_MOBILE_DEFAULT_PORT));
            }
            httpClient.getParams().setIntParameter("http.connection.timeout", 10000);
            httpClient.getParams().setIntParameter("http.socket.timeout", Config.NETWORK_SOCKET_TIMEOUT);
            httpClient.getParams().setParameter("http.useragent", Utils.getUaString(Utils.getImei(context)));
            HttpResponse response = httpClient.execute(new HttpGet(checkurl));
            if (response.getStatusLine().getStatusCode() == 200) {
                LogUtils.log(TAG, LogUtils.getThreadName() + "HTTP Code: 200");
                String result = EntityUtils.toString(response.getEntity());
                LogUtils.log(TAG, LogUtils.getThreadName() + "result = " + result);
                if (result.indexOf("<?xml version=\"1.0\"?>") == -1 || result.indexOf("<go href=") == -1) {
                    LogUtils.log(TAG, LogUtils.getThreadName() + "result" + response.getStatusLine().getStatusCode());
                    return result;
                }
                String nurl = Utils.getUrlStringByXML(result);
                LogUtils.log(TAG, LogUtils.getThreadName() + "HTTP Code: 200 ???");
                this.mTryTimes++;
                if (this.mTryTimes <= 3) {
                    return executeHttpRequest(nurl, context);
                }
                return null;
            }
        } catch (Exception e) {
            LogUtils.log(TAG, LogUtils.getThreadName() + "Exception " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public static void sendDownloadStartRequest(Context context, String downloadUrl, boolean isStart) {
        StringBuffer urlString = new StringBuffer();
        String str = downloadUrl;
        String packageName = str.substring(downloadUrl.lastIndexOf("/") + 1);
        if (Config.isTestMode(context)) {
            urlString.append(Config.TEST_HOST);
        } else {
            urlString.append(Config.NORMARL_HOST);
        }
        urlString.append("/cllt/upg/");
        if (isStart) {
            urlString.append(12100);
        } else {
            urlString.append(11100);
        }
        urlString.append("/" + packageName);
        urlString.append("&imei=" + Utils.getDecodeImei(Utils.getImei(context)));
        LogUtils.log(TAG, "sendDownloadStartRequest() urlString = " + urlString);
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlString.toString());
            if (NetworkUtils.isWapConnection(context)) {
                httpURLConnection = (HttpURLConnection) url.openConnection(new Proxy(Type.HTTP, new InetSocketAddress(Constants.CONNECTION_MOBILE_DEFAULT_HOST, Constants.CONNECTION_MOBILE_DEFAULT_PORT)));
            } else {
                httpURLConnection = (HttpURLConnection) url.openConnection();
            }
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("User-Agent", Utils.getUaString(Utils.getImei(context)));
            httpURLConnection.connect();
            int code = httpURLConnection.getResponseCode();
            LogUtils.log(TAG, "sendDownloadStartRequest () code = " + code);
            if (code == 200 && NetworkUtils.isWapConnection(context)) {
                int size = httpURLConnection.getContentLength();
                byte[] buffer = new byte[size];
                InputStream inStream = null;
                try {
                    inStream = httpURLConnection.getInputStream();
                    int totalSize = 0;
                    while (totalSize < size) {
                        int tsize = inStream.read(buffer, totalSize, size - totalSize);
                        if (tsize == -1) {
                            break;
                        }
                        totalSize += tsize;
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (Throwable th) {
                    if (inStream != null) {
                        inStream.close();
                    }
                }
                String result = new String(buffer, "UTF-8");
                LogUtils.log(TAG, "sendDownloadStartRequest() result = " + result);
                if (!(result.indexOf("<?xml version=\"1.0\"?>") == -1 || result.indexOf("<go href=") == -1)) {
                    sendDownloadStartRequest(context, downloadUrl, isStart);
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        } catch (Throwable th2) {
            httpURLConnection.disconnect();
        }
        httpURLConnection.disconnect();
    }

    public static void sendHttpRequest(String url, Context context) {
        try {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            BasicHttpParams localBasicHttpParams = new BasicHttpParams();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(localBasicHttpParams, schemeRegistry), localBasicHttpParams);
            if (NetworkUtils.isWapConnection(context)) {
                httpClient.getParams().setParameter("http.route.default-proxy", new HttpHost(Constants.CONNECTION_MOBILE_DEFAULT_HOST, Constants.CONNECTION_MOBILE_DEFAULT_PORT));
            }
            httpClient.getParams().setIntParameter("http.connection.timeout", 10000);
            httpClient.getParams().setIntParameter("http.socket.timeout", Config.NETWORK_SOCKET_TIMEOUT);
            httpClient.getParams().setParameter("http.useragent", Utils.getUaString(Utils.getImei(context)));
            LogUtils.log(TAG, "sendHttpRequest() result = " + httpClient.execute(new HttpGet(url)).getStatusLine().getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
