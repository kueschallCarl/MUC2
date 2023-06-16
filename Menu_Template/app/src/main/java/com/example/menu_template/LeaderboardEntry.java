package com.example.menu_template;

public class LeaderboardEntry {
    private String playerName;
    private String time;
    private String score;
    private String maisCount;

    public LeaderboardEntry(String playerName, String time, String score, String maisCount) {
        this.playerName = playerName;
        this.time = time;
        this.score = score;
        this.maisCount = maisCount;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTime() {
        return time;
    }

    public String getScore() {
        return score;
    }

    public String getMaisCount() {
        return maisCount;
    }
}
