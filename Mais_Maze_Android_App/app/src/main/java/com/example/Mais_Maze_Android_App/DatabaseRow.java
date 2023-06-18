package com.example.Mais_Maze_Android_App;

/**
 * This class provides the template for a row inside the leaderboard RecyclerView
 */
public class DatabaseRow {
    private int id;
    private String playerName;
    private String time;
    private String score;
    private String maisCount;
    private String rowData;

    public DatabaseRow(String playerName, String time, String maisCount, String score) {
        this.playerName = playerName;
        this.time = time;
        this.score = score;
        this.maisCount = maisCount;
        this.rowData = playerName + "   |   " + time + "   |   " + maisCount + "   |   " + score;
    }

    /**
     * This method returns the full String for one row inside the leaderboard RecyclerView
     * @return RecyclerView row
     */
    public String getRowData(){
        return rowData;
    }
}
