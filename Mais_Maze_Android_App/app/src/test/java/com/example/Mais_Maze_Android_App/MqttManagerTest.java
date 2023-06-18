package com.example.Mais_Maze_Android_App;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MqttManagerTest {
    @Mock
    private MqttClient mockMqttClient;
    @Mock
    private MqttCallbackListener mockCallbackListener;

    private MqttManager mqttManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mqttManager = new MqttManager("client_id");
        mqttManager.mqttClient = mockMqttClient;
        mqttManager.callbackListener = mockCallbackListener;
    }

    @Test
    public void testPublishToTopic() throws MqttException {
        String message = "Test message";
        String topic = "test/topic";
        MqttMessage mockMqttMessage = mock(MqttMessage.class);
        when(mockMqttClient.isConnected()).thenReturn(true);

        mqttManager.publishToTopic(message, topic);

        verify(mockMqttMessage).setPayload(message.getBytes());
        verify(mockMqttClient).publish(topic, mockMqttMessage);
        verify(mockCallbackListener, never()).onConnectionError(anyString());
        verify(mockCallbackListener, never()).onConnectionLost();
        verify(mockCallbackListener, never()).onMessageReceived(anyString(), anyString());
    }

    @Test
    public void testPublishToTopic_Exception() throws MqttException {
        String message = "Test message";
        String topic = "test/topic";
        MqttMessage mockMqttMessage = mock(MqttMessage.class);
        when(mockMqttClient.isConnected()).thenReturn(true);
        doThrow(new MqttException(MqttException.REASON_CODE_CONNECTION_LOST)).when(mockMqttClient).publish(topic, mockMqttMessage);

        mqttManager.publishToTopic(message, topic);

        verify(mockMqttMessage).setPayload(message.getBytes());
        verify(mockMqttClient).publish(topic, mockMqttMessage);
        verify(mockCallbackListener, never()).onConnectionError(anyString());
        verify(mockCallbackListener).onConnectionLost();
        verify(mockCallbackListener, never()).onMessageReceived(anyString(), anyString());
    }

    @Test
    public void testSubscribeToTopic() throws MqttException {
        String topic = "test/topic";
        when(mockMqttClient.isConnected()).thenReturn(true);

        mqttManager.subscribeToTopic(topic);

        verify(mockMqttClient).subscribe(topic);
        verify(mockCallbackListener, never()).onConnectionError(anyString());
        verify(mockCallbackListener, never()).onConnectionLost();
        verify(mockCallbackListener, never()).onMessageReceived(anyString(), anyString());
    }

    @Test
    public void testSubscribeToTopic_Exception() throws MqttException {
        String topic = "test/topic";
        when(mockMqttClient.isConnected()).thenReturn(true);
        doThrow(new MqttException(MqttException.REASON_CODE_CONNECTION_LOST)).when(mockMqttClient).subscribe(topic);

        mqttManager.subscribeToTopic(topic);

        verify(mockMqttClient).subscribe(topic);
        verify(mockCallbackListener, never()).onConnectionError(anyString());
        verify(mockCallbackListener).onConnectionLost();
        verify(mockCallbackListener, never()).onMessageReceived(anyString(), anyString());
    }

    @Test
    public void testUnsubscribeFromTopic() throws MqttException {
        String topic = "test/topic";
        when(mockMqttClient.isConnected()).thenReturn(true);

        mqttManager.unsubscribeFromTopic(topic);

        verify(mockMqttClient).unsubscribe(topic);
        verify(mockCallbackListener, never()).onConnectionError(anyString());
        verify(mockCallbackListener, never()).onConnectionLost();
        verify(mockCallbackListener, never()).onMessageReceived(anyString(), anyString());
    }

    @Test
    public void testUnsubscribeFromTopic_Exception() throws MqttException {
        String topic = "test/topic";
        when(mockMqttClient.isConnected()).thenReturn(true);
        doThrow(new MqttException(MqttException.REASON_CODE_CONNECTION_LOST)).when(mockMqttClient).unsubscribe(topic);

        mqttManager.unsubscribeFromTopic(topic);

        verify(mockMqttClient).unsubscribe(topic);
        verify(mockCallbackListener, never()).onConnectionError(anyString());
        verify(mockCallbackListener).onConnectionLost();
        verify(mockCallbackListener, never()).onMessageReceived(anyString(), anyString());
    }

    @Test
    public void testConnect_SuccessfulConnection() throws MqttException {
        SettingsDatabase mockSettingsDatabase = mock(SettingsDatabase.class);
        String clientId = "client_id";
        when(mockSettingsDatabase.getSetting(SettingsDatabase.COLUMN_BROKER_IP)).thenReturn("192.168.0.89");
        when(mockMqttClient.isConnected()).thenReturn(true);

        mqttManager.connect(mockSettingsDatabase, clientId);

        verify(mockMqttClient).connect(any(MqttConnectOptions.class));
        verify(mockCallbackListener, never()).onConnectionError(anyString());
        verify(mockCallbackListener, never()).onConnectionLost();
        verify(mockCallbackListener, never()).onMessageReceived(anyString(), anyString());
    }

    @Test
    public void testConnect_ConnectionFailed() throws MqttException {
        SettingsDatabase mockSettingsDatabase = mock(SettingsDatabase.class);
        String clientId = "client_id";
        when(mockSettingsDatabase.getSetting(SettingsDatabase.COLUMN_BROKER_IP)).thenReturn("192.168.0.89");
        when(mockMqttClient.isConnected()).thenReturn(false);

        mqttManager.connect(mockSettingsDatabase, clientId);

        verify(mockMqttClient).connect(any(MqttConnectOptions.class));
        verify(mockCallbackListener).onConnectionError("Failed to connect to MQTT broker");
        verify(mockCallbackListener, never()).onConnectionLost();
        verify(mockCallbackListener, never()).onMessageReceived(anyString(), anyString());
    }

    @Test
    public void testConnect_ConnectionException() throws MqttException {
        SettingsDatabase mockSettingsDatabase = mock(SettingsDatabase.class);
        String clientId = "client_id";
        when(mockSettingsDatabase.getSetting(SettingsDatabase.COLUMN_BROKER_IP)).thenReturn("192.168.0.89");
        doThrow(new MqttException(MqttException.REASON_CODE_CONNECTION_LOST)).when(mockMqttClient).connect(any(MqttConnectOptions.class));

        mqttManager.connect(mockSettingsDatabase, clientId);

        verify(mockMqttClient).connect(any(MqttConnectOptions.class));
        verify(mockCallbackListener).onConnectionError(anyString());
        verify(mockCallbackListener).onConnectionLost();
        verify(mockCallbackListener, never()).onMessageReceived(anyString(), anyString());
    }

    @Test
    public void testDisconnect() throws MqttException {
        when(mockMqttClient.isConnected()).thenReturn(true);

        mqttManager.disconnect();

        verify(mockMqttClient).unsubscribe("mpu/K05");
        verify(mockMqttClient).disconnect();
    }

    @Test
    public void testDisconnect_NoConnection() throws MqttException {
        when(mockMqttClient.isConnected()).thenReturn(false);

        mqttManager.disconnect();

        verify(mockMqttClient, never()).unsubscribe("mpu/K05");
        verify(mockMqttClient, never()).disconnect();
    }
}
