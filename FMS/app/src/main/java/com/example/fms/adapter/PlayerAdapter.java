package com.example.fms.adapter;

import android.content.ClipData; // Import ClipData
import android.content.ClipDescription; // Import ClipDescription
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.R;
import com.example.fms.model.Player;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private List<Player> playerList;
    private OnPlayerClickListener clickListener;
    private OnPlayerLongClickListener longClickListener; // Khai báo long click listener

    // Interface để lắng nghe sự kiện click đơn
    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
    }

    // Interface để lắng nghe sự kiện nhấn giữ (long click) và bắt đầu kéo
    public interface OnPlayerLongClickListener {
        void onPlayerLongClick(Player player, View view);
    }

    // Constructor chính, sử dụng cho cả click đơn và long click
    public PlayerAdapter(List<Player> playerList, OnPlayerClickListener clickListener, OnPlayerLongClickListener longClickListener) {
        this.playerList = playerList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    // Constructor phụ, nếu chỉ cần click đơn (không có long click/kéo thả)
    public PlayerAdapter(List<Player> playerList, OnPlayerClickListener clickListener) {
        this(playerList, clickListener, null); // Gọi constructor chính với longClickListener là null
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_list_item, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = playerList.get(position);
        holder.playerName.setText(player.getName());
        holder.playerOverall.setText(String.valueOf(player.getOverall()));
        holder.playerPos.setText(player.getPosition());
        holder.playerImage.setImageResource(player.getImageResId());

        // Xử lý sự kiện click đơn cho từng item
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPlayerClick(player);
            }
        });

        // Xử lý sự kiện nhấn giữ (long click) để bắt đầu kéo
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onPlayerLongClick(player, v);
                return true; // Trả về true để tiêu thụ sự kiện long click
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        // Đảm bảo playerList không null trước khi gọi size()
        return playerList != null ? playerList.size() : 0;
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView playerName;
        TextView playerOverall;
        TextView playerPos;
        ImageView playerImage;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.player_name);
            playerOverall = itemView.findViewById(R.id.player_overall);
            playerPos = itemView.findViewById(R.id.player_pos);
            playerImage = itemView.findViewById(R.id.player_image);
        }
    }
}
