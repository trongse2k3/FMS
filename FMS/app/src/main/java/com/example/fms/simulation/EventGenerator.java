package com.example.fms.simulation;

import com.example.fms.model.*;
import java.util.*;

public class EventGenerator {
    private Random random;
    private List<String> goalDescriptions;
    private List<String> assistDescriptions;
    private List<String> saveDescriptions;
    private List<String> missDescriptions;
    private List<String> tackleDescriptions;
    private List<String> injuryDescriptions;
    private List<String> cardDescriptions;
    private List<String> yellowCardDescriptions;
    private List<String> redCardDescriptions;

    public EventGenerator() {
        this.random = new Random();
        initializeDescriptions();
    }

    private void initializeDescriptions() {
        goalDescriptions = Arrays.asList(
            "Cú sút đẹp mắt vào góc thấp!",
            "Pha dứt điểm tinh tế trong vòng cấm!",
            "Siêu phẩm từ ngoài vòng cấm!",
            "Bàn thắng từ tình huống phạt góc!",
            "Pha volley tuyệt đẹp!",
            "Đá phạt trực tiếp vào lưới!",
            "Cú đánh đầu chuẩn xác!",
            "Bàn thắng từ chấm 11m!",
            "Pha dứt điểm gọn gàng!",
            "Bàn thắng từ tình huống phản công!"
        );

        assistDescriptions = Arrays.asList(
            "Đường chuyền tuyệt vời!",
            "Pha kiến tạo đẳng cấp!",
            "Đường chuyền dọn cỗ hoàn hảo!",
            "Cross chuẩn xác từ cánh!",
            "Pha kiến tạo thông minh!",
            "Đường chuyền xé toạc hàng phòng ngự!",
            "Through ball tuyệt đẹp!",
            "Pha chuyền bóng nhanh như chớp!"
        );

        saveDescriptions = Arrays.asList(
            "Pha cản phá xuất thần!",
            "Thủ môn bay người cứu thua!",
            "Reflec tuyệt vời của thủ môn!",
            "Cản phá bằng chân trong gang tấc!",
            "Pha cứu thua ngoạn mục!",
            "Thủ môn đấm bóng ra ngoài an toàn!",
            "Cản phá từ cự ly gần!"
        );

        missDescriptions = Arrays.asList(
            "Tiếc! Bóng đi chệch cột dọc!",
            "Hỏng ăn đáng tiếc!",
            "Bóng bay cao vượt xà ngang!",
            "Cơ hội bỏ lỡ trong gang tấc!",
            "Thủ môn đã cứu thua xuất sắc!",
            "Cú sút thiếu chính xác!",
            "Bóng trúng cột dọc rồi ra ngoài!"
        );

        tackleDescriptions = Arrays.asList(
            "Pha cướp bóng đẹp mắt!",
            "Tình huống phòng ngự hoàn hảo!",
            "Tackle chuẩn xác!",
            "Pha phòng ngự kịp thời!",
            "Cướp bóng thành công!",
            "Pha phá bóng quan trọng!"
        );

        injuryDescriptions = Arrays.asList(
            "Va chạm mạnh, cầu thủ đang đau đớn!",
            "Tình huống đáng lo ngại!",
            "Cầu thủ cần được hỗ trợ y tế!",
            "Va chạm không may!",
            "Chấn thương từ tình huống tranh chấp!",
            "Cầu thủ đang khó khăn đứng lên!"
        );

        cardDescriptions = Arrays.asList(
            "Pha phạm lỗi thô bạo!",
            "Tình huống tranh cãi với trọng tài!",
            "Phạm lỗi chiến thuật!",
            "Lỗi cản phá bất hợp pháp!",
            "Phản ứng không đúng mực!",
            "Lỗi nguy hiểm với đối thủ!",
            "Lỗi xấu từ phía sau!",
            "Tước bóng bằng cả hai chân!",
            "Cản phá cơ hội ghi bàn rõ ràng!",
            "Kéo áo đối phương!",
            "Tác động mạnh vào người đối thủ!"
        );

        yellowCardDescriptions = Arrays.asList(
            "Thẻ vàng cho cầu thủ đang đứng gần vị trí bóng!",
            "Cầu thủ đang đứng gần vị trí bóng đã phạm lỗi!",
            "Cầu thủ đang đứng gần vị trí bóng đã phạm lỗi thô bạo!",
            "Cầu thủ đang đứng gần vị trí bóng đã phạm lỗi chiến thuật!",
            "Cầu thủ đang đứng gần vị trí bóng đã phạm lỗi cản phá bất hợp pháp!"
        );

        redCardDescriptions = Arrays.asList(
            "Thẻ đỏ cho cầu thủ đang đứng gần vị trí bóng!",
            "Cầu thủ đang đứng gần vị trí bóng đã phạm lỗi thô bạo!",
            "Cầu thủ đang đứng gần vị trí bóng đã phạm lỗi chiến thuật!",
            "Cầu thủ đang đứng gần vị trí bóng đã phạm lỗi cản phá bất hợp pháp!"
        );
    }

