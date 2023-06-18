package com.K05.Mais_Maze_Android_App;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ApplicationProvider;

import com.example.Mais_Maze_Android_App.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

public class SecondFragmentTest {
    @Mock
    private GameLogic gameLogic;
    @Mock
    private SettingsDatabase settingsDatabase;
    @Mock
    private SoundPlayer soundPlayer1;
    @Mock
    private SoundPlayer soundPlayer2;
    @Mock
    private LeaderboardDatabase leaderboardDatabase;

    private SecondFragment fragment;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        fragment = new SecondFragment();
        fragment.gameLogic = gameLogic;
        fragment.settingsDatabase = settingsDatabase;
        fragment.soundPlayer1 = soundPlayer1;
        fragment.requireContext = () -> context;
        fragment.requireActivity = () -> Mockito.mock(Fragment.class);
    }

    @After
    public void tearDown() {
        fragment = null;
    }

    @Test
    public void testStartGameLoop() throws InterruptedException {
        when(settingsDatabase.getSetting(SettingsDatabase.COLUMN_AUDIO)).thenReturn("true");

        fragment.startGameLoop("steeringMethod");

        verify(gameLogic).setGameRunning(true);
        verify(gameLogic).startSensors("steeringMethod");
        verify(settingsDatabase, times(2)).getSetting(SettingsDatabase.COLUMN_AUDIO);
        verify(soundPlayer2).playSoundEffect(any(Context.class), ArgumentMatchers.eq(R.raw.background_track));
        verify(gameLogic.mqttManager).publishToTopic("1", Constants.FINISHED_TOPIC);
        verify(gameLogic).setGameRunning(false);
        verify(soundPlayer1).playSoundEffect(any(Context.class), eq(R.raw.win_sound));
    }

    @Test
    public void testSaveScore() {
        when(settingsDatabase.getSetting("name")).thenReturn("John");
        when(gameLogic.getPlayTime()).thenReturn(60);
        when(gameLogic.getMaisCount()).thenReturn(5);

        fragment.saveScore();

        verify(leaderboardDatabase).saveValue("John", "name");
        verify(leaderboardDatabase).updateLastValue("60", "time");
        verify(leaderboardDatabase).updateLastValue("5", "mais_count");
        verify(leaderboardDatabase).updateLastValue(anyString(), eq("score"));
        verify(fragment).showAlert("YOU WIN!", "Time: 60 | Mais collected: 5 | Score: <score_value>");
    }
}
