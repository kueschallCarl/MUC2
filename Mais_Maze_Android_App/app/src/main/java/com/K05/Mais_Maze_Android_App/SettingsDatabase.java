package com.K05.Mais_Maze_Android_App;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SettingsDatabase class for managing the settings data in a SQLite database.
 * This class provides functionality to create, update, and retrieve settings data.
 */
public class SettingsDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "settings_database";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "settings";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_STEERING_METHOD = "steering_method";
    public static final String COLUMN_BROKER_IP = "broker_ip";
    public static final String COLUMN_LABYRINTH_SIZE = "labyrinth_size";
    public static final String COLUMN_PLAYER_NAME = "name";
    public static final String COLUMN_AUDIO = "audio";

    private static SettingsDatabase instance = null;
    private Context mContext;

    /**
     * Constructs a new SettingsDatabase instance with the specified context.
     * @param context the context to be used for accessing the database
     */
    public SettingsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    /**
     * This method returns the singleton instance of SettingsDatabase.
     * @param context the context to be used for accessing the database
     * @return the singleton instance of SettingsDatabase
     */
    public static SettingsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsDatabase(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PLAYER_NAME + " TEXT, " +
                COLUMN_STEERING_METHOD + " TEXT, " +
                COLUMN_BROKER_IP + " TEXT, " +
                COLUMN_AUDIO + " TEXT, " +
                COLUMN_LABYRINTH_SIZE + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }

    /**
     * This method saves a setting in the database.
     * @param setting the value of the setting to be saved
     * @param column  the name of the column to which the setting should be saved
     */
    public void saveSetting(String setting, String column) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(column, setting);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    /**
     * This method updates the last saved setting in the database.
     * If there are no existing settings, a new setting is saved.
     * @param setting the value of the setting to be updated or saved
     * @param column  the name of the column to which the setting should be updated or saved
     */
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

    /**
     * This method retrieves the last saved setting from the database for the specified column.
     * @param columnName the name of the column from which to retrieve the setting
     * @return the value of the last saved setting for the specified column, or null if not found
     */
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

    /**
     * This method returns the context associated with this SettingsDatabase.
     * @return the context associated with this SettingsDatabase
     */
    public Context getContext() {
        return mContext;
    }
}