    public List<MatchEvent> generateRandomEvents(List<Player> homeSquad, List<Player> awaySquad, 
                                               int minute, int diceRoll) {
        List<MatchEvent> events = new ArrayList<>();
        
        // Xác suất sự kiện dựa trên điểm xúc xắc (1-6)
        // Điểm thấp = xác suất cao gặp sự kiện xấu
        double eventProbability = calculateEventProbability(diceRoll);
        
        if (random.nextDouble() < eventProbability) {
            MatchEvent event = generateEventBasedOnDiceRoll(homeSquad, awaySquad, minute, diceRoll);
            if (event != null) {
                events.add(event);
                
                // Tạo sự kiện liên kết nếu cần (ví dụ: kiến tạo cho bàn thắng)
                MatchEvent linkedEvent = generateLinkedEvent(event, homeSquad, awaySquad);
                if (linkedEvent != null) {
                    events.add(linkedEvent);
                }
            }
        }
        
        return events;
    }

    private double calculateEventProbability(int diceRoll) {
        // Điểm 1-2: 35% xác suất sự kiện (cao)
        // Điểm 3-4: 20% xác suất sự kiện (trung bình) 
        // Điểm 5-6: 10% xác suất sự kiện (thấp)
        switch (diceRoll) {
            case 1:
            case 2:
                return 0.35;
            case 3:
            case 4:
                return 0.20;
            case 5:
            case 6:
                return 0.10;
            default:
                return 0.15;
        }
    }

    private MatchEvent generateEventBasedOnDiceRoll(List<Player> homeSquad, List<Player> awaySquad, 
                                                   int minute, int diceRoll) {
        boolean isHomeTeam = random.nextBoolean();
        List<Player> squad = isHomeTeam ? homeSquad : awaySquad;
        
        if (squad.isEmpty()) return null;
        
        MatchEvent.EventType eventType;
        
        // Điểm xúc xắc thấp = tỷ lệ cao gặp sự kiện xấu
        if (diceRoll <= 2) {
            // Xác suất cao gặp sự kiện xấu
            eventType = generateNegativeEvent();
        } else if (diceRoll <= 4) {
            // Sự kiện trung bình
            eventType = generateNormalEvent();
        } else {
            // Sự kiện tích cực hoặc bình thường
            eventType = generatePositiveEvent();
        }
        
        Player player = selectPlayerForEvent(squad, eventType);
        MatchEvent event = new MatchEvent(minute, eventType, player, isHomeTeam);
        event.setDiceRoll(diceRoll);
        event.setDescription(getDescriptionForEvent(eventType));
        
        return event;
    }

    private MatchEvent.EventType generateNegativeEvent() {
        // Điều chỉnh xác suất để thẻ vàng và thẻ đỏ xuất hiện nhiều hơn
        double rand = random.nextDouble();
        
        if (rand < 0.4) {
            // 40% cơ hội là thẻ vàng
            return MatchEvent.EventType.YELLOW_CARD;
        } else if (rand < 0.55) {
            // 15% cơ hội là thẻ đỏ
            return MatchEvent.EventType.RED_CARD;
        } else if (rand < 0.75) {
            // 20% cơ hội là chấn thương
            return MatchEvent.EventType.INJURY;
        } else if (rand < 0.9) {
            // 15% cơ hội là bỏ lỡ
            return MatchEvent.EventType.MISS;
        } else {
            // 10% cơ hội là phạm lỗi
            return MatchEvent.EventType.FOUL;
        }
    }

    private MatchEvent.EventType generateNormalEvent() {
        MatchEvent.EventType[] normalEvents = {
            MatchEvent.EventType.GOAL,
            MatchEvent.EventType.SAVE,
            MatchEvent.EventType.CORNER,
            MatchEvent.EventType.FREE_KICK,
            MatchEvent.EventType.OFFSIDE,
            MatchEvent.EventType.BEAUTIFUL_PASS
        };
        return normalEvents[random.nextInt(normalEvents.length)];
    }

    private MatchEvent.EventType generatePositiveEvent() {
        MatchEvent.EventType[] positiveEvents = {
            MatchEvent.EventType.GOAL,
            MatchEvent.EventType.BEAUTIFUL_PASS,
            MatchEvent.EventType.GOOD_TACKLE,
            MatchEvent.EventType.SAVE,
            MatchEvent.EventType.CORNER
        };
        return positiveEvents[random.nextInt(positiveEvents.length)];
    }

    private Player selectPlayerForEvent(List<Player> squad, MatchEvent.EventType eventType) {
        // Chọn cầu thủ phù hợp với sự kiện dựa trên vị trí
        switch (eventType) {
            case GOAL:
            case MISS:
                return selectAttacker(squad);
            case SAVE:
                return selectGoalkeeper(squad);
            case GOOD_TACKLE:
                return selectDefender(squad);
            case BEAUTIFUL_PASS:
            case ASSIST:
                return selectMidfielder(squad);
            default:
                return squad.get(random.nextInt(squad.size()));
        }
    }

