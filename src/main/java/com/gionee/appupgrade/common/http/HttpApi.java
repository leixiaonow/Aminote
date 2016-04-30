package com.gionee.appupgrade.common.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public interface HttpApi {
    HttpGet createHttpGet(String str, NameValuePair... nameValuePairArr);

    HttpPost createHttpPost(String str, NameValuePair... nameValuePairArr);

    HttpURLConnection createHttpURLConnectionPost(URL url, String str) throws IOException;
}
