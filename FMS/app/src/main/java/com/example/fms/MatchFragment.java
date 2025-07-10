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

import com.example.fms.adapter.MatchScheduleAdapter;
import com.example.fms.model.Match;

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
    private AppCompatButton btnStartMatch;

    private RecyclerView rvMatchSchedule;
    private MatchScheduleAdapter matchScheduleAdapter;
    private List<Match> matchScheduleList;

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
        btnStartMatch = view.findViewById(R.id.btn_start_match);

        // Ánh xạ views cho bảng thông báo trận đấu
        matchLogCard = view.findViewById(R.id.match_log_card);
        tvMatchLogDetails = view.findViewById(R.id.tv_match_log_details);
        btnCloseMatchLog = view.findViewById(R.id.btn_close_match_log);

        // Khởi tạo Random
        random = new Random();

        // Chuẩn bị danh sách sự kiện trận đấu
        generalMatchEvents = Arrays.asList(
                "0': Hai đội nhập cuộc, chờ đợi để thăm dò nhau.",
                "3': Đội A được hưởng quả phạt sau pha phạm lỗi giữa sân.",
                "8': Một pha tấn công nguy hiểm của đội B bị chặn đứng.",
                "15': Đội B phản công nhanh, thủ môn A cản phá xuất sắc.",
                "22': Trận đấu giằng co, nhiều pha tranh chấp quyết liệt ở giữa sân.",
                "27': Đội A bỏ lỡ cơ hội ngon ăn trong vòng cấm.",
                "32': Đội B đã phạt góc, nhưng bị phá ra dễ dàng.",
                "38': Một pha cứu thua tuyệt vời của thủ môn đội B.",
                "43': Pha xử lý bóng điêu luyện của tiền vệ đội A."
        );

        penaltyEvents = Arrays.asList(
                "10': Cầu thủ đội A nhận thẻ vàng vì phạm lỗi thô bạo.",
                "20': Chấn thương bất ngờ! Cầu thủ đội B phải rời sân tạm thời.",
                "30': Thẻ vàng cho thủ môn đội A vì câu giờ.",
                "40': Cầu thủ đội B bị truất quyền thi đấu sau pha vào bóng nguy hiểm."
        );

        goalEvents = Arrays.asList(
                "12': VÀO! Đội A mở tỷ số sau pha phối hợp đẹp mắt!",
                "25': BÀN THẮNG! Đội B gỡ hòa sau cú sút xa hiểm hóc!",
                "35': SIÊU PHẨM! Đội A nâng tỷ số lên 2-1 với pha solo đẳng cấp!",
                "42': BÀN THẮNG! Đội B san bằng tỷ số 2-2 từ chấm phạt đền!"
        );

        // Đặt dữ liệu giả lập cho trận đấu hiện tại
        tvHomeTeamName.setText("LION KING");
        ivHomeTeamLogo.setImageResource(R.drawable.img_logo_1);
        tvAwayTeamName.setText("TOTER HAMBURGER");
        ivAwayTeamLogo.setImageResource(R.drawable.img_logo_2);

        // Xử lý sự kiện click cho nút START
        btnStartMatch.setOnClickListener(v -> {
            homeScore = 0; // Reset tỷ số
            awayScore = 0;
            displayMatchLog(); // Hiển thị bảng thông báo trận đấu
        });

        // Xử lý sự kiện click cho nút Đóng bảng thông báo
        btnCloseMatchLog.setOnClickListener(v -> {
            matchLogCard.setVisibility(View.GONE); // Ẩn bảng thông báo
        });

        // Ánh xạ RecyclerView cho lịch thi đấu
        rvMatchSchedule = view.findViewById(R.id.rv_match_schedule);
        rvMatchSchedule.setLayoutManager(new LinearLayoutManager(getContext()));

        // Chuẩn bị dữ liệu giả lập cho lịch thi đấu
        matchScheduleList = new ArrayList<>();
        matchScheduleList.add(new Match("Toter Hamburger", "Bacelone"));
        matchScheduleList.add(new Match("Real Madrisss", "Lion King"));
        matchScheduleList.add(new Match("Livelpool", "Lion King"));
        matchScheduleList.add(new Match("Lion King", "Manchessty Unatis"));
        // Thêm các trận đấu khác nếu cần

        matchScheduleAdapter = new MatchScheduleAdapter(matchScheduleList);
        rvMatchSchedule.setAdapter(matchScheduleAdapter);

        return view;
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
