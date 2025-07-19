package com.example.fms;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.fms.firebase.UserManager;
import com.example.fms.firebase.PlayerManager;
import com.example.fms.firebase.PlayerDataLoader;
import com.example.fms.model.Player;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// ListPlayerFragment giờ đây triển khai PlayerDisplayAdapter.OnPlayerClickListener
public class ListPlayerFragment extends Fragment implements PlayerDisplayAdapter.OnPlayerClickListener {

    private static final String TAG = "ListPlayerFragment";
    private RecyclerView recyclerView;
    private PlayerDisplayAdapter playerAdapter; // Thay đổi kiểu playerAdapter
    private List<Player> playerList;

    // Views cho bảng highlight cầu thủ
    private TextView highlightPlayerName;
    private TextView highlightPlayerPosition;
    private TextView highlightPlayerOverall;
    private TextView highlightPlayerPrice;
    private TextView highlightPlayerStatus;
    private ImageView highlightPlayerImage;
    private AppCompatButton btnBuyPlayer; // Nút MUA

    private Player currentlySelectedPlayer; // Lưu cầu thủ đang được highlight
    private boolean isPlayerOwned = false; // Kiểm tra xem đã sở hữu cầu thủ hay chưa

    // Constants cho SharedPreferences (phải giống với MainActivity và CreateActivity)
    private static final String PREFS_NAME = "TeamPrefs";
    private static final String KEY_MONEY = "currentMoney"; // Khóa để lưu tiền

    // Interface để giao tiếp với MainActivity
    public interface OnMoneyUpdateListener {
        void onMoneyUpdated(int newMoney);
    }

    private OnMoneyUpdateListener moneyUpdateListener;
    private UserManager userManager;
    private PlayerManager playerManager;
    private FirebaseUser currentUser;

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

        // Khởi tạo UserManager và PlayerManager
        userManager = new UserManager();
        playerManager = new PlayerManager();
        currentUser = userManager.getCurrentUser();

