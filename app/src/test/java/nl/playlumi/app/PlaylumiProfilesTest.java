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
        assertEquals("playlumi-kunstwerk-1.png",PlaylumiProfiles.artworkFile(1));
        assertEquals("playlumi-kunstwerk-2.png",PlaylumiProfiles.artworkFile(2));
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
    @Test public void anonymousPlayerLabelsAndExistingIconsMatchTheThreeStableSlots() {
        assertEquals(3,PlaylumiProfiles.COUNT);
        assertArrayEquals(new String[]{"⭐","🌸","🚀"},PlaylumiProfiles.ICONS);
        assertEquals("Speler 1",PlaylumiProfiles.label(0));
        assertEquals("Speler 2",PlaylumiProfiles.label(1));
        assertEquals("Speler 3",PlaylumiProfiles.label(2));
    }
    @Test public void invalidDisplaySlotsAreRejectedRatherThanMislabelingAnotherPlayer() {
        for(int profile:new int[]{Integer.MIN_VALUE,-1,3,Integer.MAX_VALUE}) {
            try{PlaylumiProfiles.label(profile);fail("accepted invalid profile "+profile);}
            catch(IllegalArgumentException expected){assertEquals("profile",expected.getMessage());}
        }
    }
    @Test public void onlyTheThreeActualAgeChoicesAreEstablishedAges() {
        for(int age=0;age<3;age++)assertTrue(PlaylumiProfiles.hasChosenAge(age));
        for(int age:new int[]{Integer.MIN_VALUE,-2,-1,3,99,Integer.MAX_VALUE})
            assertFalse(PlaylumiProfiles.hasChosenAge(age));
    }
    @Test public void missingOrDamagedAgeNeverSilentlyBecomesTheYoungestOrOldestGroup() {
        assertEquals("3–4 jaar",PlaylumiProfiles.ageLabel(0));
        assertEquals("5–6 jaar",PlaylumiProfiles.ageLabel(1));
        assertEquals("7+ jaar",PlaylumiProfiles.ageLabel(2));
        for(int age:new int[]{-1,3,Integer.MAX_VALUE})
            assertEquals("Leeftijd nog kiezen",PlaylumiProfiles.ageLabel(age));
    }
    @Test public void anUnstoredWalletDoesNotCreateAnInventedStarterBalance() {
        assertEquals("Leeftijd nog kiezen",PlaylumiProfiles.summary(-1,null));
        assertEquals("3–4 jaar",PlaylumiProfiles.summary(0,null));
        assertEquals("7+ jaar",PlaylumiProfiles.summary(2,null));
    }
    @Test public void unknownAgeDoesNotHideThePlayersActualSavedBalance() {
        assertEquals("Leeftijd nog kiezen · 75 speelsterren",PlaylumiProfiles.summary(-1,75));
        assertEquals("Leeftijd nog kiezen · 75 speelsterren",PlaylumiProfiles.summary(42,75));
        assertEquals("5–6 jaar · 75 speelsterren",PlaylumiProfiles.summary(1,75));
    }
    @Test public void walletSummaryUsesTheActualCountWithCorrectSingularAndPlural() {
        assertEquals("3–4 jaar · 0 speelsterren",PlaylumiProfiles.summary(0,0));
        assertEquals("3–4 jaar · 1 speelster",PlaylumiProfiles.summary(0,1));
        assertEquals("3–4 jaar · 2 speelsterren",PlaylumiProfiles.summary(0,2));
    }
    @Test public void corruptNegativeBalancesAreClampedWithoutOverflowOrFabricatedLevels() {
        assertEquals("7+ jaar · 0 speelsterren",PlaylumiProfiles.summary(2,Integer.MIN_VALUE));
        assertEquals("7+ jaar · 2147483647 speelsterren",PlaylumiProfiles.summary(2,Integer.MAX_VALUE));
    }
}
