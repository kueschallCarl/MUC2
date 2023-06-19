package com.K05.Mais_Maze_Android_App;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
/**
 * This class provides unit-tests for the LeaderboardDatabase class
 */
@RunWith(MockitoJUnitRunner.class)
public class LeaderboardDatabaseTest {
    @Mock
    private Context mockContext;
    @Mock
    private SQLiteDatabase mockDatabase;
    @Mock
    private Cursor mockCursor;

    private LeaderboardDatabase leaderboardDatabase;
    /**
     Set up method that initializes the test environment for LeaderboardDatabase testing.
     It creates a spy object of LeaderboardDatabase and sets up the mock behavior for the getWritableDatabase()
     and getReadableDatabase() methods.
     */
    @Before
    public void setUp() {
        leaderboardDatabase = Mockito.spy(new LeaderboardDatabase(mockContext));
        when(leaderboardDatabase.getWritableDatabase()).thenReturn(mockDatabase);
        when(leaderboardDatabase.getReadableDatabase()).thenReturn(mockDatabase);
    }
    /**

     Test case for the saveValue() method in LeaderboardDatabase.

     It verifies that the given value is saved in the specified column of the database.

     It also checks that the database is closed after the operation.
     */
    @Test
    public void testSaveValue() {
        String value = "Test Value";
        String column = "score";

        leaderboardDatabase.saveValue(value, column);

        verify(mockDatabase).insert(eq(LeaderboardDatabase.TABLE_NAME), isNull(), any(ContentValues.class));
        verify(mockDatabase).close();
    }
    /**

     Test case for the updateLastValue() method in LeaderboardDatabase when there are existing rows.

     It verifies that the last value in the specified column is updated in the database.

     It checks that the database is closed after the operation.
     */
    @Test
    public void testUpdateLastValue_WithExistingRows() {
        String value = "Test Value";
        String column = "score";
        int rowCount = 2;

        when(mockDatabase.rawQuery(anyString(), isNull())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(rowCount);

        leaderboardDatabase.updateLastValue(value, column);

        verify(mockDatabase).update(
                eq(LeaderboardDatabase.TABLE_NAME),
                any(ContentValues.class),
                eq(LeaderboardDatabase.COLUMN_ID + " = (SELECT MAX(" + LeaderboardDatabase.COLUMN_ID + ") FROM " + LeaderboardDatabase.TABLE_NAME + ")"),
                isNull()
        );
        verify(mockDatabase).close();
    }
    /**

     Test case for the updateLastValue() method in LeaderboardDatabase when there are no existing rows.

     It verifies that a new value is saved in the specified column using the saveValue() method.

     It checks that the database is not updated and is closed after the operation.
     */
    @Test
    public void testUpdateLastValue_WithNoExistingRows() {
        String value = "Test Value";
        String column = "score";
        int rowCount = 0;

        when(mockDatabase.rawQuery(anyString(), isNull())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(rowCount);

        leaderboardDatabase.updateLastValue(value, column);

        verify(leaderboardDatabase).saveValue(value, column);
        verify(mockDatabase, never()).update(anyString(), any(ContentValues.class), anyString(), isNull());
        verify(mockDatabase).close();
    }
    /**

     Test case for the getValue() method in LeaderboardDatabase.

     It verifies that the value of the specified column is retrieved from the database.

     It checks that the cursor and database are closed after the operation.
     */
    @Test
    public void testGetValue() {
        String columnName = "score";
        String expectedValue = "Test Value";

        when(mockDatabase.rawQuery(anyString(), isNull())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getColumnIndex(columnName)).thenReturn(1);
        when(mockCursor.getString(1)).thenReturn(expectedValue);

        String result = leaderboardDatabase.getValue(columnName);

        assertEquals(expectedValue, result);
        verify(mockCursor).close();
        verify(mockDatabase).close();
    }
    /**

     Test case for the getEntriesSortedByScore() method in LeaderboardDatabase.
     It verifies that a list of LeaderboardEntry objects is retrieved from the database, sorted by score.
     It checks that the cursor and database are closed after the operation.
     */
    @Test
    public void testGetEntriesSortedByScore() {
        String playerName = "John";
        String time = "12:34";
        String score = "5";
        String maisCount = "100";
        List<LeaderboardEntry> expectedEntries = new ArrayList<>();
        expectedEntries.add(new LeaderboardEntry(playerName, time, score, maisCount));

        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getColumnIndex(LeaderboardDatabase.COLUMN_PLAYER_NAME)).thenReturn(0);
        when(mockCursor.getColumnIndex(LeaderboardDatabase.COLUMN_TIME)).thenReturn(1);
        when(mockCursor.getColumnIndex(LeaderboardDatabase.COLUMN_SCORE)).thenReturn(2);
        when(mockCursor.getColumnIndex(LeaderboardDatabase.COLUMN_MAIS_COUNT)).thenReturn(3);
        when(mockCursor.getString(0)).thenReturn(playerName);
        when(mockCursor.getString(1)).thenReturn(time);
        when(mockCursor.getString(2)).thenReturn(score);
        when(mockCursor.getString(3)).thenReturn(maisCount);
        when(mockDatabase.rawQuery(anyString(), isNull())).thenReturn(mockCursor);

        List<LeaderboardEntry> result = leaderboardDatabase.getEntriesSortedByScore();

        assertEquals(expectedEntries, result);
        verify(mockCursor).close();
        verify(mockDatabase).close();
    }
}
