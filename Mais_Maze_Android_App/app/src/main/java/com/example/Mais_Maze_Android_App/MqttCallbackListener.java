package com.example.Mais_Maze_Android_App;

/**
 * provides interface to handle MQTT events for clients to implement
 * @see MqttManager
 * @see SecondFragment
 */
public interface MqttCallbackListener {
    void onMessageReceived(String topic, String message);

    void onConnectionLost();

    void onConnectionError(String message);
}
