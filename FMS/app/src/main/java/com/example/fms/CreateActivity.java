package com.example.fms;

import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
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
import androidx.core.view.WindowInsetsCompat;

public class CreateActivity extends AppCompatActivity {

    private EditText edtTeamName;
    private AppCompatButton btnInitialOk;
    private ImageView imgMainLogo;
    private HorizontalScrollView logoScroll;
    private TextView tvInstruction;
    private AppCompatButton btnSaveAndContinue;

    private int selectedLogoResId = R.drawable.logo_1;

    // Constants cho SharedPreferences
    private static final String PREFS_NAME = "TeamPrefs";
    private static final String KEY_TEAM_NAME = "teamName";
    private static final String KEY_TEAM_LOGO_ID = "teamLogoId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create);

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

            if (!teamName.isEmpty() && selectedLogoResId != 0) {
                // Lưu tên đội và ID logo vào SharedPreferences
                SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(KEY_TEAM_NAME, teamName);
                editor.putInt(KEY_TEAM_LOGO_ID, selectedLogoResId);
                editor.apply(); // Áp dụng thay đổi

                Toast.makeText(CreateActivity.this, "Tên đội và logo đã được lưu!", Toast.LENGTH_SHORT).show();

                // Chuyển sang MainActivity
                Intent intent = new Intent(CreateActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Đóng CreateActivity
            } else {
                Toast.makeText(CreateActivity.this, "Vui lòng nhập tên đội và chọn logo!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
