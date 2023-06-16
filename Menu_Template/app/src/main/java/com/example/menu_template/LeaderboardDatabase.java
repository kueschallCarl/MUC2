package com.example.menu_template;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
                COLUMN_SCORE + " FLOAT, " +
                COLUMN_MAIS_COUNT + " TEXT)";
        db.execSQL(createTableQuery);
    }





    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }

    public void saveValue(String value, String column) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        if (column.equals("score")) {
            float floatValue = Float.parseFloat(value);
            values.put(column, floatValue);
        } else {
            values.put(column, value);
        }
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateLastValue(String value, String column) {
        SQLiteDatabase db = getWritableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int rowCount = cursor.getInt(0);
        cursor.close();

        if (rowCount > 0) {
            ContentValues values = new ContentValues();
            if (column.equals("score")) {
                float floatValue = Float.parseFloat(value);
                values.put(column, floatValue);
            } else {
                values.put(column, value);
            }
            String whereClause = COLUMN_ID + " = (SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_NAME + ")";
            db.update(TABLE_NAME, values, whereClause, null);
        } else {
            saveValue(value, column);
        }

        db.close();
    }


    public String getValue(String columnName) {
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

    public List<LeaderboardEntry> getEntriesSortedByScore() {
        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_SCORE + " DESC";
        Cursor cursor = db.rawQuery(selectQuery, null);

        int playerNameIndex = cursor.getColumnIndex(COLUMN_PLAYER_NAME);
        int timeIndex = cursor.getColumnIndex(COLUMN_TIME);
        int scoreIndex = cursor.getColumnIndex(COLUMN_SCORE);
        int maisCountIndex = cursor.getColumnIndex(COLUMN_MAIS_COUNT);


        if (scoreIndex != -1) {
            if (cursor.moveToFirst()) {
                do {
                    String playerName = cursor.getString(playerNameIndex);
                    String time = cursor.getString(timeIndex);
                    String score = cursor.getString(scoreIndex);
                    String maisCount = cursor.getString(maisCountIndex);

                    LeaderboardEntry entry = new LeaderboardEntry(playerName, time, score, maisCount);
                    leaderboardEntries.add(entry);
                } while (cursor.moveToNext());
            }
        } else {
            Log.d("Error", "Column index not found for COLUMN_SCORE");
        }

        cursor.close();
        db.close();

        return leaderboardEntries;
    }



    public Context getContext() {
        return mContext;
    }



}

