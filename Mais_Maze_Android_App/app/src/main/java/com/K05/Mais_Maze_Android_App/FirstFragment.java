package com.K05.Mais_Maze_Android_App;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Mais_Maze_Android_App.R;
import com.example.Mais_Maze_Android_App.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;


/**
 * This class implements the Fragment framework and represents the View present when first opening the app.
 * It mainly contains navigation logic, but it also defines the leaderboard RecyclerView
 */
public class FirstFragment extends Fragment {

    public FragmentFirstBinding binding;
    public MqttManager mqttManager;
    public SettingsDatabase settingsDatabase;
    private RecyclerView recyclerView;
    public LeaderboardDatabase leaderboardDatabase;

    private MyAdapter adapter;
    private List<DatabaseRow> rowDataList;

    /**
     * This method overrides the implementation of creating the View
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * ChatGPT explanation of "inflating":
     * "Inflating" refers to the process of creating a View object from a layout XML file.
     * In the context of Android development, when we say a layout is inflated,
     * it means that the XML layout file is parsed and converted into a hierarchy of View objects that represent the user interface components specified in the XML.
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        mqttManager = new MqttManager("first_fragment");
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        this.leaderboardDatabase = LeaderboardDatabase.getInstance(requireContext());
        return binding.getRoot();

    }

    /**
     * This method implements what should happen once the View has been created
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.settingsDatabase = SettingsDatabase.getInstance(requireContext());

        EditText player_name = binding.nameTextField;
        player_name.setText(settingsDatabase.getSetting(SettingsDatabase.COLUMN_PLAYER_NAME));

        fillRecyclerView();

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            /**
             * This method overrides what should happen, when the specified Element is clicked
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                    EditText name = binding.nameTextField;
                    settingsDatabase.updateLastSetting(String.valueOf(name.getText()), SettingsDatabase.COLUMN_PLAYER_NAME);
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }


    /**
     * This method makes sure, that whenever the action_settings Element is clicked inside its MenuItem, the action_FirstFragment_to_SettingsFragment action is triggered.
     * @param item The menu item that was selected.
     * @return  True if the menu item selection was handled successfully, otherwise return False
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Get to the Settings Fragment
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(
                    R.id.action_FirstFragment_to_SettingsFragment,
                    null,
                    new NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.FirstFragment, false)
                            .build()
            );            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * This method overrides what should happen, whenever this View is destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    /**
     * This method retrieves LeaderboardEntry objects from the leaderboardDatabase, which have been sorted by score.
     * It creates DatabaseRow objects from that data and adds them to the rowDataList, which is then passed to the adapter.
     * Then it it instantiates the RecyclerView and sets the adapter to the RecyclerView
     */
    public void fillRecyclerView() {
        // Create a list of DatabaseRowSchema objects
        rowDataList = new ArrayList<>();

        // Get data from the LeaderboardDatabase and sort by score
        List<LeaderboardEntry> leaderboardEntries = leaderboardDatabase.getEntriesSortedByScore();

        // Create DatabaseRow objects from leaderboard entries and add them to the rowDataList
        for (LeaderboardEntry entry : leaderboardEntries) {
            String name = entry.getPlayerName();
            String time = entry.getTime();
            String maisCount = entry.getMaisCount();
            String score = entry.getScore();
            Log.d("leaderboard", score);

            DatabaseRow row = new DatabaseRow(name, time, maisCount, score);
            rowDataList.add(row);
        }

        // Create the adapter and pass the list of database rows
        adapter = new MyAdapter(rowDataList);

        // Instantiate the RecyclerView and set its layout manager
        recyclerView = requireView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);
    }


}