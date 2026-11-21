package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaylumiUsageTest {
    @Test public void remindersOnlyAfterConfiguredTime() {
        PlaylumiUsage usage=new PlaylumiUsage();
        usage.setLimit(15);
        usage.addSeconds(14*60);
        assertFalse(usage.shouldSuggestBreak());
        usage.addSeconds(60);
        assertTrue(usage.shouldSuggestBreak());
        assertFalse(usage.shouldSuggestBreak());
        usage.startNewDay();
        assertEquals(0,usage.minutes());
        assertFalse(usage.breakShown());
    }
    @Test public void reminderIsOptionalAndNeverLocksGameplay() {
        PlaylumiUsage usage=new PlaylumiUsage();
        usage.addSeconds(6*3600);
        assertFalse(usage.shouldSuggestBreak());
        assertEquals(360,usage.minutes());
    }
    @Test public void stateRestoresAcrossAppLaunches() {
        PlaylumiUsage usage=new PlaylumiUsage();
        usage.restore(1200,30,true);
        assertEquals(20,usage.minutes());
        assertEquals(30,usage.limit());
        assertTrue(usage.breakShown());
        usage.addSeconds(900);
        assertFalse(usage.shouldSuggestBreak());
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsNegativeTime() {new PlaylumiUsage().addSeconds(-5);}
    @Test(expected=IllegalArgumentException.class)
    public void rejectsUnsupportedLimit() {new PlaylumiUsage().setLimit(22);}
}
