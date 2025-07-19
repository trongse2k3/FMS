package com.example.fms.model;

import android.view.View; // Import View
import java.io.Serializable;

public class Player implements Serializable {
    private int id;
    private String name;
    private String playerName; // Từ JSON field "player_name"
    private int jerseyNumber;
    private String position;
    private int speed;
    private int stamina;
    private int skill;
    private int overall;
    private int priceUsd;
    private int imageResId;
    private transient View playerIconView;
    private String documentId; // ID document trên Firebase
    private PlayerStatus status; // Thêm trường trạng thái

    // Constructor cũ để tương thích
    public Player(String name, String position, int overall, int imageResId) {
        this.name = name;
        this.playerName = name;
        this.position = position;
        this.overall = overall;
        this.imageResId = imageResId;
        this.status = new PlayerStatus(String.valueOf(id)); // Khởi tạo status
    }

    // Constructor mới cho dữ liệu JSON
    public Player(int id, String playerName, int jerseyNumber, String position, 
                  int speed, int stamina, int skill, int overall, int priceUsd) {
        this.id = id;
        this.playerName = playerName;
        this.name = playerName; // Để tương thích với code cũ
        this.jerseyNumber = jerseyNumber;
        this.position = position;
        this.speed = speed;
        this.stamina = stamina;
        this.skill = skill;
        this.overall = overall;
        this.priceUsd = priceUsd;
        // Tính toán imageResId dựa trên id (p_1.png, p_2.png, ...)
        this.imageResId = getImageResourceByPlayerId(id);
        this.status = new PlayerStatus(String.valueOf(id)); // Khởi tạo status
    }

    // Constructor đầy đủ
    public Player(int id, String playerName, int jerseyNumber, String position, 
                  int speed, int stamina, int skill, int overall, int priceUsd, int imageResId) {
        this.id = id;
        this.playerName = playerName;
        this.name = playerName;
        this.jerseyNumber = jerseyNumber;
        this.position = position;
        this.speed = speed;
        this.stamina = stamina;
        this.skill = skill;
        this.overall = overall;
        this.priceUsd = priceUsd;
        this.imageResId = imageResId;
        this.status = new PlayerStatus(String.valueOf(id)); // Khởi tạo status
    }

    // Method để tính toán resource ID của ảnh dựa trên player ID
    private int getImageResourceByPlayerId(int playerId) {
        try {
            // Tạo tên resource theo pattern p_1, p_2, ...
            String resourceName = "p_" + playerId;
            // Trả về 0 nếu không tìm thấy, sẽ được xử lý ở UI layer
            return 0; // Sẽ được set từ context khi cần
        } catch (Exception e) {
            return 0;
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getJerseyNumber() {
        return jerseyNumber;
    }

    public String getPosition() {
        return position;
    }

    public int getSpeed() {
        return speed;
    }

    public int getStamina() {
        return stamina;
    }

    public int getSkill() {
        return skill;
    }

    public int getOverall() {
        return overall;
    }

    public int getPriceUsd() {
        return priceUsd;
    }

    public int getImageResId() {
        return imageResId;
    }

    public View getPlayerIconView() {
        return playerIconView;
    }

    public String getDocumentId() {
        return documentId;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
        this.playerName = name; // Sync với playerName
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        this.name = playerName; // Sync với name
    }

    public void setJerseyNumber(int jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public void setOverall(int overall) {
        this.overall = overall;
    }

    public void setPriceUsd(int priceUsd) {
        this.priceUsd = priceUsd;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public void setPlayerIconView(View playerIconView) {
        this.playerIconView = playerIconView;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    // Helper method để lấy image resource từ context
    public static int getPlayerImageResource(android.content.Context context, int playerId) {
        String resourceName = "p_" + playerId;
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }

    // Helper method để format giá tiền
    public String getFormattedPrice() {
        return "$" + String.format("%,d", priceUsd);
    }

    // Helper method để lấy tên vị trí đầy đủ
    public String getFullPositionName() {
        switch (position) {
            case "FW": return "Tiền đạo";
            case "MF": return "Tiền vệ"; 
            case "DF": return "Hậu vệ";
            case "GK": return "Thủ môn";
            default: return position;
        }
    }
}
