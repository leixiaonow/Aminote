package com.gionee.feedback.net.parser;

import amigoui.changecolors.ColorConfigConstants;
import android.text.TextUtils;
import com.gionee.feedback.exception.FeedBackParserException;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.logic.vo.ReplyInfo;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecordParser implements INetParser<String, List<FeedbackInfo>> {
    public List<FeedbackInfo> parser(String str) throws FeedBackParserException {
        List<FeedbackInfo> feedbackInfos = new ArrayList();
        try {
            JSONArray feedbackArray = new JSONArray(str);
            for (int i = 0; i < feedbackArray.length(); i++) {
                FeedbackInfo feedbackInfo = new FeedbackInfo();
                JSONObject feedback = feedbackArray.getJSONObject(i);
                long fid = feedback.getLong("fId");
                feedbackInfo.setContentID(fid);
                feedbackInfo.setID(-1);
                feedbackInfo.setReplyInfos(parseReplyInfos(feedback, fid));
                feedbackInfos.add(feedbackInfo);
            }
            return feedbackInfos;
        } catch (JSONException e) {
            throw new FeedBackParserException(str);
        }
    }

    private List<ReplyInfo> parseReplyInfos(JSONObject feedback, long fid) throws JSONException {
        List<ReplyInfo> replyInfos = new ArrayList();
        JSONArray replyArray = feedback.getJSONArray("r");
        for (int j = 0; j < replyArray.length(); j++) {
            JSONObject reply = replyArray.getJSONObject(j);
            ReplyInfo replyInfo = new ReplyInfo();
            replyInfo.setReplyID(reply.getLong(ColorConfigConstants.ID));
            replyInfo.setContentID(fid);
            replyInfo.setReaded(false);
            replyInfo.setReplyContent(reply.optString("c"));
            replyInfo.setReplyPerson(buildReplyPersonInfo(reply));
            replyInfo.setReplyTime(reply.optLong("ct"));
            replyInfos.add(replyInfo);
        }
        return replyInfos;
    }

    private String buildReplyPersonInfo(JSONObject reply) throws JSONException {
        StringBuilder builder = new StringBuilder();
        String person = reply.optString("rn");
        String office = reply.optString("p");
        builder.append(person);
        if (!(TextUtils.isEmpty(office) || office.equalsIgnoreCase("null"))) {
            builder.append(" (");
            builder.append(office);
            builder.append(")");
        }
        return builder.toString();
    }
}
