package com.example.fms.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fms.model.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private static final String TAG = "PlayerManager";
    private static final String PLAYERS_COLLECTION = "players"; // Cầu thủ thuộc về user
    private static final String MARKET_PLAYERS_COLLECTION = "market_players"; // Cầu thủ trong chợ chuyển nhượng
    
    private FirebaseFirestore firestore;
    
    public PlayerManager() {
        firestore = FirebaseManager.getInstance().getFirestore();
    }
    
    public interface PlayerCallback {
        void onSuccess(Player player);
        void onFailure(Exception e);
    }
    
    public interface PlayersCallback {
        void onSuccess(List<Player> players);
        void onFailure(Exception e);
    }

    public interface UploadCallback {
        void onSuccess(int count);
        void onFailure(Exception e);
    }
    
    // Interface cho Activity
    public interface OnPlayersLoadedListener {
        void onPlayersLoaded(List<Player> players);
        void onError(String error);
    }
    
    // Thêm cầu thủ mới vào team của user
    public void addPlayer(Player player, String userId, PlayerCallback callback) {
        Map<String, Object> playerData = createPlayerDataMap(player);
        playerData.put("userId", userId);
        
        firestore.collection(PLAYERS_COLLECTION)
            .add(playerData)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d(TAG, "Player added with ID: " + documentReference.getId());
                    player.setDocumentId(documentReference.getId());
                    callback.onSuccess(player);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error adding player", e);
                    callback.onFailure(e);
                }
            });
    }
    
    // Lấy danh sách cầu thủ của người dùng
    public void getPlayersByUserId(String userId, PlayersCallback callback) {
        firestore.collection(PLAYERS_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Player> players = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Player player = createPlayerFromDocument(document);
                            if (player != null) {
                                players.add(player);
                            }
                        }
                        callback.onSuccess(players);
                    } else {
                        Log.e(TAG, "Error getting players", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    // Lấy danh sách cầu thủ cho Activity (với OnPlayersLoadedListener)
    public void getPlayersByUserId(String userId, OnPlayersLoadedListener listener) {
        firestore.collection(PLAYERS_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Player> players = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Player player = createPlayerFromDocument(document);
                            if (player != null) {
                                players.add(player);
                            }
                        }
                        if (listener != null) {
                            listener.onPlayersLoaded(players);
                        }
                    } else {
                        Log.e(TAG, "Error getting players", task.getException());
                        if (listener != null) {
                            listener.onError(task.getException().getMessage());
                        }
                    }
                }
            });
    }
    
    // Lấy tất cả các cầu thủ
    public void getAllPlayers(PlayersCallback callback) {
        firestore.collection(PLAYERS_COLLECTION)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Player> players = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Player player = createPlayerFromDocument(document);
                            if (player != null) {
                                players.add(player);
                            }
                        }
                        callback.onSuccess(players);
                    } else {
                        Log.e(TAG, "Error getting players", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
    }

    // Lấy danh sách cầu thủ trong chợ chuyển nhượng
    public void getMarketPlayers(PlayersCallback callback) {
        firestore.collection(MARKET_PLAYERS_COLLECTION)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Player> players = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Player player = createPlayerFromDocument(document);
                            if (player != null) {
                                players.add(player);
                            }
                        }
                        Log.d(TAG, "Loaded " + players.size() + " market players");
                        callback.onSuccess(players);
                    } else {
                        Log.e(TAG, "Error getting market players", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
    }

    // Lấy cầu thủ theo vị trí trong chợ chuyển nhượng
    public void getMarketPlayersByPosition(String position, PlayersCallback callback) {
        firestore.collection(MARKET_PLAYERS_COLLECTION)
            .whereEqualTo("position", position)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Player> players = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Player player = createPlayerFromDocument(document);
                            if (player != null) {
                                players.add(player);
                            }
                        }
                        callback.onSuccess(players);
                    } else {
                        Log.e(TAG, "Error getting market players by position", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
    }

    // Upload dữ liệu cầu thủ từ JSON lên chợ chuyển nhượng
    public void uploadMarketPlayersFromAssets(Context context, UploadCallback callback) {
        PlayerDataLoader.loadPlayersFromAssets(context, new PlayerDataLoader.PlayerDataCallback() {
            @Override
            public void onSuccess(List<Player> players) {
                uploadPlayersToMarket(players, callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // Upload danh sách cầu thủ lên chợ chuyển nhượng
    private void uploadPlayersToMarket(List<Player> players, UploadCallback callback) {
        if (players.isEmpty()) {
            callback.onSuccess(0);
            return;
        }

        final int[] successCount = {0};
        final int[] failureCount = {0};
        final int totalPlayers = players.size();

        for (Player player : players) {
            Map<String, Object> playerData = createPlayerDataMap(player);
            
            firestore.collection(MARKET_PLAYERS_COLLECTION)
                .add(playerData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        successCount[0]++;
                        checkUploadComplete(successCount[0], failureCount[0], totalPlayers, callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        failureCount[0]++;
                        Log.e(TAG, "Error uploading player", e);
                        checkUploadComplete(successCount[0], failureCount[0], totalPlayers, callback);
                    }
                });
        }
    }

    private void checkUploadComplete(int successCount, int failureCount, int totalPlayers, UploadCallback callback) {
        if (successCount + failureCount == totalPlayers) {
            Log.d(TAG, "Upload complete: " + successCount + " success, " + failureCount + " failures");
            if (successCount > 0) {
                callback.onSuccess(successCount);
            } else {
                callback.onFailure(new Exception("No players uploaded successfully"));
            }
        }
    }

    // Mua cầu thủ từ chợ chuyển nhượng
    public void buyPlayer(Player player, String userId, PlayerCallback callback) {
        // Thêm cầu thủ vào team của user
        addPlayer(player, userId, new PlayerCallback() {
            @Override
            public void onSuccess(Player addedPlayer) {
                // Xóa cầu thủ khỏi chợ chuyển nhượng nếu có documentId
                if (player.getDocumentId() != null) {
                    removePlayerFromMarket(player.getDocumentId());
                }
                callback.onSuccess(addedPlayer);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // Xóa cầu thủ khỏi chợ chuyển nhượng
    private void removePlayerFromMarket(String documentId) {
        firestore.collection(MARKET_PLAYERS_COLLECTION)
            .document(documentId)
            .delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Player removed from market successfully");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error removing player from market", e);
                }
            });
    }
    
    // Cập nhật thông tin cầu thủ
    public void updatePlayer(String playerId, Map<String, Object> playerData, PlayerCallback callback) {
        firestore.collection(PLAYERS_COLLECTION)
            .document(playerId)
            .update(playerData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Player updated successfully");
                    
                    // Tạo đối tượng Player từ Map (simplified version)
                    String name = (String) playerData.get("playerName");
                    String position = (String) playerData.get("position");
                    int overall = playerData.get("overall") != null ? 
                        ((Long) playerData.get("overall")).intValue() : 0;
                    int imageResId = playerData.get("imageResId") != null ? 
                        ((Long) playerData.get("imageResId")).intValue() : 0;
                    
                    Player player = new Player(name, position, overall, imageResId);
                    player.setDocumentId(playerId);
                    callback.onSuccess(player);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error updating player", e);
                    callback.onFailure(e);
                }
            });
    }
    
    // Xóa cầu thủ
    public void deletePlayer(String playerId, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        firestore.collection(PLAYERS_COLLECTION)
            .document(playerId)
            .delete()
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener);
    }

    // Helper method: Tạo Map dữ liệu từ Player object
    private Map<String, Object> createPlayerDataMap(Player player) {
        Map<String, Object> playerData = new HashMap<>();
        playerData.put("id", player.getId());
        playerData.put("playerName", player.getPlayerName());
        playerData.put("name", player.getName()); // Để tương thích
        playerData.put("jerseyNumber", player.getJerseyNumber());
        playerData.put("position", player.getPosition());
        playerData.put("speed", player.getSpeed());
        playerData.put("stamina", player.getStamina());
        playerData.put("skill", player.getSkill());
        playerData.put("overall", player.getOverall());
        playerData.put("priceUsd", player.getPriceUsd());
        playerData.put("imageResId", player.getImageResId());
        return playerData;
    }

    // Helper method: Tạo Player object từ Firestore document
    private Player createPlayerFromDocument(QueryDocumentSnapshot document) {
        try {
            // Xử lý dữ liệu mới (có đầy đủ thông tin)
            if (document.contains("id") && document.contains("playerName")) {
                int id = document.getLong("id").intValue();
                String playerName = document.getString("playerName");
                int jerseyNumber = document.contains("jerseyNumber") ? 
                    document.getLong("jerseyNumber").intValue() : 0;
                String position = document.getString("position");
                int speed = document.contains("speed") ? 
                    document.getLong("speed").intValue() : 0;
                int stamina = document.contains("stamina") ? 
                    document.getLong("stamina").intValue() : 0;
                int skill = document.contains("skill") ? 
                    document.getLong("skill").intValue() : 0;
                int overall = document.getLong("overall").intValue();
                int priceUsd = document.contains("priceUsd") ? 
                    document.getLong("priceUsd").intValue() : 0;
                int imageResId = document.getLong("imageResId").intValue();

                Player player = new Player(id, playerName, jerseyNumber, position, 
                                         speed, stamina, skill, overall, priceUsd, imageResId);
                player.setDocumentId(document.getId());
                return player;
            }
            // Xử lý dữ liệu cũ (chỉ có name, position, overall, imageResId)
            else {
                String name = document.getString("name");
                String position = document.getString("position");
                int overall = document.getLong("overall").intValue();
                int imageResId = document.getLong("imageResId").intValue();
                
                Player player = new Player(name, position, overall, imageResId);
                player.setDocumentId(document.getId());
                return player;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating player from document", e);
            return null;
        }
    }
} 