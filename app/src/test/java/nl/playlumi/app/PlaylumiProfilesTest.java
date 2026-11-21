package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiProfilesTest {
    @Test public void originalProfileKeepsOldStorageIdentity() {
        assertNull(PlaylumiProfiles.preferences(0));
        assertEquals("playlumi-kunstwerk.png",PlaylumiProfiles.artworkFile(0));
    }
    @Test public void secondaryProfilesAreStrictlyIsolated() {
        assertEquals("playlumi-child-1",PlaylumiProfiles.preferences(1));
        assertEquals("playlumi-child-2",PlaylumiProfiles.preferences(2));
        assertNotEquals(PlaylumiProfiles.preferences(1),PlaylumiProfiles.preferences(2));
        assertNotEquals(PlaylumiProfiles.artworkFile(1),PlaylumiProfiles.artworkFile(2));
        assertNotEquals(PlaylumiProfiles.artworkFile(0),PlaylumiProfiles.artworkFile(1));
    }
    @Test public void unsafeSelectedIdDefaultsToFirstPlayer() {
        assertEquals(0,PlaylumiProfiles.safe(-2));
        assertEquals(0,PlaylumiProfiles.safe(10));
        assertEquals(2,PlaylumiProfiles.safe(2));
    }
    @Test(expected=IllegalArgumentException.class)
    public void unknownArtworkProfileIsRejected() {
        PlaylumiProfiles.artworkFile(3);
    }
}
