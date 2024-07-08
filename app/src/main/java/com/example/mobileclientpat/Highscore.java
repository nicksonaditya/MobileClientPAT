package com.example.mobileclientpat;

public class Highscore {
    private int rank;
    private String username;
    private int score;

    public Highscore(int rank, String username, int score) {
        this.rank = rank;
        this.username = username;
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }
}
