package org.example.chess.models;

public class GameHistory {
    private final String gameName;
    private final String result;
    private final String timePlayed;
    private final String color;

    public GameHistory(String gameName, String result, String timePlayed, String color) {
        this.gameName = gameName;
        this.result = result;
        this.timePlayed = timePlayed;
        this.color = color;
    }

    public String getGameName() { return gameName; }
    public String getResult() { return result; }
    public String getTimePlayed() { return timePlayed; }
    public String getColor() { return color; }
}
