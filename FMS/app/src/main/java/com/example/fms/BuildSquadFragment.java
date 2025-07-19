package com.example.fms;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton; // Import ImageButton

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.adapter.PlayerBuildAdapter;
import com.example.fms.firebase.ItemManager;
import com.example.fms.firebase.UserManager;
import com.example.fms.firebase.PlayerManager;
import com.example.fms.firebase.PlayerStatusManager;
import com.example.fms.model.Item;
import com.example.fms.model.Player;
import com.example.fms.model.PlayerStatus;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import android.app.AlertDialog;

// BuildSquadFragment implements PlayerBuildAdapter.OnAddPlayerClickListener
public class BuildSquadFragment extends Fragment implements PlayerBuildAdapter.OnAddPlayerClickListener, PlayerBuildAdapter.OnManagePlayerClickListener {

    private static final String TAG = "BuildSquadFragment";

    private RecyclerView recyclerViewGeneralPlayers;
    private PlayerBuildAdapter generalPlayerAdapter;
    private List<Player> generalPlayers; // Danh sách cầu thủ chung (đã sở hữu)

    private FrameLayout fieldPlayerContainer; // Container cho các icon cầu thủ trên sân
    private TextView tvSquadCount; // Hiển thị số lượng cầu thủ trong đội hình
    private AppCompatButton btnConfirmSquad; // Nút xác nhận đội hình

    private List<Player> squadPlayers; // Danh sách cầu thủ đã chọn vào đội hình

    // Biến tạm để lưu trữ View đang được kéo (để làm cho nó hiển thị lại sau khi kéo kết thúc nếu không thả hợp lệ)
    private View draggedPlayerIconView;

    // Firebase managers
    private UserManager userManager;
    private PlayerManager playerManager;
    private PlayerStatusManager playerStatusManager;
    private ItemManager itemManager;
    private FirebaseUser currentUser;

