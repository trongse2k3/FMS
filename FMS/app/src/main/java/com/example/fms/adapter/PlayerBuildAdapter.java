package com.example.fms.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.R;
import com.example.fms.model.Player;
import com.example.fms.model.PlayerStatus;

import java.util.List;

public class PlayerBuildAdapter extends RecyclerView.Adapter<PlayerBuildAdapter.PlayerBuildViewHolder> {

    private List<Player> playerList;
    private OnAddPlayerClickListener addPlayerClickListener;
    private OnManagePlayerClickListener managePlayerClickListener;

    public interface OnAddPlayerClickListener {
        void onAddPlayerClick(Player player);
    }

    public interface OnManagePlayerClickListener {
        void onManagePlayerClick(Player player);
    }

    public PlayerBuildAdapter(List<Player> playerList, OnAddPlayerClickListener addPlayerClickListener, OnManagePlayerClickListener managePlayerClickListener) {
        this.playerList = playerList;
        this.addPlayerClickListener = addPlayerClickListener;
        this.managePlayerClickListener = managePlayerClickListener;
    }

    @NonNull
    @Override
    public PlayerBuildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_list_build_item, parent, false);
        return new PlayerBuildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerBuildViewHolder holder, int position) {
        Player player = playerList.get(position);
        holder.playerName.setText(player.getName());
        holder.playerOverall.setText("OVR: " + player.getOverall());
        holder.playerPos.setText(player.getPosition());
        holder.playerImage.setImageResource(player.getImageResId());

        PlayerStatus status = player.getStatus(); // Giả sử Player có phương thức getStatus()

        if (status != null && !status.isFit()) {
            // Cầu thủ có vấn đề (chấn thương hoặc treo giò)
            holder.statusContainer.setVisibility(View.VISIBLE);
            holder.playerStatus.setText(status.getShortDescription());
            
            // Đặt màu nền cho container trạng thái
            if (status.isSuspended()) {
                holder.statusContainer.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Đỏ nhạt
            } else {
                holder.statusContainer.setCardBackgroundColor(Color.parseColor("#FCE4EC")); // Hồng nhạt
            }

            holder.btnAddPlayer.setVisibility(View.GONE);
            holder.btnManagePlayer.setVisibility(View.VISIBLE);

            holder.btnManagePlayer.setOnClickListener(v -> {
                if (managePlayerClickListener != null) {
                    managePlayerClickListener.onManagePlayerClick(player);
                }
            });
        } else {
            // Cầu thủ bình thường
            holder.statusContainer.setVisibility(View.GONE);
            holder.btnAddPlayer.setVisibility(View.VISIBLE);
            holder.btnManagePlayer.setVisibility(View.GONE);

            holder.btnAddPlayer.setOnClickListener(v -> {
                if (addPlayerClickListener != null) {
                    addPlayerClickListener.onAddPlayerClick(player);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return playerList != null ? playerList.size() : 0;
    }

    public static class PlayerBuildViewHolder extends RecyclerView.ViewHolder {
        TextView playerName, playerOverall, playerPos, playerStatus;
        ImageView playerImage;
        AppCompatButton btnAddPlayer, btnManagePlayer;
        CardView statusContainer;

        public PlayerBuildViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.player_name);
            playerOverall = itemView.findViewById(R.id.player_overall);
            playerPos = itemView.findViewById(R.id.player_pos);
            playerStatus = itemView.findViewById(R.id.player_status);
            playerImage = itemView.findViewById(R.id.player_image);
            btnAddPlayer = itemView.findViewById(R.id.btn_add_player);
            btnManagePlayer = itemView.findViewById(R.id.btn_manage_player);
            statusContainer = itemView.findViewById(R.id.status_container);
        }
    }
}
