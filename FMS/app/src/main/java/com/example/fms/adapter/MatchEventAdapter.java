package com.example.fms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fms.R;
import com.example.fms.model.MatchEvent;
import java.util.List;

public class MatchEventAdapter extends RecyclerView.Adapter<MatchEventAdapter.EventViewHolder> {
    private List<MatchEvent> events;

    public MatchEventAdapter(List<MatchEvent> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        MatchEvent event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<MatchEvent> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged();
    }

    public void addEvent(MatchEvent event) {
        events.add(0, event);
        notifyItemInserted(0);
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventText;
        TextView tvMinute;
        TextView tvEventType;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventText = itemView.findViewById(R.id.tv_event_text);
            tvMinute = itemView.findViewById(R.id.tv_minute);
            tvEventType = itemView.findViewById(R.id.tv_event_type);
        }

        public void bind(MatchEvent event) {
            tvMinute.setText(event.getMinute() + "'");
            tvEventType.setText(event.getType().getEmoji());
            tvEventText.setText(event.getFormattedEvent());
            
            // Đổi màu theo loại sự kiện
            int backgroundColor = getBackgroundColorForEvent(event.getType());
            itemView.setBackgroundColor(backgroundColor);
        }
        
        private int getBackgroundColorForEvent(MatchEvent.EventType eventType) {
            switch (eventType) {
                case GOAL:
                    return 0xFF4CAF50; // Green
                case YELLOW_CARD:
                    return 0xFFFFEB3B; // Yellow
                case RED_CARD:
                    return 0xFFF44336; // Red
                case INJURY:
                    return 0xFFFF9800; // Orange
                case SAVE:
                    return 0xFF2196F3; // Blue
                default:
                    return 0xFFE0E0E0; // Light Gray
            }
        }
    }
} 