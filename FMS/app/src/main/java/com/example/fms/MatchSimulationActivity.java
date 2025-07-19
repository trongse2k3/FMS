package com.example.fms;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.firebase.*;
import com.example.fms.model.*;
import com.example.fms.simulation.*;
import com.example.fms.adapter.MatchEventAdapter;

import java.util.*;

/**
 * MatchSimulationActivity - Demo hệ thống mô phỏng trận đấu
 * 
 * NGUYÊN TẮC: 
 * - Match simulation và events: OFFLINE (real-time processing)
 * - Chỉ lưu Firebase: User match history và stats cá nhân
 * - Teams và AI data: OFFLINE assets
 */
public class MatchSimulationActivity extends AppCompatActivity implements MatchSimulator.MatchSimulationListener {
    private static final String TAG = "MatchSimulation";
    
    // UI Components
    private TextView tvHomeTeam, tvAwayTeam, tvScore, tvMinute, tvMatchStatus;
    private TextView tvHomePossession, tvAwayPossession;
    private TextView tvDiceRoll, tvLastEvent;
    private RecyclerView rvEvents;
    private Button btnStartMatch, btnQuickSimulation;
    private ProgressBar pbMatchProgress;
    
    // Data
    private MatchSimulator matchSimulator;
    private TeamManager teamManager;
    private PlayerManager playerManager;
    private UserManager userManager;
    private MatchHistoryManager matchHistoryManager;
    
    private Team homeTeam, awayTeam;
    private List<Player> homeSquad, awaySquad;
    private MatchResult currentMatch;
    private List<MatchEvent> eventsList;
    private MatchEventAdapter eventAdapter;
    
