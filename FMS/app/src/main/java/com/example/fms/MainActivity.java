package com.example.fms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import FloatingActionButton

// MainActivity triển khai interface OnMoneyUpdateListener từ ListPlayerFragment
public class MainActivity extends AppCompatActivity implements ListPlayerFragment.OnMoneyUpdateListener {

    private NavController navController;
    private ImageView imgTeamLogo;
    private TextView tvTeamName;
    private TextView tvMoney; // Khai báo TextView cho tiền (đã thêm)
    private FloatingActionButton fabMatch; // Khai báo FloatingActionButton

    private static final String PREFS_NAME = "TeamPrefs";
    private static final String KEY_TEAM_NAME = "teamName";
    private static final String KEY_TEAM_LOGO_ID = "teamLogoId";
    private static final String KEY_MONEY = "currentMoney"; // Khóa tiền (đã thêm)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set padding cho các hệ thống insets (status bar, nav bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ views trong Top Bar
        imgTeamLogo = findViewById(R.id.img_team_logo); // ID đã được đổi trong activity_main.xml
        tvTeamName = findViewById(R.id.text_team_name); // ID đã được đổi trong activity_main.xml
        tvMoney = findViewById(R.id.text_money); // Ánh xạ TextView tiền (đã thêm)

        // Đọc dữ liệu từ SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String teamName = sharedPref.getString(KEY_TEAM_NAME, "Tên Đội Mặc Định");
        int teamLogoId = sharedPref.getInt(KEY_TEAM_LOGO_ID, R.drawable.logo_1);
        int currentMoney = sharedPref.getInt(KEY_MONEY, 2000); // Lấy tiền, mặc định 2000 (đã thêm)

        // Gán dữ liệu cho Top Bar
        tvTeamName.setText(teamName);
        imgTeamLogo.setImageResource(teamLogoId);
        tvMoney.setText(currentMoney + "$"); // Cập nhật hiển thị tiền (đã thêm)

        // Thiết lập Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment); // ID của FragmentContainerView trong activity_main.xml
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            System.err.println("Error: NavHostFragment not found! Please check your activity_main.xml layout.");
            return;
        }

        // Kết nối BottomNavigationView với NavController
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        NavigationUI.setupWithNavController(bottomNavView, navController);


    }

    // Triển khai phương thức từ OnMoneyUpdateListener interface (đã thêm)
    @Override
    public void onMoneyUpdated(int newMoney) {
        tvMoney.setText(newMoney + "$"); // Cập nhật TextView tiền
    }

    // Ghi đè phương thức này để NavController có thể xử lý nút Back đúng cách giữa các Fragment
    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null) {
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