        // Ánh xạ các views của bảng highlight
        highlightPlayerName = view.findViewById(R.id.highlight_player_name);
        highlightPlayerPosition = view.findViewById(R.id.highlight_player_position);
        highlightPlayerOverall = view.findViewById(R.id.highlight_player_overall);
        highlightPlayerPrice = view.findViewById(R.id.highlight_player_price);
        highlightPlayerStatus = view.findViewById(R.id.highlight_player_status);
        highlightPlayerImage = view.findViewById(R.id.highlight_player_image);
        btnBuyPlayer = view.findViewById(R.id.btn_buy_player); // Ánh xạ nút MUA

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_players);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách cầu thủ
        playerList = new ArrayList<>();
        
        // Khởi tạo adapter với danh sách rỗng
        playerAdapter = new PlayerDisplayAdapter(playerList, this);
        recyclerView.setAdapter(playerAdapter);
        
        // Load dữ liệu cầu thủ từ assets (offline)
        loadPlayersFromAssets();

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
    
    // Load dữ liệu cầu thủ từ assets (offline)
    private void loadPlayersFromAssets() {
        PlayerDataLoader.loadPlayersFromAssets(getContext(), new PlayerDataLoader.PlayerDataCallback() {
            @Override
            public void onSuccess(List<Player> players) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        playerList.clear();
                        playerList.addAll(players);
                        playerAdapter.notifyDataSetChanged();
                        
                        // Hiển thị cầu thủ đầu tiên nếu có
                        if (!playerList.isEmpty()) {
                            currentlySelectedPlayer = playerList.get(0);
                            updateHighlightCard(currentlySelectedPlayer);
                        }
                        
                        Log.d(TAG, "Loaded " + players.size() + " players from assets");
                        if (getContext() != null) {
                            Toast.makeText(getContext(), 
                                "Đã tải " + players.size() + " cầu thủ từ dữ liệu offline", 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error loading players from assets", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), 
                                "Lỗi khi tải dữ liệu offline: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
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
        highlightPlayerPrice.setText(player.getFormattedPrice());
        highlightPlayerImage.setImageResource(player.getImageResId());
        
        // Kiểm tra xem cầu thủ đã được mua hay chưa
        checkPlayerOwnership(player);
    }

    // Phương thức kiểm tra xem cầu thủ đã được sở hữu hay chưa
    private void checkPlayerOwnership(Player player) {
        if (currentUser == null) {
            updatePlayerStatus(false, player);
            return;
        }

        String userId = currentUser.getUid();
        
        // Kiểm tra trong Firebase xem user đã sở hữu cầu thủ này hay chưa
        playerManager.getPlayersByUserId(userId, new PlayerManager.PlayersCallback() {
            @Override
            public void onSuccess(List<Player> ownedPlayers) {
                boolean owned = false;
                for (Player ownedPlayer : ownedPlayers) {
                    if (ownedPlayer.getId() == player.getId()) {
                        owned = true;
                        break;
                    }
                }
                updatePlayerStatus(owned, player);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Không thể kiểm tra trạng thái sở hữu cầu thủ", e);
                updatePlayerStatus(false, player);
            }
        });
    }

    // Cập nhật UI dựa trên trạng thái sở hữu cầu thủ
    private void updatePlayerStatus(boolean owned, Player player) {
        isPlayerOwned = owned;
        
        if (owned) {
            highlightPlayerStatus.setText("Đã sở hữu");
            highlightPlayerStatus.setTextColor(0xFF4CAF50); // Màu xanh lá
            btnBuyPlayer.setText("ĐÃ MUA");
            btnBuyPlayer.setEnabled(false);
            btnBuyPlayer.setBackgroundResource(android.R.color.darker_gray);
        } else {
            highlightPlayerStatus.setText("Có sẵn");
            highlightPlayerStatus.setTextColor(0xFF2196F3); // Màu xanh dương
            btnBuyPlayer.setText("MUA - " + player.getFormattedPrice());
            btnBuyPlayer.setEnabled(true);
            btnBuyPlayer.setBackgroundResource(R.drawable.rounded_green_button);
        }
    }

    // Phương thức xử lý logic mua cầu thủ
    private void buyPlayer(Player playerToBuy) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thực hiện hành động này", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem đã sở hữu cầu thủ này hay chưa
        if (isPlayerOwned) {
            Toast.makeText(getContext(), "Bạn đã sở hữu cầu thủ " + playerToBuy.getName() + " rồi!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        
        // Lấy dữ liệu người dùng từ Firestore
        userManager.getUserData(userId, new UserManager.UserDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                // Lấy số tiền hiện tại của người dùng
                int currentMoney;
                
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
                
                // Lấy giá cầu thủ từ dữ liệu
                int playerPrice = playerToBuy.getPriceUsd() > 0 ? playerToBuy.getPriceUsd() : playerToBuy.getOverall();
                
                if (currentMoney >= playerPrice) {
                    // Cập nhật số tiền mới
                    int newMoney = currentMoney - playerPrice;
                    
                    // Cập nhật tiền trong Firestore
                    userManager.updateMoney(userId, newMoney, new UserManager.UserDataCallback() {
                        @Override
                        public void onSuccess(Map<String, Object> userData) {
                            // Cập nhật SharedPreferences
                            SharedPreferences sharedPref = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt(KEY_MONEY, newMoney);
                            editor.apply();
                            
                            // Thêm cầu thủ vào đội hình của người chơi trong Firestore
                            playerManager.addPlayer(playerToBuy, userId, new PlayerManager.PlayerCallback() {
                                @Override
                                public void onSuccess(Player addedPlayer) {
                                    // Thông báo thành công
                                    Toast.makeText(getContext(), 
                                        "Đã mua " + playerToBuy.getName() + " với giá " + playerToBuy.getFormattedPrice() + 
                                        ". Số dư mới: $" + String.format("%,d", newMoney) +
                                        ". Cầu thủ đã được thêm vào đội của bạn!", 
                                        Toast.LENGTH_LONG).show();
                                    
                                    // Cập nhật tiền trên MainActivity thông qua interface
                                    if (moneyUpdateListener != null) {
                                        moneyUpdateListener.onMoneyUpdated(newMoney);
                                    }
                                    
                                    // Cập nhật trạng thái UI sau khi mua thành công
                                    updatePlayerStatus(true, playerToBuy);
                                    
                                    // Không xóa cầu thủ khỏi danh sách vì dữ liệu offline
                                    // Nhưng hiển thị trạng thái đã mua
                                }
                                
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(), "Lỗi khi thêm cầu thủ vào đội: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error adding player to team", e);
                                }
                            });
                        }
                        
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Lỗi khi cập nhật số tiền: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Lỗi cập nhật số tiền", e);
                        }
                    });
                } else {
                    // Thông báo không đủ tiền
                    Toast.makeText(getContext(), "Không đủ tiền để mua " + playerToBuy.getName() + ". Cần " + playerPrice + "$ nhưng bạn chỉ có " + currentMoney + "$", Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                // Sử dụng dữ liệu từ SharedPreferences nếu không thể lấy từ Firestore
                SharedPreferences sharedPref = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int currentMoney = sharedPref.getInt(KEY_MONEY, 2000);
                int playerPrice = playerToBuy.getOverall();
                
                if (currentMoney >= playerPrice) {
                    int newMoney = currentMoney - playerPrice;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(KEY_MONEY, newMoney);
                    editor.apply();
                    
                    Toast.makeText(getContext(), "Đã mua " + playerToBuy.getName() + " với giá " + playerPrice + "$. Số dư mới: " + newMoney + "$ (chưa đồng bộ)", Toast.LENGTH_LONG).show();
                    
                    if (moneyUpdateListener != null) {
                        moneyUpdateListener.onMoneyUpdated(newMoney);
                    }
                    
                    int playerIndex = playerList.indexOf(playerToBuy);
                    if (playerIndex != -1) {
                        playerList.remove(playerIndex);
                        playerAdapter.notifyItemRemoved(playerIndex);
                        
                        if (playerList.isEmpty()) {
                            highlightPlayerName.setText("");
                            highlightPlayerPosition.setText("");
                            highlightPlayerOverall.setText("");
                            highlightPlayerImage.setImageResource(android.R.color.transparent);
                            currentlySelectedPlayer = null;
                        } else if (currentlySelectedPlayer == playerToBuy) {
                            currentlySelectedPlayer = playerList.get(0);
                            updateHighlightCard(currentlySelectedPlayer);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Không đủ tiền để mua " + playerToBuy.getName() + ". Cần " + playerPrice + "$ nhưng bạn chỉ có " + currentMoney + "$", Toast.LENGTH_LONG).show();
                }
                
                Log.e(TAG, "Không thể lấy dữ liệu người dùng từ Firestore", e);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        moneyUpdateListener = null; // Ngăn chặn rò rỉ bộ nhớ khi Fragment bị tách khỏi Activity
    }
}
