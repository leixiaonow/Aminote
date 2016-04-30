package com.gionee.feedback.logic.vo;

import com.gionee.feedback.net.IAppData;

public class AppData implements IAppData {
    private String mAppKey;
    private String mImei;

    public String getAppKey() {
        return this.mAppKey;
    }

    public String getImei() {
        return this.mImei;
    }

    public void setAppKey(String appKey) {
        this.mAppKey = appKey;
    }

    public void setImei(String imei) {
        this.mImei = imei;
    }
}
