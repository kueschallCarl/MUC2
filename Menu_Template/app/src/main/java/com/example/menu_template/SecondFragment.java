package com.example.menu_template;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.menu_template.databinding.FragmentSecondBinding;

import java.util.Arrays;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private GameLogic gameLogic;
    private String steeringMethod;
    private SettingsDatabase settingsDatabase;
    private boolean win_condition = false;
    private ImageView labyrinthImageView;
    private View fragmentView;
    private Thread gameThread;
    private LeaderboardDatabase leaderboardDatabase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentView = view;

        SettingsFragment settingsFragment = new SettingsFragment();
        try {
            this.settingsDatabase = SettingsDatabase.getInstance(requireContext());
            steeringMethod = settingsFragment.getSteeringMethod(settingsDatabase);
            Log.d("SteeringMethod", "Method: " + steeringMethod);
        } catch (Exception e) {
            Log.d("SteeringMethod", "Issue calling the getSteeringMethod(): " + e);
        }
        this.leaderboardDatabase = LeaderboardDatabase.getInstance(requireContext());
        gameLogic = new GameLogic(requireContext(), settingsDatabase);
        startGameLoop(steeringMethod);
    }

    public void startGameLoop(String steeringMethod) {
        gameLogic.setGameRunning(true);
        gameLogic.startSensors(steeringMethod);
        gameThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                float temperature = gameLogic.getTemperature();
                int play_time = gameLogic.getPlayTime();

                updateTemperatureAndPlayTime(temperature, play_time);

                win_condition = gameLogic.gameStep(steeringMethod);
                drawLabyrinth(gameLogic.labyrinth);
                if (win_condition) {
                    gameLogic.mqttManager.publishToTopic("1",Constants.FINISHED_TOPIC);
                    gameLogic.setGameRunning(false);
                    saveScore();
                    break;
                }

                try {
                    Thread.sleep(40); // Add a 100ms delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        gameThread.start();
    }


    public void saveScore(){
        String name = settingsDatabase.getSetting("name");
        int play_time = gameLogic.getPlayTime();
        int mais_count = gameLogic.getMaisCount();

        float timeWeight = -0.5f;
        float cornWeight = 1.3f;

        float score = (play_time * timeWeight) + (mais_count * cornWeight);

        leaderboardDatabase.saveValue(name, "name");
        leaderboardDatabase.updateLastValue(String.valueOf(play_time), "time");
        leaderboardDatabase.updateLastValue(String.valueOf(mais_count), "mais_count");
        leaderboardDatabase.updateLastValue(String.valueOf(score), "score");
        showAlert("YOU WIN!",  "Time: "+ play_time + " | Mais collected: "+mais_count + " | Score: " + score);

    }

    private void updateTemperatureAndPlayTime(float temperature, int play_time) {
        requireActivity().runOnUiThread(() -> {
            if (binding != null) {
                EditText timeTextField = binding.timeTextField;
                EditText temperatureTextField = binding.temperatureTextField;

                if (timeTextField != null && temperatureTextField != null) {
                    temperatureTextField.setText(String.valueOf(temperature));
                    timeTextField.setText(String.valueOf(play_time));
                }
            }
        });
    }




    public void drawLabyrinth(int[][] labyrinth) {
        Log.d("labyrinth in draw labyrinth", Arrays.deepToString(labyrinth));
        int cellSize = 50;
        int width = labyrinth.length * cellSize;
        int height = labyrinth[0].length * cellSize;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint emptyCellPaint = new Paint();
        emptyCellPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorEmptyCell));

        Paint wallPaint = new Paint();
        wallPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorWall));

        Paint startPaint = new Paint();
        startPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorStart));

        Paint maisPaint = new Paint();
        maisPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorMais));

        Paint endPaint = new Paint();
        endPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorEnd));

        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                int cellValue = labyrinth[i][j];
                float left = i * cellSize;
                float top = j * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;

                switch (cellValue) {
                    case 0:
                        canvas.drawRect(left, top, right, bottom, emptyCellPaint);
                        break;
                    case 1:
                        canvas.drawRect(left, top, right, bottom, wallPaint);
                        break;
                    case 2:
                        canvas.drawRect(left, top, right, bottom, startPaint);
                        break;
                    case 3:
                        canvas.drawRect(left, top, right, bottom, endPaint);
                        break;
                    case 6:
                        canvas.drawRect(left, top, right, bottom, maisPaint);
                        break;
                }
            }
        }

        requireActivity().runOnUiThread(() -> {
            labyrinthImageView = fragmentView.findViewById(R.id.labyrinthImageView);
            labyrinthImageView.setImageBitmap(bitmap);
        });
    }

    private void showAlert(String title, String message) {
        requireActivity().runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        gameLogic.disconnectAllClients();
        binding = null;

        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (gameLogic != null) {
            gameLogic.stopSensors(steeringMethod);
        }
    }
}

