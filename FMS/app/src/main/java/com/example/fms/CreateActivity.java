package com.example.fms;

import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.fms.firebase.UserManager;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class CreateActivity extends AppCompatActivity {

    private static final String TAG = "CreateActivity";
    private EditText edtTeamName;
    private AppCompatButton btnInitialOk;
    private ImageView imgMainLogo;
    private HorizontalScrollView logoScroll;
    private TextView tvInstruction;
    private AppCompatButton btnSaveAndContinue;
    private WindowInsetsControllerCompat windowInsetsController;

    private int selectedLogoResId = R.drawable.logo_1;
    private int selectedLogoId = 1; // ID số của logo (1-6)

    // Constants cho SharedPreferences
    private static final String PREFS_NAME = "TeamPrefs";
    private static final String KEY_TEAM_NAME = "teamName";
    private static final String KEY_TEAM_LOGO_ID = "teamLogoId";
    private static final String KEY_MONEY = "currentMoney";
    
    private UserManager userManager;
    private FirebaseUser currentUser;
    private static final int DEFAULT_MONEY = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Thiết lập chế độ toàn màn hình
        setupFullScreen();
        
        setContentView(R.layout.activity_create);
        
        // Khởi tạo UserManager và lấy người dùng hiện tại
        userManager = new UserManager();
        currentUser = userManager.getCurrentUser();
        
        // Nếu không có người dùng đăng nhập, chuyển hướng về màn hình đăng nhập
        if (currentUser == null) {
            Intent intent = new Intent(this, com.example.fms.firebase.AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Ánh xạ các views
        edtTeamName = findViewById(R.id.edtLogo);
        btnInitialOk = findViewById(R.id.btn_ok_initial);
        imgMainLogo = findViewById(R.id.imgMainLogo);
        logoScroll = findViewById(R.id.logoScroll);
        tvInstruction = findViewById(R.id.textView);
        btnSaveAndContinue = findViewById(R.id.btn_save_and_continue);

        // Áp dụng insets cho toàn màn hình
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Đặt logo mặc định khi khởi tạo
        imgMainLogo.setImageResource(selectedLogoResId);

        // Xử lý sự kiện click cho nút OK ban đầu (nhập tên đội)
        btnInitialOk.setOnClickListener(v -> {
            String teamName = edtTeamName.getText().toString().trim();
            if (!teamName.isEmpty()) {
                // Ẩn EditText và Button OK ban đầu
                edtTeamName.setVisibility(View.GONE);
                btnInitialOk.setVisibility(View.GONE);

                // Hiện logo lớn, danh sách logo và nút Lưu và Chuyển
                imgMainLogo.setVisibility(View.VISIBLE);
                logoScroll.setVisibility(View.VISIBLE);
                btnSaveAndContinue.setVisibility(View.VISIBLE);
                tvInstruction.setText("Vui lòng chọn Logo cho đội bóng");

                // Đặt logo chính là logo mặc định hoặc logo đầu tiên trong danh sách
                imgMainLogo.setImageResource(selectedLogoResId);

            } else {
                Toast.makeText(CreateActivity.this, "Vui lòng nhập tên đội bóng!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện click cho các ImageView logo nhỏ trong HorizontalScrollView
        LinearLayout logoContainer = findViewById(R.id.logoContainer);
        for (int i = 0; i < logoContainer.getChildCount(); i++) {
            View child = logoContainer.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView logoImageView = (ImageView) child;
                logoImageView.setOnClickListener(v -> {
                    // Cập nhật logo chính khi một logo nhỏ được chọn
                    // Lấy resource ID của drawable từ ImageView được click
                    selectedLogoResId = (Integer) v.getTag(); // Lấy ID đã lưu trong Tag
                    if (selectedLogoResId != 0) {
                        imgMainLogo.setImageResource(selectedLogoResId);
                        
                        // Lưu ID của logo (1-6)
                        if (v.getId() == R.id.logo1) selectedLogoId = 1;
                        else if (v.getId() == R.id.logo2) selectedLogoId = 2;
                        else if (v.getId() == R.id.logo3) selectedLogoId = 3;
                        else if (v.getId() == R.id.logo4) selectedLogoId = 4;
                        else if (v.getId() == R.id.logo5) selectedLogoId = 5;
                        else if (v.getId() == R.id.logo6) selectedLogoId = 6;
                    } else {
                        // Fallback nếu tag không có hoặc không đúng định dạng
                        Toast.makeText(CreateActivity.this, "Lỗi: Không tìm thấy ID logo", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // Gán tag cho mỗi ImageView logo nhỏ với resource ID của nó
        findViewById(R.id.logo1).setTag(R.drawable.img_logo_1);
        findViewById(R.id.logo2).setTag(R.drawable.img_logo_2);
        findViewById(R.id.logo3).setTag(R.drawable.img_logo_3);
        findViewById(R.id.logo4).setTag(R.drawable.img_logo_4);
        findViewById(R.id.logo5).setTag(R.drawable.img_logo_5);
        findViewById(R.id.logo6).setTag(R.drawable.img_logo_6);
        // Thêm các tag cho các logo khác nếu có

        // Xử lý sự kiện click cho nút "Lưu và Chuyển" mới
        btnSaveAndContinue.setOnClickListener(v -> {
            String teamName = edtTeamName.getText().toString().trim(); // Lấy tên đội (dù đã ẩn nhưng giá trị vẫn còn)

            if (!teamName.isEmpty() && selectedLogoId != 0) {
                // Lưu dữ liệu đội bóng lên Firestore
                saveTeamDataToFirestore(teamName, selectedLogoId, DEFAULT_MONEY);
            } else {
                Toast.makeText(CreateActivity.this, "Vui lòng nhập tên đội và chọn logo!", Toast.LENGTH_SHORT).show();
            }
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
    
    // Phương thức lưu dữ liệu đội bóng lên Firestore
    private void saveTeamDataToFirestore(String teamName, int logoId, int money) {
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu dữ liệu đội bóng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = currentUser.getUid();
        
        // Hiển thị thông báo đang lưu
        Toast.makeText(this, "Đang lưu dữ liệu đội bóng...", Toast.LENGTH_SHORT).show();
        
        // Gọi phương thức syncTeamData từ UserManager
        userManager.syncTeamData(userId, teamName, logoId, money, new UserManager.UserDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                // Lưu vào SharedPreferences sau khi lưu thành công lên Firestore
                saveToSharedPreferences(teamName, selectedLogoResId, money);
                
                // Thông báo thành công
                Toast.makeText(CreateActivity.this, "Đội bóng đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                
                // Chuyển sang MainActivity
                Intent intent = new Intent(CreateActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Đóng CreateActivity
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi lưu dữ liệu đội bóng", e);
                
                // Nếu lưu lên Firestore thất bại, vẫn lưu vào SharedPreferences và chuyển sang MainActivity
                saveToSharedPreferences(teamName, selectedLogoResId, money);
                
                Toast.makeText(CreateActivity.this, "Không thể đồng bộ dữ liệu lên server, nhưng đã lưu cục bộ.", Toast.LENGTH_LONG).show();
                
                // Chuyển sang MainActivity
                Intent intent = new Intent(CreateActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Đóng CreateActivity
            }
        });
    }
    
    // Phương thức lưu vào SharedPreferences
    private void saveToSharedPreferences(String teamName, int logoResId, int money) {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_TEAM_NAME, teamName);
        editor.putInt(KEY_TEAM_LOGO_ID, logoResId);
        editor.putInt(KEY_MONEY, money);
        editor.apply(); // Áp dụng thay đổi
    }
}
