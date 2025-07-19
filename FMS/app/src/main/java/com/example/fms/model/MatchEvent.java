package com.example.fms.model;

import java.io.Serializable;

public class MatchEvent implements Serializable {
    public enum EventType {
        GOAL("Bàn thắng", "⚽"),
        ASSIST("Kiến tạo", "🅰️"),
        YELLOW_CARD("Thẻ vàng", "🟨"),
        RED_CARD("Thẻ đỏ", "🟥"),
        INJURY("Chấn thương", "🏥"),
        SUBSTITUTION("Thay người", "🔄"),
        SAVE("Cứu thua", "🥅"),
        MISS("Hỏng ăn", "❌"),
        BEAUTIFUL_PASS("Pha truyền đẹp", "✨"),
        GOOD_TACKLE("Tình huống phòng ngự đẹp", "🛡️"),
        CORNER("Phạt góc", "📐"),
        FREE_KICK("Đá phạt", "⚡"),
        OFFSIDE("Việt vị", "🚩"),
        FOUL("Phạm lỗi", "⚠️");

        private final String description;
        private final String emoji;

        EventType(String description, String emoji) {
            this.description = description;
            this.emoji = emoji;
        }

        public String getDescription() { return description; }
        public String getEmoji() { return emoji; }
    }

    private int minute;
    private EventType type;
    private Player player;
    private Player assistPlayer; // Cho kiến tạo
    private String description;
    private boolean isHomeTeam;
    private int diceRoll; // Giá trị xúc xắc gây ra sự kiện

    public MatchEvent(int minute, EventType type, Player player, boolean isHomeTeam) {
        this.minute = minute;
        this.type = type;
        this.player = player;
        this.isHomeTeam = isHomeTeam;
        this.diceRoll = -1;
    }

    public MatchEvent(int minute, EventType type, Player player, Player assistPlayer, boolean isHomeTeam) {
        this.minute = minute;
        this.type = type;
        this.player = player;
        this.assistPlayer = assistPlayer;
        this.isHomeTeam = isHomeTeam;
        this.diceRoll = -1;
    }

    // Getters and Setters
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Player getAssistPlayer() { return assistPlayer; }
    public void setAssistPlayer(Player assistPlayer) { this.assistPlayer = assistPlayer; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isHomeTeam() { return isHomeTeam; }
    public void setHomeTeam(boolean homeTeam) { isHomeTeam = homeTeam; }

    public int getDiceRoll() { return diceRoll; }
    public void setDiceRoll(int diceRoll) { this.diceRoll = diceRoll; }

    public String getFormattedEvent() {
        String teamType = isHomeTeam ? "[Đội nhà]" : "[Đội khách]";
        String base = String.format("Phút %d: %s %s %s - %s", 
            minute, type.getEmoji(), teamType, type.getDescription(), player.getPlayerName());
        
        if (assistPlayer != null) {
            base += " (Kiến tạo: " + assistPlayer.getPlayerName() + ")";
        }
        
        if (description != null && !description.isEmpty()) {
            base += " - " + description;
        }
        
        return base;
    }
} 