package com.K05.Mais_Maze_Android_App;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.example.Mais_Maze_Android_App.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;


/**
 * This class handles most of the game's logic, calculations and physics, and acts as a controller inside the project, directing requests to other classes.
 */
public class GameLogic implements MqttCallbackListener {

    public MqttManager mqttManager;
    private Context context;
    private SettingsDatabase settingsDatabase;
    public final ESPSteering espSteering;
    public final PhoneSteering phoneSteering;
    private SoundPlayer soundPlayer;
    public int[][] labyrinth; //The 2D array representation of the labyrinth

    private float temperature; //the current temperature retrieved through the TEMP_TOPIC
    private int mais_count; //the current amount of mais (corn) collected by the player
    private boolean gameRunning; //a flag that determines if the game is currently running or not
    private int playTime; //the current amount of time that has passed since the start of the game
    private int size = 10; //the x,y size of the labyrinth

    private static final float MAX_ACCELEROMETER_RANGE = 9.81f; // Maximum range of accelerometer sensor (in m/s^2)
    private static final float MAX_GYROSCOPE_RANGE = 2000.0f; // Maximum range of gyroscope sensor (in degrees/second)
    private static final float ALPHA = 0.5f; // Low-pass filter constant
    private static final float ACCELEROMETER_WEIGHT = 0.7f; // Weight for accelerometer data in combined direction calculation
    private static final float GYROSCOPE_WEIGHT = 0.3f; // Weight for gyroscope data in combined direction calculation
    private static final float DEAD_ZONE_THRESHOLD = 0.05f; // Threshold to define the dead zone for sensor data (adjust as needed)
    private static final float TILT_THRESHOLD = 0.1f; // Threshold for tilt detection (adjust as needed)
    private static final float LOCK_THRESHOLD = 0.2f; // Threshold to lock/unlock the direction (adjust as needed)
    private float lastAccX, lastAccY, lastAccZ; // Last accelerometer values
    private long lastUpdateTime; // Time of the last update
    private float[] highPassAcc = new float[3]; // High-pass filter output for accelerometer data
    private float[] gyroOrientation = new float[3];
    private boolean isDirectionLocked = false; //a flag that determines if the playerObject should keep moving in the previous direction because of accelerometer and gyroscope data
    private int lastValidDirection = -1; //stores the last valid direction the playerObject has been moved toward
    private int currentDirection = -1; //stores the direction the playerObject is currently destined to be moved toward


    private Handler handler; // Handler to run code on the main thread


    /**
     * This constructor initializes all the necessary objects for later use and resets attributes if needed.
     * @param context needs to be passed through the constructor, as some of the objects require it etc.
     * @param settingsDatabase is passed through the constructor
     */
    public GameLogic(Context context, SettingsDatabase settingsDatabase) {
        this.context = context;
        handler = new Handler();
        this.mqttManager = new MqttManager("game_logic");
        this.settingsDatabase = SettingsDatabase.getInstance(context);
        this.soundPlayer = new SoundPlayer();

        mqttManager.connect(settingsDatabase, "game_logic");
        mqttManager.setCallbackListener(this);
        mqttManager.subscribeToTopic(Constants.TEMP_TOPIC);

        this.espSteering = new ESPSteering(context);
        this.phoneSteering = new PhoneSteering(context);

        this.playTime = 0;
        this.mais_count = 0;
        this.temperature =0;
        this.size = Integer.parseInt(settingsDatabase.getSetting(SettingsDatabase.COLUMN_LABYRINTH_SIZE));
        if(this.size < 10){
            this.size = 10;
        }

        mqttManager.publishToTopic("0", Constants.FINISHED_TOPIC);

        try {
            generateLabyrinth();
            Log.d("Labyrinth", "Labyrinth: " + Arrays.deepToString(this.labyrinth));
        } catch (Exception e) {
            Log.d("Labyrinth", "Problem generating Labyrinth in GameLogic.java", e);
        }
    }


        /**
         * This method implements the onMessageReceived method of the MqttCallbackListener interface.
         * It is supposed to handle message it receives on the subscribed topics.
         * @param topic an MQTT topic
         * @param message the message the MQTT topic published
         */
        @Override
        public void onMessageReceived(String topic, String message) {
            if (topic.equals(Constants.TEMP_TOPIC)) {
                Log.d("temp/K05 in GameLogic", message);
                parseTemperature(message);
            }
        }

