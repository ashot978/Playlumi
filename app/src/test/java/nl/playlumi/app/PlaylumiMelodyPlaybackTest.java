package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiMelodyPlaybackTest {
    @Test public void repeatedReplayCancelsEveryEarlierTune() {
        PlaylumiMelodyPlayback control=new PlaylumiMelodyPlayback();
        long first=control.begin();
        assertTrue(control.isActive(first));
        long next=control.begin();
        assertFalse(control.isActive(first));
        assertTrue(control.isActive(next));
        control.stop();
        assertFalse(control.isActive(next));
    }

    @Test public void stoppingTwiceCannotRestartOrReviveMelody() {
        PlaylumiMelodyPlayback control=new PlaylumiMelodyPlayback();
        long old=control.begin();
        control.stop();
        control.stop();
        assertFalse(control.isActive(old));
        assertFalse(control.isActive(0));
        assertTrue(control.isActive(control.begin()));
        assertFalse(control.isActive(old));
    }

    @Test public void manyRapidTapsNeverOverlapScheduledPlaybacks() {
        PlaylumiMelodyPlayback control=new PlaylumiMelodyPlayback();
        for(int i=0;i<2000;i++){
            long token=control.begin();
            assertTrue(control.isActive(token));
            if(i%2==0)control.stop();
            assertEquals(i%2!=0,control.isActive(token));
        }
    }
}
