package com.example.menu_template;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LeaderboardDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "leaderboards_database";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "leaderboard";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_PLAYER_NAME = "name";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_MAIS_COUNT = "mais_count";

    private static LeaderboardDatabase instance = null;
    private Context mContext;

    private LeaderboardDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static LeaderboardDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new LeaderboardDatabase(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PLAYER_NAME + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_SCORE + " TEXT, " +
                COLUMN_MAIS_COUNT + " TEXT)";
        db.execSQL(createTableQuery);
    }





    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }

    public void saveSetting(String setting, String column) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(column, setting);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateLastSetting(String setting, String column) {
        SQLiteDatabase db = getWritableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int rowCount = cursor.getInt(0);
        cursor.close();

        if (rowCount > 0) {
            ContentValues values = new ContentValues();
            values.put(column, setting);
            String whereClause = COLUMN_ID + " = (SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_NAME + ")";
            db.update(TABLE_NAME, values, whereClause, null);
        } else {
            saveSetting(setting, column);
        }

        db.close();
    }


    public String getSetting(String columnName) {
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        String setting = null;

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                setting = cursor.getString(columnIndex);
            } else {
                Log.d("ColumnIndex", "Column index not found");
            }
        }
        cursor.close();
        db.close();
        return setting;
    }


    public Context getContext() {
        return mContext;
    }
}

