package kr.maden.watson_iot.utils.sql.model;

import android.provider.BaseColumns;

public class ChatListModel {

    public static class ChatListEntry implements BaseColumns {
        public static final String TABLE_NAME = "chatlist";
        public static final String COLUMN_NAME_TO= "tous";
        public static final String COLUMN_NAME_FROM = "fromus";
        public static final String COLUMN_NAME_MSG = "msg";
        public static final String COLUMN_NAME_CREATED = "created_at";


    }
}
