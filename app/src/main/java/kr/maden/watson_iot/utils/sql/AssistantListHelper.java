package kr.maden.watson_iot.utils.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import kr.maden.watson_iot.utils.sql.model.AssistantListModel;

public class AssistantListHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + AssistantListModel.AssistantListEntry.TABLE_NAME + " (" +
                    AssistantListModel.AssistantListEntry._ID + " INTEGER PRIMARY KEY," +
                    AssistantListModel.AssistantListEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    AssistantListModel.AssistantListEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    AssistantListModel.AssistantListEntry.COLUMN_NAME_SHORT_MSG + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AssistantListModel.AssistantListEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AssistantList.db";

    public AssistantListHelper(Context context) {
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
