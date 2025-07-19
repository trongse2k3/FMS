package com.example.fms.firebase;

import android.content.Context;
import android.util.Log;

import com.example.fms.model.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PlayerDataLoader {
    private static final String TAG = "PlayerDataLoader";
    private static final String PLAYERS_JSON_FILE = "players_data.json";
    
    public interface PlayerDataCallback {
        void onSuccess(List<Player> players);
        void onFailure(Exception e);
    }
    
    /**
     * Load dữ liệu cầu thủ từ file JSON trong assets
     */
    public static void loadPlayersFromAssets(Context context, PlayerDataCallback callback) {
        try {
            String json = loadJSONFromAsset(context, PLAYERS_JSON_FILE);
            List<Player> players = parsePlayersFromJSON(context, json);
            callback.onSuccess(players);
        } catch (Exception e) {
            Log.e(TAG, "Error loading players from assets", e);
            callback.onFailure(e);
        }
    }
    
    /**
     * Đọc file JSON từ assets
     */
    private static String loadJSONFromAsset(Context context, String fileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }
    
    /**
     * Parse dữ liệu JSON thành list Player
     */
    private static List<Player> parsePlayersFromJSON(Context context, String json) throws JSONException {
        List<Player> players = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            
            int id = jsonObject.getInt("id");
            String playerName = jsonObject.getString("player_name");
            int jerseyNumber = jsonObject.getInt("jersey_number");
            String position = jsonObject.getString("position");
            int speed = jsonObject.getInt("speed");
            int stamina = jsonObject.getInt("stamina");
            int skill = jsonObject.getInt("skill");
            int overall = jsonObject.getInt("overall");
            int priceUsd = jsonObject.getInt("price_usd");
            
            // Tính toán imageResId từ player ID
            int imageResId = Player.getPlayerImageResource(context, id);
            
            Player player = new Player(id, playerName, jerseyNumber, position, 
                                     speed, stamina, skill, overall, priceUsd, imageResId);
            players.add(player);
        }
        
        Log.d(TAG, "Loaded " + players.size() + " players from JSON");
        return players;
    }
    
    /**
     * Lọc cầu thủ theo vị trí
     */
    public static List<Player> filterPlayersByPosition(List<Player> players, String position) {
        List<Player> filteredPlayers = new ArrayList<>();
        for (Player player : players) {
            if (player.getPosition().equals(position)) {
                filteredPlayers.add(player);
            }
        }
        return filteredPlayers;
    }
    
    /**
     * Lọc cầu thủ theo khoảng giá
     */
    public static List<Player> filterPlayersByPriceRange(List<Player> players, int minPrice, int maxPrice) {
        List<Player> filteredPlayers = new ArrayList<>();
        for (Player player : players) {
            int price = player.getPriceUsd();
            if (price >= minPrice && price <= maxPrice) {
                filteredPlayers.add(player);
            }
        }
        return filteredPlayers;
    }
    
    /**
     * Sắp xếp cầu thủ theo overall giảm dần
     */
    public static List<Player> sortPlayersByOverall(List<Player> players) {
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getOverall(), p1.getOverall()));
        return sortedPlayers;
    }
    
    /**
     * Sắp xếp cầu thủ theo giá tăng dần  
     */
    public static List<Player> sortPlayersByPrice(List<Player> players) {
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p1.getPriceUsd(), p2.getPriceUsd()));
        return sortedPlayers;
    }
} 