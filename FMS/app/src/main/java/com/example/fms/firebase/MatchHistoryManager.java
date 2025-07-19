package com.example.fms.firebase;

import android.util.Log;
import com.example.fms.model.*;
import com.google.firebase.firestore.*;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.*;

/**
 * MatchHistoryManager - Quản lý lịch sử trận đấu
 * 
 * NGUYÊN TẮC LỮU TRỮ DỮ LIỆU:
 * ✅ OFFLINE: Match simulation, events, teams data, AI logic
 * ✅ ONLINE (Firebase): CHỈ dữ liệu USER - owned players, user stats, user match history
 * 
 * Manager này chỉ lưu:
 * - Kết quả trận đấu của user (tỷ số, thắng/thua/hòa)
 * - Stats cá nhân của user (shots, possession, cards, etc.)
 * - Events quan trọng của user (goals, cards, injuries)
 * - Tên đối thủ (KHÔNG lưu toàn bộ opponent data)
 */
public class MatchHistoryManager {
    private static final String TAG = "MatchHistoryManager";
    private static final String MATCH_HISTORY_COLLECTION = "match_history";
    private static final String USER_MATCHES_COLLECTION = "user_matches";
    
    private FirebaseFirestore db;
    private UserManager userManager;
    
    public MatchHistoryManager() {
        this.db = FirebaseFirestore.getInstance();
        this.userManager = new UserManager();
    }
    
