package com.K05.Mais_Maze_Android_App;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.Manifest;
import android.util.Log;
import androidx.core.content.ContextCompat;


/**
 * This class utilizes the sensors inside the Smartphone to parse accelerometer/gyro values, so that the GameLogic class can
 * use that data to calculate game-physics etc.
 */
public class PhoneSteering implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private float acc_x, acc_y, acc_z;
    private float gyro_x, gyro_y, gyro_z;

        /**
         * Constructs a new PhoneSteering instance with the specified context.
         * @param context the context to be used for accessing system services and permissions
         */
        public PhoneSteering(Context context) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                Log.d("Sensor_Listener", "Permissions granted");
            } else {
                Log.d("Sensor_Listener", "Permissions not granted");
            }

            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        /**
         * This method starts listening for sensor events from the accelerometer and gyroscope sensors.
         * Registers this instance as the listener for the sensors.
         */
        public void startSensors() {
            if (accelerometer != null) {
                Log.d("Sensor_Listener", "Accelerometer listener activated");
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
            if (gyroscope != null) {
                Log.d("Sensors_Listener", "Gyro listener activated");
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
            }
        }

        /**
         * This method Stops listening for sensor events and unregisters this instance as the listener.
         */
        public void stopSensors() {
            sensorManager.unregisterListener(this);
        }

    /**
     * This method updates the accelerometer and gyroscope values by accessing the smartphone's sensors and waiting for a change in value.
     * @param event the {@link android.hardware.SensorEvent SensorEvent}.
     */
    @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                acc_x = event.values[0];
                acc_y = event.values[1];
                acc_z = event.values[2];
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyro_x = event.values[0];
                gyro_y = event.values[1];
                gyro_z = event.values[2];
            }
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        /**
         * This method returns the X-axis value of the accelerometer.
         * @return the X-axis value of the accelerometer
         */
        public float getAccX() {
            return acc_x;
        }

        /**
         * This method returns the Y-axis value of the accelerometer.
         * @return the Y-axis value of the accelerometer
         */
        public float getAccY() {
            return acc_y;
        }

        /**
         * This method returns the Z-axis value of the accelerometer.
         * @return the Z-axis value of the accelerometer
         */
        public float getAccZ() {
            return acc_z;
        }

        /**
         * This method returns the X-axis value of the gyroscope.
         * @return the X-axis value of the gyroscope
         */
        public float getGyroX() {
            return gyro_x;
        }

        /**
         * This method returns the Y-axis value of the gyroscope.
         * @return the Y-axis value of the gyroscope
         */
        public float getGyroY() {
            return gyro_y;
        }

        /**
         * This method returns the Z-axis value of the gyroscope.
         * @return the Z-axis value of the gyroscope
         */
        public float getGyroZ() {
            return gyro_z;
        }
    }

