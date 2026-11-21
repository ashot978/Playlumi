package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

/** Recency, direct re-entry and safe per-profile preference decoding. */
public final class PlaylumiPlayHistoryTest {
    @Test public void keepsLastFourDifferentActivitiesInNewestFirstOrder() {
        PlaylumiPlayHistory h=new PlaylumiPlayHistory();
        for(int i=0;i<8;i++)h.record(i,0,i%3,false);
        assertEquals(4,h.size());
        for(int i=0;i<4;i++)assertEquals(7-i,h.get(i).mode);
        assertEquals(0,h.get(0).variant);
    }

    @Test public void replayingSameGameMovesItToTopWithoutDuplication() {
        PlaylumiPlayHistory h=new PlaylumiPlayHistory();
        h.record(3,0,0,false);
        h.record(4,2,1,false);
        h.record(3,0,2,true);
        assertEquals(2,h.size());
        assertEquals(3,h.get(0).mode);
        assertEquals(0,h.get(0).variant);
        assertEquals(2,h.get(0).difficulty);
        assertTrue(h.get(0).smart);
        h.record(3,1,0,false);
        assertEquals(3,h.size()); // different variant, different activity
        assertEquals(1,h.get(0).variant);
    }

    @Test public void serializationRestoresEverySettingWithSameOrder() {
        PlaylumiPlayHistory h=new PlaylumiPlayHistory();
        h.record(0,0,0,true);
        h.record(23,4,2,false);
        h.record(18,3,1,true);
        String encoded=h.serialize();
        PlaylumiPlayHistory restored=PlaylumiPlayHistory.restore(encoded);
        assertEquals(h.size(),restored.size());
        for(int i=0;i<h.size();i++)
            assertEquals(h.get(i),restored.get(i));
        assertEquals(encoded,restored.serialize());
    }

    @Test public void invalidPreferencesDoNotCrashAndDoNotShowUnknownGames() {
        assertEquals(0,PlaylumiPlayHistory.restore(null).size());
        assertEquals(0,PlaylumiPlayHistory.restore("").size());
        assertEquals(0,PlaylumiPlayHistory.restore("lots_of_random_data").size());
        assertEquals(0,PlaylumiPlayHistory.restore(new String(new char[400])).size());
        PlaylumiPlayHistory decoded=PlaylumiPlayHistory.restore(
                "0,0,1,0;bad;24,0,0,1;1,4,2,1;0,0,4,1;2,1,2,2");
        assertEquals(2,decoded.size());
        assertEquals(0,decoded.get(0).mode);
        assertEquals(1,decoded.get(0).difficulty);
        assertEquals(1,decoded.get(1).mode);
        assertTrue(decoded.get(1).smart);
    }

    @Test public void noLeakingBetweenSeparateChildProfileHistories() {
        PlaylumiPlayHistory childA=new PlaylumiPlayHistory();
        PlaylumiPlayHistory childB=new PlaylumiPlayHistory();
        childA.record(11,2,2,true);
        assertEquals(1,childA.size());
        assertEquals(0,childB.size());
        childB.record(3,0,0,false);
        assertEquals(3,childB.get(0).mode);
        assertEquals(11,childA.get(0).mode);
    }

    @Test public void validBoundsAndInvalidSettings() {
        PlaylumiPlayHistory h=new PlaylumiPlayHistory();
        h.record(0,0,0,false);
        h.record(LearningGame.GAME_NAMES.length-1,4,2,true);
        assertThrows(IllegalArgumentException.class,()->h.record(-1,0,0,false));
        assertThrows(IllegalArgumentException.class,()->h.record(24,0,0,false));
        assertThrows(IllegalArgumentException.class,()->h.record(1,5,0,false));
        assertThrows(IllegalArgumentException.class,()->h.record(1,0,3,false));
        assertThrows(IllegalArgumentException.class,()->h.record((PlaylumiPlayHistory.Entry)null));
    }
}
