package com.example.fms;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Import Handler
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OnBoardingActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_LENGTH = 3000; // 3 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
}