package com.example.fms;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.widget.AppCompatButton; // Import AppCompatButton

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.fms.adapter.MatchHistoryAdapter;
import com.example.fms.model.Match;
import com.example.fms.firebase.UserManager;
import com.example.fms.firebase.MatchHistoryManager;
import com.example.fms.model.MatchResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections; // Import Collections for shuffling
import java.util.List;
import java.util.Random;

public class MatchFragment extends Fragment {

    private TextView tvHomeTeamName;
    private ImageView ivHomeTeamLogo;
    private TextView tvAwayTeamName;
    private ImageView ivAwayTeamLogo;
    private AppCompatButton btnSimulateMatch;

    private RecyclerView rvMatchSchedule;

    // Views cho bảng thông báo trận đấu
    private View matchLogCard;
    private TextView tvMatchLogDetails;
    private AppCompatButton btnCloseMatchLog;

    // Danh sách các sự kiện trận đấu giả lập
    private List<String> generalMatchEvents;
    private List<String> penaltyEvents;
    private List<String> goalEvents;
    private Random random;

    // Tỷ số trận đấu
    private int homeScore = 0;
    private int awayScore = 0;

    private FirebaseUser currentUser;
    private UserManager userManager;
    private MatchHistoryManager matchHistoryManager;
    private List<MatchResult> matchResultList;
    private MatchHistoryAdapter matchHistoryAdapter;

    public MatchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match, container, false);

        // Ánh xạ các views chính cho trận đấu hiện tại
        tvHomeTeamName = view.findViewById(R.id.tv_home_team_name);
        ivHomeTeamLogo = view.findViewById(R.id.iv_home_team_logo);
        tvAwayTeamName = view.findViewById(R.id.tv_away_team_name);
        ivAwayTeamLogo = view.findViewById(R.id.iv_away_team_logo);
        btnSimulateMatch = view.findViewById(R.id.btn_simulate_match);

        // Ánh xạ views cho bảng thông báo trận đấu
        matchLogCard = view.findViewById(R.id.match_log_card);
        tvMatchLogDetails = view.findViewById(R.id.tv_match_log_details);
        btnCloseMatchLog = view.findViewById(R.id.btn_close_match_log);

        // Khởi tạo các manager
        userManager = new UserManager();
        matchHistoryManager = new MatchHistoryManager();
        currentUser = userManager.getCurrentUser();

        // Ánh xạ RecyclerView cho lịch thi đấu
        rvMatchSchedule = view.findViewById(R.id.rv_match_schedule);
        rvMatchSchedule.setLayoutManager(new LinearLayoutManager(getContext()));

        matchResultList = new ArrayList<>();
        matchHistoryAdapter = new MatchHistoryAdapter(new ArrayList<>()); // Sẽ cập nhật sau
        rvMatchSchedule.setAdapter(matchHistoryAdapter);

        // Lấy dữ liệu thực tế
        loadUserMatchHistory();

        // Xử lý sự kiện click cho nút SIMULATE MATCH
        btnSimulateMatch.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MatchSimulationActivity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện click cho nút Đóng bảng thông báo
        btnCloseMatchLog.setOnClickListener(v -> {
            matchLogCard.setVisibility(View.GONE); // Ẩn bảng thông báo
        });

        return view;
    }

    private void loadUserMatchHistory() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        matchHistoryManager.getUserMatchHistory(userId, 20, new MatchHistoryManager.OnMatchHistoryLoadListener() {
            @Override
            public void onMatchHistoryLoaded(List<MatchResult> matches) {
                if (matches == null || matches.isEmpty()) {
                    Toast.makeText(getContext(), "Chưa có trận đấu nào!", Toast.LENGTH_SHORT).show();
                    matchHistoryAdapter.setMatchResults(new ArrayList<>());
                    return;
                }
                matchResultList.clear();
                matchResultList.addAll(matches);
                matchHistoryAdapter.setMatchResults(matchResultList);
                // Hiển thị thông tin trận gần nhất
                MatchResult latest = matches.get(0);
                tvHomeTeamName.setText(latest.getHomeTeam().getName());
                tvAwayTeamName.setText(latest.getAwayTeam().getName());
                // TODO: Cập nhật logo nếu có thông tin logo (nâng cấp sau)
            }
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải lịch sử trận đấu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức để hiển thị bảng thông báo trận đấu ngẫu nhiên
    private void displayMatchLog() {
        StringBuilder logBuilder = new StringBuilder();
        List<String> currentMatchEvents = new ArrayList<>();

        // Thêm sự kiện bắt đầu trận đấu
        currentMatchEvents.add("0': Trận đấu bắt đầu! Hai đội nhập cuộc.");

        // Lấy kết quả xúc xắc
        int diceRoll = rollDice();
        logBuilder.append("Kết quả xúc xắc: ").append(diceRoll).append("\n\n");

        // Dựa vào xúc xắc để thêm sự kiện penalty
        if (diceRoll <= 30) { // Ví dụ: số thấp (1-30) có tỷ lệ cao dính penalty
            currentMatchEvents.add(penaltyEvents.get(random.nextInt(penaltyEvents.size())));
        } else if (diceRoll >= 70) { // Ví dụ: số cao (70-100) có tỷ lệ cao ghi bàn
            currentMatchEvents.add(goalEvents.get(random.nextInt(goalEvents.size())));
            // Cập nhật tỷ số ngẫu nhiên
            if (random.nextBoolean()) {
                homeScore++;
            } else {
                awayScore++;
            }
        }

        // Thêm một số sự kiện chung ngẫu nhiên
        int numGeneralEvents = random.nextInt(generalMatchEvents.size() / 2) + 2; // 2-5 sự kiện chung
        List<String> tempGeneralEvents = new ArrayList<>(generalMatchEvents);
        Collections.shuffle(tempGeneralEvents); // Xáo trộn để lấy sự kiện ngẫu nhiên
        for (int i = 0; i < numGeneralEvents; i++) {
            currentMatchEvents.add(tempGeneralEvents.get(i));
        }

        // Thêm sự kiện kết thúc hiệp 1
        currentMatchEvents.add("45': Hiệp 1 kết thúc, tỷ số hiện tại: " + homeScore + " - " + awayScore + ".");

        // Sắp xếp các sự kiện theo thời gian (giả định thời gian ở đầu chuỗi)
        // Đây là cách đơn giản, bạn có thể cần một cơ chế phức tạp hơn cho thời gian chính xác
        Collections.sort(currentMatchEvents);

        for (String event : currentMatchEvents) {
            logBuilder.append("• ").append(event).append("\n\n");
        }

        tvMatchLogDetails.setText(logBuilder.toString());
        matchLogCard.setVisibility(View.VISIBLE);
    }

    // Phương thức tung xúc xắc (ngẫu nhiên từ 1 đến 100)
    private int rollDice() {
        return random.nextInt(100) + 1; // Số ngẫu nhiên từ 1 đến 100
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}