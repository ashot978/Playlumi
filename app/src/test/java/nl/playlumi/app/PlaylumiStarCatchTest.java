package nl.playlumi.app;
import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public final class PlaylumiStarCatchTest {
    @Test public void allAgeGroupsGetExactlyEnoughStarsAndDistractors() {
        Random rng=new Random(96);
        for(int age=0;age<3;age++)for(int tryNo=0;tryNo<250;tryNo++){
            PlaylumiStarCatch game=PlaylumiStarCatch.generate(age,rng);
            assertEquals(age+3,game.target());
            assertEquals(game.target(),Integer.bitCount(game.starMask()));
            assertTrue(game.total()>game.target());
            for(int i=0;i<game.total();i++){
                if(!game.isStar(i)){
                    assertFalse(game.catchStar(i));
                    assertFalse(game.caught(i));
                }
            }
            assertEquals(0,game.collected());
            for(int i=0;i<game.total();i++){
                if(game.isStar(i)){
                    assertTrue(game.catchStar(i));
                    assertFalse(game.catchStar(i));
                }
            }
            assertTrue(game.finished());
            for(int i=0;i<game.total();i++)assertFalse(game.catchStar(i));
        }
    }
    @Test public void starsAndHeartsPersistIfTheScreenRotates() {
        PlaylumiStarCatch before=PlaylumiStarCatch.generate(1,new Random(28));
        int chosen=-1;
        for(int i=0;i<before.total();i++)if(before.isStar(i)){chosen=i;break;}
        assertTrue(before.catchStar(chosen));
        PlaylumiStarCatch after=PlaylumiStarCatch.restore(before.target(),before.total(),before.starMask(),before.mask());
        assertEquals(1,after.collected());
        assertEquals(before.starMask(),after.starMask());
        assertTrue(after.caught(chosen));
        assertFalse(after.catchStar(chosen));
        for(int i=0;i<after.total();i++){
            if(!after.isStar(i))assertFalse(after.catchStar(i));
            else if(i!=chosen)assertTrue(after.catchStar(i));
        }
        assertTrue(after.finished());
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidIndexIsRejected(){
        PlaylumiStarCatch game=PlaylumiStarCatch.generate(0,new Random(42));
        game.catchStar(game.total());
    }
    @Test(expected=IllegalArgumentException.class)
    public void aHeartCannotBeRestoredAsCollected(){
        PlaylumiStarCatch.restore(3,6,7,8);
    }
    @Test(expected=IllegalArgumentException.class)
    public void tamperedStateDoesNotRestore(){
        PlaylumiStarCatch.restore(3,6,63,0);
    }
}
