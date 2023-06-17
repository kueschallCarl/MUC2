package com.example.menu_template;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.menu_template.databinding.FragmentFirstBinding;
import com.example.menu_template.databinding.FragmentSecondBinding;
import org.eclipse.paho.client.mqttv3.*;
import com.example.menu_template.MqttManager;
import com.example.menu_template.MqttCallbackListener;
import com.example.menu_template.Constants.*;
import com.example.menu_template.databinding.FragmentSettingsBinding;

import java.util.Set;


/**
 * This Fragment is displayed whenever the user selects the MenuItem with the ID of "action_settings".
 * It displays all the settings that the user is able to modify.
 */
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private MqttManager mqttManager;
    private String SteeringMethod;
    private SettingsDatabase settingsDatabase;


    /**
     * This method defines what should happen as the view is created.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the root of this Fragment's view
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * This method defines what should happen once the view has been created
     * It will immediately fill in the settings with the stored settings inside the SettingsDatabase
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Fill in Settings with stored Settings in SQLite
        RadioGroup radioGroup = binding.radioBtnGroupSteeringMethod;
        EditText sizeSettingEditText = binding.sizeSetting;
        EditText brokerIPEditText = binding.brokerAddressTextField;

        this.settingsDatabase = SettingsDatabase.getInstance(requireContext());

        try {
            String radioButtonSelection = settingsDatabase.getSetting(SettingsDatabase.COLUMN_STEERING_METHOD);
            sizeSettingEditText.setText(settingsDatabase.getSetting(SettingsDatabase.COLUMN_LABYRINTH_SIZE));
            brokerIPEditText.setText(settingsDatabase.getSetting(SettingsDatabase.COLUMN_BROKER_IP));

            int radioButtonId = -1;
            if (radioButtonSelection.equals("ESP32")) {
                radioButtonId = R.id.radio_btn_esp32_steering;
            } else if (radioButtonSelection.equals("Phone")) {
                radioButtonId = R.id.radio_btn_phone_steering;
            }
            if (radioButtonId != -1) {
                Log.d("radioButton", "Radio button: " + radioButtonId + "checked");
                radioGroup.check(radioButtonId);
            }
        }
        catch (Exception e){
            Log.d("Settings","Failed to retrieve any Settings from Database and set the xml" + e);
        }


        // Get the singleton instance of MqttManager
        mqttManager = new MqttManager("settings_fragment");

        // Handle connect button click
        binding.buttonSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String brokerIP = binding.brokerAddressTextField.getText().toString();
                String labyrinth_size = binding.sizeSetting.getText().toString();
                mqttManager.MQTT_BROKER_IP = brokerIP;
                settingsDatabase.updateLastSetting(brokerIP, SettingsDatabase.COLUMN_BROKER_IP);
                settingsDatabase.updateLastSetting(labyrinth_size, SettingsDatabase.COLUMN_LABYRINTH_SIZE);
                Log.d("MqttManager", "brokerIP: " + mqttManager.MQTT_BROKER_IP);
            }
        });

        // Set a listener for radio button changes
        binding.radioBtnGroupSteeringMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkSelectedSteeringMethod(settingsDatabase);
            }
        });
        // Call checkSelectedSteeringMethod initially to handle any pre-selected radio button
        checkSelectedSteeringMethod(settingsDatabase);
    }

    /**
     * This method defines what should happen as the Fragment itself is created.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsDatabase = SettingsDatabase.getInstance(requireContext());
        setHasOptionsMenu(true);
    }


    /**
     * This method defines what should happen as the options menu is created
     * @param menu The options menu in which you place your items.
     * @param inflater inflates the menu
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * This method defines what should happen once a MenuItem inside the menu is selected
     * @param item The menu item that was selected.
     *
     * @return True if the menu item selection was handled successfully, otherwise return False
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Navigate to the SettingsFragment
            NavHostFragment.findNavController(this).navigate(R.id.action_FirstFragment_to_SettingsFragment);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * This method simply checks which steering method's respective radio button is currently selected and stores that information in the SettingsDatabase
     * @param settingsDatabase the SettingsDatabase object which contains the settings data
     */
    private void checkSelectedSteeringMethod(SettingsDatabase settingsDatabase) {
        try {
            // Find the RadioGroup within the current fragment's view
            RadioGroup radioGroup = getView().findViewById(R.id.radio_btn_group_steering_method);

            // Check if any radio button is checked
            if (radioGroup.getCheckedRadioButtonId() != -1) {
                // At least one radio button is checked

                // Get the selected radio button's ID
                int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

                // Check if the selected radio button matches a specific radio button
                if (selectedRadioButtonId == R.id.radio_btn_esp32_steering) {
                    // ESP32 steering method is selected
                    SteeringMethod = "ESP32";
                } else if (selectedRadioButtonId == R.id.radio_btn_phone_steering) {
                    // Phone steering method is selected
                    SteeringMethod = "Phone";
                }

                // Save the selected steering method to the database
                settingsDatabase.updateLastSetting(SteeringMethod, SettingsDatabase.COLUMN_STEERING_METHOD);
                Log.d("Database", "Steering method saved: " + SteeringMethod);

                // Do something with the selected steering method
                // For example, log it or use it in your game logic
                Log.d("SteeringMethod", "Selected steering method: " + SteeringMethod);
            } else {
                Log.d("SteeringMethod", "No SteeringMethod selected");
            }
        } catch (Exception e) {
            showAlert("Radio Button Issue", "No Steering Method selected in Settings");
            Log.e("SteeringMethod", "Error saving steering method: " + e.getMessage());
        }
    }

    /**
     * This method checks the SettingsDatabase for information on which steering method has been selected
     * @param settingsDatabase the SettingsDatabase object which contains the settings data
     * @return the chosen steering method. Can be either "ESP32" or "Phone"
     */
    public String getSteeringMethod(SettingsDatabase settingsDatabase) {
        String steeringMethod = settingsDatabase.getSetting(SettingsDatabase.COLUMN_STEERING_METHOD);
        Log.d("Database", "Retrieved steering method: " + steeringMethod);
        return steeringMethod;
    }

    /**
     * This method allows this class to display an alert for the user/developer
     * @param title the title of the alert
     * @param message the message of the alert
     */
    private void showAlert(String title, String message) {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    /**
     * This method defines what should happen whenever this Fragment's view is destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
