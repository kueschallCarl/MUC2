package com.K05.Mais_Maze_Android_App;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.Mais_Maze_Android_App.R;
import com.example.Mais_Maze_Android_App.databinding.FragmentSettingsBinding;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
/**
 * This class provides unit-tests for the SettingsFragment Fragment
 */
@RunWith(MockitoJUnitRunner.class)
public class SettingsFragmentTest {
    @Mock
    private FragmentSettingsBinding mockBinding;

    @Mock
    private SettingsDatabase mockSettingsDatabase;

    @Mock
    private View mockView;

    @Mock
    private Context mockContext;

    @Mock
    private RadioGroup mockRadioGroup;

    @Mock
    private Switch mockSwitch;

    private SettingsFragment settingsFragment;
    /**
     * Set up method to initialize test environment before each test case.
     */
    @Before
    public void setUp() {
        settingsFragment = Mockito.spy(new SettingsFragment());
        settingsFragment.binding = mockBinding;
        settingsFragment.settingsDatabase = mockSettingsDatabase;

        when(mockBinding.getRoot()).thenReturn((ConstraintLayout) mockView);
        when(settingsFragment.requireContext()).thenReturn(mockContext);
        when(mockBinding.radioBtnGroupSteeringMethod).thenReturn(mockRadioGroup);
        when(mockBinding.audioSwitch).thenReturn(mockSwitch);
    }
    /**
     * Test case for `onCreateView` method in `SettingsFragment`.
     */
    @Test
    public void testOnCreateView() {
        LayoutInflater mockInflater = mock(LayoutInflater.class);
        ViewGroup mockContainer = mock(ViewGroup.class);
        View mockRootView = mock(View.class);

        when(mockInflater.inflate(anyInt(), eq(mockContainer), eq(false))).thenReturn(mockRootView);

        View result = settingsFragment.onCreateView(mockInflater, mockContainer, null);

        assertEquals(mockRootView, result);
    }
    /**
     * Test case for `onViewCreated` method in `SettingsFragment` when valid settings are present.
     */
    @Test
    public void testOnViewCreated_withValidSettings() {
        EditText mockSizeSettingEditText = mock(EditText.class);
        EditText mockBrokerIPEditText = mock(EditText.class);

        when(mockSettingsDatabase.getSetting(eq(SettingsDatabase.COLUMN_AUDIO))).thenReturn("true");
        when(mockSettingsDatabase.getSetting(eq(SettingsDatabase.COLUMN_STEERING_METHOD))).thenReturn("ESP32");
        when(mockSettingsDatabase.getSetting(eq(SettingsDatabase.COLUMN_LABYRINTH_SIZE))).thenReturn("5");
        when(mockSettingsDatabase.getSetting(eq(SettingsDatabase.COLUMN_BROKER_IP))).thenReturn("192.168.1.1");

        when(mockBinding.sizeSetting).thenReturn(mockSizeSettingEditText);
        when(mockBinding.brokerAddressTextField).thenReturn(mockBrokerIPEditText);

        settingsFragment.onViewCreated(mockView, null);

        verify(mockSwitch).setChecked(eq(true));
        verify(mockSizeSettingEditText).setText(eq("5"));
        verify(mockBrokerIPEditText).setText(eq("192.168.1.1"));
        verify(mockRadioGroup).check(ArgumentMatchers.eq(R.id.radio_btn_esp32_steering));
    }
    /**
     * Test case for `onViewCreated` method in `SettingsFragment` when invalid settings are present.
     */
    @Test
    public void testOnViewCreated_withInvalidSettings() {
        when(mockSettingsDatabase.getSetting(anyString())).thenThrow(new RuntimeException("Database error"));

        settingsFragment.onViewCreated(mockView, null);

        verify(mockSwitch, never()).setChecked(anyBoolean());
        verify(mockRadioGroup, never()).check(anyInt());
        verify(mockBinding.sizeSetting, never()).setText(anyString());
        verify(mockBinding.brokerAddressTextField, never()).setText(anyString());

        // Verify that an alert dialog is shown
        verify(settingsFragment).showAlert(eq("AudioSwitch"), anyString());
        verify(settingsFragment).showAlert(eq("Settings"), anyString());
    }
    /**
     * Test case for `onCreateOptionsMenu` method in `SettingsFragment`.
     */
    @Test
    public void testOnCreateOptionsMenu() {
        Menu mockMenu = mock(Menu.class);
        MenuInflater mockMenuInflater = mock(MenuInflater.class);

        settingsFragment.onCreateOptionsMenu(mockMenu, mockMenuInflater);

        verify(mockMenuInflater).inflate(eq(R.menu.menu_main), eq(mockMenu));
    }

