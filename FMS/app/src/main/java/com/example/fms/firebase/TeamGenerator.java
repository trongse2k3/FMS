package com.example.fms.firebase;

import android.content.Context;
import android.util.Log;

import com.example.fms.R;
import com.example.fms.model.Player;
import com.example.fms.model.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TeamGenerator {
    private static final String TAG = "TeamGenerator";
    
    // Danh sách tên đội bóng
    private static final String[] TEAM_NAMES = {
        "FC Barcelona", "Real Madrid", "Manchester United", "Liverpool FC", 
        "Bayern Munich", "Paris Saint-Germain", "Chelsea FC", "Manchester City",
        "Arsenal FC", "Juventus FC"
    };
    
    // Danh sách quốc gia tương ứng
    private static final String[] COUNTRIES = {
        "Spain", "Spain", "England", "England",
        "Germany", "France", "England", "England", 
        "England", "Italy"
    };
    
    // Danh sách sơ đồ chiến thuật
    private static final String[] FORMATIONS = {
        "4-3-3", "4-4-2", "3-5-2", "4-2-3-1", "5-3-2"
    };
    
    // Danh sách resource ID cho logo (logo_1.png đến logo_10.png)
    private static final int[] LOGO_RESOURCES = {
        R.drawable.logo_1, R.drawable.img_logo_1, R.drawable.img_logo_2, R.drawable.img_logo_3,
        R.drawable.img_logo_4, R.drawable.img_logo_5, R.drawable.img_logo_6
    };
    
    private Random random;
    
    public TeamGenerator() {
        this.random = new Random();
    }
    
    /**
     * Sinh ra danh sách 10 đội bóng với logo từ logo_1.png đến logo_10.png
     */
    public List<Team> generateTeams() {
        List<Team> teams = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            Team team = generateSingleTeam(i + 1);
            teams.add(team);
            Log.d(TAG, "Đã sinh đội: " + team.toString());
        }
        
        Log.i(TAG, "Đã sinh tổng cộng " + teams.size() + " đội bóng");
        return teams;
    }
    
    /**
     * Sinh ra một đội bóng duy nhất
     */
    private Team generateSingleTeam(int teamId) {
        String teamName = TEAM_NAMES[(teamId - 1) % TEAM_NAMES.length];
        String country = COUNTRIES[(teamId - 1) % COUNTRIES.length];
        String formation = FORMATIONS[random.nextInt(FORMATIONS.length)];
        
        // Chọn logo theo thứ tự từ logo_1.png đến logo_10.png
        int logoResId = getLogoResourceId(teamId);
        
        // Rating ngẫu nhiên từ 75-95
        int rating = 75 + random.nextInt(21);
        
        Team team = new Team(teamId, teamName, country, logoResId, rating, formation);
        
        // Thêm danh sách cầu thủ cho đội (tùy chọn)
        generatePlayersForTeam(team);
        
        return team;
    }
    
    /**
     * Lấy resource ID cho logo dựa trên teamId
     */
    private int getLogoResourceId(int teamId) {
        switch (teamId) {
            case 1: return R.drawable.logo_1;
            case 2: return R.drawable.img_logo_1;
            case 3: return R.drawable.img_logo_2;
            case 4: return R.drawable.img_logo_3;
            case 5: return R.drawable.img_logo_4;
            case 6: return R.drawable.img_logo_5;
            case 7: return R.drawable.img_logo_6;
            case 8: return R.drawable.img_logo_1; // Lặp lại nếu không đủ logo
            case 9: return R.drawable.img_logo_2;
            case 10: return R.drawable.img_logo_3;
            default: return R.drawable.logo_1;
        }
    }
    
    /**
     * Sinh ra danh sách cầu thủ cho đội (11 cầu thủ)
     */
    private void generatePlayersForTeam(Team team) {
        // Danh sách tên cầu thủ mẫu
        String[] playerNames = {
            "John Smith", "Carlos Rodriguez", "Ahmed Hassan", "Luca Romano",
            "Pierre Martin", "Hans Mueller", "Yuki Tanaka", "Diego Silva",
            "Ivan Petrov", "Marco Gonzalez", "Andre Santos"
        };
        
        // Các vị trí cần thiết cho đội hình 4-3-3
        String[] positions = {"GK", "DF", "DF", "DF", "DF", "MF", "MF", "MF", "FW", "FW", "FW"};
        
        for (int i = 0; i < 11; i++) {
            String playerName = playerNames[i % playerNames.length] + " " + (team.getId() * 10 + i);
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
     * Lấy resource ID cho hình ảnh cầu thủ
     */
    private int getPlayerImageResource(int imageId) {
        // Mapping từ 1-60 thành p_1.png đến p_60.png
        String resourceName = "p_" + imageId;
        Context context = null; // Sẽ được handle trong TeamManager
        
        // Trả về ID mặc định, sẽ được xử lý properly trong TeamManager
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
    
    /**
     * Sinh ra một đội bóng với tên và logo tùy chỉnh
     */
    public Team generateCustomTeam(int teamId, String teamName, String country, int logoResId) {
        String formation = FORMATIONS[random.nextInt(FORMATIONS.length)];
        int rating = 75 + random.nextInt(21);
        
        Team team = new Team(teamId, teamName, country, logoResId, rating, formation);
        generatePlayersForTeam(team);
        
        return team;
    }
    
    /**
     * Cập nhật rating cho đội dựa trên rating trung bình của cầu thủ
     */
    public void updateTeamRating(Team team) {
        if (team.getPlayers() != null && !team.getPlayers().isEmpty()) {
            int averageRating = team.getAverageRating();
            team.setRating(averageRating);
            Log.d(TAG, "Cập nhật rating cho đội " + team.getName() + ": " + averageRating);
        }
    }
    
    /**
     * Trộn ngẫu nhiên danh sách đội bóng
     */
    public void shuffleTeams(List<Team> teams) {
        Collections.shuffle(teams, random);
        Log.d(TAG, "Đã trộn ngẫu nhiên " + teams.size() + " đội bóng");
    }
} 