    public BuildSquadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_build_squad, container, false);

        // Khởi tạo Firebase managers
        userManager = new UserManager();
        playerManager = new PlayerManager();
        playerStatusManager = new PlayerStatusManager();
        itemManager = new ItemManager();
        currentUser = userManager.getCurrentUser();

        // Ánh xạ views
        fieldPlayerContainer = view.findViewById(R.id.field_player_container);
        tvSquadCount = view.findViewById(R.id.tv_squad_count);
        btnConfirmSquad = view.findViewById(R.id.btn_confirm_squad);
        recyclerViewGeneralPlayers = view.findViewById(R.id.recycler_view_general_players);
        recyclerViewGeneralPlayers.setLayoutManager(new LinearLayoutManager(getContext()));

        squadPlayers = new ArrayList<>(); // Khởi tạo danh sách đội hình

        // Khởi tạo danh sách cầu thủ rỗng
        generalPlayers = new ArrayList<>();
        generalPlayerAdapter = new PlayerBuildAdapter(generalPlayers, this, this);
        recyclerViewGeneralPlayers.setAdapter(generalPlayerAdapter);

        // Load danh sách cầu thủ đã sở hữu từ Firebase
        loadOwnedPlayers();

        // Thiết lập OnDragListener cho vùng sân bóng
        fieldPlayerContainer.setOnDragListener(new FieldPlayerDragListener());

        // Cập nhật số lượng cầu thủ ban đầu
        updateSquadCountText();

        btnConfirmSquad.setOnClickListener(v -> {
            if (squadPlayers.size() == 11) {
//                Toast.makeText(getContext(), "Đội hình 11 cầu thủ đã sẵn sàng!", Toast.LENGTH_SHORT).show();
                // TODO: Lưu đội hình đã chọn vào SharedPreferences hoặc Firestore
                // TODO: Chuyển sang màn hình tiếp theo (ví dụ: màn hình trận đấu)
            } else {
//                Toast.makeText(getContext(), "Đội hình chưa đủ 11 cầu thủ. Hiện có: " + squadPlayers.size() + "/11", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Load danh sách cầu thủ đã sở hữu từ Firebase
    private void loadOwnedPlayers() {
        if (currentUser == null) {
//            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem cầu thủ của bạn", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "User chưa đăng nhập");
            return;
        }

        String userId = currentUser.getUid();
        
        playerManager.getPlayersByUserId(userId, new PlayerManager.PlayersCallback() {
            @Override
            public void onSuccess(List<Player> ownedPlayers) {
                Log.d(TAG, "Load thành công " + ownedPlayers.size() + " cầu thủ đã sở hữu");
                
                if (ownedPlayers.isEmpty()) {
                    generalPlayers.clear();
                    generalPlayerAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(),
                        "Bạn chưa sở hữu cầu thủ nào. Hãy mua cầu thủ ở trang Player List!", 
                        Toast.LENGTH_LONG).show();
                } else {
                    // Tải trạng thái cho từng cầu thủ
                    loadStatusForPlayers(ownedPlayers);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi load cầu thủ đã sở hữu", e);
                Toast.makeText(getContext(),
                    "Lỗi khi tải danh sách cầu thủ: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                
                generalPlayers.clear();
                generalPlayerAdapter.notifyDataSetChanged();
            }
        });
    }

    // Tải trạng thái cho danh sách cầu thủ
    private void loadStatusForPlayers(List<Player> players) {
        final int totalPlayers = players.size();
        final int[] counter = {0};

        for (Player player : players) {
            String statusDocId = String.valueOf(player.getId());
            Log.d(TAG, "[DEBUG] Đọc status cho player: " + player.getName() + ", statusDocId=" + statusDocId);
            playerStatusManager.getPlayerStatus(statusDocId, new PlayerStatusManager.StatusCallback() {
                @Override
                public void onStatusLoaded(PlayerStatus status) {
                    Log.d(TAG, "[DEBUG] Player: " + player.getName() + ", injuryType=" + status.getInjuryType() + ", isSuspended=" + status.isSuspended() + ", statusObject=" + status);
                    player.setStatus(status);
                    finishLoading();
                }

                @Override
                public void onStatusNotFound() {
                    Log.d(TAG, "[DEBUG] Player: " + player.getName() + " không có status trên Firestore, tạo mặc định.");
                    player.setStatus(new PlayerStatus(player.getDocumentId()));
                    finishLoading();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Lỗi khi tải status cho cầu thủ " + player.getDocumentId(), e);
                    player.setStatus(new PlayerStatus(player.getDocumentId())); // Fallback
                    finishLoading();
                }

                private void finishLoading() {
                    counter[0]++;
                    if (counter[0] == totalPlayers) {
                        // Đã tải xong tất cả status, cập nhật UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                generalPlayers.clear();
                                generalPlayers.addAll(players);
                                generalPlayerAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(),
                                    "Đã tải xong trạng thái cầu thủ", 
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
            });
        }
    }

    // Phương thức public để refresh danh sách cầu thủ từ bên ngoài
    public void refreshOwnedPlayers() {
        loadOwnedPlayers();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh danh sách khi fragment được hiển thị lại
        if (currentUser != null) {
            loadOwnedPlayers();
        }
    }

    // Triển khai onAddPlayerClick từ PlayerBuildAdapter.OnAddPlayerClickListener (nút "+")
    @Override
    public void onAddPlayerClick(Player playerToAdd) {
        // Kiểm tra trạng thái cầu thủ trước khi thêm
        if (playerToAdd.getStatus() != null && !playerToAdd.getStatus().isAvailable()) {
            Toast.makeText(getContext(), playerToAdd.getName() + " không sẵn sàng thi đấu!", Toast.LENGTH_SHORT).show();
            onManagePlayerClick(playerToAdd); // Mở thẳng dialog quản lý
            return;
        }

        if (squadPlayers.size() >= 11) {
            Toast.makeText(getContext(), "Đội hình đã đủ 11 cầu thủ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (squadPlayers.contains(playerToAdd)) {
            Toast.makeText(getContext(), playerToAdd.getName() + " đã có trong đội hình.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo một icon cầu thủ mới trên sân
        addPlayerIconToField(playerToAdd);

        // Thêm cầu thủ vào danh sách đội hình
        squadPlayers.add(playerToAdd);
        // Xóa cầu thủ khỏi danh sách chung
        int originalIndex = generalPlayers.indexOf(playerToAdd);
        if (originalIndex != -1) {
            generalPlayers.remove(originalIndex);
            generalPlayerAdapter.notifyItemRemoved(originalIndex);
            generalPlayerAdapter.notifyItemRangeChanged(originalIndex, generalPlayers.size());
        }

        updateSquadCountText();
        Toast.makeText(getContext(), "Đã thêm " + playerToAdd.getName() + " vào đội hình.", Toast.LENGTH_SHORT).show();
    }

    // Triển khai onManagePlayerClick từ PlayerBuildAdapter.OnManagePlayerClickListener (nút "Manage")
    @Override
    public void onManagePlayerClick(Player player) {
        if (getContext() == null) return;

        // Lấy danh sách item c���a user
        itemManager.getUserItems(currentUser.getUid()).addOnSuccessListener(userItems -> {
            PlayerStatus status = player.getStatus();
            List<Item> applicableItems;

            // Lọc các item phù hợp
            if (status.getInjuryType() != PlayerStatus.InjuryType.NONE) {
                applicableItems = userItems.stream()
                                           .filter(item -> item.getType().isHealingItem())
                                           .collect(Collectors.toList());
            } else if (status.isSuspended()) {
                applicableItems = userItems.stream()
                                           .filter(item -> item.getType() == Item.ItemType.LAWYER)
                                           .collect(Collectors.toList());
            } else {
                Toast.makeText(getContext(), player.getName() + " không cần sử dụng vật phẩm.", Toast.LENGTH_SHORT).show();
                return;
            }

            showUseItemDialog(player, applicableItems);

        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi khi tải vật phẩm của bạn.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to load user items", e);
        });
    }

    private void showUseItemDialog(Player player, List<Item> items) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sử dụng vật phẩm cho " + player.getName());
        
        if (items.isEmpty()) {
            builder.setMessage("Bạn không có vật phẩm nào phù hợp.");
            builder.setPositiveButton("OK", null);
        } else {
            String[] itemNames = items.stream().map(Item::getName).toArray(String[]::new);
            builder.setItems(itemNames, (dialog, which) -> {
                Item selectedItem = items.get(which);
                useItemOnPlayer(player, selectedItem);
            });
            builder.setNegativeButton("Hủy", null);
        }
        
        builder.create().show();
    }

    private void useItemOnPlayer(Player player, Item item) {
        if (currentUser == null) return;

        // Gọi đến ItemManager để sử dụng item
        itemManager.useItem(currentUser.getUid(), item.getDocumentId(), player.getDocumentId())
            .addOnSuccessListener(success -> {
                Toast.makeText(getContext(), "Đã sử dụng " + item.getName() + " thành công!", Toast.LENGTH_SHORT).show();

                // Cập nhật trạng thái cầu thủ
                PlayerStatus status = player.getStatus();
                if (item.getType().isHealingItem()) {
                    status.recover();
                } else if (item.getType() == Item.ItemType.LAWYER) {
                    status.clearSuspension();
                }
                
                // Lưu trạng thái mới lên Firebase
                playerStatusManager.savePlayerStatus(status, new PlayerStatusManager.StatusUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        // Tải lại danh sách để cập nhật UI
                        loadOwnedPlayers();
                    }
                    @Override
                    public void onError(Exception e) {
                       Toast.makeText(getContext(), "Lỗi khi cập nhật trạng thái cầu thủ.", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Sử dụng vật phẩm thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    // Phương thức để thêm icon cầu thủ vào sân bóng
    private void addPlayerIconToField(Player player) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View playerIconView = inflater.inflate(R.layout.squad_player_icon, fieldPlayerContainer, false);

        ImageView iconImage = playerIconView.findViewById(R.id.player_icon_image);
        TextView iconName = playerIconView.findViewById(R.id.player_icon_name);
        ImageButton removeButton = playerIconView.findViewById(R.id.btn_remove_squad_player_icon);

        iconImage.setImageResource(player.getImageResId());
        iconName.setText(player.getName().split(" ")[0]); // Chỉ hiển thị tên đầu tiên

        // Lưu đối tượng Player vào tag của View để dễ dàng truy xuất khi kéo thả hoặc click
        playerIconView.setTag(player);
        // Sửa lỗi: Lưu View icon vào trường playerIconView của Player object
        player.setPlayerIconView(playerIconView); // Sử dụng setter của Player

        // Làm cho icon có thể kéo được
        playerIconView.setOnLongClickListener(v -> {
            ClipData.Item clipItem = new ClipData.Item(player.getName());
            ClipData dragData = new ClipData(
                    player.getName(),
                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                    clipItem
            );
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);

            // Lưu View đang được kéo vào biến tạm thời
            draggedPlayerIconView = v;
            v.setVisibility(View.INVISIBLE); // Ẩn View gốc khi bắt đầu kéo

            // Bắt đầu thao tác kéo, truyền Player làm localState
            v.startDragAndDrop(dragData, shadowBuilder, player, 0);
            return true;
        });

        // Xử lý click vào icon để hiển thị thông tin
        playerIconView.setOnClickListener(v -> {
            Player p = (Player) v.getTag();
            if (p != null) {
                Toast.makeText(getContext(), "Tên: " + p.getName() + ", Vị trí: " + p.getPosition() + ", Overall: " + p.getOverall(), Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý click nút X để xóa cầu thủ khỏi đội hình
        removeButton.setOnClickListener(v -> {
            Player pToRemove = (Player) playerIconView.getTag(); // Lấy Player từ tag của icon View
            if (pToRemove != null) {
                removePlayerFromSquad(pToRemove, playerIconView); // Truyền player và view của nó
            }
        });

        // Thêm icon vào container trên sân
        // Cần đảm bảo fieldPlayerContainer đã được layout để có kích thước
        fieldPlayerContainer.post(() -> {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            // Đặt vị trí ban đầu ngẫu nhiên hoặc ở một vị trí mặc định
            // Cần đảm bảo vị trí này nằm trong giới hạn của fieldPlayerContainer
            params.leftMargin = (int) (Math.random() * (fieldPlayerContainer.getWidth() * 0.8)); // 80% chiều rộng
            params.topMargin = (int) (Math.random() * (fieldPlayerContainer.getHeight() * 0.8)); // 80% chiều cao
            params.gravity = Gravity.TOP | Gravity.LEFT; // Đảm bảo vị trí được áp dụng
            fieldPlayerContainer.addView(playerIconView, params);

            // Hiển thị nút xóa
            removeButton.setVisibility(View.VISIBLE);
        });
    }

    // Phương thức xóa cầu thủ khỏi đội hình trên sân
    private void removePlayerFromSquad(Player playerToRemove, View playerIconView) {
        if (squadPlayers.contains(playerToRemove)) {
            squadPlayers.remove(playerToRemove);
            fieldPlayerContainer.removeView(playerIconView); // Xóa icon khỏi sân

            // Thêm lại cầu thủ vào danh sách chung
            generalPlayers.add(playerToRemove);
            generalPlayerAdapter.notifyItemInserted(generalPlayers.size() - 1);

            updateSquadCountText();
            Toast.makeText(getContext(), "Đã xóa " + playerToRemove.getName() + " khỏi đội hình.", Toast.LENGTH_SHORT).show();
        }
    }

    // Lớp xử lý kéo thả trên sân bóng
    private class FieldPlayerDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(Color.parseColor("#330000FF")); // Màu xanh mờ khi kéo vào
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(Color.TRANSPARENT);
                    return true;
                case DragEvent.ACTION_DROP:
                    Player droppedPlayer = (Player) event.getLocalState(); // Lấy đối tượng Player

                    // Nếu đây là cầu thủ mới từ danh sách chung (chưa có trên sân)
                    // Logic này sẽ không được gọi nếu bạn thêm bằng nút "+",
                    // nhưng hữu ích nếu bạn muốn kéo trực tiếp từ danh sách chung.
                    // Hiện tại, chúng ta sẽ chỉ xử lý việc di chuyển cầu thủ đã có trên sân.
                    if (!squadPlayers.contains(droppedPlayer)) {
                        Toast.makeText(getContext(), "Vui lòng sử dụng nút '+' để thêm cầu thủ.", Toast.LENGTH_SHORT).show();
                        if (draggedPlayerIconView != null) {
                            draggedPlayerIconView.setVisibility(View.VISIBLE); // Hiển thị lại View gốc nếu không thả hợp lệ
                        }
                        v.setBackgroundColor(Color.TRANSPARENT);
                        return false;
                    } else {
                        // Đây là cầu thủ đã có trên sân, di chuyển nó
                        View playerIconView = droppedPlayer.getPlayerIconView(); // Sửa lỗi: Lấy View từ Player object
                        if (playerIconView != null) {
                            // Cập nhật vị trí của View
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) playerIconView.getLayoutParams();
                            // Điều chỉnh vị trí để tâm của icon trùng với điểm kéo
                            params.leftMargin = (int) event.getX() - (playerIconView.getWidth() / 2);
                            params.topMargin = (int) event.getY() - (playerIconView.getHeight() / 2);

                            // Giới hạn leftMargin
                            params.leftMargin = Math.max(0, Math.min(params.leftMargin, fieldPlayerContainer.getWidth() - playerIconView.getWidth()));
                            // Giới hạn topMargin
                            params.topMargin = Math.max(0, Math.min(params.topMargin, fieldPlayerContainer.getHeight() - playerIconView.getHeight()));

                            playerIconView.setLayoutParams(params);
                            playerIconView.setVisibility(View.VISIBLE); // Đảm bảo hiển thị lại
                            Toast.makeText(getContext(), "Đã di chuyển " + droppedPlayer.getName(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    v.setBackgroundColor(Color.TRANSPARENT);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    // Nếu thao tác kéo không thành công (không được thả vào vùng hợp lệ)
                    if (!event.getResult() && draggedPlayerIconView != null) {
                        draggedPlayerIconView.setVisibility(View.VISIBLE); // Hiển thị lại View gốc
                    }
                    v.setBackgroundColor(Color.TRANSPARENT);
                    draggedPlayerIconView = null; // Xóa tham chiếu
                    return true;
                default:
                    return false;
            }
        }
    }

    // Phương thức để cập nhật text của nút xác nhận đội hình
    private void updateSquadCountText() {
        tvSquadCount.setText("Đội hình: " + squadPlayers.size() + "/11");
        btnConfirmSquad.setText("XÁC NHẬN ĐỘI HÌNH (" + squadPlayers.size() + "/11)");
    }
}
