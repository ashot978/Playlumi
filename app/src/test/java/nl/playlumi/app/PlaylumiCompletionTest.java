package nl.playlumi.app;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiCompletionTest {
    @Test public void partialRoundsNeverMarkAndCompletedRoutesOnlyCountOnce(){
        for(int age=0;age<3;age++){
            Set<String> marks=new HashSet<>();
            for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age)){
                assertFalse(PlaylumiCompletion.record(marks,age,e,0,8));
                assertFalse(PlaylumiCompletion.record(marks,age,e,7,8));
                assertEquals(PlaylumiCompletion.eligible(e),PlaylumiCompletion.record(marks,age,e,8,8));
                assertFalse(PlaylumiCompletion.record(marks,age,e,8,8));
            }
            assertEquals(marks.size(),PlaylumiCompletion.completed(age,marks).size());
        }
    }
    @Test public void ageScopeAndUnknownRoutes(){
        Set<String> marks=new HashSet<>();
        PlaylumiGameCatalog.Entry advanced=null;
        for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(2))
            if(e.kind==PlaylumiGameCatalog.QUIZ&&e.mode==ExtraGames.MATH&&e.variant==5)advanced=e;
        assertNotNull(advanced);
        assertFalse(PlaylumiCompletion.record(marks,0,advanced,8,8));
        assertTrue(PlaylumiCompletion.record(marks,2,advanced,8,8));
        assertTrue(PlaylumiCompletion.completed(0,marks).isEmpty());
        assertNotEquals(PlaylumiCompletion.key(1),PlaylumiCompletion.key(2));
    }
    @Test public void summaryIsPresentationOnly(){
        PlaylumiWardrobe wallet=new PlaylumiWardrobe(100);
        assertEquals(24,PlaylumiCompletion.baseStars(8));
        assertEquals(3,PlaylumiCompletion.baseStars(1));
        assertEquals(0,PlaylumiCompletion.baseStars(-8));
        assertEquals(100,wallet.stars());
        assertTrue(PlaylumiCompletion.sideBySide(700,800));
        assertFalse(PlaylumiCompletion.sideBySide(320,800));
        assertTrue(PlaylumiCompletion.medalSize(400)<PlaylumiCompletion.medalSize(800));
    }
}