    private Handler mainHandler;
    // Thêm biến lưu tên đội user
    private String userTeamName = "My Team";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_simulation);
        
        initViews();
        initManagers();
        initRecyclerView();
        setupListeners();
        loadTeamsAndPlayers();
    }
    
    private void initViews() {
        tvHomeTeam = findViewById(R.id.tv_home_team);
        tvAwayTeam = findViewById(R.id.tv_away_team);
        tvScore = findViewById(R.id.tv_score);
        tvMinute = findViewById(R.id.tv_minute);
        tvMatchStatus = findViewById(R.id.tv_match_status);
        tvHomePossession = findViewById(R.id.tv_home_possession);
        tvAwayPossession = findViewById(R.id.tv_away_possession);
        tvDiceRoll = findViewById(R.id.tv_dice_roll);
        tvLastEvent = findViewById(R.id.tv_last_event);
        rvEvents = findViewById(R.id.rv_events);
        btnStartMatch = findViewById(R.id.btn_start_match);
        btnQuickSimulation = findViewById(R.id.btn_quick_simulation);
        pbMatchProgress = findViewById(R.id.pb_match_progress);
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Set default values để user thấy UI ngay
        tvScore.setText("0 - 0");
        tvMinute.setText("0'");
        tvMatchStatus.setText("Chuẩn bị trận đấu...");
        tvHomePossession.setText("50%");
        tvAwayPossession.setText("50%");
        tvDiceRoll.setText("🎲 Ready");
        tvLastEvent.setText("Chờ bắt đầu trận đấu...");
    }
    
    private void initManagers() {
        matchSimulator = new MatchSimulator();
        matchSimulator.setListener(this);
        
        teamManager = new TeamManager(this);
        playerManager = new PlayerManager();
        userManager = new UserManager();
        matchHistoryManager = new MatchHistoryManager();
    }
    
    private void initRecyclerView() {
        eventsList = new ArrayList<>();
        eventAdapter = new MatchEventAdapter(eventsList);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(eventAdapter);
    }
    
    private void setupListeners() {
        btnStartMatch.setOnClickListener(v -> startRealTimeMatch());
        btnQuickSimulation.setOnClickListener(v -> startQuickSimulation());
    }
    
    private void loadTeamsAndPlayers() {
        Log.d(TAG, "Loading teams and players...");
        String userId = userManager.getCurrentUserId();

        if (userId == null) {
            Log.e(TAG, "User not logged in. Falling back to demo mode.");
            runOnUiThread(() -> {
                Toast.makeText(this, "Bạn chưa đăng nhập! Sử dụng dữ liệu demo.", Toast.LENGTH_SHORT).show();
                createDemoTeamsAndPlayers();
            });
            return;
        }

        // Bắt đầu chuỗi tải dữ liệu bất đồng bộ
        loadUserTeamData(userId);
    }

    private void loadUserTeamData(String userId) {
        userManager.getUserData(userId, new UserManager.UserDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                String teamName = (String) userData.get("teamName");
                if (teamName == null || teamName.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(MatchSimulationActivity.this, "Không tìm thấy tên đội. Sử dụng dữ liệu demo.", Toast.LENGTH_SHORT).show();
                        createDemoTeamsAndPlayers();
                    });
                    return;
                }

                // Tạo đội nhà với thông tin từ Firestore
                // TODO: Lấy thêm logoId, country, etc. nếu có trong userData
                homeTeam = new Team(1, teamName, "User's Country", R.drawable.logo_1, 80, "4-3-3");
                
                // Tải cầu thủ của user
                loadUserPlayers(userId);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load user team data. Falling back to demo.", e);
                runOnUiThread(() -> createDemoTeamsAndPlayers());
            }
        });
    }

    private void loadUserPlayers(String userId) {
        playerManager.getPlayersByUserId(userId, new PlayerManager.PlayersCallback() {
            @Override
            public void onSuccess(List<Player> userPlayers) {
                if (userPlayers == null || userPlayers.size() < 11) {
                     runOnUiThread(() -> {
                        Toast.makeText(MatchSimulationActivity.this, "Bạn không có đủ 11 cầu thủ. Sử dụng dữ liệu demo.", Toast.LENGTH_SHORT).show();
                        createDemoTeamsAndPlayers();
                    });
                    return;
                }
                homeSquad = createSquadFromPlayers(userPlayers, true);
                homeTeam.setPlayers(homeSquad);

                // Tải đội AI
                loadAiTeam();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load user players. Falling back to demo.", e);
                runOnUiThread(() -> createDemoTeamsAndPlayers());
            }
        });
    }

    private void loadAiTeam() {
        teamManager.getAllTeams(new TeamManager.TeamsCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                if (teams.isEmpty()) {
                    Log.e(TAG, "No AI teams found in assets. Falling back to demo.");
                    runOnUiThread(() -> createDemoTeamsAndPlayers());
                    return;
                }

                // Chọn một đội AI ngẫu nhiên
                awayTeam = teams.get(new Random().nextInt(teams.size()));

                // Tải danh sách cầu thủ từ assets để chọn cho đội AI
                PlayerDataLoader.loadPlayersFromAssets(MatchSimulationActivity.this, new PlayerDataLoader.PlayerDataCallback() {
                    @Override
                    public void onSuccess(List<Player> allPlayers) {
                        awaySquad = createSquadFromPlayers(allPlayers, false);
                        awayTeam.setPlayers(awaySquad);

                        // Cập nhật UI và sẵn sàng để bắt đầu
                        runOnUiThread(() -> {
                            tvHomeTeam.setText(homeTeam.getName());
                            tvAwayTeam.setText(awayTeam.getName());
                            updateMatchInfo();
                            enableMatchButtons();
                            Toast.makeText(MatchSimulationActivity.this, "✅ Đội bóng đã sẵn sàng! Bấm Start Match để bắt đầu", Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                         Log.e(TAG, "Failed to load AI players. Falling back to demo.", e);
                         runOnUiThread(() -> createDemoTeamsAndPlayers());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load AI teams. Falling back to demo.", e);
                runOnUiThread(() -> createDemoTeamsAndPlayers());
            }
        });
    }
    
    private List<Player> createSquadFromPlayers(List<Player> allPlayers, boolean isHomeTeam) {
        List<Player> squad = new ArrayList<>();
        
        // Lấy 11 cầu thủ ngẫu nhiên
        List<Player> availablePlayers = new ArrayList<>(allPlayers);
        Collections.shuffle(availablePlayers);
        
        // Lấy 1 GK, 4 DF, 3 MF, 3 FW
        int gkCount = 0, dfCount = 0, mfCount = 0, fwCount = 0;
        
        for (Player player : availablePlayers) {
            if (squad.size() >= 11) break;
            
            String position = player.getPosition();
            
            if ("GK".equals(position) && gkCount < 1) {
                squad.add(player);
                gkCount++;
            } else if ("DF".equals(position) && dfCount < 4) {
                squad.add(player);
                dfCount++;
            } else if ("MF".equals(position) && mfCount < 3) {
                squad.add(player);
                mfCount++;
            } else if ("FW".equals(position) && fwCount < 3) {
                squad.add(player);
                fwCount++;
            }
        }
        
        // Nếu chưa đủ 11 người, thêm bất kỳ cầu thủ nào
        while (squad.size() < 11 && squad.size() < availablePlayers.size()) {
            for (Player player : availablePlayers) {
                if (!squad.contains(player)) {
                    squad.add(player);
                    break;
                }
            }
        }
        
        return squad;
    }
    
    private void createDemoTeamsAndPlayers() {
        // Tạo đội nhà (đội của user)
        homeTeam = new Team(1, "My Team", "Vietnam", R.drawable.logo_1, 85, "4-3-3");
        homeSquad = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            Player player = new Player(i, "Player " + i, i, 
                getPositionForIndex(i), 70 + i, 70 + i, 70 + i, 75 + i, 1000);
            homeSquad.add(player);
        }
        homeTeam.setPlayers(homeSquad);
        
        // Tạo đội khách ngẫu nhiên
        awayTeam = new Team(2, "Opponent Team", "Unknown", R.drawable.logo_1, 78, "4-4-2");
        awaySquad = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            Player player = new Player(i + 100, "Opponent Player " + i, i + 100, 
                getPositionForIndex(i), 70 + i, 70 + i, 70 + i, 75 + i, 1000);
            awaySquad.add(player);
        }
        awayTeam.setPlayers(awaySquad);
        
        runOnUiThread(() -> {
            tvHomeTeam.setText(homeTeam.getName());
            tvAwayTeam.setText(awayTeam.getName());
            updateMatchInfo();
            enableMatchButtons();
            
            // Thông báo cho user
            Toast.makeText(this, "✅ Đội bóng đã sẵn sàng! Bấm Start Match để bắt đầu", 
                Toast.LENGTH_LONG).show();
        });
    }
    
    private String getPositionForIndex(int index) {
        if (index == 1) return "GK";
        if (index <= 4) return "DF";
        if (index <= 8) return "MF";
        return "FW";
    }
    

    
    private void enableMatchButtons() {
        Log.d(TAG, "enableMatchButtons() called");
        Log.d(TAG, "homeTeam: " + (homeTeam != null ? homeTeam.getName() : "null"));
        Log.d(TAG, "awayTeam: " + (awayTeam != null ? awayTeam.getName() : "null"));
        Log.d(TAG, "homeSquad size: " + (homeSquad != null ? homeSquad.size() : 0));
        Log.d(TAG, "awaySquad size: " + (awaySquad != null ? awaySquad.size() : 0));
        
        if (homeTeam != null && awayTeam != null && 
            homeSquad != null && awaySquad != null) {
            btnStartMatch.setEnabled(true);
            btnQuickSimulation.setEnabled(true);
            Log.d(TAG, "Buttons ENABLED!");
        } else {
            Log.w(TAG, "Buttons NOT enabled - missing data");
        }
    }
    
    private void startRealTimeMatch() {
        if (!validateSquads()) return;
        
        resetMatch();
        btnStartMatch.setEnabled(false);
        btnQuickSimulation.setEnabled(false);
        pbMatchProgress.setVisibility(ProgressBar.VISIBLE);
        
        // Chạy simulation trong background thread
        new Thread(() -> {
            currentMatch = matchSimulator.simulateMatch(homeTeam, awayTeam, homeSquad, awaySquad);
        }).start();
    }
    
    private void startQuickSimulation() {
        if (!validateSquads()) return;
        
        resetMatch();
        btnStartMatch.setEnabled(false);
        btnQuickSimulation.setEnabled(false);
        pbMatchProgress.setVisibility(ProgressBar.VISIBLE);
        
        new Thread(() -> {
            MatchResult result = matchSimulator.simulateMatchQuick(homeTeam, awayTeam, homeSquad, awaySquad);
            
            mainHandler.post(() -> {
                currentMatch = result;
                updateFinalResult();
                saveMatchResult();
            });
        }).start();
    }
    
    private boolean validateSquads() {
        if (homeSquad == null || homeSquad.size() < 11) {
            Toast.makeText(this, "Đội nhà cần đủ 11 cầu thủ!", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (awaySquad == null || awaySquad.size() < 11) {
            Toast.makeText(this, "Đội khách cần đủ 11 cầu thủ!", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void resetMatch() {
        eventsList.clear();
        eventAdapter.notifyDataSetChanged();
        
        tvScore.setText("0 - 0");
        tvMinute.setText("0'");
        tvMatchStatus.setText("Chuẩn bị");
        tvHomePossession.setText("50%");
        tvAwayPossession.setText("50%");
        tvDiceRoll.setText("--");
        tvLastEvent.setText("Trận đấu sắp bắt đầu...");
    }
    
    private void updateMatchInfo() {
        if (homeTeam != null && awayTeam != null) {
            tvHomeTeam.setText(homeTeam.getName());
            tvAwayTeam.setText(awayTeam.getName());
            tvScore.setText("0 - 0");
            tvMatchStatus.setText("Sẵn sàng");
        }
    }
    
    private void updateFinalResult() {
        if (currentMatch != null) {
            // KHÔNG add lại eventsList để tránh lặp sự kiện
            eventAdapter.notifyDataSetChanged();
            rvEvents.scrollToPosition(eventsList.size() - 1);
            // Cập nhật UI
            updateMatchDisplay();
            tvMatchStatus.setText("Kết thúc");
            pbMatchProgress.setVisibility(ProgressBar.GONE);
            btnStartMatch.setEnabled(true);
            btnQuickSimulation.setEnabled(true);
            Toast.makeText(this, currentMatch.getWinnerText(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void updateMatchDisplay() {
        if (currentMatch != null) {
            tvHomeTeam.setText(homeTeam != null ? homeTeam.getName() : userTeamName);
            tvAwayTeam.setText(awayTeam != null ? awayTeam.getName() : "Đội khách");
            tvScore.setText(currentMatch.getHomeScore() + " - " + currentMatch.getAwayScore());
            tvMinute.setText(currentMatch.getCurrentMinute() + "'");
            tvHomePossession.setText(currentMatch.getHomePossession() + "%");
            tvAwayPossession.setText(currentMatch.getAwayPossession() + "%");
        }
    }
    
    private void saveMatchResult() {
        if (currentMatch != null) {
            String userId = userManager.getCurrentUserId();
            if (userId == null) {
                Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
                return;
            }
            matchHistoryManager.saveMatchResult(userId, currentMatch,
                new MatchHistoryManager.OnMatchHistoryListener() {
                    @Override
                    public void onMatchSaved(String matchId) {
                        Toast.makeText(MatchSimulationActivity.this, "Đã lưu lịch sử trận đấu!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onMatchDeleted(String matchId) {
                        // Không dùng ở đây
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(MatchSimulationActivity.this, "Lỗi lưu lịch sử trận đấu! " + error, Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }
    
    // MatchSimulationListener implementation
    @Override
    public void onMinuteUpdate(int minute) {
        mainHandler.post(() -> {
            tvMinute.setText(minute + "'");
            pbMatchProgress.setProgress((minute * 100) / 90);
        });
    }
    
    @Override
    public void onEventGenerated(MatchEvent event) {
        mainHandler.post(() -> {
            eventsList.add(0, event); // Thêm vào đầu danh sách
            eventAdapter.notifyItemInserted(0);
            rvEvents.scrollToPosition(0);
            
            tvLastEvent.setText(event.getFormattedEvent());
            updateMatchDisplay();
        });
    }
    
    @Override
    public void onMatchFinished(MatchResult result) {
        mainHandler.post(() -> {
            currentMatch = result;
            updateFinalResult();
            saveMatchResult();
        });
    }
    
    @Override
    public void onDiceRolled(int diceValue, int minute) {
        mainHandler.post(() -> {
            tvDiceRoll.setText("🎲 " + diceValue);
        });
    }
    
    @Override
    public void onPlayerStatusChanged(Player player, PlayerStatus status) {
        mainHandler.post(() -> {
            String message = player.getPlayerName() + ": " + status.getStatusDescription();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
} 