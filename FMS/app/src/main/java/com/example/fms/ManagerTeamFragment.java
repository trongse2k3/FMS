package com.example.fms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView; // Import AdapterView
import android.widget.ArrayAdapter; // Import ArrayAdapter
import android.widget.SeekBar; // Import SeekBar
import android.widget.Spinner; // Import Spinner
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton; // Import AppCompatButton
import androidx.fragment.app.Fragment;

import java.util.ArrayList; // Import ArrayList
import java.util.Arrays; // Import Arrays
import java.util.List; // Import List

public class ManagerTeamFragment extends Fragment {

    private SeekBar volumeSeekBar;
    private Spinner languageSpinner;
    private AppCompatButton btnSaveGame;
    private AppCompatButton btnOtherSettings;
    private AppCompatButton btnLogout;

    public ManagerTeamFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manager_team, container, false);

        // Ánh xạ các views
        volumeSeekBar = view.findViewById(R.id.volume_seekbar);
        languageSpinner = view.findViewById(R.id.language_spinner);
        btnSaveGame = view.findViewById(R.id.btn_save_game);
        btnOtherSettings = view.findViewById(R.id.btn_other_settings);
        btnLogout = view.findViewById(R.id.btn_logout);

        // --- Xử lý SeekBar Âm lượng ---
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress từ 0 đến 100
                Toast.makeText(getContext(), "Âm lượng: " + progress + "%", Toast.LENGTH_SHORT).show();
                // TODO: Cập nhật âm lượng thực tế của ứng dụng
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Khi người dùng bắt đầu chạm vào SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Khi người dùng ngừng chạm vào SeekBar
            }
        });
        // Đặt giá trị mặc định cho SeekBar (ví dụ: 50%)
        volumeSeekBar.setProgress(50);

        // --- Xử lý Spinner Ngôn ngữ ---
        List<String> languages = Arrays.asList("Tiếng Việt", "English", "日本語", "한국어");
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Ngôn ngữ đã chọn: " + selectedLanguage, Toast.LENGTH_SHORT).show();
                // TODO: Cập nhật ngôn ngữ của ứng dụng
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì khi không có gì được chọn
            }
        });

        // --- Xử lý các nút ---
        btnSaveGame.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đang lưu tiến trình trò chơi...", Toast.LENGTH_SHORT).show();
            // TODO: Triển khai logic lưu trò chơi
        });

        btnOtherSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở cài đặt nâng cao...", Toast.LENGTH_SHORT).show();
            // TODO: Triển khai logic cho cài đặt nâng cao
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đang đăng xuất...", Toast.LENGTH_SHORT).show();
            // TODO: Triển khai logic đăng xuất (ví dụ: chuyển về màn hình đăng nhập)
        });

        return view;
    }
}
