package com.example.fms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.R;
import com.example.fms.model.Player;

import java.util.List;

public class SquadPositionAdapter extends RecyclerView.Adapter<SquadPositionAdapter.SquadPlayerViewHolder> {

    private List<Player> playerList;
    private OnRemovePlayerClickListener removeListener; // New listener for removal

    // Interface to listen for player removal from squad position
    public interface OnRemovePlayerClickListener { 
        void onRemovePlayerClick(Player player);
    }

    public SquadPositionAdapter(List<Player> playerList) {
        this.playerList = playerList;
    }

    // Constructor with removal listener
    public SquadPositionAdapter(List<Player> playerList, OnRemovePlayerClickListener removeListener) {
        this.playerList = playerList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public SquadPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.squad_position_item, parent, false);
        return new SquadPlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SquadPlayerViewHolder holder, int position) {
        Player player = playerList.get(position);
        holder.playerName.setText(player.getName());
        holder.playerOverall.setText(String.valueOf(player.getOverall()));
        
        // Cập nhật hiển thị ảnh với CardView mới
        holder.playerImage.setImageResource(player.getImageResId());

        // Đặt màu sắc cho thanh bên trái dựa trên overall
        int overall = player.getOverall();
        int markerColor;
        if (overall >= 80) {
            markerColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
        } else if (overall >= 70) {
            markerColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark);
        } else {
            markerColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark);
        }
        holder.positionMarker.setBackgroundColor(markerColor);

        // Set click listener to remove player
        holder.itemView.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemovePlayerClick(player);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    // Method to add a player to the list
    public void addPlayer(Player player) {
        if (!playerList.contains(player)) {
            playerList.add(player);
            notifyItemInserted(playerList.size() - 1);
        }
    }

    // Method to remove a player from the list
    public void removePlayer(Player player) {
        int index = playerList.indexOf(player);
        if (index != -1) {
            playerList.remove(index);
            notifyItemRemoved(index);
        }
    }

    public static class SquadPlayerViewHolder extends RecyclerView.ViewHolder {
        TextView playerName;
        TextView playerOverall;
        ImageView playerImage;
        View positionMarker;

        public SquadPlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.squad_player_name);
            playerOverall = itemView.findViewById(R.id.squad_player_overall);
            playerImage = itemView.findViewById(R.id.squad_player_image);
            positionMarker = itemView.findViewById(R.id.position_marker);
        }
    }
}
