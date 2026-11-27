package nl.playlumi.app;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiExpeditionTest {
    @Test public void allPublishedMissionsExistAndAreDistinctForEachAge(){
        for(int age=0;age<3;age++){
            Set<String> ids=new HashSet<>();Set<Integer> worlds=new HashSet<>();
            for(int chapter=0;chapter<6;chapter++){
                List<PlaylumiGameCatalog.Entry> entries=PlaylumiExpedition.chapter(age,chapter);
                assertEquals(5,entries.size());
                for(int i=0;i<5;i++){
                    PlaylumiGameCatalog.Entry entry=entries.get(i);
                    assertEquals(PlaylumiExpedition.routeId(age,chapter*5+i),entry.id);
                    assertTrue(ids.add(entry.id));worlds.add(entry.world);
                    assertTrue(PlaylumiCompletion.eligible(entry));
                    assertNotNull(PlaylumiGameCatalog.find(age,entry.id));
                }
            }
            assertEquals(30,ids.size());assertEquals(6,worlds.size());
            assertEquals("0:6:4:0",PlaylumiExpedition.routeId(age,0));
            assertEquals("0:6:0:0",PlaylumiExpedition.routeId(age,4));
        }
    }
    @Test public void progressAndRewardCannotBeClaimedByFreePlayOrRepeating(){
        for(int age=0;age<3;age++){
            int progress=0,stars=0;
            for(int i=0;i<30;i++){
                String wanted=PlaylumiExpedition.routeId(age,progress);
                assertFalse(PlaylumiExpedition.complete(age,progress,wanted,false,5,5).changed());
                assertFalse(PlaylumiExpedition.complete(age,progress,wanted,true,4,5).changed());
                assertFalse(PlaylumiExpedition.complete(age,progress,"bad route",true,5,5).changed());
                PlaylumiExpedition.Advance done=PlaylumiExpedition.complete(age,progress,wanted,true,5,5);
                assertTrue(done.changed());assertEquals(progress+1,done.after);
                progress=done.after;stars+=done.bonus;
                assertFalse(PlaylumiExpedition.complete(age,progress,wanted,true,5,5).changed());
                assertEquals(PlaylumiExpedition.starsEarned(progress),stars);
            }
            assertEquals(120,stars);assertEquals(6,PlaylumiExpedition.stamps(progress));
            assertTrue(PlaylumiExpedition.finished(progress));assertNull(PlaylumiExpedition.route(age,progress));
        }
    }
    @Test public void chapterLocksAndIndependentAgeKeysAreStable(){
        assertEquals(0,PlaylumiExpedition.safe(-3));assertEquals(30,PlaylumiExpedition.safe(90));
        assertEquals(0,PlaylumiExpedition.currentChapter(0));assertEquals(1,PlaylumiExpedition.currentChapter(5));
        assertEquals(5,PlaylumiExpedition.currentChapter(30));
        assertFalse(PlaylumiExpedition.available(4,1));assertTrue(PlaylumiExpedition.available(5,1));
        assertEquals(2,PlaylumiExpedition.chapterProgress(12,2));assertEquals(0,PlaylumiExpedition.chapterProgress(12,3));
        assertEquals(15,PlaylumiExpedition.starsEarned(5));assertEquals(75,PlaylumiExpedition.starsEarned(29));
        assertEquals(45,PlaylumiExpedition.complete(2,29,PlaylumiExpedition.routeId(2,29),true,5,5).bonus);
        assertEquals("expedition_v1_progress_0",PlaylumiExpedition.key(0));assertEquals("expedition_v1_progress_2",PlaylumiExpedition.key(2));
    }
}
