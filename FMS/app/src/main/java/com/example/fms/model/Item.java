package com.example.fms.model;

import java.io.Serializable;

public class Item implements Serializable {
    public enum ItemType {
        // Healing Items
        FIRST_AID_KIT("Hộp cứu thương", "Hồi phục chấn thương nhẹ", "item_1", 100, true),
        PHYSIOTHERAPIST("Bác sĩ trị liệu", "Hồi phục chấn thương nặng", "item_2", 300, true),

        // Status Items
        LAWYER("Luật sư", "Xóa thẻ phạt, giảm án treo giò", "item_3", 500, false),

        // Boost Items
        ENERGY_DRINK("Nước tăng lực", "Tăng thể lực tạm thời", "item_4", 120, false),
        SKILL_BOOK("Sách kỹ năng", "Tăng kỹ năng tạm thời", "item_5", 250, false);


        private final String name;
        private final String description;
        private final String iconResource;
        private final int price;
        private final boolean isHealingItem;

        ItemType(String name, String description, String iconResource, int price, boolean isHealingItem) {
            this.name = name;
            this.description = description;
            this.iconResource = iconResource;
            this.price = price;
            this.isHealingItem = isHealingItem;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getIconResource() { return iconResource; }
        public int getPrice() { return price; }
        public boolean isHealingItem() { return isHealingItem; }
    }

    private String documentId;
    private ItemType type;
    private int quantity;
    private String ownerId; // ID của user sở hữu
    private long purchaseDate;
    private boolean isUsed; // Giữ lại để tương thích
    private String targetPlayerId; // Giữ lại để tương thích

    public Item() {
        this.isUsed = false;
        this.quantity = 1;
        this.purchaseDate = System.currentTimeMillis();
    }

    // Constructor cho shop
    public Item(ItemType type) {
        this();
        this.type = type;
    }
    
    public Item(ItemType type, int quantity, String ownerId) {
        this();
        this.type = type;
        this.quantity = quantity;
        this.ownerId = ownerId;
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    // Giữ lại itemId để tương thích
    public String getItemId() { return documentId; }
    public void setItemId(String documentId) { this.documentId = documentId; }

    public String getName() { return type.getName(); }
    public String getDescription() { return type.getDescription(); }
    public int getPrice() { return type.getPrice(); }

    public ItemType getType() { return type; }
    public void setType(ItemType type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public long getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(long purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }

    public String getTargetPlayerId() { return targetPlayerId; }
    public void setTargetPlayerId(String targetPlayerId) { this.targetPlayerId = targetPlayerId; }


    // Utility methods
    public boolean canUse() {
        return quantity > 0;
    }
    
    public void use() {
        if (canUse()) {
            this.quantity--;
            if (this.quantity <= 0) {
                this.isUsed = true; // Vẫn giữ logic này nếu cần
            }
        }
    }

    public String getDisplayName() {
        return type.getName() + " (còn " + quantity + ")";
    }

    @Override
    public String toString() {
        return "Item{" +
                "documentId='" + documentId + '\'' +
                ", type=" + type +
                ", quantity=" + quantity +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }
} 