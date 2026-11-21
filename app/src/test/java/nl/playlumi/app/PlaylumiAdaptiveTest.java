package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiAdaptiveTest {
    @Test public void increasesOnlyAfterEightStrongAnswers() {
        PlaylumiAdaptive adaptive=new PlaylumiAdaptive();
        assertEquals(0,adaptive.level(0,0));
        for(int i=0;i<7;i++)assertFalse(adaptive.observe(0,true,0));
        assertTrue(adaptive.observe(0,true,0));
        assertEquals(1,adaptive.level(0,0));
        for(int i=0;i<8;i++)adaptive.observe(0,true,0);
        assertEquals(1,adaptive.level(0,0)); // preschool cap
    }
    @Test public void lowersAfterRepeatedDifficultyAndDoesNotJumpLevels() {
        PlaylumiAdaptive adaptive=new PlaylumiAdaptive();
        adaptive.restore(1,2,0,0);
        for(int i=0;i<7;i++)assertFalse(adaptive.observe(1,false,2));
        assertTrue(adaptive.observe(1,false,2));
        assertEquals(1,adaptive.level(1,2));
        for(int i=0;i<8;i++)adaptive.observe(1,false,2);
        assertEquals(0,adaptive.level(1,2));
        for(int i=0;i<8;i++)adaptive.observe(1,false,2);
        assertEquals(0,adaptive.level(1,2));
    }
    @Test public void independentCategoriesAndRestoredWindows() {
        PlaylumiAdaptive adaptive=new PlaylumiAdaptive();
        adaptive.restore(5,1,6,6);
        adaptive.observe(5,true,1);
        assertEquals(7,adaptive.sampleCount(5));
        assertTrue(adaptive.observe(5,true,1));
        assertEquals(2,adaptive.level(5,1));
        assertEquals(0,adaptive.sampleCount(5));
        assertEquals(0,adaptive.level(6,0));
    }
    @Test public void moderatePerformanceKeepsLevel() {
        PlaylumiAdaptive adaptive=new PlaylumiAdaptive();
        for(int i=0;i<8;i++)adaptive.observe(3,i<5,1);
        assertEquals(1,adaptive.level(3,1));
        assertEquals(0,adaptive.sampleCorrect(3));
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidCategoryRejected() {new PlaylumiAdaptive().level(99,1);}
}
