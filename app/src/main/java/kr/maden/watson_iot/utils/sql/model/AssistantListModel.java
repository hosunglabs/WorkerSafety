package kr.maden.watson_iot.utils.sql.model;

import android.provider.BaseColumns;

public class AssistantListModel {

    public static class AssistantListEntry implements BaseColumns {
        public static final String TABLE_NAME = "assistant";
        public static final String COLUMN_NAME_TYPE= "type";
        public static final String COLUMN_NAME_DESCRIPTION = "desc";
        public static final String COLUMN_NAME_SHORT_MSG = "msg";
    }
}
