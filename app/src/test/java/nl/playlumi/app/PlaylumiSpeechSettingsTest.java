package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiSpeechSettingsTest {
    @Test public void safeDefaultForInvalidStoredValues() {
        assertEquals(PlaylumiSpeechSettings.NORMAL,
                PlaylumiSpeechSettings.normalize(-12));
        assertEquals(PlaylumiSpeechSettings.NORMAL,
                PlaylumiSpeechSettings.normalize(999));
        assertEquals("Normaal",PlaylumiSpeechSettings.label(-15));
    }

    @Test public void speedsStayInSafeRange() {
        float slow=PlaylumiSpeechSettings.rate(PlaylumiSpeechSettings.SLOW);
        float normal=PlaylumiSpeechSettings.rate(PlaylumiSpeechSettings.NORMAL);
        float brisk=PlaylumiSpeechSettings.rate(PlaylumiSpeechSettings.BRISK);
        assertTrue(slow>=.8f);
        assertTrue(slow<normal);
        assertEquals(1.0f,normal,0.0001f);
        assertTrue(brisk>normal);
        assertTrue(brisk<=1.15f);
    }

    @Test public void speedsCycleWithoutSpeechOrSideEffects() {
        assertEquals(PlaylumiSpeechSettings.BRISK,
                PlaylumiSpeechSettings.next(PlaylumiSpeechSettings.NORMAL));
        assertEquals(PlaylumiSpeechSettings.SLOW,
                PlaylumiSpeechSettings.next(PlaylumiSpeechSettings.BRISK));
        assertEquals(PlaylumiSpeechSettings.NORMAL,
                PlaylumiSpeechSettings.next(PlaylumiSpeechSettings.SLOW));
        assertEquals("Rustig",PlaylumiSpeechSettings.label(0));
        assertEquals("Vlot",PlaylumiSpeechSettings.label(2));
    }
}
