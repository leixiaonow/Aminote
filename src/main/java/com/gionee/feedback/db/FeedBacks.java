package com.gionee.feedback.db;

import android.provider.BaseColumns;

public class FeedBacks {
    protected static final String APP_DATA_TABLE = "appdata";
    protected static final String DRAFT_TABLE = "draft_save";
    protected static final String MESSAGE_TABLE = "message";
    protected static final String REPLY_TABLE = "reply";
    protected static final String TOKEN_TABLE = "token";

    protected static final class AppDataImpl implements BaseColumns {
        public static final String APP_KEY = "app_key";
        public static final String IMEI = "imei";

        protected AppDataImpl() {
        }
    }

    protected static final class DraftImpl implements BaseColumns {
        public static final String ATTACH = "attach";
        public static final String CONTENT = "content";
        public static final String USER_CONTENT = "user_content";

        protected DraftImpl() {
        }
    }

    protected static final class MessageImpl implements BaseColumns {
        public static final String ATTACHS = "attachs";
        public static final String CONTENT = "content";
        public static final String CONTENT_ID = "content_id";
        public static final String SEND_TIME = "send_time";
        public static final String USER_CONTACT = "user_contact";

        protected MessageImpl() {
        }
    }

    protected static final class ReplyImpl implements BaseColumns {
        public static final String CONTENT_ID = "content_id";
        public static final String IS_READ = "is_read";
        public static final String REPLY_CONTENT = "reply_content";
        public static final String REPLY_ID = "reply_id";
        public static final String REPLY_PERSON = "reply_person";
        public static final String REPLY_TIME = "reply_time";

        protected ReplyImpl() {
        }
    }

    protected static final class TokenImpl implements BaseColumns {
        public static final String TOKEN = "token";

        protected TokenImpl() {
        }
    }

    FeedBacks() {
        throw new RuntimeException("Stub!");
    }
}
