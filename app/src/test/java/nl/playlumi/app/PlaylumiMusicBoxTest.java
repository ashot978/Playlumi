package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public final class PlaylumiMusicBoxTest {
    @Test public void twelveDistinctNamedJinglesUseTheExistingSoundBank(){
        assertEquals(12,PlaylumiMusicBox.COUNT);
        assertEquals(2,PlaylumiMusicBox.PAGES);
        Set<Integer> sounds=new HashSet<>();
        Set<String> names=new HashSet<>();
        for(int i=0;i<PlaylumiMusicBox.COUNT;i++){
            assertTrue(names.add(PlaylumiMusicBox.name(i)));
            assertTrue(PlaylumiMusicBox.title(i).contains(PlaylumiMusicBox.name(i)));
            int effect=PlaylumiMusicBox.effect(i);
            assertEquals(PlaylumiTunes.BONUS+i,effect);
            assertTrue(effect<PlaylumiTunes.COUNT);
            assertTrue(sounds.add(java.util.Arrays.hashCode(PlaylumiTunes.wav(effect))));
        }
        assertEquals(12,sounds.size());
        assertEquals(12,names.size());
    }
    @Test public void sixChoicesPerPageAndSafeRotation(){
        for(int p=0;p<PlaylumiMusicBox.PAGES;p++){
            int first=PlaylumiMusicBox.firstOnPage(p);
            assertEquals(p*6,first);
            assertEquals(6,Math.min(first+PlaylumiMusicBox.PAGE_SIZE,
                    PlaylumiMusicBox.COUNT)-first);
        }
        assertEquals(0,PlaylumiMusicBox.safePage(-1));
        assertEquals(0,PlaylumiMusicBox.safePage(30));
        assertEquals(-1,PlaylumiMusicBox.safeSelected(99));
    }
    @Test public void surprisesNeverRepeatTheirLastTrack(){
        for(int last=0;last<PlaylumiMusicBox.COUNT;last++){
            Set<Integer> results=new HashSet<>();
            for(int draw=0;draw<PlaylumiMusicBox.COUNT-1;draw++){
                int next=PlaylumiMusicBox.surprise(last,draw);
                assertNotEquals(last,next);
                assertTrue(next>=0 && next<PlaylumiMusicBox.COUNT);
                assertTrue(results.add(next));
            }
            assertEquals(11,results.size());
        }
        Set<Integer> initial=new HashSet<>();
        for(int d=0;d<PlaylumiMusicBox.COUNT;d++)
            assertTrue(initial.add(PlaylumiMusicBox.surprise(-1,d)));
        assertEquals(12,initial.size());
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidTrackRejected(){PlaylumiMusicBox.effect(12);}
    @Test(expected=IllegalArgumentException.class)
    public void badRandomDrawRejected(){PlaylumiMusicBox.surprise(3,11);}
}
