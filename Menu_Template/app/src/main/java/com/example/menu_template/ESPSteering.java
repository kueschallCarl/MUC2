package com.example.menu_template;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.menu_template.MqttManager;
import com.example.menu_template.MqttCallbackListener;
import com.example.menu_template.Constants.*;

import java.util.Arrays;

/**
 * This class parses the ESP32's Accelerometer/Gyro-Value-Strings received through the MPU_TOPIC,
 * so that the GameLogic Class can access that data to calculate game-physics etc.
 */
public class ESPSteering implements MqttCallbackListener{

    private MqttManager mqttManager;
    private Context context;
    private float acc_x;
    private float acc_y;
    private float acc_z;
    private float gyro_x;
    private float gyro_y;
    private float gyro_z;

    private SettingsDatabase settingsDatabase;
    public ESPSteering(Context context) {
        this.context = context;
        //initialize MqttManager object, to manage Mqtt functions inside this class
        this.mqttManager = new MqttManager("esp_steering");
        //initialize SettingsDatabase object, to get access to the SettingsDatabase
        this.settingsDatabase = SettingsDatabase.getInstance(context);

        //connect to broker
        mqttManager.connect(settingsDatabase, "esp_steering");
        //set this class as a CallbackListener for the mqttManager with the "esp_steering" id
        mqttManager.setCallbackListener(this);
        //subscribe to the mpuTopic to start receiving mpu6050 data on the mqttManager
        mqttManager.subscribeToTopic(Constants.MPU_TOPIC);
    }


    /**
     * This method tells the mqttManager instance of this class to subscribe to the mpuTopic
     * so that this class starts to receive the mpu6050 data
     */
    public void startSensors() {
        mqttManager.subscribeToTopic(Constants.MPU_TOPIC);
    }

    /**
     * This method tells the mqttManager instance of this class to unsubscribe to the mpuTopic
     * so that this class stops receiving the mpu6050 data
     */
    public void stopSensors() {
        mqttManager.unsubscribeFromTopic(Constants.MPU_TOPIC);

    }


    /**
     * This method implements the onMessageReceived method of the MqttCallbackListener interface.
     * It is supposed to handle message it receives on the subscribed topics.
     * @param topic an MQTT topic
     * @param message the message the MQTT topic published
     */
    @Override
        public void onMessageReceived(String topic, String message) {
            if (topic.equals(Constants.MPU_TOPIC)) {
                parseAndAssignValues(message);
                Log.d("mpu/K05 in ESPSteering", message);
            }
        }

    /**
     * This method implements the onConnectionLost method of the MqttCallbackListener interface.
     * This method handles connectionLost errors
      */
    @Override
        public void onConnectionLost() {
            // Show alert to the user
            Log.d("MqttManager", "Connection lost in ESPSteering");
            showAlert("Connection Lost", "The MQTT connection to "+
                    mqttManager.MQTT_BROKER_METHOD+"://"+mqttManager.MQTT_BROKER_IP+":"+mqttManager.MQTT_BROKER_PORT + "was lost.");
        }
    /**
     * This method implements the onConnectionError method of the MqttCallbackListener interface.
     * This method handles Exceptions that occur when the client isn't able to connect to the broker
     */
    @Override
    public void onConnectionError(String message) {
        // Handle connection error
        // Show alert to the user with the error message
        showAlert("Connection Error", "Failed to connect to the MQTT broker at: " +
                mqttManager.MQTT_BROKER_METHOD+"://"+mqttManager.MQTT_BROKER_IP+":"+mqttManager.MQTT_BROKER_PORT);
    }


    /**
     * This method takes in the String published on the mpuTopic and converts it into the corresponding values.
     * @param message the message received on the mpuTopic
     */
    private void parseAndAssignValues(String message) {
        String[] values = message.replaceAll("[()]", "").split(",");
        if (values.length == 6) {
            try {
                acc_x = Float.parseFloat(values[0]);
                acc_y = Float.parseFloat(values[1]);
                acc_z = Float.parseFloat(values[2]);
                gyro_x = Float.parseFloat(values[3]);
                gyro_y = Float.parseFloat(values[4]);
                gyro_z = Float.parseFloat(values[5]);

                Log.d("ParsedValues", "acc_x: " + acc_x + ", acc_y: " + acc_y + ", acc_z: " + acc_z
                        + ", gyro_x: " + gyro_x + ", gyro_y: " + gyro_y + ", gyro_z: " + gyro_z);
            } catch (NumberFormatException e) {
                Log.e("ParseError", "Error parsing values: " + e.getMessage());
            }
        } else {
            Log.d("NumberOfValues", "Invalid number of values: " + values.length);
        }
    }


    /**
     * This method allows this class to display an alert for the user/developer
     * @param title the title of the alert
     * @param message the message of the alert
     */
    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


    /**
     This method retrieves the X-axis value for acceleration.
     @return The X-axis value for acceleration.
     */
    public float getAccX() {
        return acc_x;
    }

    /**
     This method retrieves the Y-axis value for acceleration.
     @return The Y-axis value for acceleration.
     */
    public float getAccY() {
        return acc_y;
    }

    /**
     This method retrieves the Z-axis value for acceleration.
     @return The Z-axis value for acceleration.
     */
    public float getAccZ() {
        return acc_z;
    }

    /**
     This method retrieves the X-axis value for gyroscope.
     @return The X-axis value for gyroscope.
     */
    public float getGyroX() {
        return gyro_x;
    }

    /**
     This method retrieves the Y-axis value for gyroscope.
     @return The Y-axis value for gyroscope.
     */
    public float getGyroY() {
        return gyro_y;
    }

    /**
     This method retrieves the Z-axis value for gyroscope.
     @return The Z-axis value for gyroscope.
     */
    public float getGyroZ() {
        return gyro_z;
    }

    /**
     * This method disconnects this class' mqttManager client from the broker
     */
    public void disconnect(){
        mqttManager.disconnect();
    }
}
