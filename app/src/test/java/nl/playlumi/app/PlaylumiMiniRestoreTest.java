package nl.playlumi.app;
import org.junit.Test;
import java.util.Random;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class PlaylumiMiniRestoreTest {
    @Test public void memoryKeepsMatchedPairsAndOneOpenCard(){
        for(int age=0;age<3;age++)for(int i=0;i<100;i++){
            PlaylumiMiniChallenge a=PlaylumiMiniChallenge.generate(PlaylumiMiniChallenge.MEMORY,age,new Random(i));
            int pair=a.choices.lastIndexOf(a.choices.get(0));
            assertEquals(1,a.flip(0));assertEquals(2,a.flip(pair));
            int open=1;while(a.matched(open))open++;
            assertEquals(1,a.flip(open));
            PlaylumiMiniChallenge b=PlaylumiMiniChallenge.restore(a.kind,age,a.target,a.choices,a.correctIndex,a.matchedMask(),a.firstOpen());
            assertEquals(a.choices,b.choices);assertEquals(1,b.matchedPairs());
            assertTrue(b.visible(open));assertEquals(a.matchedMask(),b.matchedMask());
            int match=b.choices.indexOf(b.choices.get(open));if(match==open)match=b.choices.lastIndexOf(b.choices.get(open));
            assertEquals(2,b.flip(match));
        }
    }
    @Test public void missedPairClosesWithoutLockingResumedBoard(){
        PlaylumiMiniChallenge a=PlaylumiMiniChallenge.generate(PlaylumiMiniChallenge.MEMORY,0,new Random(3));
        int other=1;while(a.choices.get(0).equals(a.choices.get(other)))other++;
        a.flip(0);assertEquals(-1,a.flip(other));assertEquals(-1,a.firstOpen());
        PlaylumiMiniChallenge b=PlaylumiMiniChallenge.restore(a.kind,0,a.target,a.choices,-1,a.matchedMask(),a.firstOpen());
        assertEquals(1,b.flip(0));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsHalfMatchedPair(){
        PlaylumiMiniChallenge a=PlaylumiMiniChallenge.generate(PlaylumiMiniChallenge.MEMORY,0,new Random(3));
        PlaylumiMiniChallenge.restore(a.kind,0,a.target,a.choices,-1,1,-1);
    }
}
