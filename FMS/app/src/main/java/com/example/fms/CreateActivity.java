package com.example.fms;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create);

        EditText edtLogo = findViewById(R.id.edtLogo);
        AppCompatButton btnOk = findViewById(R.id.btn_ok);
        ImageView imgMainLogo = findViewById(R.id.imgMainLogo);
        HorizontalScrollView logoScroll = findViewById(R.id.logoScroll);
        TextView textView = findViewById(R.id.textView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnOk.setOnClickListener(v -> {
            String teamName = edtLogo.getText().toString().trim();
            if (!teamName.isEmpty()) {
                // Ẩn EditText và Button
                edtLogo.setVisibility(View.GONE);
                btnOk.setVisibility(View.GONE);

                // Hiện logo lớn và danh sách logo
                imgMainLogo.setVisibility(View.VISIBLE);
                logoScroll.setVisibility(View.VISIBLE);
                textView.setText("Vui lòng chọn Logo cho đội bóng");
            } else {
                Toast.makeText(CreateActivity.this, "Vui lòng nhập tên đội bóng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}