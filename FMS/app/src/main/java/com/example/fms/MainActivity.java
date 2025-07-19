package com.example.fms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.fms.firebase.FirebaseManager;
import com.example.fms.firebase.UserManager;
import com.example.fms.firebase.AuthActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

// MainActivity triển khai interface OnMoneyUpdateListener từ ListPlayerFragment và ShopFragment
public class MainActivity extends AppCompatActivity implements ListPlayerFragment.OnMoneyUpdateListener, ShopFragment.OnMoneyUpdateListener {

    private static final String TAG = "MainActivity";
    private NavController navController;
    private ImageView imgTeamLogo;
    private TextView tvTeamName;
    private TextView tvMoney; // Khai báo TextView cho tiền (đã thêm)
    private FloatingActionButton fabMatch; // Khai báo FloatingActionButton
    private ImageButton btnLogout; // Khai báo nút đăng xuất
    private Button btnTestItem; // Khai báo nút test item
    private WindowInsetsControllerCompat windowInsetsController; // Khai báo biến điều khiển Insets

    private static final String PREFS_NAME = "TeamPrefs";
    private static final String KEY_TEAM_NAME = "teamName";
    private static final String KEY_TEAM_LOGO_ID = "teamLogoId";
    private static final String KEY_MONEY = "currentMoney"; // Khóa tiền (đã thêm)
    
    private UserManager userManager;
    private FirebaseUser currentUser;
    
    // Biến lưu thông tin hiện tại
    private String teamName;
    private int teamLogoId;
    private int currentMoney;
    
    // BroadcastReceiver để nhận thông báo cập nhật tiền
    private BroadcastReceiver moneyUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.fms.MONEY_UPDATED".equals(intent.getAction())) {
                int updatedMoney = intent.getIntExtra("updatedMoney", -1);
                if (updatedMoney != -1) {
                    // Cập nhật số tiền trong MainActivity
                    onMoneyUpdated(updatedMoney);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Thiết lập chế độ toàn màn hình
        setupFullScreen();
        
        setContentView(R.layout.activity_main);
        
        // Đăng ký BroadcastReceiver để nhận thông báo cập nhật tiền
        IntentFilter intentFilter = new IntentFilter("com.example.fms.MONEY_UPDATED");
        registerReceiver(moneyUpdateReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        
        userManager = new UserManager();
        
        // Kiểm tra người dùng đăng nhập
        if (!userManager.isUserLoggedIn()) {
            startAuthActivity();
            return;
        }
        
        currentUser = userManager.getCurrentUser();

        // Xử lý edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Thêm padding cho top bar để tránh bị che bởi status bar
            View topBar = findViewById(R.id.top_bar_container);
            if (topBar != null) {
                // Chỉ thêm padding top, giữ nguyên các padding khác
                int paddingTop = systemBars.top + 8; // 8dp là giá trị margin_top ban đầu
                topBar.setPadding(
                    topBar.getPaddingLeft(),
                    paddingTop,
                    topBar.getPaddingRight(),
                    topBar.getPaddingBottom()
                );
                
                // Cập nhật lại margin top về 0 vì đã xử lý trong padding
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
                params.topMargin = 0;
                topBar.setLayoutParams(params);
            }
            
            // Thêm padding cho bottom navigation để tránh bị che bởi navigation bar
            View bottomNav = findViewById(R.id.bottom_nav_view);
            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
                // Đảm bảo bottom nav hiển thị đúng
                bottomNav.setElevation(12);
                bottomNav.bringToFront();
            }
            
            return insets;
        });

        // Ánh xạ views trong Top Bar
        imgTeamLogo = findViewById(R.id.img_team_logo); // ID đã được đổi trong activity_main.xml
        tvTeamName = findViewById(R.id.text_team_name); // ID đã được đổi trong activity_main.xml
        tvMoney = findViewById(R.id.text_money); // Ánh xạ TextView tiền (đã thêm)
//        btnLogout = findViewById(R.id.btn_logout); // Ánh xạ nút đăng xuất
//        btnTestItem = findViewById(R.id.btn_test_item); // Ánh xạ nút test item
        
        // Thiết lập sự kiện click cho nút đăng xuất
