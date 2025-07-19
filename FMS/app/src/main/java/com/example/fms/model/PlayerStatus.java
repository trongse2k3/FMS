package com.example.fms.model;

import java.io.Serializable;
import java.util.Date;

public class PlayerStatus implements Serializable {
    public PlayerStatus() {
    // Nên khởi tạo các giá trị mặc định nếu cần
    this.injuryType = InjuryType.NONE;
    this.cardStatus = CardStatus.NONE;
    this.yellowCardCount = 0;
    this.isAvailable = true;
    this.isSubstituted = false;
    this.matchesSupended = 0;
}
    public enum InjuryType {
        NONE("Không chấn thương", 0),
        LIGHT("Chấn thương nhẹ", 1),
        MODERATE("Chấn thương trung bình", 3),
        SEVERE("Chấn thương nặng", 7),
        CRITICAL("Chấn thương nghiêm trọng", 14);

        private final String description;
        private final int recoveryDays;

        InjuryType(String description, int recoveryDays) {
            this.description = description;
            this.recoveryDays = recoveryDays;
        }

        public String getDescription() { return description; }
        public int getRecoveryDays() { return recoveryDays; }
    }

    public enum CardStatus {
        NONE("Không thẻ phạt"),
        YELLOW("Thẻ vàng - Cảnh cáo"),
        DOUBLE_YELLOW("2 thẻ vàng - Bị đuổi"),
        RED("Thẻ đỏ - Bị đuổi");

        private final String description;

        CardStatus(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    private String playerId;
    private InjuryType injuryType;
    private Date injuryDate;
    private Date recoveryDate;
    private CardStatus cardStatus;
    private int yellowCardCount;
    private boolean isAvailable; // Có thể thi đấu không
    private boolean isSubstituted; // Đã bị thay trong trận không
    private int matchesSupended; // Số trận bị cấm thi đấu

    public PlayerStatus(String playerId) {
        this.playerId = playerId;
        this.injuryType = InjuryType.NONE;
        this.cardStatus = CardStatus.NONE;
        this.yellowCardCount = 0;
        this.isAvailable = true;
        this.isSubstituted = false;
        this.matchesSupended = 0;
    }

    // Phương thức gây chấn thương
    public void setInjury(InjuryType injury) {
        this.injuryType = injury;
        this.injuryDate = new Date();
        if (injury != InjuryType.NONE) {
            // Tính ngày hồi phục
            long recoveryTime = injury.getRecoveryDays() * 24 * 60 * 60 * 1000L;
            this.recoveryDate = new Date(injuryDate.getTime() + recoveryTime);
            this.isAvailable = false;
        } else {
            this.recoveryDate = null;
            this.isAvailable = true;
        }
    }

    // Phương thức nhận thẻ phạt
    public void receiveYellowCard() {
        this.yellowCardCount++;
        if (yellowCardCount >= 2) {
            this.cardStatus = CardStatus.DOUBLE_YELLOW;
            this.isAvailable = false;
            this.matchesSupended = 1;
        } else {
            this.cardStatus = CardStatus.YELLOW;
        }
    }

    public void receiveRedCard() {
        this.cardStatus = CardStatus.RED;
        this.isAvailable = false;
        this.matchesSupended = 1; // Có thể tăng tùy thuộc mức độ nghiêm trọng
    }

    // Phương thức hồi phục
    public void recover() {
        this.injuryType = InjuryType.NONE;
        this.injuryDate = null;
        this.recoveryDate = null;
        checkAvailability();
    }

    // Phương thức xóa án treo giò
    public void clearSuspension() {
        this.matchesSupended = 0;
        if (this.cardStatus == CardStatus.DOUBLE_YELLOW || this.cardStatus == CardStatus.RED) {
            this.cardStatus = CardStatus.NONE;
        }
        checkAvailability();
    }

    // Phương thức reset thẻ phạt sau trận
    public void resetCardsAfterMatch() {
        if (cardStatus == CardStatus.YELLOW) {
            this.cardStatus = CardStatus.NONE;
            this.yellowCardCount = 0;
        } else if (cardStatus == CardStatus.DOUBLE_YELLOW || cardStatus == CardStatus.RED) {
            if (matchesSupended > 0) {
                matchesSupended--;
                if (matchesSupended == 0) {
                    this.cardStatus = CardStatus.NONE;
                    this.yellowCardCount = 0;
                }
            }
        }
        checkAvailability();
    }

    // Kiểm tra xem cầu thủ có thể thi đấu không
    private void checkAvailability() {
        if (injuryType != InjuryType.NONE) {
            isAvailable = false;
            return;
        }
        
        if (cardStatus == CardStatus.DOUBLE_YELLOW || cardStatus == CardStatus.RED) {
            if (matchesSupended > 0) {
                isAvailable = false;
                return;
            }
        }
        
        isAvailable = true;
    }

    // Kiểm tra xem có hồi phục từ chấn thương chưa
    public boolean isRecoveredFromInjury() {
        if (injuryType == InjuryType.NONE) return true;
        if (recoveryDate == null) return false;
        return new Date().after(recoveryDate);
    }

    // Phương thức tiện ích mới
    public boolean isFit() {
        return isAvailable;
    }

    public boolean isSuspended() {
        return matchesSupended > 0;
    }

    public String getShortDescription() {
        if (isSuspended()) {
            return "Treo giò: " + matchesSupended + " trận";
        }
        if (injuryType != InjuryType.NONE) {
            return "Chấn thương: " + injuryType.getRecoveryDays() + " ngày";
        }
        return "Sẵn sàng";
    }

    // Getters and Setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public InjuryType getInjuryType() { return injuryType; }
    public void setInjuryType(InjuryType injuryType) { this.injuryType = injuryType; }

    public Date getInjuryDate() { return injuryDate; }
    public void setInjuryDate(Date injuryDate) { this.injuryDate = injuryDate; }

    public Date getRecoveryDate() { return recoveryDate; }
    public void setRecoveryDate(Date recoveryDate) { this.recoveryDate = recoveryDate; }

    public CardStatus getCardStatus() { return cardStatus; }
    public void setCardStatus(CardStatus cardStatus) { this.cardStatus = cardStatus; }

    public int getYellowCardCount() { return yellowCardCount; }
    public void setYellowCardCount(int yellowCardCount) { this.yellowCardCount = yellowCardCount; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isSubstituted() { return isSubstituted; }
    public void setSubstituted(boolean substituted) { isSubstituted = substituted; }

    public int getMatchesSupended() { return matchesSupended; }
    public void setMatchesSupended(int matchesSupended) { this.matchesSupended = matchesSupended; }

    public String getStatusDescription() {
        if (!isAvailable) {
            if (injuryType != InjuryType.NONE) {
                return injuryType.getDescription();
            }
            if (cardStatus == CardStatus.DOUBLE_YELLOW || cardStatus == CardStatus.RED) {
                return cardStatus.getDescription() + " (" + matchesSupended + " trận)";
            }
        }
        if (cardStatus == CardStatus.YELLOW) {
            return cardStatus.getDescription();
        }
        return "Sẵn sàng thi đấu";
    }
} 