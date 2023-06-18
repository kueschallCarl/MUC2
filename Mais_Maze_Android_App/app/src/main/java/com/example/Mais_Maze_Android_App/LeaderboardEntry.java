package com.example.Mais_Maze_Android_App;

/**
 * This class defines a template for leaderboard entries
 */
public class LeaderboardEntry {
    private String playerName;
    private String time;
    private String score;
    private String maisCount;


    /**
     * This constructor creates a new leaderboardEntry object with all the values that should be displayed inside the RecyclerView
     * @param playerName the player's name
     * @param time the time the player required to finish the labyrinth
     * @param score the score the player received for their completion of the labyrinth. Takes the time and maisCount into account
     * @param maisCount the amount of Mais (corn) the player collected
     */
    public LeaderboardEntry(String playerName, String time, String score, String maisCount) {
        this.playerName = playerName;
        this.time = time;
        this.score = score;
        this.maisCount = maisCount;
    }

    /**
     * This method retrieves the player name associated with the leaderboard entry.
     * @return the player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * This method retrieves the time associated with the leaderboard entry.
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * This method retrieves the score associated with the leaderboard entry.
     * @return the score
     */
    public String getScore() {
        return score;
    }

    /**
     * This method retrieves the mais count associated with the leaderboard entry.
     * @return the mais count
     */
    public String getMaisCount() {
        return maisCount;
    }
}
