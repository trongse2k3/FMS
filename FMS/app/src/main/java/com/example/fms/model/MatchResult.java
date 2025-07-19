package com.example.fms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MatchResult implements Serializable {
    private String matchId;
    private Team homeTeam;
    private Team awayTeam;
    private List<Player> homeSquad;
    private List<Player> awaySquad;
    private int homeScore;
    private int awayScore;
    private List<MatchEvent> events;
    private Date matchDate;
    private boolean isFinished;
    private int currentMinute;
    private String matchStatus; // "PREPARING", "PLAYING", "HALF_TIME", "FINISHED"
    
    // Thống kê trận đấu
    private int homePossession; // % kiểm soát bóng
    private int awayPossession;
    private int homeShots;
    private int awayShots;
    private int homeShotsOnTarget;
    private int awayShotsOnTarget;
    private int homeCorners;
    private int awayCorners;
    private int homeFouls;
    private int awayFouls;
    private int homeYellowCards;
    private int awayYellowCards;
    private int homeRedCards;
    private int awayRedCards;

    public MatchResult() {
        this.events = new ArrayList<>();
        this.matchDate = new Date();
        this.isFinished = false;
        this.currentMinute = 0;
        this.matchStatus = "PREPARING";
        this.homeScore = 0;
        this.awayScore = 0;
        initializeStats();
    }

    public MatchResult(Team homeTeam, Team awayTeam, List<Player> homeSquad, List<Player> awaySquad) {
        this();
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeSquad = homeSquad != null ? new ArrayList<>(homeSquad) : new ArrayList<>();
        this.awaySquad = awaySquad != null ? new ArrayList<>(awaySquad) : new ArrayList<>();
        this.matchId = generateMatchId();
    }

    private void initializeStats() {
        this.homePossession = 50;
        this.awayPossession = 50;
        this.homeShots = 0;
        this.awayShots = 0;
        this.homeShotsOnTarget = 0;
        this.awayShotsOnTarget = 0;
        this.homeCorners = 0;
        this.awayCorners = 0;
        this.homeFouls = 0;
        this.awayFouls = 0;
        this.homeYellowCards = 0;
        this.awayYellowCards = 0;
        this.homeRedCards = 0;
        this.awayRedCards = 0;
    }

    private String generateMatchId() {
        return "match_" + System.currentTimeMillis();
    }

    public void addEvent(MatchEvent event) {
        events.add(event);
        updateStatsFromEvent(event);
    }

    private void updateStatsFromEvent(MatchEvent event) {
        boolean isHome = event.isHomeTeam();
        
        switch (event.getType()) {
            case GOAL:
                if (isHome) homeScore++;
                else awayScore++;
                break;
                
            case YELLOW_CARD:
                if (isHome) homeYellowCards++;
                else awayYellowCards++;
                break;
                
            case RED_CARD:
                if (isHome) homeRedCards++;
                else awayRedCards++;
                break;
                
            case CORNER:
                if (isHome) homeCorners++;
                else awayCorners++;
                break;
                
            case FOUL:
                if (isHome) homeFouls++;
                else awayFouls++;
                break;
                
            case MISS:
            case SAVE:
                if (isHome) homeShots++;
                else awayShots++;
                break;
        }
    }

    public void updatePossession(int homePoss, int awayPoss) {
        this.homePossession = homePoss;
        this.awayPossession = awayPoss;
    }

    public void incrementShots(boolean isHomeTeam, boolean onTarget) {
        if (isHomeTeam) {
            homeShots++;
            if (onTarget) homeShotsOnTarget++;
        } else {
            awayShots++;
            if (onTarget) awayShotsOnTarget++;
        }
    }

    public String getResultText() {
        return homeTeam.getName() + " " + homeScore + " - " + awayScore + " " + awayTeam.getName();
    }

    public String getWinnerText() {
        if (!isFinished) return "Trận đấu chưa kết thúc";
        
        if (homeScore > awayScore) {
            return homeTeam.getName() + " thắng";
        } else if (awayScore > homeScore) {
            return awayTeam.getName() + " thắng";
        } else {
            return "Hòa";
        }
    }

    public List<MatchEvent> getEventsByType(MatchEvent.EventType type) {
        List<MatchEvent> filteredEvents = new ArrayList<>();
        for (MatchEvent event : events) {
            if (event.getType() == type) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }

    public List<MatchEvent> getGoalEvents() {
        return getEventsByType(MatchEvent.EventType.GOAL);
    }

    public List<MatchEvent> getCardEvents() {
        List<MatchEvent> cardEvents = new ArrayList<>();
        cardEvents.addAll(getEventsByType(MatchEvent.EventType.YELLOW_CARD));
        cardEvents.addAll(getEventsByType(MatchEvent.EventType.RED_CARD));
        return cardEvents;
    }

    // Getters and Setters
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }

    public List<Player> getHomeSquad() { return homeSquad; }
    public void setHomeSquad(List<Player> homeSquad) { this.homeSquad = homeSquad; }

    public List<Player> getAwaySquad() { return awaySquad; }
    public void setAwaySquad(List<Player> awaySquad) { this.awaySquad = awaySquad; }

    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }

    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }

    public List<MatchEvent> getEvents() { return events; }
    public void setEvents(List<MatchEvent> events) { this.events = events; }

    public Date getMatchDate() { return matchDate; }
    public void setMatchDate(Date matchDate) { this.matchDate = matchDate; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }

    public int getCurrentMinute() { return currentMinute; }
    public void setCurrentMinute(int currentMinute) { this.currentMinute = currentMinute; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }

    // Getters cho thống kê
    public int getHomePossession() { return homePossession; }
    public int getAwayPossession() { return awayPossession; }
    public int getHomeShots() { return homeShots; }
    public int getAwayShots() { return awayShots; }
    public int getHomeShotsOnTarget() { return homeShotsOnTarget; }
    public int getAwayShotsOnTarget() { return awayShotsOnTarget; }
    public int getHomeCorners() { return homeCorners; }
    public int getAwayCorners() { return awayCorners; }
    public int getHomeFouls() { return homeFouls; }
    public int getAwayFouls() { return awayFouls; }
    public int getHomeYellowCards() { return homeYellowCards; }
    public int getAwayYellowCards() { return awayYellowCards; }
    public int getHomeRedCards() { return homeRedCards; }
    public int getAwayRedCards() { return awayRedCards; }
} 