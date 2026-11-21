package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiEasyPlayTest {
    @Test public void firstLaunchStartsWithCounting() {
        assertEquals(0,PlaylumiEasyPlay.category(-1));
        assertEquals(0,PlaylumiEasyPlay.variant(-1));
    }
    @Test public void lastGameIsPreservedWhenValid() {
        assertEquals(18,PlaylumiEasyPlay.category(18));
        assertEquals(4,PlaylumiEasyPlay.variant(4));
        assertEquals(0,PlaylumiEasyPlay.category(24));
        assertEquals(0,PlaylumiEasyPlay.variant(5));
        assertEquals(0,PlaylumiEasyPlay.category(Integer.MAX_VALUE));
        assertEquals(0,PlaylumiEasyPlay.variant(Integer.MIN_VALUE));
    }
    @Test public void youngerChildrenReceiveOptionalAudioByDefault() {
        assertTrue(PlaylumiEasyPlay.autoReadDefault(0));
        assertFalse(PlaylumiEasyPlay.autoReadDefault(1));
        assertFalse(PlaylumiEasyPlay.autoReadDefault(2));
    }
}
