package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiComposerTempoTest {
    @Test public void threeSimpleSpeedsWithNaturalDefault(){
        assertEquals(3,PlaylumiComposerTempo.FAST+1);
        assertEquals(PlaylumiComposerTempo.NORMAL,PlaylumiComposerTempo.safe(80));
        assertEquals(PlaylumiComposerTempo.NORMAL,PlaylumiComposerTempo.safe(-2));
        assertTrue(PlaylumiComposerTempo.delayMillis(PlaylumiComposerTempo.SLOW)>
                PlaylumiComposerTempo.delayMillis(PlaylumiComposerTempo.NORMAL));
        assertTrue(PlaylumiComposerTempo.delayMillis(PlaylumiComposerTempo.NORMAL)>
                PlaylumiComposerTempo.delayMillis(PlaylumiComposerTempo.FAST));
        assertEquals(420,PlaylumiComposerTempo.delayMillis(-100));
    }
    @Test public void cyclesWithoutEverStartingPlayback(){
        assertEquals(PlaylumiComposerTempo.FAST,
                PlaylumiComposerTempo.next(PlaylumiComposerTempo.NORMAL));
        assertEquals(PlaylumiComposerTempo.SLOW,
                PlaylumiComposerTempo.next(PlaylumiComposerTempo.FAST));
        assertEquals(PlaylumiComposerTempo.NORMAL,
                PlaylumiComposerTempo.next(PlaylumiComposerTempo.SLOW));
        assertEquals("🐢 Rustig",PlaylumiComposerTempo.label(0));
    }
}