    private Player selectAttacker(List<Player> squad) {
        List<Player> attackers = new ArrayList<>();
        for (Player p : squad) {
            if (p.getPosition().equals("FW")) {
                attackers.add(p);
            }
        }
        if (attackers.isEmpty()) {
            // Nếu không có tiền đạo, chọn tiền vệ tấn công
            for (Player p : squad) {
                if (p.getPosition().equals("MF")) {
                    attackers.add(p);
                }
            }
        }
        return attackers.isEmpty() ? squad.get(random.nextInt(squad.size())) : 
               attackers.get(random.nextInt(attackers.size()));
    }

    private Player selectDefender(List<Player> squad) {
        List<Player> defenders = new ArrayList<>();
        for (Player p : squad) {
            if (p.getPosition().equals("DF")) {
                defenders.add(p);
            }
        }
        return defenders.isEmpty() ? squad.get(random.nextInt(squad.size())) : 
               defenders.get(random.nextInt(defenders.size()));
    }

    private Player selectMidfielder(List<Player> squad) {
        List<Player> midfielders = new ArrayList<>();
        for (Player p : squad) {
            if (p.getPosition().equals("MF")) {
                midfielders.add(p);
            }
        }
        return midfielders.isEmpty() ? squad.get(random.nextInt(squad.size())) : 
               midfielders.get(random.nextInt(midfielders.size()));
    }

    private Player selectGoalkeeper(List<Player> squad) {
        for (Player p : squad) {
            if (p.getPosition().equals("GK")) {
                return p;
            }
        }
        return squad.get(random.nextInt(squad.size()));
    }

    private MatchEvent generateLinkedEvent(MatchEvent mainEvent, List<Player> homeSquad, List<Player> awaySquad) {
        if (mainEvent.getType() == MatchEvent.EventType.GOAL) {
            // 70% xác suất có kiến tạo cho bàn thắng
            if (random.nextDouble() < 0.7) {
                List<Player> squad = mainEvent.isHomeTeam() ? homeSquad : awaySquad;
                Player assistPlayer = selectMidfielder(squad);
                
                // Đảm bảo người kiến tạo khác người ghi bàn
                if (assistPlayer.getId() == mainEvent.getPlayer().getId() && squad.size() > 1) {
                    assistPlayer = squad.get(random.nextInt(squad.size()));
                    while (assistPlayer.getId() == mainEvent.getPlayer().getId()) {
                        assistPlayer = squad.get(random.nextInt(squad.size()));
                    }
                }
                
                MatchEvent assistEvent = new MatchEvent(mainEvent.getMinute(), 
                    MatchEvent.EventType.ASSIST, assistPlayer, mainEvent.isHomeTeam());
                assistEvent.setDescription(getRandomDescription(assistDescriptions));
                
                // Cập nhật bàn thắng với thông tin kiến tạo
                mainEvent.setAssistPlayer(assistPlayer);
                
                return assistEvent;
            }
        }
        return null;
    }

    private String getDescriptionForEvent(MatchEvent.EventType eventType) {
        switch (eventType) {
            case GOAL:
                return getRandomDescription(goalDescriptions);
            case ASSIST:
                return getRandomDescription(assistDescriptions);
            case SAVE:
                return getRandomDescription(saveDescriptions);
            case MISS:
                return getRandomDescription(missDescriptions);
            case GOOD_TACKLE:
                return getRandomDescription(tackleDescriptions);
            case INJURY:
                return getRandomDescription(injuryDescriptions);
            case YELLOW_CARD:
                return getRandomDescription(yellowCardDescriptions);
            case RED_CARD:
                return getRandomDescription(redCardDescriptions);
            default:
                return "";
        }
    }

    private String getRandomDescription(List<String> descriptions) {
        return descriptions.get(random.nextInt(descriptions.size()));
    }

    // Tạo sự kiện đặc biệt cho những phút quan trọng
    public List<MatchEvent> generateSpecialMomentEvents(List<Player> homeSquad, List<Player> awaySquad, int minute) {
        List<MatchEvent> events = new ArrayList<>();
        
        // Phút 45, 90: Thời gian bù giờ - tăng xác suất ghi bàn
        if (minute == 45 || minute == 90) {
            if (random.nextDouble() < 0.3) { // 30% xác suất ghi bàn phút bù giờ
                boolean isHomeTeam = random.nextBoolean();
                List<Player> squad = isHomeTeam ? homeSquad : awaySquad;
                Player scorer = selectAttacker(squad);
                
                MatchEvent goalEvent = new MatchEvent(minute, MatchEvent.EventType.GOAL, scorer, isHomeTeam);
                goalEvent.setDescription("Bàn thắng trong thời gian bù giờ!");
                events.add(goalEvent);
            }
        }
        
        return events;
    }
} 