        /**
         * This method implements the onConnectionLost method of the MqttCallbackListener interface.
         * This method handles connectionLost errors
         */
        @Override
        public void onConnectionLost() {
            // Show alert to the user
            Log.d("MqttManager", "Connection lost in GameLogic");
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
            Log.d("mqttManager", "connection error in gameLogic");
            // Show alert to the user with the error message
            showAlert("Connection Error", "Failed to connect to the MQTT broker at: " +
                    mqttManager.MQTT_BROKER_METHOD+"://"+mqttManager.MQTT_BROKER_IP+":"+mqttManager.MQTT_BROKER_PORT);
        }


    /**
     *  This method converts message containing the temperature value as a String to a float
      * @param message the message received on the TEMP_TOPIC containing the temperature value from the mpu6050 sensor
     */
    public void parseTemperature(String message){
        this.temperature = Float.parseFloat(message);
        if(gameRunning){
            playTime+=1;
        }
    }

    /**
     * This method sets the status of the game running.
     * @param gameRunning the status of the game running
     */
    public void setGameRunning(boolean gameRunning){
        this.gameRunning = gameRunning;
    }

    /**
     * This method retrieves the status of the game running.
     * @return the status of the game running
     */
    public boolean getGameRunning(){
        return gameRunning;
    }

    /**
     * This method retrieves the play time.
     * @return the play time
     */
    public int getPlayTime(){
        return playTime;
    }

    /**
     * This method retrieves the temperature.
     * @return the temperature
     */
    public float getTemperature(){
        return temperature;
    }

    /**
     * This method defines one 'step' of the GameLogic part of the game's loop.
     * It gets the next player direction and moves the player (modifying the labyrinth accordingly)
     * @param steeringType is either set to "ESP32" or "PHONE" and determines which steering method should be utilized for this step
     * @return True if labyrinth is empty (winning condition), otherwise return False
     */
    public boolean gameStep(String steeringType) {
        int playerDirection = getPlayerDirection(steeringType);
        Log.d("playerDirection", String.valueOf(playerDirection));
        labyrinth = movePlayer(labyrinth, playerDirection);

        if (isLabyrinthEmpty(labyrinth)) {
            return true;
        }
        else{
            return false;
        }
    }


    /**
     * This method gets all the accelerometer and gyro values from the ESPSteering object
     * @return returns the accelerometer and gyro values from the ESPSteering object as an array of floats
     */
    public float[] getValuesFromESPSensor() {
        float accX = espSteering.getAccX();
        float accY = espSteering.getAccY();
        float accZ = espSteering.getAccZ();
        float gyroX = espSteering.getGyroX();
        float gyroY = espSteering.getGyroY();
        float gyroZ = espSteering.getGyroZ();
        Log.d("ESPSensor", "ESPSensorValue: "+ Arrays.toString(new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ}));

