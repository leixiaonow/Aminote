package com.gionee.feedback.net;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import com.gionee.feedback.config.NetConfig;
import com.gionee.feedback.db.ITokenProvider;
import com.gionee.feedback.db.ProviderFactory;
import com.gionee.feedback.db.vo.Token;
import com.gionee.feedback.exception.FeedBackException;
import com.gionee.feedback.exception.FeedBackNetException;
import com.gionee.feedback.logic.vo.CertificationInfo;
import com.gionee.feedback.logic.vo.ErrorInfo;
import com.gionee.feedback.logic.vo.ResultCode;
import com.gionee.feedback.net.parser.CertificationParser;
import com.gionee.feedback.net.parser.ErrorParser;
import com.gionee.feedback.utils.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpHelper {
    private static final boolean DEBUG = true;
    private static final String TAG = "HttpHelper";

    @TargetApi(8)
    public static HttpClient getDefaultHttpClient() {
        HttpClient httpClient = AndroidHttpClient.newInstance(null);
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setSocketBufferSize(params, 2048);
        HttpConnectionParams.setConnectionTimeout(params, 20000);
        HttpConnectionParams.setSoTimeout(params, NetConfig.SO_TIME_OUT);
        return httpClient;
    }

    public static String executeHttpPost(Context context, String uri, List<NameValuePair> pairs, boolean gzip, String charSet, IAppData appData) throws FeedBackException {
        HttpPost request = new HttpPost(uri);
        Token token = (Token) ProviderFactory.tokenProvider(context).getToken();
        if (token != null) {
            setAuthorization(request, token.getToken());
        }
        setCharset(request, charSet);
        if (!(pairs == null || pairs.isEmpty())) {
            UrlEncodedFormEntity requestEntity = null;
            try {
                requestEntity = new UrlEncodedFormEntity(pairs, charSet);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            request.setEntity(requestEntity);
        }
        return executeHttpRequest(context, request, gzip, appData);
    }

    public static String invokeByPost(Context context, String url, HttpEntity entity, boolean gzip, String charSet, IAppData appData) throws FeedBackException {
        Log.d(TAG, "invokeByPost");
        HttpPost post = new HttpPost(url);
        Token token = (Token) ProviderFactory.tokenProvider(context).getToken();
        Log.d(TAG, "token = " + token);
        if (token != null) {
            setAuthorization(post, token.getToken());
        }
        post.setEntity(entity);
        return executeHttpRequest(context, post, gzip, appData);
    }

    public static String executeHttpGet(Context context, String uri, List<NameValuePair> pairs, boolean gzip, String charSet, IAppData appData) throws IOException, FeedBackException {
        HttpGet request = new HttpGet(uri);
        Token token = (Token) ProviderFactory.tokenProvider(context).getToken();
        if (token != null) {
            setAuthorization(request, token.getToken());
        }
        setCharset(request, charSet);
        StringBuilder sb = new StringBuilder(uri);
        if (!(pairs == null || pairs.isEmpty())) {
            sb.append("?");
            for (NameValuePair pair : pairs) {
                sb.append(pair.getName());
                sb.append("=");
                sb.append(pair.getValue());
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return executeHttpRequest(context, request, gzip, appData);
    }

    @TargetApi(8)
    private static String executeHttpRequest(Context context, HttpUriRequest request, boolean gzip, IAppData appData) throws FeedBackException {
        int status;
        ITokenProvider tokenProvider;
        Token token;
        Log.d(TAG, "executeHttpRequest");
        HttpClient client = getDefaultHttpClient();
        if (gzip) {
            request.getParams().setParameter("Accept-Encoding", "gzip, deflate");
        }
        Log.d(TAG, "request = " + request.getURI());
        HttpResponse response = null;
        String resultResponse;
        ErrorInfo errorInfo;
        CertificationInfo info;
        try {
            response = client.execute(request);
            resultResponse = parseResponse(response, "UTF-8");
            if (response != null) {
                status = response.getStatusLine().getStatusCode();
                Log.d(TAG, "final url = " + request.getURI().toASCIIString() + "  statusCode = " + status);
                if (status != 200) {
                    tokenProvider = ProviderFactory.tokenProvider(context);
                    errorInfo = new ErrorParser().parser(parseEntity(response.getEntity(), "UTF-8"));
                    if (RetryManager.getInstance().isRetry()) {
                        info = new CertificationParser().parser(HttpUtils.login(context, appData));
                        if (!TextUtils.isEmpty(info.getAccessToken())) {
                            token = new Token();
                            token.setToken(info.getAccessToken());
                            tokenProvider.update(token);
                            setAuthorization(request, info.getAccessToken());
                            resultResponse = executeHttpRequest(context, request, gzip, appData);
                        }
                    } else {
                        throw new FeedBackNetException(errorInfo.getErrorCode());
                    }
                }
            }
            if (response == null) {
                request.abort();
            }
            if (client != null && (client instanceof AndroidHttpClient)) {
                ((AndroidHttpClient) client).close();
            }
            return resultResponse;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            throw new FeedBackNetException(ResultCode.CODE_NETWORK_UNAVAILABLE.value());
        } catch (Throwable th) {
            if (response != null) {
                status = response.getStatusLine().getStatusCode();
                Log.d(TAG, "final url = " + request.getURI().toASCIIString() + "  statusCode = " + status);
                if (status != 200) {
                    tokenProvider = ProviderFactory.tokenProvider(context);
                    errorInfo = new ErrorParser().parser(parseEntity(response.getEntity(), "UTF-8"));
                    if (RetryManager.getInstance().isRetry()) {
                        info = new CertificationParser().parser(HttpUtils.login(context, appData));
                        if (!TextUtils.isEmpty(info.getAccessToken())) {
                            token = new Token();
                            token.setToken(info.getAccessToken());
                            tokenProvider.update(token);
                            setAuthorization(request, info.getAccessToken());
                            resultResponse = executeHttpRequest(context, request, gzip, appData);
                        }
                    } else {
                        FeedBackNetException feedBackNetException = new FeedBackNetException(errorInfo.getErrorCode());
                    }
                }
            }
            if (response == null) {
                request.abort();
            }
            if (client != null && (client instanceof AndroidHttpClient)) {
                ((AndroidHttpClient) client).close();
            }
        }
    }

    private static void setAuthorization(HttpUriRequest request, String token) {
        if (token != null && token.length() > 0) {
            request.setHeader("Authorization", token);
        }
    }

    private static void setCharset(HttpUriRequest request, String set) {
        request.setHeader("Accept-Charset", set);
    }

    private static String parseResponse(HttpResponse resp, String charset) throws FeedBackException {
        Log.d(TAG, "parseResponse resp is null " + (resp == null ? DEBUG : false));
        if (resp != null) {
            int status = resp.getStatusLine().getStatusCode();
            Log.d(TAG, "resp status = " + status);
            if (status == 200) {
                String result = parseEntity(resp.getEntity(), charset);
                Log.d(TAG, "result = " + result);
                return result;
            }
        }
        return "";
    }

    private static String parseEntity(HttpEntity entity, String charset) throws FeedBackException {
        Log.d(TAG, "parseEntity");
        if (entity.isStreaming()) {
            boolean gzip = false;
            Header header = entity.getContentEncoding();
            if (header != null) {
                gzip = header.getValue().toLowerCase().indexOf("gzip") != -1 ? DEBUG : false;
            }
            BufferedReader reader = null;
            StringBuffer sb = new StringBuffer();
            if (gzip) {
                try {
                    reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(entity.getContent(), 2048), charset));
                } catch (IOException e) {
                    throw new FeedBackException("INVALID_NEWWORK");
                } catch (Throwable th) {
                    consume(entity);
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e2) {
                            throw new FeedBackException("IO Close Error");
                        }
                    }
                }
            }
            reader = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            String jsonResult = sb.toString();
            Log.d(TAG, "jsonResult = " + jsonResult);
            if ("".endsWith(jsonResult)) {
                throw new FeedBackException("INVALID_RECEIVE_DATA");
            }
            consume(entity);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    throw new FeedBackException("IO Close Error");
                }
            }
            return jsonResult;
        }
        throw new FeedBackException("INVALID_RECEIVE_DATA");
    }

    private static void consume(HttpEntity entity) {
        if (entity != null && entity.isStreaming()) {
            try {
                InputStream instream = entity.getContent();
                if (instream != null) {
                    instream.close();
                }
            } catch (Throwable th) {
            }
        }
    }
}
