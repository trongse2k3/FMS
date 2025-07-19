package com.example.fms.simulation;

import android.util.Log;
import com.example.fms.model.*;
import com.example.fms.firebase.ItemManager;
import com.example.fms.firebase.PlayerStatusManager;
import java.util.*;

public class MatchSimulator {
    private static final String TAG = "MatchSimulator";
    private EventGenerator eventGenerator;
    private Random random;
    private Map<String, PlayerStatus> playerStatusMap;
    private ItemManager itemManager;
    private PlayerStatusManager playerStatusManager; // Thêm manager
    
    // Listener để cập nhật UI theo thời gian thực
    public interface MatchSimulationListener {
        void onMinuteUpdate(int minute);
        void onEventGenerated(MatchEvent event);
        void onMatchFinished(MatchResult result);
        void onDiceRolled(int diceValue, int minute);
        void onPlayerStatusChanged(Player player, PlayerStatus status);
    }
    
    private MatchSimulationListener listener;
    
    public MatchSimulator() {
        this.eventGenerator = new EventGenerator();
        this.random = new Random();
        this.playerStatusMap = new HashMap<>();
        this.itemManager = new ItemManager();
        this.playerStatusManager = new PlayerStatusManager(); // Khởi tạo
    }
    
    public void setListener(MatchSimulationListener listener) {
        this.listener = listener;
    }

    public MatchResult simulateMatch(Team homeTeam, Team awayTeam, 
                                   List<Player> homeSquad, List<Player> awaySquad) {
        MatchResult result = new MatchResult(homeTeam, awayTeam, homeSquad, awaySquad);
        
        // Khởi tạo hoặc tải trạng thái cầu thủ
        initializePlayerStatus(homeSquad, true); // Đội nhà là người dùng
        initializePlayerStatus(awaySquad, false); // Đội khách là AI
        
        result.setMatchStatus("PLAYING");
        
        // Mô phỏng 90 phút + thời gian bù giờ
        simulateHalf(result, 1, 45); // Hiệp 1
        
        result.setMatchStatus("HALF_TIME");
        if (listener != null) {
            listener.onMinuteUpdate(45);
        }
        
        simulateHalf(result, 46, 90); // Hiệp 2
        
        // Thời gian bù giờ (1-5 phút ngẫu nhiên)
        int extraTime = random.nextInt(5) + 1;
        simulateExtraTime(result, 91, 90 + extraTime);
        
        result.setFinished(true);
        result.setMatchStatus("FINISHED");
        
        // Cập nhật và LƯU trạng thái cầu thủ sau trận
        savePlayerStatusesAfterMatch(result.getHomeSquad());
        
        if (listener != null) {
            listener.onMatchFinished(result);
        }
        
        return result;
    }