    /**
     * Test case for `onOptionsItemSelected` method in `SettingsFragment` when selecting an item other than the specified one.
     */
    @Test
    public void testOnOptionsItemSelected_withOtherItem() {
        MenuItem mockMenuItem = mock(MenuItem.class);

        when(mockMenuItem.getItemId()).thenReturn(R.id.broker_address_label);

        boolean result = settingsFragment.onOptionsItemSelected(mockMenuItem);

        assertFalse(result);
    }
    /**
     * Test case for `checkSelectedSteeringMethod` method in `SettingsFragment` when ESP32 steering method is selected.
     */
    @Test
    public void testCheckSelectedSteeringMethod_withESP32() {
        int esp32RadioButtonId = R.id.radio_btn_esp32_steering;
        when(mockRadioGroup.getCheckedRadioButtonId()).thenReturn(esp32RadioButtonId);

        settingsFragment.checkSelectedSteeringMethod(mockSettingsDatabase);

        verify(mockSettingsDatabase).updateLastSetting(eq("ESP32"), eq(SettingsDatabase.COLUMN_STEERING_METHOD));
        verify(settingsFragment).showAlert(eq("SteeringMethod"), eq("Selected steering method: ESP32"));
    }
    /**
     * Test case for `checkSelectedSteeringMethod` method in `SettingsFragment` when phone steering method is selected.
     */
    @Test
    public void testCheckSelectedSteeringMethod_withPhone() {
        int phoneRadioButtonId = R.id.radio_btn_phone_steering;
        when(mockRadioGroup.getCheckedRadioButtonId()).thenReturn(phoneRadioButtonId);

        settingsFragment.checkSelectedSteeringMethod(mockSettingsDatabase);

        verify(mockSettingsDatabase).updateLastSetting(eq("Phone"), eq(SettingsDatabase.COLUMN_STEERING_METHOD));
        verify(settingsFragment).showAlert(eq("SteeringMethod"), eq("Selected steering method: Phone"));
    }
    /**
     * Test case for `checkSelectedSteeringMethod` method in `SettingsFragment` when no steering method is selected.
     */
    @Test
    public void testCheckSelectedSteeringMethod_withNoSelection() {
        when(mockRadioGroup.getCheckedRadioButtonId()).thenReturn(-1);

        settingsFragment.checkSelectedSteeringMethod(mockSettingsDatabase);

        verify(mockSettingsDatabase, never()).updateLastSetting(anyString(), eq(SettingsDatabase.COLUMN_STEERING_METHOD));
        verify(settingsFragment).showAlert(eq("SteeringMethod"), eq("No SteeringMethod selected"));
    }
    /**
     * Test case for `checkSelectedSteeringMethod` method in `SettingsFragment` when an exception occurs while retrieving the selected steering method.
     */
    @Test
    public void testCheckSelectedSteeringMethod_withException() {
        when(mockRadioGroup.getCheckedRadioButtonId()).thenThrow(new RuntimeException("Radio button error"));

        settingsFragment.checkSelectedSteeringMethod(mockSettingsDatabase);

        verify(mockSettingsDatabase, never()).updateLastSetting(anyString(), eq(SettingsDatabase.COLUMN_STEERING_METHOD));
        verify(settingsFragment).showAlert(eq("Radio Button Issue"), eq("No Steering Method selected in Settings"));
    }
    /**
     * Test case for `getSteeringMethod` method in `SettingsFragment`.
     */
    @Test
    public void testGetSteeringMethod() {
        when(mockSettingsDatabase.getSetting(eq(SettingsDatabase.COLUMN_STEERING_METHOD))).thenReturn("ESP32");

        String result = settingsFragment.getSteeringMethod(mockSettingsDatabase);

        assertEquals("ESP32", result);
        verify(mockSettingsDatabase).getSetting(eq(SettingsDatabase.COLUMN_STEERING_METHOD));
    }
    /**
     * Test case for `getSteeringMethod` method in `SettingsFragment` when an exception occurs while retrieving the steering method.
     */
    @Test
    public void testGetSteeringMethod_withException() {
        when(mockSettingsDatabase.getSetting(anyString())).thenThrow(new RuntimeException("Database error"));

        String result = settingsFragment.getSteeringMethod(mockSettingsDatabase);

        assertNull(result);
        verify(mockSettingsDatabase).getSetting(eq(SettingsDatabase.COLUMN_STEERING_METHOD));
    }
    /**
     * Test case for `showAlert` method in `SettingsFragment`.
     */
    @Test
    public void testShowAlert() {
        Context mockContext = mock(Context.class);
        AlertDialog.Builder mockBuilder = mock(AlertDialog.Builder.class);
        AlertDialog mockAlertDialog = mock(AlertDialog.class);

        when(settingsFragment.getContext()).thenReturn(mockContext);
        when(mockBuilder.setTitle(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setMessage(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setPositiveButton(eq("OK"), isNull())).thenReturn(mockBuilder);
        when(mockBuilder.show()).thenReturn(mockAlertDialog);

        settingsFragment.showAlert("Title", "Message");

        verify(mockBuilder).setTitle(eq("Title"));
        verify(mockBuilder).setMessage(eq("Message"));
        verify(mockBuilder).setPositiveButton(eq("OK"), isNull());
        verify(mockBuilder).show();
    }
    /**
     * Test case for `onDestroyView` method in `SettingsFragment`.
     */
    @Test
    public void testOnDestroyView() {
        settingsFragment.onDestroyView();

        assertNull(settingsFragment.binding);
    }
}
