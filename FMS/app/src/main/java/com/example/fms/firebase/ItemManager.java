package com.example.fms.firebase;

import android.util.Log;
import com.example.fms.model.Item;
import com.example.fms.model.Player;
import com.example.fms.model.PlayerStatus;
import com.google.firebase.firestore.*;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.*;

/**
 * ItemManager - Quản lý items của user
 * 
 * NGUYÊN TẮC LƯU TRỮ:
 * ✅ ONLINE (Firebase): User's owned items, usage history
 * ✅ OFFLINE: Item definitions, effects logic
 * 
 * Chức năng:
 * - Lấy danh sách item của user
 * - Sử dụng item cho cầu thủ
 * - Mua item (tích hợp với shop)
 * - Lưu lịch sử sử dụng
 */
public class ItemManager {
    private static final String TAG = "ItemManager";
    private static final String COLLECTION_USER_ITEMS = "user_items";
    private static final String COLLECTION_ITEM_USAGE = "item_usage";
    
    private FirebaseFirestore db;
    
    public ItemManager() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Lấy danh sách item của user
     */
    public Task<List<Item>> getUserItems(String userId) {
        return db.collection(COLLECTION_USER_ITEMS)
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("isUsed", false)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<Item> items = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Item item = createItemFromDocument(document);
                                if (item != null && item.canUse()) {
                                    items.add(item);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing item: " + document.getId(), e);
                            }
                        }
                        Log.d(TAG, "Loaded " + items.size() + " items for user: " + userId);
                        return items;
                    } else {
                        Log.e(TAG, "Error getting user items", task.getException());
                        return new ArrayList<>();
                    }
                });
    }
    
    /**
     * Sử dụng item cho cầu thủ
     */
    public Task<Boolean> useItem(String userId, String itemId, String targetPlayerId) {
        return db.collection(COLLECTION_USER_ITEMS)
                .document(itemId)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Item item = createItemFromDocument(document);
                            if (item != null && item.canUse() && item.getOwnerId().equals(userId)) {
                                // Giảm số lượng item và áp dụng hiệu ứng
                                item.use();
                                return applyItemEffect(item, targetPlayerId);
                            }
                        }
                    }
                    return Tasks.forResult(false);
                });
    }
    
    /**
     * Áp dụng hiệu ứng item cho cầu thủ
     */
    private Task<Boolean> applyItemEffect(Item item, String targetPlayerId) {
        return Tasks.call(() -> {
            try {
                // Tạo PlayerStatus để áp dụng hiệu ứng
                PlayerStatus playerStatus = new PlayerStatus(targetPlayerId);
                
                switch (item.getType()) {
                    case FIRST_AID_KIT:
                    case PHYSIOTHERAPIST:
                        // Phục hồi sức khỏe - xóa chấn thương
                        playerStatus.setInjuryType(PlayerStatus.InjuryType.NONE);
                        playerStatus.setInjuryDate(null);
                        playerStatus.setRecoveryDate(null);
                        Log.d(TAG, "Applied HEALING for player: " + targetPlayerId);
                        break;
                        
                    case LAWYER:
                        // Xóa thẻ vàng/đỏ
                        playerStatus.setCardStatus(PlayerStatus.CardStatus.NONE);
                        playerStatus.setYellowCardCount(0);
                        playerStatus.setMatchesSupended(0);
                        Log.d(TAG, "Applied LAWYER for player: " + targetPlayerId);
                        break;
                        
                    case ENERGY_DRINK:
                        // Tăng thể lực
                        savePlayerBoost(targetPlayerId, "stamina", 5); // 5 ngày
                        Log.d(TAG, "Applied STAMINA_BOOST for player: " + targetPlayerId);
                        break;
                    
                    case SKILL_BOOK:
                         // Tăng kỹ năng tạm thời
                        savePlayerBoost(targetPlayerId, "skill", 3); // 3 ngày
                        Log.d(TAG, "Applied SKILL_ENHANCER for player: " + targetPlayerId);
                        break;
                }
                
                // Cập nhật trạng thái cầu thủ
                updatePlayerStatus(targetPlayerId, playerStatus);
                
                // Cập nhật trạng thái item đã sử dụng (giảm số lượng)
                updateItemUsage(item);
                
                // Lưu lịch sử sử dụng
                saveItemUsageHistory(item, targetPlayerId);
                
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error applying item effect", e);
                return false;
            }
        });
    }
    
    /**
     * Lưu trạng thái boost của cầu thủ
     */
    private void savePlayerBoost(String playerId, String boostType, int durationDays) {
        Map<String, Object> boost = new HashMap<>();
        boost.put("playerId", playerId);
        boost.put("boostType", boostType);
        boost.put("startDate", System.currentTimeMillis());
        boost.put("endDate", System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000L));
        boost.put("isActive", true);
        
        db.collection("player_boosts")
                .add(boost)
                .addOnSuccessListener(documentReference -> 
                    Log.d(TAG, "Boost saved: " + documentReference.getId()))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error saving boost", e));
    }
    
    /**
     * Cập nhật trạng thái cầu thủ
     */
    private void updatePlayerStatus(String playerId, PlayerStatus status) {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("playerId", playerId);
        statusData.put("injuryType", status.getInjuryType().name());
        statusData.put("cardStatus", status.getCardStatus().name());
        statusData.put("yellowCardCount", status.getYellowCardCount());
        statusData.put("matchesSupended", status.getMatchesSupended());
        statusData.put("isAvailable", status.isAvailable());
        statusData.put("lastUpdated", System.currentTimeMillis());
        
        db.collection("player_status")
                .document(playerId)
                .set(statusData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> 
                    Log.d(TAG, "Player status updated: " + playerId))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error updating player status", e));
    }
    
    /**
     * Cập nhật trạng thái item đã sử dụng
     */
    private void updateItemUsage(Item item) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", item.getQuantity());
        
        // Nếu quantity = 0 thì đánh dấu là đã sử dụng
        if (item.getQuantity() <= 0) {
            updates.put("isUsed", true);
        }
        
        updates.put("lastUsed", System.currentTimeMillis());
        
        db.collection(COLLECTION_USER_ITEMS)
                .document(item.getItemId())
                .update(updates)
                .addOnSuccessListener(aVoid -> 
                    Log.d(TAG, "Item usage updated: " + item.getItemId() + ", remaining: " + item.getQuantity()))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error updating item usage", e));
    }
    
    /**
     * Lưu lịch sử sử dụng item
     */
    private void saveItemUsageHistory(Item item, String targetPlayerId) {
        Map<String, Object> history = new HashMap<>();
        history.put("userId", item.getOwnerId());
        history.put("itemId", item.getItemId());
        history.put("itemType", item.getType().name());
        history.put("targetPlayerId", targetPlayerId);
        history.put("usageDate", System.currentTimeMillis());
        
        db.collection(COLLECTION_ITEM_USAGE)
                .add(history)
                .addOnSuccessListener(documentReference -> 
                    Log.d(TAG, "Usage history saved: " + documentReference.getId()))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error saving usage history", e));
    }
    
    /**
     * Thêm item cho user (khi mua hoặc tặng)
     */
    public Task<Boolean> addItemToUser(String userId, Item.ItemType itemType, int quantity) {
        // Kiểm tra xem user đã có item cùng loại chưa
        return db.collection(COLLECTION_USER_ITEMS)
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("itemType", itemType.name())
                .whereEqualTo("isUsed", false)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // User đã có item cùng loại, cập nhật số lượng
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            int currentQuantity = document.getLong("quantity").intValue();
                            int newQuantity = currentQuantity + quantity;
                            
                            return db.collection(COLLECTION_USER_ITEMS)
                                    .document(document.getId())
                                    .update("quantity", newQuantity)
                                    .continueWith(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Log.d(TAG, "Item quantity updated for user: " + userId + ", type: " + itemType + ", new quantity: " + newQuantity);
                                            return true;
                                        } else {
                                            Log.e(TAG, "Error updating item quantity", updateTask.getException());
                                            return false;
                                        }
                                    });
                        } else {
                            // User chưa có item loại này, tạo mới
                            Item item = new Item(
                                itemType,
                                quantity,
                                userId
                            );
                            item.setItemId(UUID.randomUUID().toString());
                            
                            Map<String, Object> itemData = createItemDocument(item);
                            
                            return db.collection(COLLECTION_USER_ITEMS)
                                    .document(item.getItemId())
                                    .set(itemData)
                                    .continueWith(setTask -> {
                                        if (setTask.isSuccessful()) {
                                            Log.d(TAG, "New item added to user: " + userId + ", type: " + itemType);
                                            return true;
                                        } else {
                                            Log.e(TAG, "Error adding new item to user", setTask.getException());
                                            return false;
                                        }
                                    });
                        }
                    } else {
                        Log.e(TAG, "Error checking existing items", task.getException());
                        return Tasks.forResult(false);
                    }
                });
    }
    
    /**
     * Kiểm tra boost của cầu thủ
     */
    public Task<List<Map<String, Object>>> getPlayerBoosts(String playerId) {
        return db.collection("player_boosts")
                .whereEqualTo("playerId", playerId)
                .whereEqualTo("isActive", true)
                .whereGreaterThan("endDate", System.currentTimeMillis())
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> boosts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            boosts.add(document.getData());
                        }
                        return boosts;
                    } else {
                        Log.e(TAG, "Error getting player boosts", task.getException());
                        return new ArrayList<>();
                    }
                });
    }
    
    // Helper methods
    private Item createItemFromDocument(DocumentSnapshot document) {
        try {
            Item item = new Item();
            item.setItemId(document.getId());
            item.setType(Item.ItemType.valueOf(document.getString("itemType")));
            item.setQuantity(document.getLong("quantity").intValue());
            item.setOwnerId(document.getString("ownerId"));
            item.setPurchaseDate(document.getLong("purchaseDate"));
            item.setUsed(document.getBoolean("isUsed"));
            item.setTargetPlayerId(document.getString("targetPlayerId"));
            return item;
        } catch (Exception e) {
            Log.e(TAG, "Error creating item from document", e);
            return null;
        }
    }
    
    private Map<String, Object> createItemDocument(Item item) {
        Map<String, Object> data = new HashMap<>();
        data.put("itemType", item.getType().name());
        data.put("quantity", item.getQuantity());
        data.put("ownerId", item.getOwnerId());
        data.put("purchaseDate", item.getPurchaseDate());
        data.put("isUsed", item.isUsed());
        data.put("targetPlayerId", item.getTargetPlayerId());
        return data;
    }
} 