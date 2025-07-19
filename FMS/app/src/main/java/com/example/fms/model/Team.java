package com.example.fms.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private int id;
    private String name;
    private String country;
    private int logoResId; // Resource ID cho logo (logo_1.png, logo_2.png, etc.)
    private int rating; // Điểm số tổng thể của đội
    private String formation; // Sơ đồ chiến thuật (4-4-2, 4-3-3, etc.)
    private List<Player> players; // Danh sách cầu thủ trong đội
    private String documentId; // ID trong Firestore (nếu cần)

    // Constructor mặc định
    public Team() {
        this.players = new ArrayList<>();
    }

    // Constructor đầy đủ
    public Team(int id, String name, String country, int logoResId, int rating, String formation) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.logoResId = logoResId;
        this.rating = rating;
        this.formation = formation;
        this.players = new ArrayList<>();
    }

    // Constructor với documentId
    public Team(int id, String name, String country, int logoResId, int rating, String formation, String documentId) {
        this(id, name, country, logoResId, rating, formation);
        this.documentId = documentId;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getLogoResId() {
        return logoResId;
    }

    public void setLogoResId(int logoResId) {
        this.logoResId = logoResId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getFormation() {
        return formation;
    }

    public void setFormation(String formation) {
        this.formation = formation;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Phương thức tiện ích
    public void addPlayer(Player player) {
        if (this.players == null) {
            this.players = new ArrayList<>();
        }
        this.players.add(player);
    }

    public void removePlayer(Player player) {
        if (this.players != null) {
            this.players.remove(player);
        }
    }

    public int getPlayerCount() {
        return players != null ? players.size() : 0;
    }

    public int getAverageRating() {
        if (players == null || players.isEmpty()) {
            return rating;
        }
        
        int totalRating = 0;
        for (Player player : players) {
            totalRating += player.getOverall();
        }
        return totalRating / players.size();
    }

    public String getFormattedRating() {
        return "⭐ " + rating;
    }

    // Override toString để debug dễ dàng
    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", rating=" + rating +
                ", formation='" + formation + '\'' +
                ", playerCount=" + getPlayerCount() +
                '}';
    }
} 