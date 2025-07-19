package com.example.fms;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Import Handler
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class OnBoardingActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_LENGTH = 3000; // 3 giây
    private WindowInsetsControllerCompat windowInsetsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Thiết lập chế độ toàn màn hình
        setupFullScreen();
        
        setContentView(R.layout.activity_on_boarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Sử dụng Handler để trì hoãn việc chuyển màn hình
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // Tạo Intent để chuyển sang MainActivity
                Intent mainIntent = new Intent(OnBoardingActivity.this, CreateActivity.class);
                startActivity(mainIntent);
                finish(); // Đóng OnBoardingActivity để người dùng không thể quay lại
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
    
    // Thiết lập chế độ toàn màn hình
    private void setupFullScreen() {
        // Đặt ứng dụng ở chế độ toàn màn hình
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Lấy điều khiển WindowInsets để ẩn thanh trạng thái và thanh điều hướng
        windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            // Ẩn thanh trạng thái và thanh điều hướng
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            // Đặt hành vi khi vuốt màn hình - hiện thanh điều hướng tạm thời
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && windowInsetsController != null) {
            // Khi cửa sổ được focus lại, ẩn lại thanh trạng thái và thanh điều hướng
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }
    }
}