package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;
public final class PlaylumiLearningProfileTest {
    @Test public void adaptsWithinAgeBoundsAfterSixQuestions(){
        PlaylumiLearningProfile p=new PlaylumiLearningProfile(2,1,2,0,0,0,0);
        for(int i=0;i<6;i++)p.record(false,true);
        assertEquals(1,p.level);assertEquals(6,p.samples);assertEquals(0,p.firstTryPercent());
        for(int i=0;i<6;i++)p.record(true,true);
        assertEquals(2,p.level);assertEquals(12,p.samples);assertEquals(50,p.firstTryPercent());
        for(int i=0;i<12;i++)p.record(true,true);
        assertEquals(100,p.firstTryPercent());assertEquals(24,p.total);assertEquals(2,p.level);
    }
    @Test public void approvedAdvancedGamesAndYoungestTierRemainIntact(){
        assertEquals(2,PlaylumiLearningProfile.minimum(2,LearningGame.PATTERNS,0));
        assertEquals(2,PlaylumiLearningProfile.minimum(2,16,3));
        PlaylumiLearningProfile p=new PlaylumiLearningProfile(0,0,2,0,0,0,0);
        for(int i=0;i<18;i++)p.record(true,true);
        assertEquals(0,p.level);
    }
    @Test public void historyPersistsAndAdaptiveCanBeDisabled(){
        PlaylumiLearningProfile p=new PlaylumiLearningProfile(2,1,2,0,0,0,0);
        for(int i=0;i<10;i++)p.record(false,false);
        assertEquals(2,p.level);assertEquals(0,p.window);
        PlaylumiLearningProfile restored=new PlaylumiLearningProfile(2,1,p.level,p.bits,p.samples,p.total,p.window);
        assertTrue(restored.priority(true)>restored.priority(false)-1);
        assertTrue(restored.priority(true)>40);
        assertEquals(10,restored.total);
    }
}
