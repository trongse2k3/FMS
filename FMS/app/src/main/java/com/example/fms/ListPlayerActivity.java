package com.example.fms;

import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent; // Import for starting new activities
import android.view.MenuItem; // Import for menu item selection

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fms.adapter.PlayerAdapter;
import com.example.fms.model.Player;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ListPlayerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PlayerAdapter playerAdapter;
    private List<Player> playerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_player);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_players);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Prepare player data (replace with actual data fetching)
        playerList = new ArrayList<>();
        playerList.add(new Player("Criss Ronnaldo", "FW", 94));
        playerList.add(new Player("Kyllen Mbapnu", "FW", 93));
        playerList.add(new Player("Earling Halaad", "FW", 92));
        playerList.add(new Player("Neymah Jr", "FW", 91));
        // Add more players as needed

        playerAdapter = new PlayerAdapter(playerList);
        recyclerView.setAdapter(playerAdapter);

        // Initialize Floating Action Button
        FloatingActionButton fabMatch = findViewById(R.id.fab_match);
        fabMatch.setOnClickListener(v -> {
            Toast.makeText(ListPlayerActivity.this, "Nút MATCH đã được nhấp!", Toast.LENGTH_SHORT).show();
        });

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        // Set the selected item based on the current activity
        bottomNavView.setSelectedItemId(R.id.navigation_player_list);

        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_player_list) {
                    // Already on this screen, do nothing or refresh
                    Toast.makeText(ListPlayerActivity.this, "Bạn đang ở màn hình Danh sách người chơi", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.navigation_build_team) {
                    // Navigate to BuildSquadActivity
                    Toast.makeText(ListPlayerActivity.this, "Chuyển đến Xây dựng đội hình", Toast.LENGTH_SHORT).show();
                    // Intent intent = new Intent(ListPlayerActivity.this, BuildSquadActivity.class);
                    // startActivity(intent);
                    // finish(); // Optional: finish current activity if you don't want it on back stack
                    return true;
                } else if (itemId == R.id.navigation_manage_team) {
                    // Navigate to ManageTeamActivity
                    Toast.makeText(ListPlayerActivity.this, "Chuyển đến Quản lý đội", Toast.LENGTH_SHORT).show();
                    // Intent intent = new Intent(ListPlayerActivity.this, ManageTeamActivity.class);
                    // startActivity(intent);
                    // finish();
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    // Navigate to SettingsActivity
                    Toast.makeText(ListPlayerActivity.this, "Chuyển đến Cài đặt", Toast.LENGTH_SHORT).show();
                    // Intent intent = new Intent(ListPlayerActivity.this, SettingsActivity.class);
                    // startActivity(intent);
                    // finish();
                    return true;
                }
                return false;
            }
        });
    }
}

