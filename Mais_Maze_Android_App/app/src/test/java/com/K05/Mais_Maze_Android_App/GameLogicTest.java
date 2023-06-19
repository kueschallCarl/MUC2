package com.K05.Mais_Maze_Android_App;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class GameLogicTest {

    @Mock
    private Context mockContext;

    @Mock
    private SettingsDatabase mockSettingsDatabase;

    @Mock
    private ESPSteering mockEspSteering;

    @Mock
    private PhoneSteering mockPhoneSteering;

    private GameLogic gameLogic;


    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        gameLogic = new GameLogic(mockContext, mockSettingsDatabase);
    }

    @Test
    public void testOnMessageReceived() {
        String topic = Constants.TEMP_TOPIC;
        String message = "25.0";

        gameLogic.onMessageReceived(topic, message);

        assertEquals(25.0f, gameLogic.getTemperature(), 0.01f);
        assertEquals(1, gameLogic.getPlayTime());
    }

    @Test
    public void testParseTemperature() {
        GameLogic gameLogic = new GameLogic(mockContext, mockSettingsDatabase);

        // Test case 1: Valid temperature string
        String validTemperature = "25.5";
        gameLogic.parseTemperature(validTemperature);
        assertEquals(25.5f, gameLogic.getTemperature(), 0.01f);
        assertEquals(0, gameLogic.getPlayTime());

        // Test case 2: Valid temperature string with game running
        String validTemperatureWithGameRunning = "27.8";
        gameLogic.setGameRunning(true);
        gameLogic.parseTemperature(validTemperatureWithGameRunning);
        assertEquals(27.8f, gameLogic.getTemperature(), 0.01f);
        assertEquals(1, gameLogic.getPlayTime());

        // Test case 3: Invalid temperature string
        String invalidTemperature = "abc";
        org.junit.Assert.assertThrows(NumberFormatException.class, () -> {
            gameLogic.parseTemperature(invalidTemperature);
        });
        // Ensure temperature and play time remain unchanged
        assertEquals(27.8f, gameLogic.getTemperature(), 0.01f);
        assertEquals(1, gameLogic.getPlayTime());
    }

    @Test
    public void testGetValuesFromESPSensor() {
        float accX = 0.5f;
        float accY = 0.2f;
        float accZ = -0.3f;
        float gyroX = 0.1f;
        float gyroY = -0.4f;
        float gyroZ = 0.6f;

        when(mockEspSteering.getAccX()).thenReturn(accX);
        when(mockEspSteering.getAccY()).thenReturn(accY);
        when(mockEspSteering.getAccZ()).thenReturn(accZ);
        when(mockEspSteering.getGyroX()).thenReturn(gyroX);
        when(mockEspSteering.getGyroY()).thenReturn(gyroY);
        when(mockEspSteering.getGyroZ()).thenReturn(gyroZ);

        float[] expectedValues = {accX, accY, accZ, gyroX, gyroY, gyroZ};
        float[] actualValues = gameLogic.getValuesFromESPSensor();

        assertEquals(expectedValues, actualValues);
        verify(mockEspSteering, times(1)).getAccX();
        verify(mockEspSteering, times(1)).getAccY();
        verify(mockEspSteering, times(1)).getAccZ();
        verify(mockEspSteering, times(1)).getGyroX();
        verify(mockEspSteering, times(1)).getGyroY();
        verify(mockEspSteering, times(1)).getGyroZ();
    }

    @Test
    public void testGetValuesFromPhoneSensor() {
        float accX = 0.3f;
        float accY = -0.1f;
        float accZ = 0.2f;
        float gyroX = -0.2f;
        float gyroY = 0.5f;
        float gyroZ = -0.4f;

        when(mockPhoneSteering.getAccX()).thenReturn(accX);
        when(mockPhoneSteering.getAccY()).thenReturn(accY);
        when(mockPhoneSteering.getAccZ()).thenReturn(accZ);
        when(mockPhoneSteering.getGyroX()).thenReturn(gyroX);
        when(mockPhoneSteering.getGyroY()).thenReturn(gyroY);
        when(mockPhoneSteering.getGyroZ()).thenReturn(gyroZ);

        float[] expectedValues = {accX, accY, accZ, gyroX, gyroY, gyroZ};
        float[] actualValues = gameLogic.getValuesFromPhoneSensor();

        assertEquals(expectedValues, actualValues);
        verify(mockPhoneSteering, times(1)).getAccX();
        verify(mockPhoneSteering, times(1)).getAccY();
        verify(mockPhoneSteering, times(1)).getAccZ();
        verify(mockPhoneSteering, times(1)).getGyroX();
        verify(mockPhoneSteering, times(1)).getGyroY();
        verify(mockPhoneSteering, times(1)).getGyroZ();
    }

    @Test
    public void testParsePlayerDirection() {
        float[] sensorData = {0.5f, -0.3f, 0.1f, 0.2f, -0.4f, 0.6f};
        int expectedDirection = 2;
        int actualDirection = gameLogic.parsePlayerDirection(sensorData);
        assertEquals(expectedDirection, actualDirection);
    }

    @Test
    public void testStartSensors() {
        String steeringType = "ESP32";
        gameLogic.startSensors(steeringType);
        verify(mockEspSteering, times(1)).startSensors();
        verify(mockPhoneSteering, never()).startSensors();

        steeringType = "Phone";
        gameLogic.startSensors(steeringType);
        verify(mockPhoneSteering, times(1)).startSensors();
        verify(mockEspSteering, never()).startSensors();

        steeringType = "Unknown";
        gameLogic.startSensors(steeringType);
        verify(mockEspSteering, never()).startSensors();
        verify(mockPhoneSteering, never()).startSensors();
    }

    @Test
    public void testStopSensors() {
        String steeringType = "ESP32";
        gameLogic.stopSensors(steeringType);
        verify(mockEspSteering, times(1)).stopSensors();
        verify(mockPhoneSteering, never()).stopSensors();

        steeringType = "Phone";
        gameLogic.stopSensors(steeringType);
        verify(mockPhoneSteering, times(1)).stopSensors();
        verify(mockEspSteering, never()).stopSensors();

        steeringType = "Unknown";
        gameLogic.stopSensors(steeringType);
        verify(mockEspSteering, never()).stopSensors();
        verify(mockPhoneSteering, never()).stopSensors();
    }

    @Test
    public void testMovePlayer_PlayerNotFound() {
        int[][] labyrinth = {
                {1, 1, 1},
                {1, 0, 1},
                {1, 1, 1}
        };
        int playerDirection = 2;
        int[][] expectedLabyrinth = {
                {1, 1, 1},
                {1, 0, 1},
                {1, 1, 1}
        };
        GameLogic gameLogic = new GameLogic(mockContext, mockSettingsDatabase);
        int[][] result = gameLogic.movePlayer(labyrinth, playerDirection);
        assertArrayEquals(expectedLabyrinth, result);
    }

    @Test
    public void testMovePlayer_InvalidDirection() {
        int[][] labyrinth = {
                {1, 1, 1},
                {1, 2, 1},
                {1, 1, 1}
        };
        int playerDirection = 4;
        int[][] expectedLabyrinth = {
                {1, 1, 1},
                {1, 2, 1},
                {1, 1, 1}
        };
        GameLogic gameLogic = new GameLogic(mockContext, mockSettingsDatabase);
        int[][] result = gameLogic.movePlayer(labyrinth, playerDirection);
        assertArrayEquals(expectedLabyrinth, result);
    }

    @Test
    public void testMovePlayer_OutOfBounds() {
        int[][] labyrinth = {
                {1, 1, 1},
                {1, 2, 1},
                {1, 1, 1}
        };
        int playerDirection = 3;
        int[][] expectedLabyrinth = {
                {1, 1, 1},
                {1, 2, 1},
                {1, 1, 1}
        };
        GameLogic gameLogic = new GameLogic(mockContext, mockSettingsDatabase);
        int[][] result = gameLogic.movePlayer(labyrinth, playerDirection);
        assertArrayEquals(expectedLabyrinth, result);
    }

    @Test
    public void testMovePlayer_HitsWall() {
        int[][] labyrinth = {
                {1, 1, 1},
                {1, 2, 1},
                {1, 0, 1}
        };
        int playerDirection = 2;
        int[][] expectedLabyrinth = {
                {1, 1, 1},
                {1, 2, 1},
                {1, 0, 1}
        };
        GameLogic gameLogic = new GameLogic(mockContext, mockSettingsDatabase);
        int[][] result = gameLogic.movePlayer(labyrinth, playerDirection);
        assertArrayEquals(expectedLabyrinth, result);
    }

    @Test
    public void testMovePlayer_MaisCollected() {
        int[][] labyrinth = {
                {1, 1, 1},
                {1, 2, 6},
                {1, 0, 1}
        };
        int playerDirection = 2;
        int[][] expectedLabyrinth = {
                {1, 1, 1},
                {1, 0, 6},
                {1, 0, 1}
        };
        GameLogic gameLogic = new GameLogic(mockContext, mockSettingsDatabase);
        int[][] result = gameLogic.movePlayer(labyrinth, playerDirection);
        assertArrayEquals(expectedLabyrinth, result);
    }

    @Test
    public void testMovePlayer_Victory() {
        int[][] labyrinth = {{1, 1, 1},
        {1, 2, 3},
        {1, 0, 1}
    };
    int playerDirection = 2;
    int[][] expectedLabyrinth = {
            {1, 1, 1},
            {1, 0, 3},
            {1, 0, 1}
    };
    int[][] result = gameLogic.movePlayer(labyrinth, playerDirection);
    assertArrayEquals(expectedLabyrinth, result);
}

    @Test
    public void testMovePlayer_NormalMove() {
        int[][] labyrinth = {
                {1, 1, 1},
                {1, 2, 1},
                {1, 0, 1}
        };
        int playerDirection = 1;
        int[][] expectedLabyrinth = {
                {1, 1, 1},
                {1, 0, 1},
                {1, 0, 1}
        };
        GameLogic gameLogic = new GameLogic(mockContext, mockSettingsDatabase);
        int[][] result = gameLogic.movePlayer(labyrinth, playerDirection);
        assertArrayEquals(expectedLabyrinth, result);
    }

    @Test
    public void testIsLabyrinthEmpty_EmptyLabyrinth() {
        int[][] labyrinth = {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        GameLogic gameLogic = new GameLogic(mockContext, mockSettingsDatabase);
        boolean result = gameLogic.isLabyrinthEmpty(labyrinth);
        assertTrue(result);
    }

    @Test
    public void testIsLabyrinthEmpty_NonEmptyLabyrinth() {
        int[][] labyrinth = {
                {1, 1, 1},
                {1, 0, 1},
                {1, 1, 1}
        };
        GameLogic gameLogic = new GameLogic(mockContext, mockSettingsDatabase);
        boolean result = gameLogic.isLabyrinthEmpty(labyrinth);
        assertFalse(result);
    }

    @Test
    public void testGenerateLabyrinth_ValidLabyrinth() {
        gameLogic = new GameLogic(mockContext, mockSettingsDatabase); // Create a GameLogic instance with a size of 5
        gameLogic.generateLabyrinth();
        int[][] labyrinth = gameLogic.getLabyrinth();

        // Check if the labyrinth is not null
        assertNotNull(labyrinth);

        // Check if the labyrinth has the correct dimensions
        assertEquals(5, labyrinth.length);
        assertEquals(5, labyrinth[0].length);

        // Check if the labyrinth has a valid starting point (2) and ending point (3)
        boolean hasStartingPoint = false;
        boolean hasEndingPoint = false;
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                if (labyrinth[i][j] == 2) {
                    hasStartingPoint = true;
                } else if (labyrinth[i][j] == 3) {
                    hasEndingPoint = true;
                }
            }
        }
        assertTrue(hasStartingPoint);
        assertTrue(hasEndingPoint);

        // TODO: Add more assertions to check the labyrinth structure
    }

    @Test
    public void testHasAdjacentZeros_AdjacentZerosExist() {
        gameLogic = new GameLogic(mockContext, mockSettingsDatabase); // Create a GameLogic instance with a size of 5
        int[][] labyrinth = {
                {1, 1, 1, 1, 1},
                {1, 1, 0, 1, 1},
                {1, 0, 0, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1}
        };
        gameLogic.setLabyrinth(labyrinth);

        boolean result = gameLogic.hasAdjacentZeros(2, 2);
        assertTrue(result);
    }

    @Test
    public void testHasAdjacentZeros_AdjacentZerosDoNotExist() {
        gameLogic = new GameLogic(mockContext, mockSettingsDatabase); // Create a GameLogic instance with a size of 5
        int[][] labyrinth = {
                {1, 1, 1, 1, 1},
                {1, 1, 0, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1}
        };
        gameLogic.setLabyrinth(labyrinth);

        boolean result = gameLogic.hasAdjacentZeros(2, 2);
        assertFalse(result);
    }

    @Test
    public void testGetUnvisitedNeighbors_HasUnvisitedNeighbors() {
        gameLogic = new GameLogic(mockContext, mockSettingsDatabase); // Create a GameLogic instance with a size of 5
        int[][] labyrinth = {
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 0, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1}
        };
        gameLogic.setLabyrinth(labyrinth);

        List<int[]> unvisitedNeighbors = gameLogic.getUnvisitedNeighbors(2, 1);
        assertEquals(4, unvisitedNeighbors.size());

        // TODO: Add assertions to check the coordinates of the unvisited neighbors
    }

    @Test
    public void testGetUnvisitedNeighbors_NoUnvisitedNeighbors() {
        gameLogic = new GameLogic(mockContext, mockSettingsDatabase); // Create a GameLogic instance with a size of 5
        int[][] labyrinth = {
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1}
        };
        gameLogic.setLabyrinth(labyrinth);

        List<int[]> unvisitedNeighbors = gameLogic.getUnvisitedNeighbors(2, 2);
        assertEquals(0, unvisitedNeighbors.size());
    }

    @Test
    public void testGetRandomNumber_ValidRange() {
        gameLogic = new GameLogic(mockContext, mockSettingsDatabase); // Create a GameLogic instance with a size of 5

        int min = 1;
        int max = 10;
        int randomNumber = gameLogic.getRandomNumber(min, max);

        assertTrue(randomNumber >= min && randomNumber <= max);
    }
}
