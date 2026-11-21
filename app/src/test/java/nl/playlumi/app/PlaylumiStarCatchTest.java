package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public final class PlaylumiStarCatchTest {
    @Test public void allAgeGroupsGetReachableStarTargets() {
        Random rng=new Random(96);
        for(int age=0;age<3;age++)for(int tryNo=0;tryNo<250;tryNo++){
            PlaylumiStarCatch game=PlaylumiStarCatch.generate(age,rng);
            assertEquals(age+3,game.target());
            assertTrue(game.total()>game.target());
            for(int index=0;index<game.target();index++){
                assertTrue(game.catchStar(index));
                assertFalse(game.catchStar(index));
            }
            assertTrue(game.finished());
            assertFalse(game.catchStar(game.target()));
        }
    }
    @Test public void starsPersistIfTheScreenRotates() {
        PlaylumiStarCatch before=PlaylumiStarCatch.generate(1,new Random(28));
        before.catchStar(0);
        before.catchStar(2);
        PlaylumiStarCatch after=PlaylumiStarCatch.restore(
                before.target(),before.total(),before.mask());
        assertEquals(2,after.collected());
        assertTrue(after.caught(0));
        assertTrue(after.caught(2));
        assertFalse(after.catchStar(0));
        for(int i=0;i<after.total()&&!after.finished();i++)after.catchStar(i);
        assertTrue(after.finished());
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidIndexIsRejected(){
        PlaylumiStarCatch game=PlaylumiStarCatch.generate(0,new Random(42));
        game.catchStar(game.total());
    }
    @Test(expected=IllegalArgumentException.class)
    public void tamperedStateDoesNotRestore(){
        PlaylumiStarCatch.restore(3,6,63);
    }
}
