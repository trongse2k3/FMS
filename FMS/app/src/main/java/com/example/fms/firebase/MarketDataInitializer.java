package com.example.fms.firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Class helper để khởi tạo dữ liệu chợ chuyển nhượng
 * Chỉ upload một lần khi chưa có dữ liệu
 */
public class MarketDataInitializer {
    private static final String TAG = "MarketDataInitializer";
    private static final String MARKET_PLAYERS_COLLECTION = "market_players";
    
    public interface InitializationCallback {
        void onComplete(boolean wasInitialized, int playerCount);
        void onFailure(Exception e);
    }
    
    /**
     * Kiểm tra và khởi tạo dữ liệu market nếu cần
     */
    public static void initializeMarketDataIfNeeded(Context context, InitializationCallback callback) {
        FirebaseFirestore firestore = FirebaseManager.getInstance().getFirestore();
        
        // Kiểm tra xem đã có dữ liệu trong market chưa
        firestore.collection(MARKET_PLAYERS_COLLECTION)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot result = task.getResult();
                    
                    if (result.isEmpty()) {
                        // Chưa có dữ liệu, upload từ assets
                        Log.d(TAG, "Market is empty, uploading players from assets...");
                        uploadMarketData(context, callback);
                    } else {
                        // Đã có dữ liệu
                        Log.d(TAG, "Market already has data, skipping initialization");
                        callback.onComplete(false, result.size());
                    }
                } else {
                    Log.e(TAG, "Error checking market data", task.getException());
                    callback.onFailure(task.getException());
                }
            });
    }
    
    /**
     * Buộc upload lại dữ liệu market (xóa hết và upload lại)
     */
    public static void forceReinitializeMarketData(Context context, InitializationCallback callback) {
        Log.d(TAG, "Force reinitializing market data...");
        clearMarketData(() -> {
            uploadMarketData(context, callback);
        }, callback::onFailure);
    }
    
    /**
     * Upload dữ liệu từ assets lên Firebase
     */
    private static void uploadMarketData(Context context, InitializationCallback callback) {
        PlayerManager playerManager = new PlayerManager();
        
        playerManager.uploadMarketPlayersFromAssets(context, new PlayerManager.UploadCallback() {
            @Override
            public void onSuccess(int count) {
                Log.d(TAG, "Successfully uploaded " + count + " players to market");
                callback.onComplete(true, count);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to upload market data", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Xóa toàn bộ dữ liệu market
     */
    private static void clearMarketData(Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        FirebaseFirestore firestore = FirebaseManager.getInstance().getFirestore();
        
        firestore.collection(MARKET_PLAYERS_COLLECTION)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot result = task.getResult();
                    
                    if (result.isEmpty()) {
                        onSuccess.run();
                        return;
                    }
                    
                    final int[] deletedCount = {0};
                    final int totalCount = result.size();
                    
                    result.forEach(document -> {
                        document.getReference().delete()
                            .addOnCompleteListener(deleteTask -> {
                                deletedCount[0]++;
                                if (deletedCount[0] == totalCount) {
                                    Log.d(TAG, "Cleared " + totalCount + " players from market");
                                    onSuccess.run();
                                }
                            });
                    });
                } else {
                    onFailure.accept(task.getException());
                }
            });
    }
    
    /**
     * Lấy số lượng cầu thủ hiện có trong market
     */
    public static void getMarketPlayerCount(PlayerManager.PlayersCallback callback) {
        PlayerManager playerManager = new PlayerManager();
        playerManager.getMarketPlayers(callback);
    }
} 