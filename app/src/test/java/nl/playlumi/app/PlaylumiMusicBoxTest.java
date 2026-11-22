package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public final class PlaylumiMusicBoxTest {
    @Test public void eighteenDistinctNamedJinglesUseTheExistingSoundBank(){
        assertEquals(18,PlaylumiMusicBox.COUNT);
        assertEquals(3,PlaylumiMusicBox.PAGES);
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
        assertEquals(18,sounds.size());
        assertEquals(18,names.size());
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
        assertEquals(2,PlaylumiMusicBox.safePage(2));
        assertEquals(12,PlaylumiMusicBox.firstOnPage(2));
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
            assertEquals(17,results.size());
        }
        Set<Integer> initial=new HashSet<>();
        for(int d=0;d<PlaylumiMusicBox.COUNT;d++)
            assertTrue(initial.add(PlaylumiMusicBox.surprise(-1,d)));
        assertEquals(18,initial.size());
    }
    @Test public void selectedAndFavoriteMarkersAlwaysMatchTrackState(){
        for(int i=0;i<PlaylumiMusicBox.COUNT;i++){
            String plain=PlaylumiMusicBox.displayTitle(i,false,false);
            String selected=PlaylumiMusicBox.displayTitle(i,true,false);
            String favorite=PlaylumiMusicBox.displayTitle(i,false,true);
            String both=PlaylumiMusicBox.displayTitle(i,true,true);
            assertEquals(PlaylumiMusicBox.title(i),plain);
            assertEquals("🎵 "+plain,selected);
            assertEquals("⭐ "+plain,favorite);
            assertEquals("🎵 ⭐ "+plain,both);
        }
    }
    @Test public void musicBoxNavigationKeepsPlayingButLeavingStopsSound(){
        assertFalse(PlaylumiMusicBox.shouldStopForRefresh(true,true));
        assertTrue(PlaylumiMusicBox.shouldStopForRefresh(true,false));
        assertTrue(PlaylumiMusicBox.shouldStopForRefresh(false,true));
        assertTrue(PlaylumiMusicBox.shouldStopForRefresh(false,false));
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidTrackRejected(){PlaylumiMusicBox.effect(18);}
    @Test(expected=IllegalArgumentException.class)
    public void badRandomDrawRejected(){PlaylumiMusicBox.surprise(3,17);}
}
