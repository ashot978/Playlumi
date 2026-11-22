package nl.playlumi.app;

import org.junit.Test;
import java.util.Locale;
import static org.junit.Assert.*;

public final class PlaylumiVoiceChoiceTest {
    @Test public void dutchOfflinePreferred() {
        int dutch=PlaylumiVoiceChoice.rank(Locale.forLanguageTag("nl-NL"),false,400);
        int belgian=PlaylumiVoiceChoice.rank(Locale.forLanguageTag("nl-BE"),false,500);
        assertTrue(dutch>belgian);
        assertTrue(belgian>0);
        assertEquals(-1,PlaylumiVoiceChoice.rank(Locale.forLanguageTag("nl-NL"),true,500));
        assertEquals(-1,PlaylumiVoiceChoice.rank(Locale.ENGLISH,false,500));
        assertEquals(-1,PlaylumiVoiceChoice.rank(null,false,500));
    }
    @Test public void naturalVoiceQualityAffectsRanking() {
        int high=PlaylumiVoiceChoice.rank(Locale.forLanguageTag("nl-NL"),false,500);
        int low=PlaylumiVoiceChoice.rank(Locale.forLanguageTag("nl-NL"),false,100);
        assertTrue(high>low);
    }
    @Test public void qualityRankIsBounded() {
        assertEquals(PlaylumiVoiceChoice.rank(Locale.forLanguageTag("nl-NL"),false,0),
                PlaylumiVoiceChoice.rank(Locale.forLanguageTag("nl-NL"),false,-1));
        assertEquals(PlaylumiVoiceChoice.rank(Locale.forLanguageTag("nl-NL"),false,500),
                PlaylumiVoiceChoice.rank(Locale.forLanguageTag("nl-NL"),false,1000));
    }
}
