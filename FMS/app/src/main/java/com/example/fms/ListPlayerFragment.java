package com.example.fms;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.AppCompatButton;

import com.example.fms.adapter.PlayerDisplayAdapter; // Thay đổi import từ PlayerAdapter sang PlayerDisplayAdapter
import com.example.fms.model.Player;

import java.util.ArrayList;
import java.util.List;

// ListPlayerFragment giờ đây triển khai PlayerDisplayAdapter.OnPlayerClickListener
public class ListPlayerFragment extends Fragment implements PlayerDisplayAdapter.OnPlayerClickListener {

    private RecyclerView recyclerView;
    private PlayerDisplayAdapter playerAdapter; // Thay đổi kiểu playerAdapter
    private List<Player> playerList;

    // Views cho bảng highlight cầu thủ
    private TextView highlightPlayerName;
    private TextView highlightPlayerPosition;
    private TextView highlightPlayerOverall;
    private ImageView highlightPlayerImage;
    private AppCompatButton btnBuyPlayer; // Nút MUA

    private Player currentlySelectedPlayer; // Lưu cầu thủ đang được highlight

    // Constants cho SharedPreferences (phải giống với MainActivity và CreateActivity)
    private static final String PREFS_NAME = "TeamPrefs";
    private static final String KEY_MONEY = "currentMoney"; // Khóa để lưu tiền

    // Interface để giao tiếp với MainActivity
    public interface OnMoneyUpdateListener {
        void onMoneyUpdated(int newMoney);
    }

    private OnMoneyUpdateListener moneyUpdateListener;

    public ListPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Đảm bảo Activity chứa Fragment này triển khai OnMoneyUpdateListener
        if (context instanceof OnMoneyUpdateListener) {
            moneyUpdateListener = (OnMoneyUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnMoneyUpdateListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_player, container, false);

        // Ánh xạ các views của bảng highlight
        highlightPlayerName = view.findViewById(R.id.highlight_player_name);
        highlightPlayerPosition = view.findViewById(R.id.highlight_player_position);
        highlightPlayerOverall = view.findViewById(R.id.highlight_player_overall);
        highlightPlayerImage = view.findViewById(R.id.highlight_player_image);
        btnBuyPlayer = view.findViewById(R.id.btn_buy_player); // Ánh xạ nút MUA

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_players);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Prepare player data (replace with actual data fetching)
        playerList = new ArrayList<>();
        // Sử dụng các drawable placeholder cho ảnh cầu thủ. Bạn cần thay thế bằng ảnh thật.
        playerList.add(new Player("Criss Ronnaldo", "FW", 94, R.drawable.img_player_1));
        playerList.add(new Player("Kyllen Mbapnu", "FW", 93, R.drawable.img_player_2));
        playerList.add(new Player("Earling Halaad", "FW", 92, R.drawable.img_player_3));
        playerList.add(new Player("Neymah Jr", "FW", 91, R.drawable.img_player_4));
        playerList.add(new Player("Leo Missuey", "MF", 96, R.drawable.img_player_5));
        playerList.add(new Player("Kevin De Bruyne", "MF", 91, R.drawable.img_player_6));
        playerList.add(new Player("Virgil van Dijk", "DF", 90, R.drawable.img_player_7));
        playerList.add(new Player("Alisson Becker", "GK", 89, R.drawable.img_player_8));
        // Add more players as needed

        // Truyền 'this' (Fragment) làm listener cho PlayerDisplayAdapter
        playerAdapter = new PlayerDisplayAdapter(playerList, this); // Sử dụng constructor của PlayerDisplayAdapter
        recyclerView.setAdapter(playerAdapter);

        // Hiển thị thông tin cầu thủ đầu tiên (hoặc cầu thủ mặc định) khi Fragment khởi tạo
        if (!playerList.isEmpty()) {
            currentlySelectedPlayer = playerList.get(0); // Đặt cầu thủ đầu tiên là được chọn mặc định
            updateHighlightCard(currentlySelectedPlayer);
        }