    // Lưu kết quả trận đấu
    public void saveMatchResult(String userId, MatchResult matchResult, OnMatchHistoryListener listener) {
        if (userId == null || matchResult == null) {
            if (listener != null) {
                listener.onError("Dữ liệu không hợp lệ");
            }
            return;
        }
        
        // Tạo dữ liệu để lưu
        Map<String, Object> matchData = createMatchData(userId, matchResult);
        
        // Lưu vào collection match_history
        db.collection(MATCH_HISTORY_COLLECTION)
            .document(matchResult.getMatchId())
            .set(matchData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Lưu lịch sử trận đấu thành công: " + matchResult.getMatchId());
                
                // Thưởng tiền cho người chơi
                rewardUserForMatch(userId, matchResult);
                
                // Cập nhật danh sách trận đấu của user
                updateUserMatchList(userId, matchResult.getMatchId(), listener);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi lưu lịch sử trận đấu", e);
                if (listener != null) {
                    listener.onError("Lỗi lưu lịch sử: " + e.getMessage());
                }
            });
    }
    
    // Thưởng tiền cho người dùng sau trận đấu
    private void rewardUserForMatch(String userId, MatchResult matchResult) {
        int rewardAmount = 0;
        String result = getSimpleResult(matchResult);
        
        switch (result) {
            case "WIN":
                rewardAmount = 100; // Thưởng cho trận thắng
                break;
            case "DRAW":
                rewardAmount = 50;  // Thưởng cho trận hòa
                break;
            case "LOSE":
                rewardAmount = 20;  // Thưởng cho trận thua
                break;
        }
        
        if (rewardAmount > 0) {
            Log.d(TAG, "Thưởng cho người dùng " + userId + " số tiền: " + rewardAmount + " cho kết quả " + result);
            userManager.addMoney(userId, rewardAmount, new UserManager.UserDataCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    Log.d(TAG, "Cộng tiền thưởng thành công cho user " + userId);
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Lỗi khi cộng tiền thưởng cho user " + userId, e);
                }
            });
        }
    }
    
    // Cập nhật danh sách trận đấu của user
    private void updateUserMatchList(String userId, String matchId, OnMatchHistoryListener listener) {
        DocumentReference userMatchesRef = db.collection(USER_MATCHES_COLLECTION).document(userId);
        
        userMatchesRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userData = new HashMap<>();
            
            if (documentSnapshot.exists()) {
                userData = documentSnapshot.getData();
            }
            
            // Lấy danh sách matchIds hiện tại
            List<String> matchIds = (List<String>) userData.get("matchIds");
            if (matchIds == null) {
                matchIds = new ArrayList<>();
            }
            
            // Thêm matchId mới (nếu chưa có)
            if (!matchIds.contains(matchId)) {
                matchIds.add(0, matchId); // Thêm vào đầu danh sách
                
                // Giới hạn tối đa 100 trận gần nhất
                if (matchIds.size() > 100) {
                    matchIds = matchIds.subList(0, 100);
                }
            }
            
            userData.put("matchIds", matchIds);
            userData.put("lastUpdated", new Date());
            userData.put("totalMatches", matchIds.size());
            
            // Cập nhật thống kê
            updateUserStats(userData, matchIds);
            
            userMatchesRef.set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật danh sách trận đấu user thành công");
                    if (listener != null) {
                        listener.onMatchSaved(matchId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi cập nhật danh sách trận đấu user", e);
                    if (listener != null) {
                        listener.onError("Lỗi cập nhật danh sách: " + e.getMessage());
                    }
                });
        });
    }
    
    // Tải lịch sử trận đấu của user
    public void getUserMatchHistory(String userId, int limit, OnMatchHistoryLoadListener listener) {
        if (userId == null) {
            if (listener != null) {
                listener.onError("User ID không hợp lệ");
            }
            return;
        }
        
        DocumentReference userMatchesRef = db.collection(USER_MATCHES_COLLECTION).document(userId);
        
        userMatchesRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                if (listener != null) {
                    listener.onMatchHistoryLoaded(new ArrayList<>());
                }
                return;
            }
            
            List<String> matchIds = (List<String>) documentSnapshot.get("matchIds");
            if (matchIds == null || matchIds.isEmpty()) {
                if (listener != null) {
                    listener.onMatchHistoryLoaded(new ArrayList<>());
                }
                return;
            }
            
            // Giới hạn số lượng
            List<String> limitedMatchIds = matchIds.subList(0, Math.min(limit, matchIds.size()));
            
            // Tải chi tiết từng trận đấu
            loadMatchDetails(limitedMatchIds, listener);
            
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi tải danh sách trận đấu user", e);
            if (listener != null) {
                listener.onError("Lỗi tải danh sách: " + e.getMessage());
            }
        });
    }
    
    // Tải chi tiết các trận đấu
    private void loadMatchDetails(List<String> matchIds, OnMatchHistoryLoadListener listener) {
        if (matchIds.isEmpty()) {
            if (listener != null) {
                listener.onMatchHistoryLoaded(new ArrayList<>());
            }
            return;
        }
        
        // Sử dụng batch query để tải nhiều trận đấu cùng lúc
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        
        for (String matchId : matchIds) {
            Task<DocumentSnapshot> task = db.collection(MATCH_HISTORY_COLLECTION)
                .document(matchId)
                .get();
            tasks.add(task);
        }
        
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
            List<MatchResult> matchResults = new ArrayList<>();
            
            for (Object obj : objects) {
                DocumentSnapshot doc = (DocumentSnapshot) obj;
                if (doc.exists()) {
                    MatchResult matchResult = parseMatchResult(doc.getData());
                    if (matchResult != null) {
                        matchResults.add(matchResult);
                    }
                }
            }
            
            // Sắp xếp theo ngày (mới nhất trước)
            matchResults.sort((m1, m2) -> m2.getMatchDate().compareTo(m1.getMatchDate()));
            
            if (listener != null) {
                listener.onMatchHistoryLoaded(matchResults);
            }
            
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi tải chi tiết trận đấu", e);
            if (listener != null) {
                listener.onError("Lỗi tải chi tiết: " + e.getMessage());
            }
        });
    }
    
    // Tải thống kê user
    public void getUserStats(String userId, OnUserStatsListener listener) {
        DocumentReference userMatchesRef = db.collection(USER_MATCHES_COLLECTION).document(userId);
        
        userMatchesRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> stats = new HashMap<>();
            
            if (documentSnapshot.exists()) {
                stats = documentSnapshot.getData();
            }
            
            if (listener != null) {
                listener.onStatsLoaded(stats);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi tải thống kê user", e);
            if (listener != null) {
                listener.onError("Lỗi tải thống kê: " + e.getMessage());
            }
        });
    }
    
    // Xóa trận đấu khỏi lịch sử
    public void deleteMatch(String userId, String matchId, OnMatchHistoryListener listener) {
        // Xóa khỏi danh sách user
        DocumentReference userMatchesRef = db.collection(USER_MATCHES_COLLECTION).document(userId);
        
        userMatchesRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> userData = documentSnapshot.getData();
                List<String> matchIds = (List<String>) userData.get("matchIds");
                
                if (matchIds != null && matchIds.contains(matchId)) {
                    matchIds.remove(matchId);
                    userData.put("matchIds", matchIds);
                    userData.put("totalMatches", matchIds.size());
                    
                    userMatchesRef.set(userData)
                        .addOnSuccessListener(aVoid -> {
                            // Xóa chi tiết trận đấu
                            db.collection(MATCH_HISTORY_COLLECTION)
                                .document(matchId)
                                .delete()
                                .addOnSuccessListener(aVoid2 -> {
                                    if (listener != null) {
                                        listener.onMatchDeleted(matchId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (listener != null) {
                                        listener.onError("Lỗi xóa chi tiết trận đấu: " + e.getMessage());
                                    }
                                });
                        })
                        .addOnFailureListener(e -> {
                            if (listener != null) {
                                listener.onError("Lỗi cập nhật danh sách: " + e.getMessage());
                            }
                        });
                }
            }
        });
    }
    
    // Helper methods
    private Map<String, Object> createMatchData(String userId, MatchResult matchResult) {
        Map<String, Object> data = new HashMap<>();
        
        // Chỉ lưu dữ liệu USER - theo nguyên tắc offline/online
        data.put("userId", userId);
        data.put("matchId", matchResult.getMatchId());
        data.put("opponentName", matchResult.getAwayTeam().getName()); // Chỉ tên đối thủ
        data.put("userScore", matchResult.getHomeScore()); // Điểm của user
        data.put("opponentScore", matchResult.getAwayScore()); // Điểm đối thủ
        data.put("matchDate", matchResult.getMatchDate());
        data.put("isWin", matchResult.getHomeScore() > matchResult.getAwayScore());
        data.put("isDraw", matchResult.getHomeScore() == matchResult.getAwayScore());
        
        // Thống kê USER (chỉ của đội nhà - user)
        data.put("userPossession", matchResult.getHomePossession());
        data.put("userShots", matchResult.getHomeShots());
        data.put("userShotsOnTarget", matchResult.getHomeShotsOnTarget());
        data.put("userCorners", matchResult.getHomeCorners());
        data.put("userFouls", matchResult.getHomeFouls());
        data.put("userYellowCards", matchResult.getHomeYellowCards());
        data.put("userRedCards", matchResult.getHomeRedCards());
        
        // Chỉ lưu events của USER (không lưu toàn bộ simulation)
        List<Map<String, Object>> userEventsData = new ArrayList<>();
        for (MatchEvent event : matchResult.getEvents()) {
            if (event.isHomeTeam()) { // Chỉ events của user
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("minute", event.getMinute());
                eventData.put("type", event.getType().name());
                eventData.put("playerName", event.getPlayer().getPlayerName());
                
                // Chỉ lưu events quan trọng
                if (isImportantEvent(event.getType())) {
                    userEventsData.add(eventData);
                }
            }
        }
        data.put("userEvents", userEventsData);
        
        // Kết quả tóm tắt
        data.put("result", getSimpleResult(matchResult));
        
        return data;
    }
    
    private boolean isImportantEvent(MatchEvent.EventType eventType) {
        // Chỉ lưu events quan trọng của user
        switch (eventType) {
            case GOAL:
            case YELLOW_CARD:
            case RED_CARD:
            case INJURY:
                return true;
            default:
                return false;
        }
    }
    
    private String getSimpleResult(MatchResult matchResult) {
        if (matchResult.getHomeScore() > matchResult.getAwayScore()) {
            return "WIN";
        } else if (matchResult.getHomeScore() < matchResult.getAwayScore()) {
            return "LOSE";
        } else {
            return "DRAW";
        }
    }
    
    private MatchResult parseMatchResult(Map<String, Object> data) {
        if (data == null) return null;
        try {
            // Tạo MatchResult đơn giản từ dữ liệu USER đã lưu
            MatchResult result = new MatchResult();
            result.setMatchId((String) data.get("matchId"));
            result.setHomeScore(((Long) data.get("userScore")).intValue());
            result.setAwayScore(((Long) data.get("opponentScore")).intValue());
            Object matchDateObj = data.get("matchDate");
            if (matchDateObj instanceof com.google.firebase.Timestamp) {
                result.setMatchDate(((com.google.firebase.Timestamp) matchDateObj).toDate());
            } else if (matchDateObj instanceof java.util.Date) {
                result.setMatchDate((java.util.Date) matchDateObj);
            } else {
                result.setMatchDate(null);
            }
            result.setFinished(true); // Đã finished vì đã lưu
            // Tạo teams đơn giản (chỉ cần tên)
            Team userTeam = new Team();
            userTeam.setName("My Team");
            Team opponentTeam = new Team();
            opponentTeam.setName((String) data.get("opponentName"));
            result.setHomeTeam(userTeam);
            result.setAwayTeam(opponentTeam);
            // Chỉ parse stats của user (theo nguyên tắc offline/online)
            if (data.get("userPossession") != null) {
                result.updatePossession(((Long) data.get("userPossession")).intValue(), 
                                      100 - ((Long) data.get("userPossession")).intValue());
            }
            // Note: Không cần parse lại toàn bộ events vì đã offline
            // Chỉ lưu summary cho user history
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi parse MatchResult từ user data", e);
            return null;
        }
    }
    
    private void updateUserStats(Map<String, Object> userData, List<String> matchIds) {
        // Cập nhật stats USER - theo nguyên tắc chỉ lưu dữ liệu user
        userData.put("totalMatches", matchIds.size());
        
        // Stats cơ bản (sẽ được tính từ match history khi cần thiết)
        userData.put("lastPlayed", new Date());
        
        // TODO: Có thể thêm stats tổng hợp như:
        // - totalWins, totalLoses, totalDraws
        // - totalGoalsScored, totalGoalsConceded
        // - averageRating, bestWin, etc.
        // Nhưng chỉ calculate khi user request, không lưu redundant data
    }
    
    // Interfaces
    public interface OnMatchHistoryListener {
        void onMatchSaved(String matchId);
        void onMatchDeleted(String matchId);
        void onError(String error);
    }
    
    public interface OnMatchHistoryLoadListener {
        void onMatchHistoryLoaded(List<MatchResult> matches);
        void onError(String error);
    }
    
    public interface OnUserStatsListener {
        void onStatsLoaded(Map<String, Object> stats);
        void onError(String error);
    }
} 