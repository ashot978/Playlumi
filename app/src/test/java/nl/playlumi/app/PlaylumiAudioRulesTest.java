package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiAudioRulesTest {
    @Test public void deliberateMelodyWorksWithQuietEffectsOff() {
        assertTrue(PlaylumiAudioRules.shouldPlay(true,false,true));
        assertTrue(PlaylumiAudioRules.shouldPlay(true,true,true));
        assertFalse(PlaylumiAudioRules.shouldPlay(true,false,false));
        assertTrue(PlaylumiAudioRules.shouldPlay(true,true,false));
    }

    @Test public void globalMuteStillMutesEverything() {
        for(boolean effect:new boolean[]{false,true})
            for(boolean musical:new boolean[]{false,true})
                assertFalse(PlaylumiAudioRules.shouldPlay(false,effect,musical));
    }
}
