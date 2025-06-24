package com.example.fms.model;


public class Player {
    private String name;
    private String position;
    private int overall;

    public Player(String name, String position, int overall) {
        this.name = name;
        this.position = position;
        this.overall = overall;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public int getOverall() {
        return overall;
    }
}

