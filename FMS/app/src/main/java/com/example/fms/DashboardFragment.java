package com.example.fms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class DashboardFragment extends Fragment {
    public DashboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        // Ánh xạ các view
        CardView cardStadium = view.findViewById(R.id.stadium_card);
        CardView cardPlayers = view.findViewById(R.id.card_players);
        CardView cardSquad = view.findViewById(R.id.card_squad);
        CardView cardMatch = view.findViewById(R.id.card_match);
        CardView cardShop = view.findViewById(R.id.card_shop);
        Button btnJoinLeague = view.findViewById(R.id.btn_join_league);
        
        // Xử lý sự kiện click cho thẻ sân vận động
        cardStadium.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng sân vận động đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        // Xử lý sự kiện click cho thẻ cầu thủ
        cardPlayers.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.playerListFragment);
        });
        
        // Xử lý sự kiện click cho thẻ đội hình
        cardSquad.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.buildSquadFragment);
        });
        
        // Xử lý sự kiện click cho thẻ trận đấu
        cardMatch.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.matchFragment);
        });
        
        // Xử lý sự kiện click cho thẻ cửa hàng
        cardShop.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.shopFragment);
        });
        
        // Xử lý sự kiện click cho nút tham gia giải đấu
        btnJoinLeague.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng giải đấu đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        return view;
    }
}
