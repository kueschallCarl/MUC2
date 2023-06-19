package com.K05.Mais_Maze_Android_App;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.AlertDialog;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.Mais_Maze_Android_App.R;
import com.example.Mais_Maze_Android_App.databinding.FragmentFirstBinding;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
/**
 * This class provides unit-tests for the FirstFragment Fragment
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MqttManager.class })
public class FirstFragmentTest {

    @Mock
    private FragmentFirstBinding mockBinding;

    @Mock
    private MqttManager mockMqttManager;

    @Mock
    private SettingsDatabase mockSettingsDatabase;

    @Mock
    private LeaderboardDatabase mockLeaderboardDatabase;

    @Mock
    private EditText mockNameTextField;

    @Mock
    private View mockView;

    @Mock
    private AlertDialog.Builder mockBuilder;

    @Mock
    private AlertDialog mockAlertDialog;

    private FirstFragment firstFragment;
    /**

     Set up method that initializes the necessary dependencies and mocks for the tests.
     */
    @Before
    public void setUp() {
        firstFragment = new FirstFragment();
        firstFragment.binding = mockBinding;
        firstFragment.mqttManager = mockMqttManager;
        firstFragment.settingsDatabase = mockSettingsDatabase;
        firstFragment.leaderboardDatabase = mockLeaderboardDatabase;
    }
    /**

     Test case for the onCreate method of the FirstFragment class.

     @throws Exception if an exception occurs during the test
     */
    @Test
    public void testOnCreateView() throws Exception {
        when(mockBinding.getRoot()).thenReturn((ConstraintLayout) mockView);

        View result = firstFragment.onCreateView(null, null, null);

        assertEquals(mockView, result);
        PowerMockito.whenNew(MqttManager.class).withArguments(eq("first_fragment")).thenReturn(mockMqttManager);
        FragmentFirstBinding.inflate(any(), any(), anyBoolean());
        LeaderboardDatabase.getInstance(any(Context.class));
    }
    /**

     Test case for the onViewCreated method of the FirstFragment class.
     */
    @Test
    public void testOnViewCreated() {
        when(mockBinding.nameTextField).thenReturn(mockNameTextField);
        when(mockBinding.nameTextField).thenReturn(Mockito.mock(EditText.class));
        when(mockSettingsDatabase.getSetting(SettingsDatabase.COLUMN_PLAYER_NAME)).thenReturn("Test Name");

        firstFragment.onViewCreated(mockView, null);

        SettingsDatabase.getInstance(any(Context.class));
        verify(mockNameTextField).setText("Test Name");
        verify(mockBinding.buttonFirst).setOnClickListener(any(View.OnClickListener.class));
        verify(mockSettingsDatabase).updateLastSetting(eq("Test Name"), eq(SettingsDatabase.COLUMN_PLAYER_NAME));
        verify(mockView).findViewById(R.id.recyclerView);
    }
    /**

     Test case for the onOptionsItemSelected method of the FirstFragment class.

     The action is Settings, which should navigate to the SettingsFragment.
     */
    @Test
    public void testOnOptionsItemSelected_SettingsActionNavigatesToSettingsFragment() {
        when(mockLeaderboardDatabase.getEntriesSortedByScore()).thenReturn(new ArrayList<>());
        NavController mockNavController = Mockito.mock(NavController.class);
        NavHostFragment mockNavHostFragment = Mockito.mock(NavHostFragment.class);
        when(NavHostFragment.findNavController(firstFragment)).thenReturn(mockNavController);
        when(NavHostFragment.findNavController(any())).thenReturn(mockNavController);
        when(mockBinding.getRoot().getContext()).thenReturn(mock(Context.class));

        firstFragment.onViewCreated(mockView, null);
        firstFragment.onOptionsItemSelected(Mockito.mock(MenuItem.class));

        verify(mockNavController).navigate(eq(R.id.action_FirstFragment_to_SettingsFragment), any(), any(NavOptions.class));
        verify(mockNavController, never()).navigate(eq(R.id.action_FirstFragment_to_SecondFragment), any());
    }
    /**

     Test case for the onOptionsItemSelected method of the FirstFragment class.
     The action is not Settings, so no navigation should occur.
     */
    @Test
    public void testOnOptionsItemSelected_OtherActionDoesNotNavigate() {
        NavController mockNavController = Mockito.mock(NavController.class);
        NavHostFragment mockNavHostFragment = Mockito.mock(NavHostFragment.class);
        when(NavHostFragment.findNavController(firstFragment)).thenReturn(mockNavController);
        when(NavHostFragment.findNavController(any())).thenReturn(mockNavController);
        when(mockBinding.getRoot().getContext()).thenReturn(mock(Context.class));

        firstFragment.onViewCreated(mockView, null);
        firstFragment.onOptionsItemSelected(Mockito.mock(MenuItem.class));

        verify(mockNavController, never()).navigate(anyInt(), any(), any());
    }
}
