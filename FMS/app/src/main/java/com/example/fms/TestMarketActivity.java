package com.example.fms;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fms.firebase.MarketDataInitializer;
import com.example.fms.firebase.PlayerManager;
import com.example.fms.model.Player;

import java.util.List;

public class TestMarketActivity extends AppCompatActivity {
    private static final String TAG = "TestMarketActivity";
    
    private TextView tvStatus;
    private Button btnUploadData;
    private Button btnLoadData;
    private Button btnForceReupload;
    
    private PlayerManager playerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_market);
        
        playerManager = new PlayerManager();
        
        initViews();
        setupListeners();
        
        updateStatus("Test Market Activity khởi tạo thành công");
    }
    
    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        btnUploadData = findViewById(R.id.btn_upload_data);
        btnLoadData = findViewById(R.id.btn_load_data);
        btnForceReupload = findViewById(R.id.btn_force_reupload);
    }
    
    private void setupListeners() {
        btnUploadData.setOnClickListener(v -> uploadMarketData());
        btnLoadData.setOnClickListener(v -> loadMarketData());
        btnForceReupload.setOnClickListener(v -> forceReuploadData());
    }
    
    private void uploadMarketData() {
        updateStatus("Đang kiểm tra và upload dữ liệu...");
        
        MarketDataInitializer.initializeMarketDataIfNeeded(this, new MarketDataInitializer.InitializationCallback() {
            @Override
            public void onComplete(boolean wasInitialized, int playerCount) {
                runOnUiThread(() -> {
                    if (wasInitialized) {
                        updateStatus("✅ Đã upload " + playerCount + " cầu thủ lên Firebase");
                        Toast.makeText(TestMarketActivity.this, 
                            "Upload thành công " + playerCount + " cầu thủ", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        updateStatus("ℹ️ Dữ liệu đã tồn tại (" + playerCount + " cầu thủ)");
                        Toast.makeText(TestMarketActivity.this, 
                            "Dữ liệu đã có sẵn", 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    updateStatus("❌ Lỗi upload: " + e.getMessage());
                    Toast.makeText(TestMarketActivity.this, 
                        "Lỗi: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, "Upload failed", e);
            }
        });
    }
    
    private void loadMarketData() {
        updateStatus("Đang tải dữ liệu từ Firebase...");
        
        playerManager.getMarketPlayers(new PlayerManager.PlayersCallback() {
            @Override
            public void onSuccess(List<Player> players) {
                runOnUiThread(() -> {
                    StringBuilder status = new StringBuilder();
                    status.append("✅ Tải thành công ").append(players.size()).append(" cầu thủ:\n\n");
                    
                    // Hiển thị 5 cầu thủ đầu tiên làm ví dụ
                    int count = Math.min(5, players.size());
                    for (int i = 0; i < count; i++) {
                        Player player = players.get(i);
                        status.append("• ").append(player.getPlayerName())
                              .append(" - ").append(player.getFullPositionName())
                              .append(" - Overall: ").append(player.getOverall())
                              .append(" - ").append(player.getFormattedPrice())
                              .append("\n");
                    }
                    
                    if (players.size() > 5) {
                        status.append("... và ").append(players.size() - 5).append(" cầu thủ khác");
                    }
                    
                    updateStatus(status.toString());
                    Toast.makeText(TestMarketActivity.this, 
                        "Tải thành công " + players.size() + " cầu thủ", 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    updateStatus("❌ Lỗi tải dữ liệu: " + e.getMessage());
                    Toast.makeText(TestMarketActivity.this, 
                        "Lỗi: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, "Load failed", e);
            }
        });
    }
    
    private void forceReuploadData() {
        updateStatus("Đang xóa và upload lại dữ liệu...");
        
        MarketDataInitializer.forceReinitializeMarketData(this, new MarketDataInitializer.InitializationCallback() {
            @Override
            public void onComplete(boolean wasInitialized, int playerCount) {
                runOnUiThread(() -> {
                    updateStatus("✅ Đã xóa và upload lại " + playerCount + " cầu thủ");
                    Toast.makeText(TestMarketActivity.this, 
                        "Reupload thành công " + playerCount + " cầu thủ", 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    updateStatus("❌ Lỗi reupload: " + e.getMessage());
                    Toast.makeText(TestMarketActivity.this, 
                        "Lỗi: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, "Reupload failed", e);
            }
        });
    }
    
    private void updateStatus(String message) {
        tvStatus.setText(message);
        Log.d(TAG, message);
    }
} 