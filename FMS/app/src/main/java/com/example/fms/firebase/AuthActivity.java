package com.example.fms.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.fms.CreateActivity;
import com.example.fms.MainActivity;
import com.example.fms.R;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";
    
    private EditText emailEditText, passwordEditText, usernameEditText;
    private Button loginButton, registerButton;
    private TextView switchModeTextView;
    
    private UserManager userManager;
    private boolean isLoginMode = true;
    private WindowInsetsControllerCompat windowInsetsController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Thiết lập chế độ toàn màn hình
        setupFullScreen();
        
        setContentView(R.layout.activity_auth);
        
        userManager = new UserManager();
        
        // Kiểm tra nếu người dùng đã đăng nhập
        if (userManager.isUserLoggedIn()) {
            checkUserTeam();
            return;
        }
        
        // Khởi tạo các view
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        switchModeTextView = findViewById(R.id.switchModeTextView);
        
        // Thiết lập chế độ mặc định là đăng nhập
        updateUiForMode();
        
        // Thiết lập sự kiện click cho các nút
        loginButton.setOnClickListener(v -> handleLogin());
        registerButton.setOnClickListener(v -> handleRegister());
        switchModeTextView.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUiForMode();
        });
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
    
    private void updateUiForMode() {
        if (isLoginMode) {
            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.GONE);
            usernameEditText.setVisibility(View.GONE);
            switchModeTextView.setText(R.string.switch_to_register);
        } else {
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            usernameEditText.setVisibility(View.VISIBLE);
            switchModeTextView.setText(R.string.switch_to_login);
        }
    }
    
    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        
        userManager.loginUser(email, password, new UserManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Đăng nhập thành công: " + user.getUid());
                Toast.makeText(AuthActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                checkUserTeam();
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Đăng nhập thất bại", e);
                Toast.makeText(AuthActivity.this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void handleRegister() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        
        userManager.registerUser(email, password, username, new UserManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Đăng ký thành công: " + user.getUid());
                Toast.makeText(AuthActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                // Điều hướng người dùng đăng ký mới đến CreateActivity
                startCreateActivity();
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Đăng ký thất bại", e);
                Toast.makeText(AuthActivity.this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Kiểm tra xem người dùng đã có đội bóng chưa
    private void checkUserTeam() {
        String userId = userManager.getCurrentUserId();
        if (userId != null) {
            userManager.hasTeamData(userId, new UserManager.UserDataCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    boolean hasTeam = (boolean) userData.get("hasTeam");
                    if (hasTeam) {
                        // Nếu người dùng đã có đội bóng, chuyển đến MainActivity
                        startMainActivity();
                    } else {
                        // Nếu chưa có đội bóng, chuyển đến CreateActivity để tạo đội
                        startCreateActivity();
                    }
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Lỗi kiểm tra dữ liệu đội bóng", e);
                    // Chuyển đến CreateActivity nếu không thể kiểm tra
                    startCreateActivity();
                }
            });
        } else {
            // Nếu không lấy được userId, chuyển đến CreateActivity
            startCreateActivity();
        }
    }
    
    private void startMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void startCreateActivity() {
        Intent intent = new Intent(AuthActivity.this, CreateActivity.class);
        startActivity(intent);
        finish();
    }
} 