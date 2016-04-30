package com.gionee.feedback.net.parser;

import com.gionee.feedback.exception.FeedBackParserException;
import com.gionee.feedback.logic.vo.ErrorInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class ErrorParser implements INetParser<String, ErrorInfo> {
    public ErrorInfo parser(String str) throws FeedBackParserException {
        ErrorInfo errorInfo = new ErrorInfo();
        try {
            JSONObject jsonObject = new JSONObject(str);
            errorInfo.setErrorCode(jsonObject.optInt("code"));
            errorInfo.setErrorMsg(jsonObject.optString("msg"));
            return errorInfo;
        } catch (JSONException e) {
            throw new FeedBackParserException(str);
        }
    }
}
