package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class PlaylumiMiniChallengeTest {
    @Test public void allAgeLevelsProducePlayableBoards() {
        Random r=new Random(700);
        for(int kind=0;kind<3;kind++)
            for(int level=0;level<3;level++)
                for(int round=0;round<250;round++) {
                    PlaylumiMiniChallenge c=PlaylumiMiniChallenge.generate(kind,level,r);
                    assertEquals(kind,c.kind);
                    assertEquals(level,c.level);
                    assertFalse(c.question.isEmpty());
                    assertFalse(c.choices.isEmpty());
                    if(kind==PlaylumiMiniChallenge.MEMORY) {
                        assertEquals(level==0?3:level==1?4:6,c.pairCount());
                        Set<String> unique=new HashSet<>(c.choices);
                        assertEquals(c.pairCount(),unique.size());
                        for(String value:unique)
                            assertEquals(2,java.util.Collections.frequency(c.choices,value));
                    } else {
                        assertEquals(level==0?3:level==1?4:6,c.choices.size());
                        assertEquals(c.choices.size(),new HashSet<>(c.choices).size());
                        assertEquals(c.target,c.choices.get(c.correctIndex));
                        for(int i=0;i<c.choices.size();i++)
                            assertEquals(c.correctIndex==i,kind==PlaylumiMiniChallenge.DRAG?c.drop(i):c.choose(i));
                    }
                }
    }
    @Test(expected=IllegalStateException.class)
    public void tappingAShapeCannotCompleteADragChallenge() {
        PlaylumiMiniChallenge drag=PlaylumiMiniChallenge.generate(
                PlaylumiMiniChallenge.DRAG,0,new Random(13));
        drag.choose(drag.correctIndex);
    }
    @Test public void onlyRealDropUsesShapeRule() {
        PlaylumiMiniChallenge drag=PlaylumiMiniChallenge.generate(
                PlaylumiMiniChallenge.DRAG,0,new Random(34));
        for(int i=0;i<drag.choices.size();i++)
            assertEquals(i==drag.correctIndex,drag.drop(i));
    }
    @Test public void memoryPairsCanAllBeMatchedAndGameCompletes() {
        Random r=new Random(19);
        for(int level=0;level<3;level++) {
            PlaylumiMiniChallenge c=PlaylumiMiniChallenge.generate(PlaylumiMiniChallenge.MEMORY,level,r);
            for(int i=0;i<c.choices.size();i++) {
                if(c.matched(i))continue;
                int matching=-1;
                for(int j=i+1;j<c.choices.size();j++)
                    if(c.choices.get(i).equals(c.choices.get(j))) {matching=j;break;}
                assertTrue(matching>=0);
                assertEquals(1,c.flip(i));
                int result=c.flip(matching);
                if(c.matchedPairs()<c.pairCount())assertEquals(2,result);
                else assertEquals(3,result);
            }
            assertTrue(c.solved());
            assertEquals(c.pairCount(),c.matchedPairs());
        }
    }
    @Test public void wrongPairHidesAfterDelayWithoutAwarding() {
        PlaylumiMiniChallenge c=PlaylumiMiniChallenge.generate(PlaylumiMiniChallenge.MEMORY,0,new Random(10));
        int second=1;
        while(c.choices.get(0).equals(c.choices.get(second)))second++;
        assertEquals(1,c.flip(0));
        assertEquals(-1,c.flip(second));
        assertTrue(c.visible(0));
        assertTrue(c.visible(second));
        assertEquals(0,c.flip(2));
        c.hideMiss();
        assertFalse(c.visible(0));
        assertFalse(c.visible(second));
        assertEquals(0,c.matchedPairs());
        assertFalse(c.solved());
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsOutOfRangeAge() {
        PlaylumiMiniChallenge.generate(0,3,new Random());
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsOutOfRangeGame() {
        PlaylumiMiniChallenge.generate(3,0,new Random());
    }
}
