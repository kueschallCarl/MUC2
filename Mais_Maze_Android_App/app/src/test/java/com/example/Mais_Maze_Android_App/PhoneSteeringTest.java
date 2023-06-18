package com.example.Mais_Maze_Android_App;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PhoneSteeringTest {

    @Mock
    private Context mockContext;
    @Mock
    private PackageManager mockPackageManager;
    @Mock
    private SensorManager mockSensorManager;
    @Mock
    private Sensor mockAccelerometer;
    @Mock
    private Sensor mockGyroscope;
    private PhoneSteering phoneSteering;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mockSensorManager);
        phoneSteering = new PhoneSteering(mockContext);
    }


    @Test
    public void startSensors_accelerometerNotNull_registerListenerCalled() {
        when(mockSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)).thenReturn(mockAccelerometer);

        phoneSteering.startSensors();

        verify(mockSensorManager).registerListener(phoneSteering, mockAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Test
    public void startSensors_gyroscopeNotNull_registerListenerCalled() {
        when(mockSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)).thenReturn(mockGyroscope);

        phoneSteering.startSensors();

        verify(mockSensorManager).registerListener(phoneSteering, mockGyroscope, SensorManager.SENSOR_DELAY_GAME);
    }

    @Test
    public void stopSensors_unregisterListenerCalled() {
        phoneSteering.stopSensors();

        verify(mockSensorManager).unregisterListener(phoneSteering);
    }

    @Test
    public void onSensorChanged_accelerometerEvent_updatesAccelerometerValues() {
        SensorEvent mockEvent = mock(SensorEvent.class);
        when(mockEvent.sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        when(mockEvent.values).thenReturn(new float[]{1.0f, 2.0f, 3.0f});

        phoneSteering.onSensorChanged(mockEvent);

        assertEquals(1.0f, phoneSteering.getAccX(), 0.0f);
        assertEquals(2.0f, phoneSteering.getAccY(), 0.0f);
        assertEquals(3.0f, phoneSteering.getAccZ(), 0.0f);
    }

    @Test
    public void onSensorChanged_gyroscopeEvent_updatesGyroscopeValues() {
        SensorEvent mockEvent = mock(SensorEvent.class);
        when(mockEvent.sensor.getType()).thenReturn(Sensor.TYPE_GYROSCOPE);
        when(mockEvent.values).thenReturn(new float[]{4.0f, 5.0f, 6.0f});

        phoneSteering.onSensorChanged(mockEvent);

        assertEquals(4.0f, phoneSteering.getGyroX(), 0.0f);
        assertEquals(5.0f, phoneSteering.getGyroY(), 0.0f);
        assertEquals(6.0f, phoneSteering.getGyroZ(), 0.0f);
    }

    @Test
    public void onAccuracyChanged_noAssertionsNeeded() {
        Sensor mockSensor = mock(Sensor.class);
        int mockAccuracy = 123;

        phoneSteering.onAccuracyChanged(mockSensor, mockAccuracy);
    }
}