        // Xử lý sự kiện click cho nút MUA
        btnBuyPlayer.setOnClickListener(v -> {
            if (currentlySelectedPlayer != null) {
                buyPlayer(currentlySelectedPlayer);
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn một cầu thủ để mua!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Triển khai phương thức từ OnPlayerClickListener interface
    @Override
    public void onPlayerClick(Player player) {
        currentlySelectedPlayer = player; // Cập nhật cầu thủ đang được chọn
        updateHighlightCard(player);
        // Toast.makeText(getContext(), "Đã chọn: " + player.getName(), Toast.LENGTH_SHORT).show(); // Có thể bỏ Toast này
    }

    // Phương thức để cập nhật bảng highlight cầu thủ
    private void updateHighlightCard(Player player) {
        highlightPlayerName.setText(player.getName());
        highlightPlayerPosition.setText(player.getPosition());
        highlightPlayerOverall.setText(String.valueOf(player.getOverall()));
        highlightPlayerImage.setImageResource(player.getImageResId());
    }

    // Phương thức xử lý logic mua cầu thủ
    private void buyPlayer(Player playerToBuy) {
        // Lấy SharedPreferences từ Activity chứa Fragment
        SharedPreferences sharedPref = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Lấy số tiền hiện tại, mặc định là 2000 nếu chưa có
        int currentMoney = sharedPref.getInt(KEY_MONEY, 2000);
        // Giả sử Overall của cầu thủ là giá của cầu thủ
        int playerPrice = playerToBuy.getOverall();

        if (currentMoney >= playerPrice) {
            int newMoney = currentMoney - playerPrice;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(KEY_MONEY, newMoney);
            editor.apply(); // Áp dụng thay đổi

            // Thông báo thành công
            Toast.makeText(getContext(), "Đã mua " + playerToBuy.getName() + " với giá " + playerPrice + "$. Số dư mới: " + newMoney + "$", Toast.LENGTH_LONG).show();

            // Cập nhật tiền trên MainActivity thông qua interface
            if (moneyUpdateListener != null) {
                moneyUpdateListener.onMoneyUpdated(newMoney);
            }

            // TODO: Thêm logic để thêm cầu thủ vào đội hình của người chơi (ví dụ: lưu vào SharedPreferences hoặc Firestore)

            // Xóa cầu thủ đã mua khỏi danh sách hiển thị
            int playerIndex = playerList.indexOf(playerToBuy);
            if (playerIndex != -1) {
                playerList.remove(playerIndex);
                playerAdapter.notifyItemRemoved(playerIndex); // Thông báo cho adapter biết item đã bị xóa
                // Cập nhật lại highlight card nếu cầu thủ bị xóa là cầu thủ đang được highlight
                if (playerList.isEmpty()) {
                    // Nếu danh sách trống, xóa thông tin trên highlight card
                    highlightPlayerName.setText("");
                    highlightPlayerPosition.setText("");
                    highlightPlayerOverall.setText("");
                    highlightPlayerImage.setImageResource(android.R.color.transparent); // Đặt ảnh trong suốt
                    currentlySelectedPlayer = null;
                } else if (currentlySelectedPlayer == playerToBuy) {
                    // Nếu cầu thủ bị xóa là cầu thủ đang được highlight, chọn cầu thủ đầu tiên còn lại
                    currentlySelectedPlayer = playerList.get(0);
                    updateHighlightCard(currentlySelectedPlayer);
                }
            }

        } else {
            // Thông báo không đủ tiền
            Toast.makeText(getContext(), "Không đủ tiền để mua " + playerToBuy.getName() + ". Cần " + playerPrice + "$ nhưng bạn chỉ có " + currentMoney + "$", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        moneyUpdateListener = null; // Ngăn chặn rò rỉ bộ nhớ khi Fragment bị tách khỏi Activity
    }
}
