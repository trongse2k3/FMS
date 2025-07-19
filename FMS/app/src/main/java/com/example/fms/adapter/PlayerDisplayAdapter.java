package com.example.fms.adapter;

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

public class PlayerDisplayAdapter extends RecyclerView.Adapter<PlayerDisplayAdapter.PlayerDisplayViewHolder> {

    private List<Player> playerList;
    private OnPlayerClickListener clickListener;

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
    }

    public PlayerDisplayAdapter(List<Player> playerList, OnPlayerClickListener clickListener) {
        this.playerList = playerList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PlayerDisplayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout player_list_display_item.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_list_display_item, parent, false);
        return new PlayerDisplayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerDisplayViewHolder holder, int position) {
        Player player = playerList.get(position);
        holder.playerName.setText(player.getName());
        holder.playerOverall.setText("OVR: " + player.getOverall()); // Thêm "OVR:"
        holder.playerPos.setText(player.getPosition());
        holder.playerPrice.setText(player.getFormattedPrice());
        holder.playerImage.setImageResource(player.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPlayerClick(player);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playerList != null ? playerList.size() : 0;
    }

    public static class PlayerDisplayViewHolder extends RecyclerView.ViewHolder {
        TextView playerName;
        TextView playerOverall;
        TextView playerPos;
        TextView playerPrice;
        ImageView playerImage;

        public PlayerDisplayViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.player_name);
            playerOverall = itemView.findViewById(R.id.player_overall);
            playerPos = itemView.findViewById(R.id.player_pos);
            playerPrice = itemView.findViewById(R.id.player_price);
            playerImage = itemView.findViewById(R.id.player_image);
        }
    }
}
