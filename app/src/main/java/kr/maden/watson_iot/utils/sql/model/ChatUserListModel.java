package kr.maden.watson_iot.utils.sql.model;

import android.provider.BaseColumns;

public class ChatUserListModel {

    public static class ChatListEntry implements BaseColumns {
        public static final String TABLE_NAME = "chatuser";
        public static final String COLUMN_NAME_WORKER_ID = "workerid";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_DATE = "latestdate";
        public static final String COLUMN_NAME_MSG = "msg";
    }
}
