package com.example.fms.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fms.model.PlayerStatus;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class PlayerStatusManager {
    private static final String TAG = "PlayerStatusManager";
    private static final String COLLECTION_NAME = "player_status";

    private FirebaseFirestore db;

    public interface StatusCallback {
        void onStatusLoaded(PlayerStatus status);
        void onStatusNotFound();
        void onError(Exception e);
    }

    public interface StatusUpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public PlayerStatusManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Lấy trạng thái của một cầu thủ
    public void getPlayerStatus(String playerId, StatusCallback callback) {
        if (playerId == null || playerId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Player ID is required."));
            return;
        }
        DocumentReference statusRef = db.collection(COLLECTION_NAME).document(playerId);

        statusRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                PlayerStatus status = documentSnapshot.toObject(PlayerStatus.class);
                if (status != null) {
                    callback.onStatusLoaded(status);
                } else {
                    // Document exists but cannot be converted
                    callback.onError(new Exception("Failed to parse player status."));
                }
            } else {
                // Không có document, nghĩa là cầu thủ này chưa có status (bình thường)
                callback.onStatusNotFound();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting player status for player " + playerId, e);
            callback.onError(e);
        });
    }

    // Lưu hoặc cập nhật trạng thái của cầu thủ
    public void savePlayerStatus(PlayerStatus status, StatusUpdateCallback callback) {
        if (status == null || status.getPlayerId() == null || status.getPlayerId().isEmpty()) {
            callback.onError(new IllegalArgumentException("Player status and ID are required."));
            return;
        }
        DocumentReference statusRef = db.collection(COLLECTION_NAME).document(status.getPlayerId());

        statusRef.set(status, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Player status saved successfully for player " + status.getPlayerId());
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving player status for player " + status.getPlayerId(), e);
                callback.onError(e);
            });
    }
}
