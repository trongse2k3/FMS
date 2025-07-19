package com.example.fms.firebase;

import android.content.Context;
import android.util.Log;

import com.example.fms.model.Team;

import java.util.List;

public class TeamManager {
    private static final String TAG = "TeamManager";
    
    private Context context;
    private TeamDataLoader teamDataLoader;
    
    public TeamManager(Context context) {
        this.context = context;
        this.teamDataLoader = new TeamDataLoader(context);
    }
    
    // Interfaces để callback (tương thích với TeamDataLoader)
    public interface TeamCallback {
        void onSuccess(Team team);
        void onFailure(Exception e);
    }
    
    public interface TeamsCallback {
        void onSuccess(List<Team> teams);
        void onFailure(Exception e);
    }
    
    // Interface cho Activity
    public interface OnTeamLoadedListener {
        void onTeamLoaded(Team team);
        void onError(String error);
    }
    
    /**
     * Lấy tất cả đội bóng từ assets (offline)
     */
    public void getAllTeams(TeamsCallback callback) {
        teamDataLoader.loadTeamsFromAssets(new TeamDataLoader.TeamsLoadCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                Log.d(TAG, "Lấy thành công " + teams.size() + " đội bóng từ assets");
                callback.onSuccess(teams);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi lấy danh sách đội bóng từ assets", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy đội bóng ngẫu nhiên để đấu
     */
    public void getRandomOpponentTeam(TeamCallback callback) {
        teamDataLoader.getRandomTeam(new TeamDataLoader.TeamLoadCallback() {
            @Override
            public void onSuccess(Team team) {
                Log.d(TAG, "Chọn đội ngẫu nhiên: " + team.getName());
                callback.onSuccess(team);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi chọn đội ngẫu nhiên", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy đội bóng ngẫu nhiên cho Activity (với OnTeamLoadedListener)
     */
    public void getRandomOpponentTeam(OnTeamLoadedListener listener) {
        teamDataLoader.getRandomTeam(new TeamDataLoader.TeamLoadCallback() {
            @Override
            public void onSuccess(Team team) {
                Log.d(TAG, "Chọn đội ngẫu nhiên: " + team.getName());
                if (listener != null) {
                    listener.onTeamLoaded(team);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi chọn đội ngẫu nhiên", e);
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * Lấy đội bóng theo ID
     */
    public void getTeamById(int teamId, TeamCallback callback) {
        teamDataLoader.getTeamById(teamId, new TeamDataLoader.TeamLoadCallback() {
            @Override
            public void onSuccess(Team team) {
                Log.d(TAG, "Tìm thấy đội: " + team.getName());
                callback.onSuccess(team);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Không tìm thấy đội với ID: " + teamId, e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy đội bóng theo quốc gia
     */
    public void getTeamsByCountry(String country, TeamsCallback callback) {
        teamDataLoader.getTeamsByCountry(country, new TeamDataLoader.TeamsLoadCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                Log.d(TAG, "Tìm thấy " + teams.size() + " đội từ " + country);
                callback.onSuccess(teams);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi tìm đội theo quốc gia: " + country, e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy đội bóng theo khoảng rating
     */
    public void getTeamsByRatingRange(int minRating, int maxRating, TeamsCallback callback) {
        teamDataLoader.getTeamsByRatingRange(minRating, maxRating, new TeamDataLoader.TeamsLoadCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                Log.d(TAG, "Tìm thấy " + teams.size() + " đội với rating " + minRating + "-" + maxRating);
                callback.onSuccess(teams);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi tìm đội theo rating", e);
                callback.onFailure(e);
            }
        });
    }
} 