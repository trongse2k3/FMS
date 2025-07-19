package com.example.fms.firebase;

import android.content.Context;
import android.util.Log;

import com.example.fms.R;
import com.example.fms.model.Player;
import com.example.fms.model.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TeamDataLoader {
    private static final String TAG = "TeamDataLoader";
    private static final String TEAMS_JSON_FILE = "teams_data.json";
    
    private Context context;
    private Random random;
    
    public TeamDataLoader(Context context) {
        this.context = context;
        this.random = new Random();
    }
    
    public interface TeamsLoadCallback {
        void onSuccess(List<Team> teams);
        void onFailure(Exception e);
    }
    
    public interface TeamLoadCallback {
        void onSuccess(Team team);
        void onFailure(Exception e);
    }
    
    /**
     * Load tất cả đội bóng từ assets/teams_data.json
     */
    public void loadTeamsFromAssets(TeamsLoadCallback callback) {
        try {
            // Đọc file JSON từ assets
            InputStream inputStream = context.getAssets().open(TEAMS_JSON_FILE);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            
            // Parse JSON thành List<Team>
            List<Team> teams = parseTeamsFromJson(jsonString);
            
            // Sinh cầu thủ cho mỗi đội
            for (Team team : teams) {
                generatePlayersForTeam(team);
            }
            
            Log.d(TAG, "Đã load " + teams.size() + " đội bóng từ assets");
            callback.onSuccess(teams);
            
        } catch (IOException e) {
            Log.e(TAG, "Lỗi đọc file " + TEAMS_JSON_FILE, e);
            callback.onFailure(e);
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi parse JSON " + TEAMS_JSON_FILE, e);
            callback.onFailure(e);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi load teams", e);
            callback.onFailure(e);
        }
    }
    
    /**
     * Lấy đội bóng ngẫu nhiên để đấu
     */
    public void getRandomTeam(TeamLoadCallback callback) {
        loadTeamsFromAssets(new TeamsLoadCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                if (teams.isEmpty()) {
                    callback.onFailure(new Exception("Không có đội bóng nào"));
                    return;
                }
                
                // Chọn ngẫu nhiên một đội
                Team randomTeam = teams.get(random.nextInt(teams.size()));
                Log.d(TAG, "Chọn đội ngẫu nhiên: " + randomTeam.getName());
                callback.onSuccess(randomTeam);
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy đội bóng theo ID
     */
    public void getTeamById(int teamId, TeamLoadCallback callback) {
        loadTeamsFromAssets(new TeamsLoadCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                for (Team team : teams) {
                    if (team.getId() == teamId) {
                        callback.onSuccess(team);
                        return;
                    }
                }
                callback.onFailure(new Exception("Không tìm thấy đội với ID: " + teamId));
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Parse JSON thành List<Team>
     */
    private List<Team> parseTeamsFromJson(String jsonString) throws JSONException {
        List<Team> teams = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject teamJson = jsonArray.getJSONObject(i);
            
            int id = teamJson.getInt("id");
            String name = teamJson.getString("name");
            String country = teamJson.getString("country");
            String logoResName = teamJson.getString("logoResId");
            int rating = teamJson.getInt("rating");
            String formation = teamJson.getString("formation");
            
            // Convert tên resource thành resource ID
            int logoResId = getLogoResourceId(logoResName);
            
            Team team = new Team(id, name, country, logoResId, rating, formation);
            teams.add(team);
        }
        
        return teams;
    }
    
    /**
     * Sinh cầu thủ cho đội (11 cầu thủ)
     */
    private void generatePlayersForTeam(Team team) {
        // Danh sách tên cầu thủ mẫu
        String[] firstNames = {"John", "Carlos", "Ahmed", "Luca", "Pierre", "Hans", "Yuki", "Diego", "Ivan", "Marco", "Andre"};
        String[] lastNames = {"Smith", "Rodriguez", "Hassan", "Romano", "Martin", "Mueller", "Tanaka", "Silva", "Petrov", "Gonzalez", "Santos"};
        
        // Các vị trí cần thiết cho đội hình 4-3-3
        String[] positions = {"GK", "DF", "DF", "DF", "DF", "MF", "MF", "MF", "FW", "FW", "FW"};
        
        for (int i = 0; i < 11; i++) {
            String firstName = firstNames[i % firstNames.length];
            String lastName = lastNames[(i + team.getId()) % lastNames.length];
            String playerName = firstName + " " + lastName;
            String position = positions[i];
            
            // Rating cầu thủ dựa trên rating đội +/- 5
            int playerRating = Math.max(60, Math.min(99, team.getRating() + random.nextInt(11) - 5));
            
            // Sử dụng image ngẫu nhiên từ p_1.png đến p_60.png
            int imageId = 1 + random.nextInt(60);
            int imageResId = getPlayerImageResource(imageId);
            
            Player player = new Player(playerName, position, playerRating, imageResId);
            team.addPlayer(player);
        }
        
        Log.d(TAG, "Đã sinh " + team.getPlayerCount() + " cầu thủ cho đội " + team.getName());
    }
    
    /**
     * Convert tên logo resource thành resource ID
     */
    private int getLogoResourceId(String logoResName) {
        switch (logoResName) {
            case "logo_1": return R.drawable.logo_1;
            case "img_logo_1": return R.drawable.img_logo_1;
            case "img_logo_2": return R.drawable.img_logo_2;
            case "img_logo_3": return R.drawable.img_logo_3;
            case "img_logo_4": return R.drawable.img_logo_4;
            case "img_logo_5": return R.drawable.img_logo_5;
            case "img_logo_6": return R.drawable.img_logo_6;
            default: return R.drawable.logo_1;
        }
    }
    
    /**
     * Lấy resource ID cho hình ảnh cầu thủ
     */
    private int getPlayerImageResource(int imageId) {
        // Sử dụng pattern tương tự PlayerDataLoader
        String resourceName = "p_" + imageId;
        int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        
        // Nếu không tìm thấy resource, dùng mặc định
        if (resourceId == 0) {
            switch (imageId % 10) {
                case 1: return R.drawable.p_1;
                case 2: return R.drawable.p_2;
                case 3: return R.drawable.p_3;
                case 4: return R.drawable.p_4;
                case 5: return R.drawable.p_5;
                case 6: return R.drawable.p_6;
                case 7: return R.drawable.p_7;
                case 8: return R.drawable.p_8;
                case 9: return R.drawable.p_9;
                default: return R.drawable.p_10;
            }
        }
        
        return resourceId;
    }
    
    /**
     * Lọc đội bóng theo quốc gia
     */
    public void getTeamsByCountry(String country, TeamsLoadCallback callback) {
        loadTeamsFromAssets(new TeamsLoadCallback() {
            @Override
            public void onSuccess(List<Team> allTeams) {
                List<Team> filteredTeams = new ArrayList<>();
                for (Team team : allTeams) {
                    if (team.getCountry().equalsIgnoreCase(country)) {
                        filteredTeams.add(team);
                    }
                }
                callback.onSuccess(filteredTeams);
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lọc đội bóng theo rating
     */
    public void getTeamsByRatingRange(int minRating, int maxRating, TeamsLoadCallback callback) {
        loadTeamsFromAssets(new TeamsLoadCallback() {
            @Override
            public void onSuccess(List<Team> allTeams) {
                List<Team> filteredTeams = new ArrayList<>();
                for (Team team : allTeams) {
                    if (team.getRating() >= minRating && team.getRating() <= maxRating) {
                        filteredTeams.add(team);
                    }
                }
                callback.onSuccess(filteredTeams);
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
} 