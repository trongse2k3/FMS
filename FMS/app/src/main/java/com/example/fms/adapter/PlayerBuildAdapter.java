package com.example.fms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.R;
import com.example.fms.model.Player;

import java.util.List;

public class PlayerBuildAdapter extends RecyclerView.Adapter<PlayerBuildAdapter.PlayerBuildViewHolder> {

    private List<Player> playerList;
    private OnAddPlayerClickListener addPlayerClickListener;

    public interface OnAddPlayerClickListener {
        void onAddPlayerClick(Player player);
    }

    public PlayerBuildAdapter(List<Player> playerList, OnAddPlayerClickListener addPlayerClickListener) {
        this.playerList = playerList;
        this.addPlayerClickListener = addPlayerClickListener;
    }

    @NonNull
    @Override
    public PlayerBuildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout player_list_build_item.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_list_build_item, parent, false);
        return new PlayerBuildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerBuildViewHolder holder, int position) {
        Player player = playerList.get(position);
        holder.playerName.setText(player.getName());
        holder.playerOverall.setText("OVR: " + player.getOverall()); // Thêm "OVR:"
        holder.playerPos.setText(player.getPosition());
        holder.playerImage.setImageResource(player.getImageResId());

        holder.btnAddPlayer.setOnClickListener(v -> {
            if (addPlayerClickListener != null) {
                addPlayerClickListener.onAddPlayerClick(player);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playerList != null ? playerList.size() : 0;
    }

    public static class PlayerBuildViewHolder extends RecyclerView.ViewHolder {
        TextView playerName;
        TextView playerOverall;
        TextView playerPos;
        ImageView playerImage;
        AppCompatButton btnAddPlayer;

        public PlayerBuildViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.player_name);
            playerOverall = itemView.findViewById(R.id.player_overall);
            playerPos = itemView.findViewById(R.id.player_pos);
            playerImage = itemView.findViewById(R.id.player_image);
            btnAddPlayer = itemView.findViewById(R.id.btn_add_player);
        }
    }
}
