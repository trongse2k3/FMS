package com.example.fms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fms.R;
import com.example.fms.model.MatchResult;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchHistoryAdapter extends RecyclerView.Adapter<MatchHistoryAdapter.MatchHistoryViewHolder> {

    private List<MatchResult> matchResults;

    public MatchHistoryAdapter(List<MatchResult> matchResults) {
        this.matchResults = matchResults;
    }

    @NonNull
    @Override
    public MatchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_schedule_item, parent, false);
        return new MatchHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchHistoryViewHolder holder, int position) {
        MatchResult matchResult = matchResults.get(position);
        holder.bind(matchResult);
    }

    @Override
    public int getItemCount() {
        return matchResults.size();
    }

    public void setMatchResults(List<MatchResult> matchResults) {
        this.matchResults = matchResults;
        notifyDataSetChanged();
    }

    static class MatchHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHomeTeam, tvAwayTeam, tvScore, tvMatchDate;

        public MatchHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHomeTeam = itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = itemView.findViewById(R.id.tv_match_away_team);
            tvScore = itemView.findViewById(R.id.tv_match_score);
            tvMatchDate = itemView.findViewById(R.id.tv_match_date);
        }

        public void bind(MatchResult matchResult) {
            tvHomeTeam.setText(matchResult.getHomeTeam().getName());
            tvAwayTeam.setText(matchResult.getAwayTeam().getName());
            tvScore.setText(String.format(Locale.getDefault(), "%d - %d", matchResult.getHomeScore(), matchResult.getAwayScore()));

            if (matchResult.getMatchDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String dateString = sdf.format(matchResult.getMatchDate());
                tvMatchDate.setText(dateString);
            } else {
                tvMatchDate.setText("N/A");
            }
        }
    }
}
