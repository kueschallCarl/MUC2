package com.K05.Mais_Maze_Android_App;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 * This class provides unit-tests for the ESPSteering class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Log.class)
public class ESPSteeringTest {
    private ESPSteering espSteering;
    private MqttManager mqttManager;
    private Context context;
    /**

     Set up method that initializes the necessary dependencies and mocks for the tests.
     */
    @Before
    public void setUp() {
        context = Mockito.mock(Context.class);
        mqttManager = Mockito.mock(MqttManager.class);
        espSteering = new ESPSteering(context);
        espSteering.mqttManager = mqttManager;
    }
    /**

     Tear down method that cleans up the objects after each test.
     */
    @After
    public void tearDown() {
        espSteering = null;
        mqttManager = null;
        context = null;
    }
    /**

     Test case for the startSensors method of the ESPSteering class.

     Verifies that the method subscribes to the MPU topic.
     */
    @Test
    public void testStartSensors_SubscribesToMpuTopic() {
        espSteering.startSensors();
        verify(mqttManager).subscribeToTopic(Constants.MPU_TOPIC);
    }
    /**

     Test case for the stopSensors method of the ESPSteering class.

     Verifies that the method unsubscribes from the MPU topic.
     */
    @Test
    public void testStopSensors_UnsubscribesFromMpuTopic() {
        espSteering.stopSensors();
        verify(mqttManager).unsubscribeFromTopic(Constants.MPU_TOPIC);
    }
    /**

     Test case for the onMessageReceived method of the ESPSteering class

     with the MPU topic. Verifies that the method calls parseAndAssignValues

     with the correct message and logs the message.
     */
    @Test
    public void testOnMessageReceived_WithMpuTopic_CallsParseAndAssignValues() {
        String topic = Constants.MPU_TOPIC;
        String message = "1.23,4.56,7.89,0.12,3.45,6.78";

        espSteering.onMessageReceived(topic, message);

        // Verify that parseAndAssignValues is called with the correct message
        verify(espSteering).parseAndAssignValues(message);

        // Verify that the log statement is executed with the correct message
        verifyStatic(Log.class, times(1));
        Log.d(eq("mpu/K05 in ESPSteering"), eq(message));
    }
    /**

     Test case for the onMessageReceived method of the ESPSteering class

     with a different topic. Verifies that the method does not call

     parseAndAssignValues and does not log the message.
     */
    @Test
    public void testOnMessageReceived_WithDifferentTopic_DoesNotCallParseAndAssignValues() {
        String topic = "other_topic";
        String message = "1.23,4.56,7.89,0.12,3.45,6.78";

        espSteering.onMessageReceived(topic, message);

        // Verify that parseAndAssignValues is not called
        verify(espSteering, never()).parseAndAssignValues(anyString());

        // Verify that the log statement is not executed
        verifyStatic(Log.class, never());
        Log.d(eq("mpu/K05 in ESPSteering"), anyString());
    }
    /**

     Test case for the onConnectionLost method of the ESPSteering class.

     Verifies that the method shows an alert with the correct parameters.
     */
    @Test
    public void testOnConnectionLost_ShowsAlert() {
        espSteering.onConnectionLost();

        // Verify that showAlert is called with the correct parameters
        verify(espSteering).showAlert("Connection Lost",
                "The MQTT connection to " + mqttManager.MQTT_BROKER_METHOD +
                        "://" + mqttManager.MQTT_BROKER_IP + ":" + mqttManager.MQTT_BROKER_PORT + "was lost.");
    }
    /**

     Test case for the onConnectionError method of the ESPSteering class.

     Verifies that the method shows an alert with the correct parameters.
     */
    @Test
    public void testOnConnectionError_ShowsAlert() {
        String errorMessage = "Failed to connect";

        espSteering.onConnectionError(errorMessage);

        // Verify that showAlert is called with the correct parameters
        verify(espSteering).showAlert("Connection Error",
                "Failed to connect to the MQTT broker at: " +
                        mqttManager.MQTT_BROKER_METHOD + "://" + mqttManager.MQTT_BROKER_IP +
                        ":" + mqttManager.MQTT_BROKER_PORT);
    }
    /**

     Test case for the parseAndAssignValues method of the ESPSteering class.

     Verifies that the method correctly parses and assigns the values from the message.
     */
    @Test
    public void testParseAndAssignValues_ParsesAndAssignsValues() {
        String message = "1.23,4.56,7.89,0.12,3.45,6.78";

        espSteering.parseAndAssignValues(message);

        // Verify that the values are correctly assigned
        assertEquals(1.23f, espSteering.getAccX(), 0.001);
        assertEquals(4.56f, espSteering.getAccY(), 0.001);
        assertEquals(7.89f, espSteering.getAccZ(), 0.001);
        assertEquals(0.12f, espSteering.getGyroX(), 0.001);
        assertEquals(3.45f, espSteering.getGyroY(), 0.001);
        assertEquals(6.78f, espSteering.getGyroZ(), 0.001);
    }
    /**

     Test case for the parseAndAssignValues method of the ESPSteering class

     with an invalid number of values in the message. Verifies that the

     method logs an error message.
     */
    @Test
    public void testParseAndAssignValues_InvalidNumberOfValues_LogsError() {
        String message = "1.23,4.56,7.89,0.12,3.45";

        espSteering.parseAndAssignValues(message);

        // Verify that the log statement is executed with the correct message
        verifyStatic(Log.class, times(1));
        Log.d(eq("NumberOfValues"), eq("Invalid number of values: " + 5));
    }

    /**

     Test case for the showAlert method of the ESPSteering class.
     Verifies that the method displays an alert dialog with the correct parameters.
     */
    @Test
    public void testShowAlert_DisplaysAlertDialog() {
        AlertDialog.Builder builder = Mockito.mock(AlertDialog.Builder.class);
        AlertDialog alertDialog = Mockito.mock(AlertDialog.class);
        when(builder.setTitle(anyString())).thenReturn(builder);
        when(builder.setMessage(anyString())).thenReturn(builder);
        when(builder.setPositiveButton(eq("OK"), any())).thenReturn(builder);
        when(builder.show()).thenReturn(alertDialog);
        Mockito.doReturn(builder).when(builder).show();
        Mockito.doReturn(builder).when(builder).setTitle(anyString());
        Mockito.doReturn(builder).when(builder).setMessage(anyString());
        Mockito.doReturn(builder).when(builder).setPositiveButton(eq("OK"), any());
        Mockito.doReturn(alertDialog).when(builder).create();

        espSteering.showAlert("Test Title", "Test Message");

        // Verify that the AlertDialog.Builder is created with the correct title and message
        verify(builder).setTitle("Test Title");
        verify(builder).setMessage("Test Message");
        verify(builder).setPositiveButton("OK", null);
        verify(builder).show();
    }
}