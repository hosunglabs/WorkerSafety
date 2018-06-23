package kr.maden.watson_iot.utils.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import kr.maden.watson_iot.utils.sql.model.ChatListModel;

public class ChatListHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ChatListModel.ChatListEntry.TABLE_NAME + " (" +
                    ChatListModel.ChatListEntry._ID + " INTEGER PRIMARY KEY," +
                    ChatListModel.ChatListEntry.COLUMN_NAME_FROM + TEXT_TYPE + COMMA_SEP +
                    ChatListModel.ChatListEntry.COLUMN_NAME_TO + TEXT_TYPE + COMMA_SEP +
                    ChatListModel.ChatListEntry.COLUMN_NAME_CREATED + " INTEGER" + COMMA_SEP +
                    ChatListModel.ChatListEntry.COLUMN_NAME_MSG + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ChatListModel.ChatListEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ChatList.db";

    public ChatListHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
