package com.gionee.feedback.net.parser;

import com.gionee.feedback.exception.FeedBackParserException;
import com.gionee.feedback.logic.vo.CertificationInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class CertificationParser implements INetParser<String, CertificationInfo> {
    public CertificationInfo parser(String str) throws FeedBackParserException {
        CertificationInfo info = new CertificationInfo();
        try {
            JSONObject jsonObject = new JSONObject(str);
            info.setAccessToken(jsonObject.optString("at"));
            info.setEffectiveTime(jsonObject.optInt("ex"));
            return info;
        } catch (JSONException e) {
            throw new FeedBackParserException(str);
        }
    }
}
