package nl.playlumi.app;

import java.util.ArrayList;
import java.util.List;

/**
 * One independently tracked score and achievement collection per category.
 * No Android dependency: rules can be unit tested without a device.
 */
public final class PlaylumiScores {
    public static final int CATEGORIES = 24;
    public static final String[] BADGE_ICONS = {"🌱","⭐","🏅","🏆","🔥","👑"};
    public static final String[] BADGE_NAMES = {
            "Eerste stap", "Sterrenstarter", "Doorzetter",
            "Superontdekker", "Vijf op rij", "Avontuur voltooid"
    };
    public static final String[] BADGE_HINTS = {
            "1 goed antwoord", "10 goede antwoorden", "50 goede antwoorden",
            "200 goede antwoorden", "5 antwoorden achter elkaar", "Een korte speelronde afronden"
    };
    public static final int BADGES = BADGE_NAMES.length;
    private final Score[] scores = new Score[CATEGORIES];

    public PlaylumiScores() {
        for (int i=0;i<CATEGORIES;i++) scores[i] = new Score();
    }
    public static final class Score {
        public int correct, attempts, currentStreak, bestStreak, completedSessions;
        public int badgeMask() {
            int mask=0;
            if (correct>=1) mask |= 1;
            if (correct>=10) mask |= 1<<1;
            if (correct>=50) mask |= 1<<2;
            if (correct>=200) mask |= 1<<3;
            if (bestStreak>=5) mask |= 1<<4;
            if (completedSessions>=1) mask |= 1<<5;
            return mask;
        }
        public boolean hasBadge(int index) {
            if (index<0 || index>=BADGES) throw new IllegalArgumentException("badge");
            return (badgeMask() & (1<<index))!=0;
        }
        public int badgeProgress(int index) {
            switch(index) {
                case 0: return Math.min(correct,1);
                case 1: return Math.min(correct,10);
                case 2: return Math.min(correct,50);
                case 3: return Math.min(correct,200);
                case 4: return Math.min(bestStreak,5);
                case 5: return Math.min(completedSessions,1);
                default: throw new IllegalArgumentException("badge");
            }
        }
        public int badgeGoal(int index) {
            switch(index) {
                case 0: case 5: return 1;
                case 1: return 10;
                case 2: return 50;
                case 3: return 200;
                case 4: return 5;
                default: throw new IllegalArgumentException("badge");
            }
        }
        public int earnedBadges() { return Integer.bitCount(badgeMask()); }
    }
    public Score get(int category) {
        if(category<0 || category>=CATEGORIES) throw new IllegalArgumentException("category");
        return scores[category];
    }
    public void restore(int category, int correct, int attempts, int best, int streak, int sessions) {
        Score s=get(category);
        s.correct=Math.max(0,correct);
        s.attempts=Math.max(Math.max(0,attempts),s.correct);
        s.bestStreak=Math.max(0,best);
        s.currentStreak=Math.max(0,streak);
        s.completedSessions=Math.max(0,sessions);
    }
    public List<Integer> answer(int category, boolean correct) {
        Score s=get(category);
        int before=s.badgeMask();
        s.attempts++;
        if (correct) {
            s.correct++;
            s.currentStreak++;
            if(s.currentStreak>s.bestStreak) s.bestStreak=s.currentStreak;
        } else s.currentStreak=0;
        return unlocked(before,s.badgeMask());
    }
    public List<Integer> finishSession(int category) {
        Score s=get(category);
        int before=s.badgeMask();
        s.completedSessions++;
        return unlocked(before,s.badgeMask());
    }
    private List<Integer> unlocked(int before, int after) {
        List<Integer> badges=new ArrayList<>();
        int delta=after & ~before;
        for(int i=0;i<BADGES;i++) if((delta&(1<<i))!=0) badges.add(i);
        return badges;
    }
}
