package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;
public final class PlaylumiAchievementsTest {
    @Test public void playDaysCountOnlyOnceAndKeepPersonalRecord(){
        PlaylumiPlayDays d=new PlaylumiPlayDays(Long.MIN_VALUE,0,0,0);
        assertTrue(d.record(100));assertFalse(d.record(100));assertTrue(d.record(101));assertTrue(d.record(102));
        assertEquals(3,d.current(103));assertEquals(0,d.current(104));
        assertEquals(3,d.best);assertTrue(d.record(105));assertEquals(1,d.run);
        assertEquals(3,d.best);assertEquals(4,d.total);assertFalse(d.record(99));
        assertEquals(4,d.total);
    }
    @Test public void earnedAchievementsNeverDisappear(){
        int[] stats=new int[PlaylumiAchievements.METRICS];
        assertEquals(0L,PlaylumiAchievements.unlock(0,stats));
        stats[0]=100;stats[11]=7;
        long earned=PlaylumiAchievements.unlock(0,stats);
        assertEquals(5,Long.bitCount(earned));
        assertEquals(earned,PlaylumiAchievements.unlock(earned,new int[PlaylumiAchievements.METRICS]));
        java.util.Arrays.fill(stats,Integer.MAX_VALUE);
        assertEquals(PlaylumiAchievements.ALL.length,Long.bitCount(PlaylumiAchievements.unlock(earned,stats)));
    }
}
