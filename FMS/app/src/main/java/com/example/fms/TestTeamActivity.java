package com.example.fms;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fms.firebase.TeamManager;
import com.example.fms.model.Team;

import java.util.List;

public class TestTeamActivity extends AppCompatActivity {
    private static final String TAG = "TestTeamActivity";
    
    private TeamManager teamManager;
    private TextView tvStatus;
    private Button btnGenerateTeams;
    private Button btnLoadTeams;
    private Button btnClearTeams;
    private Button btnGetRandomTeam;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_team);
        
        // Khởi tạo TeamManager với Context
        teamManager = new TeamManager(this);
        
        // Ánh xạ views
        tvStatus = findViewById(R.id.tv_status);
        btnGenerateTeams = findViewById(R.id.btn_generate_teams);
        btnLoadTeams = findViewById(R.id.btn_load_teams);
        btnClearTeams = findViewById(R.id.btn_clear_teams);
        btnGetRandomTeam = findViewById(R.id.btn_get_random_team);
        
        // Thiết lập sự kiện
        setupClickListeners();
        
        // Hiển thị trạng thái ban đầu
        updateStatus("Sẵn sàng để test đội bóng offline");
    }
    
    private void setupClickListeners() {
        btnGenerateTeams.setOnClickListener(v -> loadTeams());
        btnLoadTeams.setOnClickListener(v -> loadTeams());
        btnClearTeams.setOnClickListener(v -> testFilteringTeams());
        btnGetRandomTeam.setOnClickListener(v -> getRandomTeam());
    }
    
    private void testFilteringTeams() {
        updateStatus("🔍 Test lọc đội bóng theo quốc gia...");
        
        teamManager.getTeamsByCountry("England", new TeamManager.TeamsCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                runOnUiThread(() -> {
                    StringBuilder status = new StringBuilder("🏴󠁧󠁢󠁥󠁮󠁧󠁿 Đội bóng Anh (" + teams.size() + "):\n\n");
                    
                    for (Team team : teams) {
                        status.append("⚽ ").append(team.getName())
                              .append(" - Rating: ").append(team.getRating())
                              .append(" - ").append(team.getFormation())
                              .append("\n");
                    }
                    
                    updateStatus(status.toString());
                    Toast.makeText(TestTeamActivity.this, 
                        "Tìm thấy " + teams.size() + " đội bóng Anh", 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    updateStatus("❌ Lỗi khi lọc đội bóng: " + e.getMessage());
                    Toast.makeText(TestTeamActivity.this, 
                        "Lỗi: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, "Lỗi filter teams", e);
            }
        });
    }
    
    private void loadTeams() {
        updateStatus("Đang tải danh sách đội bóng...");
        
        teamManager.getAllTeams(new TeamManager.TeamsCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                runOnUiThread(() -> {
                    StringBuilder status = new StringBuilder("📋 Danh sách đội bóng (" + teams.size() + "):\n\n");
                    
                    for (Team team : teams) {
                        status.append("🏆 ").append(team.getName())
                              .append(" (").append(team.getCountry()).append(")")
                              .append(" - Rating: ").append(team.getRating())
                              .append(" - Sơ đồ: ").append(team.getFormation())
                              .append(" - Cầu thủ: ").append(team.getPlayerCount())
                              .append("\n");
                    }
                    
                    updateStatus(status.toString());
                    Toast.makeText(TestTeamActivity.this, 
                        "Đã tải " + teams.size() + " đội bóng", 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    updateStatus("❌ Lỗi khi tải đội bóng: " + e.getMessage());
                    Toast.makeText(TestTeamActivity.this, 
                        "Lỗi tải dữ liệu: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, "Lỗi load teams", e);
            }
        });
    }
    

    
    private void getRandomTeam() {
        updateStatus("Đang chọn đội ngẫu nhiên...");
        
        teamManager.getRandomOpponentTeam(new TeamManager.TeamCallback() {
            @Override
            public void onSuccess(Team team) {
                runOnUiThread(() -> {
                    StringBuilder status = new StringBuilder("🎲 Đội ngẫu nhiên được chọn:\n\n" +
                                  "🏆 Tên: " + team.getName() + "\n" +
                                  "🌍 Quốc gia: " + team.getCountry() + "\n" +
                                  "⭐ Rating: " + team.getRating() + "\n" +
                                  "⚽ Sơ đồ: " + team.getFormation() + "\n" +
                                  "👥 Số cầu thủ: " + team.getPlayerCount() + "\n\n" +
                                  "🔥 Đội hình:\n");
                    
                    // Hiển thị top 3 cầu thủ
                    int count = 0;
                    for (com.example.fms.model.Player player : team.getPlayers()) {
                        if (count < 3) {
                            status.append("  ").append(player.getPosition())
                                  .append(" - ").append(player.getName())
                                  .append(" (").append(player.getOverall()).append(")\n");
                            count++;
                        }
                    }
                    
                    updateStatus(status.toString());
                    Toast.makeText(TestTeamActivity.this, 
                        "Chọn đội: " + team.getName(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    updateStatus("❌ Lỗi khi chọn đội ngẫu nhiên: " + e.getMessage());
                    Toast.makeText(TestTeamActivity.this, 
                        "Lỗi: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, "Lỗi get random team", e);
            }
        });
    }
    
    private void updateStatus(String message) {
        tvStatus.setText(message);
        Log.i(TAG, message);
    }
} 