package com.example.fms.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fms.model.Match;
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

public class MatchManager {
    private static final String TAG = "MatchManager";
    private static final String MATCHES_COLLECTION = "matches";
    
    private FirebaseFirestore firestore;
    
    public MatchManager() {
        firestore = FirebaseManager.getInstance().getFirestore();
    }
    
    public interface MatchCallback {
        void onSuccess(Match match);
        void onFailure(Exception e);
    }
    
    public interface MatchesCallback {
        void onSuccess(List<Match> matches);
        void onFailure(Exception e);
    }
    
    // Thêm trận đấu mới vào Firestore
    public void addMatch(Match match, String userId, MatchCallback callback) {
        Map<String, Object> matchData = new HashMap<>();
        matchData.put("homeTeam", match.getHomeTeam());
        matchData.put("awayTeam", match.getAwayTeam());
        matchData.put("userId", userId);
        matchData.put("timestamp", System.currentTimeMillis());
        
        firestore.collection(MATCHES_COLLECTION)
            .add(matchData)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d(TAG, "Match added with ID: " + documentReference.getId());
                    callback.onSuccess(match);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error adding match", e);
                    callback.onFailure(e);
                }
            });
    }
    
    // Lấy danh sách trận đấu của người dùng
    public void getMatchesByUserId(String userId, MatchesCallback callback) {
        firestore.collection(MATCHES_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Match> matches = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String homeTeam = document.getString("homeTeam");
                            String awayTeam = document.getString("awayTeam");
                            
                            Match match = new Match(homeTeam, awayTeam);
                            matches.add(match);
                        }
                        callback.onSuccess(matches);
                    } else {
                        Log.e(TAG, "Error getting matches", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    // Lấy tất cả các trận đấu
    public void getAllMatches(MatchesCallback callback) {
        firestore.collection(MATCHES_COLLECTION)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Match> matches = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String homeTeam = document.getString("homeTeam");
                            String awayTeam = document.getString("awayTeam");
                            
                            Match match = new Match(homeTeam, awayTeam);
                            matches.add(match);
                        }
                        callback.onSuccess(matches);
                    } else {
                        Log.e(TAG, "Error getting matches", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    // Cập nhật thông tin trận đấu
    public void updateMatch(String matchId, Map<String, Object> matchData, MatchCallback callback) {
        firestore.collection(MATCHES_COLLECTION)
            .document(matchId)
            .update(matchData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Match updated successfully");
                    
                    // Tạo đối tượng Match từ Map
                    String homeTeam = (String) matchData.get("homeTeam");
                    String awayTeam = (String) matchData.get("awayTeam");
                    
                    Match match = new Match(homeTeam, awayTeam);
                    callback.onSuccess(match);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error updating match", e);
                    callback.onFailure(e);
                }
            });
    }
    
    // Xóa trận đấu
    public void deleteMatch(String matchId, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        firestore.collection(MATCHES_COLLECTION)
            .document(matchId)
            .delete()
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener);
    }
} 