//        btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                logoutUser();
//            }
//        });
        
        // Thiết lập sự kiện click cho nút test item
        // btnTestItem.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         Intent intent = new Intent(MainActivity.this, TestItemActivity.class);
        //         startActivity(intent);
        //     }
        // });

        // Lấy thông tin người dùng từ Firestore
        loadUserData();

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

    // Phương thức xử lý đăng xuất
    private void logoutUser() {
        // Đăng xuất người dùng từ Firebase
        userManager.logoutUser();
        
        // Hiển thị thông báo
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        
        // Chuyển hướng đến trang đăng nhập
        startAuthActivity();
    }
    
    // Lấy thông tin người dùng từ Firestore
    private void loadUserData() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            userManager.getUserData(userId, new UserManager.UserDataCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    // Lấy dữ liệu từ Firestore
                    String username = (String) userData.get("username");
                    
                    // Lấy thông tin đội bóng từ Firestore
                    if (userData.containsKey("teamName")) {
                        teamName = (String) userData.get("teamName");
                    } else {
                        teamName = "Tên Đội Mặc Định";
                    }
                    
                    if (userData.containsKey("teamLogoId")) {
                        if (userData.get("teamLogoId") instanceof Long) {
                            teamLogoId = ((Long) userData.get("teamLogoId")).intValue();
                        } else if (userData.get("teamLogoId") instanceof Integer) {
                            teamLogoId = (Integer) userData.get("teamLogoId");
                        } else {
                            teamLogoId = 1; // Mặc định
                        }
                    } else {
                        teamLogoId = 1;
                    }
                    
                    if (userData.containsKey("money")) {
                        if (userData.get("money") instanceof Long) {
                            currentMoney = ((Long) userData.get("money")).intValue();
                        } else if (userData.get("money") instanceof Integer) {
                            currentMoney = (Integer) userData.get("money");
                        } else {
                            currentMoney = 2000; // Mặc định
                        }
                    } else {
                        currentMoney = 2000;
                    }
                    
                    // Lưu dữ liệu vào SharedPreferences để truy cập offline
                    saveToSharedPreferences();
                    
                    // Gán dữ liệu cho Top Bar
                    updateUI();
                    
                    Toast.makeText(MainActivity.this, "Xin chào " + username, Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Không thể lấy dữ liệu người dùng", e);
                    
                    // Sử dụng dữ liệu từ SharedPreferences nếu không lấy được từ Firestore
                    loadFromSharedPreferences();
                    
                    // Gán dữ liệu cho Top Bar
                    updateUI();
                }
            });
        }
    }
    
    // Lưu dữ liệu vào SharedPreferences
    private void saveToSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_TEAM_NAME, teamName);
        editor.putInt(KEY_TEAM_LOGO_ID, getTeamLogoResId(teamLogoId));
        editor.putInt(KEY_MONEY, currentMoney);
        editor.apply();
    }
    
    // Lấy dữ liệu từ SharedPreferences
    private void loadFromSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        teamName = sharedPref.getString(KEY_TEAM_NAME, "Tên Đội Mặc Định");
        teamLogoId = sharedPref.getInt(KEY_TEAM_LOGO_ID, R.drawable.logo_1);
        currentMoney = sharedPref.getInt(KEY_MONEY, 2000);
    }
    
    // Cập nhật giao diện
    private void updateUI() {
        tvTeamName.setText(teamName);
        imgTeamLogo.setImageResource(getTeamLogoResId(teamLogoId));
        tvMoney.setText(currentMoney + "$");
    }
    
    // Chuyển đổi từ ID logo sang resource ID
    private int getTeamLogoResId(int logoId) {
        switch(logoId) {
            case 1:
                return R.drawable.logo_1;
            case 2:
                return R.drawable.img_logo_2;
            case 3:
                return R.drawable.img_logo_3;
            case 4:
                return R.drawable.img_logo_4;
            case 5:
                return R.drawable.img_logo_5;
            case 6:
                return R.drawable.img_logo_6;
            default:
                return R.drawable.logo_1;
        }
    }

    // Triển khai phương thức từ OnMoneyUpdateListener interface
    @Override
    public void onMoneyUpdated(int newMoney) {
        this.currentMoney = newMoney;
        tvMoney.setText(newMoney + "$"); // Cập nhật TextView tiền
        
        // Lưu tiền mới vào SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_MONEY, newMoney);
        editor.apply();
        
        // Đồng bộ với Firestore
        if (currentUser != null) {
            userManager.updateMoney(currentUser.getUid(), newMoney, new UserManager.UserDataCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    Log.d(TAG, "Cập nhật tiền thành công");
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Cập nhật tiền thất bại", e);
                }
            });
        }
    }
    
    // Cập nhật tên đội
    public void updateTeamName(String newTeamName) {
        this.teamName = newTeamName;
        tvTeamName.setText(newTeamName);
        
        // Lưu tên đội mới vào SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_TEAM_NAME, newTeamName);
        editor.apply();
        
        // Đồng bộ với Firestore
        if (currentUser != null) {
            userManager.updateTeamName(currentUser.getUid(), newTeamName, new UserManager.UserDataCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    Log.d(TAG, "Cập nhật tên đội thành công");
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Cập nhật tên đội thất bại", e);
                }
            });
        }
    }
    
    // Cập nhật logo đội
    public void updateTeamLogo(int logoId) {
        this.teamLogoId = logoId;
        imgTeamLogo.setImageResource(getTeamLogoResId(logoId));
        
        // Lưu logo đội mới vào SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_TEAM_LOGO_ID, getTeamLogoResId(logoId));
        editor.apply();
        
        // Đồng bộ với Firestore
        if (currentUser != null) {
            userManager.updateTeamLogo(currentUser.getUid(), logoId, new UserManager.UserDataCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    Log.d(TAG, "Cập nhật logo đội thành công");
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Cập nhật logo đội thất bại", e);
                }
            });
        }
    }

    // Ghi đè phương thức này để NavController có thể xử lý nút Back đúng cách giữa các Fragment
    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null) {
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
    
    // Chuyển đến AuthActivity để đăng nhập/đăng ký
    private void startAuthActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        // Hủy đăng ký BroadcastReceiver khi Activity bị hủy
        if (moneyUpdateReceiver != null) {
            unregisterReceiver(moneyUpdateReceiver);
        }
        super.onDestroy();
    }
}
