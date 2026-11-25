package nl.playlumi.app;
import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;
public final class PlaylumiStarCatchTest {
    @Test public void allAgeGroupsGetReachableTargetsAndHeartsNeverCount() {
        Random rng=new Random(96);
        for(int age=0;age<3;age++)for(int tryNo=0;tryNo<250;tryNo++){
            PlaylumiStarCatch game=PlaylumiStarCatch.generate(age,rng);
            assertEquals(age+3,game.target());
            assertTrue(game.total()>game.target());
            assertEquals(game.target(),Integer.bitCount(game.starMask()));
            for(int index=0;index<game.total();index++){
                if(game.isStar(index))continue;
                assertFalse(game.catchStar(index));
                assertFalse(game.caught(index));
            }
            assertEquals(0,game.collected());
            for(int index=0;index<game.total();index++){
                if(!game.isStar(index))continue;
                assertTrue(game.catchStar(index));
                assertFalse(game.catchStar(index));
            }
            assertTrue(game.finished());
            assertEquals(game.target(),game.collected());
            for(int index=0;index<game.total();index++)assertFalse(game.catchStar(index));
        }
    }
    @Test public void starPositionsAndProgressPersistIfTheScreenRotates() {
        PlaylumiStarCatch before=PlaylumiStarCatch.generate(1,new Random(28));
        int first=-1,heart=-1;
        for(int i=0;i<before.total();i++) {
            if(before.isStar(i) && first<0)first=i;
            if(!before.isStar(i))heart=i;
        }
        assertTrue(before.catchStar(first));
        assertFalse(before.catchStar(heart));
        PlaylumiStarCatch after=PlaylumiStarCatch.restore(
                before.target(),before.total(),before.starMask(),before.mask());
        assertEquals(before.starMask(),after.starMask());
        assertEquals(1,after.collected());
        assertTrue(after.caught(first));
        assertFalse(after.catchStar(first));
        assertFalse(after.catchStar(heart));
        for(int i=0;i<after.total();i++)if(after.isStar(i))after.catchStar(i);
        assertTrue(after.finished());
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidIndexIsRejected(){
        PlaylumiStarCatch game=PlaylumiStarCatch.generate(0,new Random(42));
        game.catchStar(game.total());
    }
    @Test(expected=IllegalArgumentException.class)
    public void tamperedStateDoesNotRestore(){
        PlaylumiStarCatch.restore(3,6,7,8);
    }
}
