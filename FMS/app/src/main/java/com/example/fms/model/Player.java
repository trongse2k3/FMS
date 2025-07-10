package com.example.fms.model;

import android.view.View; // Import View
import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private String position;
    private int overall;
    private int imageResId;
    private transient View playerIconView;

    public Player(String name, String position, int overall, int imageResId) {
        this.name = name;
        this.position = position;
        this.overall = overall;
        this.imageResId = imageResId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public int getOverall() {
        return overall;
    }

    public int getImageResId() {
        return imageResId;
    }

    public View getPlayerIconView() {
        return playerIconView;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setOverall(int overall) {
        this.overall = overall;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public void setPlayerIconView(View playerIconView) {
        this.playerIconView = playerIconView;
    }
}
