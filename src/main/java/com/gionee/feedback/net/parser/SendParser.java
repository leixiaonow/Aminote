package com.gionee.feedback.net.parser;

import com.gionee.feedback.exception.FeedBackParserException;

public class SendParser implements INetParser<String, Long> {
    public Long parser(String str) throws FeedBackParserException {
        try {
            return Long.valueOf(Long.parseLong(str));
        } catch (NumberFormatException e) {
            throw new FeedBackParserException(str);
        }
    }
}