        return new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ};
    }
    /**
     * This method gets all the accelerometer and gyro values from the PhoneSteering object
     * @return returns the accelerometer and gyro values from the PhoneSteering object as an array of floats
     */
    public float[] getValuesFromPhoneSensor() {
        float accX = phoneSteering.getAccX();
        float accY = phoneSteering.getAccY();
        float accZ = phoneSteering.getAccZ();
        float gyroX = phoneSteering.getGyroX();
        float gyroY = phoneSteering.getGyroY();
        float gyroZ = phoneSteering.getGyroZ();

        Log.d("PhoneSensor", "PhoneSensorValue: "+ Arrays.toString(new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ}));

        return new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ};
    }


    /**
     * This method takes in an array of floats containing the accelerometer and gyroscope values of the chosen steering method.
     * It then calculates an appropriate direction the player object should move toward.
     * @param sensorData contains accelerometer and gyro values from the chosen steering method
     * @return a direction the player object should be moved toward
     */
    public int parsePlayerDirection(float[] sensorData) {
        float accelerometerX = sensorData[0];
        float accelerometerY = sensorData[1];
        float accelerometerZ = sensorData[2];
        float gyroX = sensorData[3];
        float gyroY = sensorData[4];
        float gyroZ = sensorData[5];

        // Normalize accelerometer and gyroscope data
        float normalizedAccX = accelerometerX / MAX_ACCELEROMETER_RANGE;
        float normalizedAccY = accelerometerY / MAX_ACCELEROMETER_RANGE;
        float normalizedAccZ = accelerometerZ / MAX_ACCELEROMETER_RANGE;

        float normalizedGyroX = gyroX / MAX_GYROSCOPE_RANGE;
        float normalizedGyroY = gyroY / MAX_GYROSCOPE_RANGE;
        float normalizedGyroZ = gyroZ / MAX_GYROSCOPE_RANGE;

        // Apply high-pass filter to accelerometer data
        highPassAcc[0] = ALPHA * (highPassAcc[0] + normalizedAccX - lastAccX);
        highPassAcc[1] = ALPHA * (highPassAcc[1] + normalizedAccY - lastAccY);
        highPassAcc[2] = ALPHA * (highPassAcc[2] + normalizedAccZ - lastAccZ);

        // Calculate time elapsed since the last update
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        // Integrate gyroscope data using time-based integration
        gyroOrientation[0] += normalizedGyroX * deltaTime;
        gyroOrientation[1] += normalizedGyroY * deltaTime;
        gyroOrientation[2] += normalizedGyroZ * deltaTime;

        // Apply dead zone to prevent small fluctuations from triggering movements
        float accelMagnitude = (float) Math.sqrt(highPassAcc[0] * highPassAcc[0] + highPassAcc[1] * highPassAcc[1] + highPassAcc[2] * highPassAcc[2]);
        float gyroMagnitude = (float) Math.sqrt(gyroOrientation[0] * gyroOrientation[0] + gyroOrientation[1] * gyroOrientation[1] + gyroOrientation[2] * gyroOrientation[2]);

        if (accelMagnitude < DEAD_ZONE_THRESHOLD) {
            highPassAcc[0] = 0.0f;
            highPassAcc[1] = 0.0f;
            highPassAcc[2] = 0.0f;
        }

        if (gyroMagnitude < DEAD_ZONE_THRESHOLD) {
            gyroOrientation[0] = 0.0f;
            gyroOrientation[1] = 0.0f;
            gyroOrientation[2] = 0.0f;
        }

        // Combine accelerometer and gyroscope data to determine direction
        float combinedX = ACCELEROMETER_WEIGHT * highPassAcc[0] + GYROSCOPE_WEIGHT * gyroOrientation[0];
        float combinedY = ACCELEROMETER_WEIGHT * highPassAcc[1] + GYROSCOPE_WEIGHT * gyroOrientation[1];
        float combinedZ = ACCELEROMETER_WEIGHT * highPassAcc[2] + GYROSCOPE_WEIGHT * gyroOrientation[2];

        // Adjust the thresholds based on your specific requirements
        final float tiltThreshold = TILT_THRESHOLD;
        final float lockThreshold = LOCK_THRESHOLD;

        // Check if the current direction is locked
        if (isDirectionLocked) {
            // Check if the tilt threshold in the opposite direction is crossed
            if (Math.abs(combinedX) < lockThreshold && Math.abs(combinedY) < lockThreshold) {
                // Unlock the direction
                isDirectionLocked = false;
                // Reset gyroscope and accelerometer orientation
                resetOrientation();
            }
        } else {
            // Check the combined values to determine the direction
            if (Math.abs(combinedX) > tiltThreshold && Math.abs(combinedX) > Math.abs(combinedY)) {
                if (combinedX > 0) {
                    // Player is tilting the device to the right
                    currentDirection = 2;
                    isDirectionLocked = true;
                } else {
                    // Player is tilting the device to the left
                    currentDirection = 3;
                    isDirectionLocked = true;
                }
            } else if (Math.abs(combinedY) > tiltThreshold) {
                if (combinedY > 0) {
                    // Player is tilting the device forward
                    currentDirection = 0;
                    isDirectionLocked = true;
                } else {
                    // Player is tilting the device backward
                    currentDirection = 1;
                    isDirectionLocked = true;
                }
            }
        }

        if (currentDirection != -1) {
            resetOrientation();
            // Update the last valid direction
            lastValidDirection = currentDirection;
        }

        return lastValidDirection;
    }

    /**
     * This method resets the orientation of the accelerometer and gyroscope respectively
     */
    private void resetOrientation() {
        highPassAcc[0] = 0.0f;
        highPassAcc[1] = 0.0f;
        highPassAcc[2] = 0.0f;

        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;
    }


    /**
     * This method takes the steering method into account and then executes the appropriate methods to get the next player direction
     * @param steeringType the chosen steering method. Is either set to "ESP32" or "PHONE"
     * @return the next direction the playerObject should be moved toward.
     */
    public int getPlayerDirection(String steeringType){
        float[] sensor_data = new float[6];
        switch (steeringType) {
            case "ESP32":
                sensor_data = getValuesFromESPSensor();
                break;
            case "Phone":
                sensor_data = getValuesFromPhoneSensor();
                break;
        }
        return parsePlayerDirection(sensor_data);
    }

    /**
     * This method takes the chosen steering method into account and executes the appropriate startSensor() method of the equivalent object
     * @param steeringType the chosen steering method. Is either set to "ESP32" or "PHONE"
     */
    public void startSensors(String steeringType) {
        switch (steeringType) {
            case "ESP32":
                this.espSteering.startSensors();
                Log.d("gameLoop", "ESPSensor started");
                break;
            case "Phone":
                this.phoneSteering.startSensors();
                Log.d("gameLoop", "PhoneSensor started");
                break;
            default:
                Log.d("gameLoop", "Steering Type unknown " + steeringType);
        }
    }
    /**
     * This method takes the chosen steering method into account and executes the appropriate stopSensor() method of the equivalent object
     * @param steeringType the chosen steering method. Is either set to "ESP32" or "PHONE"
     */
    public void stopSensors(String steeringType) {
        switch (steeringType) {
            case "ESP32":
                this.espSteering.stopSensors();
                break;
            case "Phone":
                this.phoneSteering.stopSensors();
                break;
        }
    }

    /**
     * This method takes in the current 2D array representing the labyrinth, as well as the previously calculated direction the player should be moved toward.
     * It then checks for multiple conditions, making sure that the position the player would occupy after moving in the direction isn't out of bounds or a wall.
     * It also checks for mais (corn) objects and handles what happens when the player objects 'touches' them. And lastly it checks for the winning condition, which is currently met when
     * the player is in the 'n4' neighborhood of the finish's position. This is to avoid the player having to aim for the exact position of the finish, which can be tricky at great speeds.
     * @param labyrinth the current 2D array representing the labyrinth
     * @param playerDirection the calculated direction the player should next be moved toward
     * @return the 'redrawn' labyrinth with the player moved etc.
     */
    public int[][] movePlayer(int[][] labyrinth, int playerDirection) {
        int playerX = -1;
        int playerY = -1;

        // Find the current position of the player (value 2) in the labyrinth
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                if (labyrinth[i][j] == 2) {
                    playerX = i;
                    playerY = j;
                    break;
                }
            }
        }

        // Check if the new position is in the vicinity of the finish (value 3)
        int finishX = -1;
        int finishY = -1;

        // Find the position of the finish (value 3) in the labyrinth
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                if (labyrinth[i][j] == 3) {
                    finishX = i;
                    finishY = j;
                    break;
                }
            }
        }

        if (playerX == -1 || playerY == -1) {
            // Player not found in the labyrinth
            Log.d("movePlayer", "Player not found in the labyrinth");
            return labyrinth;
        }

        // Determine the new position based on the player's direction
        int newPlayerX = playerX;
        int newPlayerY = playerY;

        switch (playerDirection) {
            case 0: // Right direction
                newPlayerY++;
                break;
            case 1: // Left direction
                newPlayerY--;
                break;
            case 2: // Forward direction
                newPlayerX--;
                break;
            case 3: // Backward direction
                newPlayerX++;
                break;
            default:
                // Invalid direction
                Log.d("movePlayer", "Invalid direction: " + playerDirection);
                return labyrinth;
        }

        // Check if the new position is within the bounds of the labyrinth
        if (newPlayerX < 0 || newPlayerX >= labyrinth.length || newPlayerY < 0 || newPlayerY >= labyrinth[0].length) {
            // Player is out of bounds
            Log.d("movePlayer", "Player is out of bounds");
            return labyrinth;
        }

        // Check if the new position is a wall (value 1)
        if (labyrinth[newPlayerX][newPlayerY] == 1) {
            // Player cannot move to a wall
            Log.d("movePlayer", "Player cannot move to a wall");
            return labyrinth;
        }

        if (labyrinth[newPlayerX][newPlayerY] == 6) {
            // Player cannot move to a wall
            Log.d("Mais", "Mais collected at: "+ "[" + newPlayerX + "]"+ "," + "[" + newPlayerY + "]");
            if(Boolean.parseBoolean(settingsDatabase.getSetting(SettingsDatabase.COLUMN_AUDIO))){
                soundPlayer.playSoundEffect(context, R.raw.mais_pickup_sound);
            }
            this.mais_count += 1;
        }

        // Calculate the difference between the player's position and the finish's position
        int deltaX = Math.abs(newPlayerX - finishX);
        int deltaY = Math.abs(newPlayerY - finishY);

        // Check if the new position is the winning position (value 3)
        if ((deltaX == 0 && deltaY == 1) || (deltaX == 1 && deltaY == 0)) {
            // Player is in the vicinity of the finish
            Log.d("movePlayer", "Player is in the vicinity of the finish");
            // Set all elements in the labyrinth to 0 (empty space)
            for (int[] ints : labyrinth) {
                Arrays.fill(ints, 0);
            }
            return labyrinth;
        }

        // Move the player to the new position
        labyrinth[playerX][playerY] = 0; // Set the current position to 0 (empty space)
        labyrinth[newPlayerX][newPlayerY] = 2; // Set the new position to 2 (player)

        return labyrinth;
    }


    /**
     * This method checks if the 2D array representing the labyrinth is completely 'empty', meaning it is filled with zeros.
     * @param labyrinth the current 2D array representing the labyrinth
     * @return true if it is empty, false otherwise
     */
    public boolean isLabyrinthEmpty(int[][] labyrinth) {
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                if (labyrinth[i][j] != 0) {
                    return false; // Found a non-zero element, labyrinth is not empty
                }
            }
        }
        return true; // All elements are zeros, labyrinth is empty
    }



    /**
     * This method generates a labyrinth using a modified version of the depth-first search algorithm.
     * The labyrinth is represented as a two-dimensional array of integers.
     * The starting point is marked as 2 and the ending point is marked as 3.
     * Walls are represented by 1, and empty spaces are represented by 0.
     * The labyrinth is generated by removing walls between cells to create a maze structure.
     * If the ending point does not have adjacent empty spaces, the labyrinth is regenerated.
     * Once the labyrinth is generated, mais (corn) will be placed within it, represented as integers with values of six.
     */
    public void generateLabyrinth() {
        this.labyrinth = new int[size][size];

        // Set all cells as walls
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                labyrinth[i][j] = 1;
            }
        }

        // Choose a random starting point on the top boundary
        int startX = 0;
        int startY = getRandomNumber(1, size - 2);
        labyrinth[startX][startY] = 2; // Mark the starting point as 2

        // Choose a random ending point on the bottom boundary
        int endX = size - 1;
        int endY = getRandomNumber(1, size - 2);
        labyrinth[endX][endY] = 3; // Mark the ending point as 3

        // Create a stack to keep track of visited cells
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] currentCell = stack.peek();
            int currentX = currentCell[0];
            int currentY = currentCell[1];

            // Get the unvisited neighbors of the current cell
            List<int[]> unvisitedNeighbors = getUnvisitedNeighbors(currentX, currentY);
            if (!unvisitedNeighbors.isEmpty()) {
                // Choose a random unvisited neighbor
                int[] randomNeighbor = unvisitedNeighbors.get(getRandomNumber(0, unvisitedNeighbors.size() - 1));
                int neighborX = randomNeighbor[0];
                int neighborY = randomNeighbor[1];

                // Remove the wall between the current cell and the chosen neighbor
                int wallX = (currentX + neighborX) / 2;
                int wallY = (currentY + neighborY) / 2;
                labyrinth[wallX][wallY] = 0;

                labyrinth[neighborX][neighborY] = 0; // Mark the neighbor as part of the maze
                stack.push(new int[]{neighborX, neighborY});
            } else {
                // All neighbors visited, backtrack
                stack.pop();
            }
        }

        // Check if the end point has adjacent 0's, if not, regenerate the labyrinth
        if (!hasAdjacentZeros(endX, endY)) {
            Log.d("Labyrinth", "Labyrinth before regen: " + Arrays.deepToString(this.labyrinth));
            generateLabyrinth();
        }

        // Place "Mais" in the finished labyrinth
        placeMais(labyrinth);
    }

    /**
     * This method checks if the specified cell at the given coordinates has adjacent empty spaces (0's).
     * The generateLabyrinth method uses this method to make sure that the start and finish points are reachable.
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return true if the cell has adjacent empty spaces, false otherwise
     */
    public boolean hasAdjacentZeros(int x, int y) {
        // Check the four cardinal directions
        if (x > 0 && labyrinth[x - 1][y] == 0) {
            return true; // There is a 0 to the north
        }
        if (x < size - 1 && labyrinth[x + 1][y] == 0) {
            return true; // There is a 0 to the south
        }
        if (y > 0 && labyrinth[x][y - 1] == 0) {
            return true; // There is a 0 to the west
        }
        if (y < size - 1 && labyrinth[x][y + 1] == 0) {
            return true; // There is a 0 to the east
        }

        return false; // No adjacent 0's
    }

    /**
     * This method retrieves the list of unvisited neighbors of the specified cell at the given coordinates.
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return the list of unvisited neighbors as an ArrayList of integer arrays,
     *         where each array contains the x and y coordinates of a neighbor
     */
    public List<int[]> getUnvisitedNeighbors(int x, int y) {
        List<int[]> unvisitedNeighbors = new ArrayList<>();

        // Check the four cardinal directions
        if (x > 1 && labyrinth[x - 2][y] == 1) {
            unvisitedNeighbors.add(new int[]{x - 2, y});
        }
        if (x < size - 2 && labyrinth[x + 2][y] == 1) {
            unvisitedNeighbors.add(new int[]{x + 2, y});
        }
        if (y > 1 && labyrinth[x][y - 2] == 1) {
            unvisitedNeighbors.add(new int[]{x, y - 2});
        }
        if (y < size - 2 && labyrinth[x][y + 2] == 1) {
            unvisitedNeighbors.add(new int[]{x, y + 2});
        }

        return unvisitedNeighbors;
    }

    /**
     * This method generates a random integer between the specified minimum and maximum values.
     * @param min the minimum value
     * @param max the maximum value
     * @return a random integer between the minimum and maximum values
     */
    public int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }


    /**
     * This method randomly places a certain amount of mais (corn) throughout the empty spaces of the 2D array representing the labyrinth.
     * The mais is represented by integers with values of six.
     * @param labyrinth the current 2D array representing the labyrinth
     */
    public void placeMais(int[][] labyrinth) {
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (labyrinth[i][j] == 0) {

                    //this line determines the amount of mais inside the labyrinth
                    double chance = 0.1;
                    if (random.nextDouble() < chance) {
                        labyrinth[i][j] = 6;
                    }
                }
            }
        }
    }


    /**
     * This method allows this class to display an alert for the user/developer
     * @param title the title of the alert
     * @param message the message of the alert
     */
    private void showAlert(String title, String message) {

        //The handler makes sure to execute the method on the right thread
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    /**
     * This method retrieves the ESP steering object.
     * @return the ESP steering object
     */
    public ESPSteering getEspSteering() {
        return espSteering;
    }

    /**
     * This method retrieves the phone steering object.
     * @return the phone steering object
     */
    public PhoneSteering getPhoneSteering() {
        return phoneSteering;
    }

    /**
     * This method retrieves the labyrinth as a 2D integer array.
     * @return the labyrinth represented as a 2D integer array
     */
    public int[][] getLabyrinth() {
        return labyrinth;
    }

    /**
     * This method disconnects all clients
     */
    public void disconnectAllClients(){
        mqttManager.disconnect();
        espSteering.disconnect();
    }

    /**
     * This method retrieves the current amount of mais (corn) collected by the player
     * @return the count of Mais
     */
    public int getMaisCount() {
        return mais_count;
    }

    public void setLabyrinth(int[][] labyrinth) {
        this.labyrinth = labyrinth;
    }
}