package com.example.fms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView; // Nếu có icon VS

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.R;
import com.example.fms.model.Match;

import java.util.List;

public class MatchScheduleAdapter extends RecyclerView.Adapter<MatchScheduleAdapter.MatchViewHolder> {

    private List<Match> matchList;

    public MatchScheduleAdapter(List<Match> matchList) {
        this.matchList = matchList;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_schedule_item, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matchList.get(position);
        holder.homeTeamTextView.setText(match.getHomeTeam());
        holder.awayTeamTextView.setText(match.getAwayTeam());
        // holder.vsIconImageView.setImageResource(R.drawable.ic_vs); // Nếu có icon VS
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView homeTeamTextView;
        TextView awayTeamTextView;
        ImageView vsIconImageView; // Nếu có icon VS

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            homeTeamTextView = itemView.findViewById(R.id.tv_match_home_team);
            awayTeamTextView = itemView.findViewById(R.id.tv_match_away_team);
            vsIconImageView = itemView.findViewById(R.id.iv_vs_icon); // Ánh xạ icon VS
        }
    }
}