    private void simulateHalf(MatchResult result, int startMinute, int endMinute) {
        for (int minute = startMinute; minute <= endMinute; minute++) {
            result.setCurrentMinute(minute);
            
            if (listener != null) {
                listener.onMinuteUpdate(minute);
            }
            
            // Roll xúc xắc mỗi phút
            int diceRoll = rollDice();
            if (listener != null) {
                listener.onDiceRolled(diceRoll, minute);
            }
            
            // Tạo sự kiện dựa trên xúc xắc
            List<MatchEvent> events = eventGenerator.generateRandomEvents(
                getAvailablePlayers(result.getHomeSquad()),
                getAvailablePlayers(result.getAwaySquad()),
                minute, diceRoll
            );
            
            // Thêm sự kiện đặc biệt cho phút quan trọng
            if (minute == 45 || minute == endMinute) {
                events.addAll(eventGenerator.generateSpecialMomentEvents(
                    getAvailablePlayers(result.getHomeSquad()),
                    getAvailablePlayers(result.getAwaySquad()),
                    minute
                ));
            }
            
            // Xử lý từng sự kiện
            for (MatchEvent event : events) {
                processEvent(event, result);
                result.addEvent(event);
                
                if (listener != null) {
                    listener.onEventGenerated(event);
                }
            }
            
            // Cập nhật kiểm soát bóng
            updatePossession(result);
            
            // Delay để tạo cảm giác thời gian thực (nếu cần)
            try {
                Thread.sleep(100); // 100ms per minute
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void simulateExtraTime(MatchResult result, int startMinute, int endMinute) {
        for (int minute = startMinute; minute <= endMinute; minute++) {
            result.setCurrentMinute(minute);
            
            if (listener != null) {
                listener.onMinuteUpdate(minute);
            }
            
            // Thời gian bù giờ có xác suất sự kiện cao hơn
            int diceRoll = rollDice();
            if (diceRoll <= 3) { // Tăng xác suất sự kiện trong bù giờ
                List<MatchEvent> events = eventGenerator.generateRandomEvents(
                    getAvailablePlayers(result.getHomeSquad()),
                    getAvailablePlayers(result.getAwaySquad()),
                    minute, diceRoll
                );
                
                for (MatchEvent event : events) {
                    processEvent(event, result);
                    result.addEvent(event);
                    
                    if (listener != null) {
                        listener.onEventGenerated(event);
                    }
                }
            }
            
            try {
                Thread.sleep(150); // Chậm hơn một chút trong bù giờ
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private int rollDice() {
        return random.nextInt(6) + 1; // 1-6
    }

    private void processEvent(MatchEvent event, MatchResult result) {
        Player player = event.getPlayer();
        if (player == null) return;

        String playerId = player.getDocumentId() != null ? player.getDocumentId() : String.valueOf(player.getId());
        PlayerStatus status = playerStatusMap.get(playerId);
        if (status == null) return; // Bỏ qua nếu không có status
        
        switch (event.getType()) {
            case INJURY:
                handleInjury(player, event.getDiceRoll());
                break;
                
            case YELLOW_CARD:
                status.receiveYellowCard();
                break;
                
            case RED_CARD:
                status.receiveRedCard();
                break;
                
            case GOAL:
                result.incrementShots(event.isHomeTeam(), true);
                break;
                
            case MISS:
                result.incrementShots(event.isHomeTeam(), false);
                break;
                
            case SAVE:
                result.incrementShots(!event.isHomeTeam(), false);
                break;
        }
        
        // Thông báo cho listener về sự thay đổi trạng thái
        if (listener != null) {
            listener.onPlayerStatusChanged(player, status);
        }
    }

    private void handleInjury(Player player, int diceRoll) {
        String playerId = player.getDocumentId() != null ? player.getDocumentId() : String.valueOf(player.getId());
        PlayerStatus status = playerStatusMap.get(playerId);
        
        if (status != null) {
            // Mức độ chấn thương dựa trên điểm xúc xắc
            PlayerStatus.InjuryType injuryType;
            switch (diceRoll) {
                case 1:
                    injuryType = PlayerStatus.InjuryType.CRITICAL;
                    break;
                case 2:
                    injuryType = PlayerStatus.InjuryType.SEVERE;
                    break;
                case 3:
                case 4:
                    injuryType = PlayerStatus.InjuryType.MODERATE;
                    break;
                default:
                    injuryType = PlayerStatus.InjuryType.LIGHT;
                    break;
            }
            
            status.setInjury(injuryType);
            status.setSubstituted(true); // Cầu thủ chấn thương phải rời sân
        }
    }

    private void initializePlayerStatus(List<Player> squad, boolean isUserTeam) {
        for (Player player : squad) {
            String playerId = isUserTeam ? player.getDocumentId() : String.valueOf(player.getId());
            if (playerId == null) continue;

            // Lấy trạng thái đã có của cầu thủ (nếu có)
            PlayerStatus existingStatus = player.getStatus();
            if (existingStatus != null) {
                playerStatusMap.put(playerId, existingStatus);
            } else {
                playerStatusMap.put(playerId, new PlayerStatus(playerId));
            }
        }
    }

    private void savePlayerStatusesAfterMatch(List<Player> userSquad) {
        Log.d(TAG, "Bắt đầu lưu trạng thái cầu thủ sau trận đấu.");
        for (Player player : userSquad) {
            String playerId = player.getDocumentId();
            if (playerId == null) continue;

            PlayerStatus finalStatus = playerStatusMap.get(playerId);
            if (finalStatus != null) {
                // Reset thẻ phạt và cập nhật trạng thái treo giò
                finalStatus.resetCardsAfterMatch();
                
                // Lưu vào Firebase
                playerStatusManager.savePlayerStatus(finalStatus, new PlayerStatusManager.StatusUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Đã lưu trạng thái thành công cho cầu thủ: " + playerId);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Lỗi khi lưu trạng thái cho cầu thủ: " + playerId, e);
                    }
                });
            }
        }
    }

    private List<Player> getAvailablePlayers(List<Player> squad) {
        List<Player> availablePlayers = new ArrayList<>();
        
        for (Player player : squad) {
            String playerId = player.getDocumentId() != null ? player.getDocumentId() : String.valueOf(player.getId());
            PlayerStatus status = playerStatusMap.get(playerId);
            
            if (status != null && status.isAvailable() && !status.isSubstituted()) {
                availablePlayers.add(player);
            } else if (status == null) {
                availablePlayers.add(player);
            }
        }
        
        return availablePlayers;
    }

    private void updatePossession(MatchResult result) {
        // Tính kiểm soát bóng dựa trên chỉ số overall của đội
        int homeAverage = calculateTeamAverage(result.getHomeSquad());
        int awayAverage = calculateTeamAverage(result.getAwaySquad());
        
        int totalRating = homeAverage + awayAverage;
        if (totalRating > 0) {
            int homePossession = (homeAverage * 100) / totalRating;
            
            // Thêm yếu tố ngẫu nhiên
            int randomVariation = random.nextInt(21) - 10; // -10 to +10
            homePossession = Math.max(30, Math.min(70, homePossession + randomVariation));
            
            result.updatePossession(homePossession, 100 - homePossession);
        }
    }

    private int calculateTeamAverage(List<Player> squad) {
        if (squad == null || squad.isEmpty()) return 50;
        
        int total = 0;
        int availableCount = 0;
        
        for (Player player : squad) {
            String playerId = player.getDocumentId() != null ? player.getDocumentId() : String.valueOf(player.getId());
            PlayerStatus status = playerStatusMap.get(playerId);
            
            if (status == null || (status.isAvailable() && !status.isSubstituted())) {
                total += player.getOverall();
                availableCount++;
            }
        }
        
        return availableCount > 0 ? total / availableCount : 50;
    }

    // Method để mô phỏng nhanh (không có delay)
    public MatchResult simulateMatchQuick(Team homeTeam, Team awayTeam, 
                                        List<Player> homeSquad, List<Player> awaySquad) {
        // Tương tự simulateMatch nhưng không có sleep() và listener calls
        MatchResult result = new MatchResult(homeTeam, awayTeam, homeSquad, awaySquad);
        
        initializePlayerStatus(homeSquad, true);
        initializePlayerStatus(awaySquad, false);
        
        // Mô phỏng nhanh 90 phút
        for (int minute = 1; minute <= 90; minute++) {
            result.setCurrentMinute(minute);
            
            int diceRoll = rollDice();
            List<MatchEvent> events = eventGenerator.generateRandomEvents(
                getAvailablePlayers(result.getHomeSquad()),
                getAvailablePlayers(result.getAwaySquad()),
                minute, diceRoll
            );
            
            for (MatchEvent event : events) {
                processEvent(event, result);
                result.addEvent(event);
            }
            
            updatePossession(result);
        }
        
        result.setFinished(true);
        result.setMatchStatus("FINISHED");
        savePlayerStatusesAfterMatch(homeSquad);
        
        return result;
    }
} 