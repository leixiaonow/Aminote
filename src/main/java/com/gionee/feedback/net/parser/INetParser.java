package com.gionee.feedback.net.parser;

import com.gionee.feedback.exception.FeedBackParserException;

public interface INetParser<K, T> {
    T parser(K k) throws FeedBackParserException;
}
