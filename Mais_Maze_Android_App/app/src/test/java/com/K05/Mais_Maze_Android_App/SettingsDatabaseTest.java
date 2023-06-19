package com.K05.Mais_Maze_Android_App;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 * This class provides unit-tests for the SettingsDatabase class
 */
public class SettingsDatabaseTest {
    private SettingsDatabase settingsDatabase;
    private SQLiteDatabase mockDatabase;
    /**
     * Set up method to initialize the test environment before each test case.
     */
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        settingsDatabase = new SettingsDatabase(context) {
            @Override
            public SQLiteDatabase getWritableDatabase() {
                return mockDatabase;
            }

            @Override
            public SQLiteDatabase getReadableDatabase() {
                return mockDatabase;
            }
        };
        mockDatabase = mock(SQLiteDatabase.class);
        settingsDatabase.onCreate(mockDatabase);
    }
    /**
     * Tear down method to clean up the test environment after each test case.
     */
    @After
    public void tearDown() {
        settingsDatabase.onUpgrade(mockDatabase, 1, 1);
        settingsDatabase = null;
        mockDatabase = null;
    }
    /**
     * Test case for the `saveSetting` method in `SettingsDatabase`.
     */
    @Test
    public void testSaveSetting() {
        ContentValues expectedValues = new ContentValues();
        expectedValues.put("steering_method", "method");

        settingsDatabase.saveSetting("method", "steering_method");

        verify(mockDatabase).insert(eq("settings"), eq(null), eq(expectedValues));
        verify(mockDatabase).close();
    }
    /**
     * Test case for the `updateLastSetting` method in `SettingsDatabase` when existing settings are present.
     */
    @Test
    public void testUpdateLastSetting_existingSettings() {
        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(1);
        when(mockDatabase.rawQuery(anyString(), any())).thenReturn(mockCursor);

        ContentValues expectedValues = new ContentValues();
        expectedValues.put("steering_method", "method");

        settingsDatabase.updateLastSetting("method", "steering_method");

        verify(mockCursor).close();
        verify(mockDatabase).update(eq("settings"), eq(expectedValues), anyString(), eq(null));
        verify(mockDatabase).close();
    }
    /**
     * Test case for the `updateLastSetting` method in `SettingsDatabase` when no existing settings are present.
     */
    @Test
    public void testUpdateLastSetting_noExistingSettings() {
        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(0);
        when(mockDatabase.rawQuery(anyString(), any())).thenReturn(mockCursor);

        ContentValues expectedValues = new ContentValues();
        expectedValues.put("steering_method", "method");

        settingsDatabase.updateLastSetting("method", "steering_method");

        verify(mockCursor).close();
        verify(mockDatabase).insert(eq("settings"), eq(null), eq(expectedValues));
        verify(mockDatabase).close();
    }
    /**
     * Test case for the `getSetting` method in `SettingsDatabase` when the column exists.
     */
    @Test
    public void testGetSetting_existingColumn() {
        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getColumnIndex("steering_method")).thenReturn(1);
        when(mockCursor.getString(1)).thenReturn("method");
        when(mockDatabase.rawQuery(anyString(), any())).thenReturn(mockCursor);

        String setting = settingsDatabase.getSetting("steering_method");

        assertEquals("method", setting);
        verify(mockCursor).close();
        verify(mockDatabase).close();
    }
    /**
     * Test case for the `getSetting` method in `SettingsDatabase` when the column does not exist.
     */
    @Test
    public void testGetSetting_nonExistingColumn() {
        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getColumnIndex("non_existing")).thenReturn(-1);
        when(mockDatabase.rawQuery(anyString(), any())).thenReturn(mockCursor);

        String setting = settingsDatabase.getSetting("non_existing");

        assertNull(setting);
        verify(mockCursor).close();
        verify(mockDatabase).close();
    }
}
