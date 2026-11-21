package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiGardenJourneyTest {
    @Test public void everyFourthCompletedSessionBloomsAndAwardsOnce() {
        PlaylumiGarden garden = new PlaylumiGarden();
        int blooms = 0;
        for (int round = 0; round < 48; round++) {
            PlaylumiGardenJourney.Growth g =
                    PlaylumiGardenJourney.afterCompletedRound(garden);
            assertNotNull(g);
            assertEquals(round / 4, g.plant);
            assertEquals(round % 4, g.fromStage);
            assertEquals(round % 4 + 1, g.toStage);
            assertEquals(round % 2, g.action);
            assertEquals(round % 4 == 3, g.firstBloom);
            if (g.firstBloom) blooms++;
        }
        assertEquals(12, blooms);
        assertEquals(PlaylumiGarden.COUNT, garden.bloomedCount());
        assertEquals((1 << PlaylumiGarden.COUNT) - 1, garden.rewardMask());
        assertNull(PlaylumiGardenJourney.afterCompletedRound(garden));
        assertNull(PlaylumiGardenJourney.afterCompletedRound(garden));
    }

    @Test public void finishesManuallyStartedPlantBeforeNewSeeds() {
        PlaylumiGarden garden = new PlaylumiGarden();
        assertEquals(0, garden.grow(6, PlaylumiGarden.WATER));
        assertEquals(0, garden.grow(6, PlaylumiGarden.SUN));
        assertEquals(0, garden.grow(6, PlaylumiGarden.WATER));
        PlaylumiGardenJourney.Growth g =
                PlaylumiGardenJourney.afterCompletedRound(garden);
        assertEquals(6, g.plant);
        assertEquals(3, g.fromStage);
        assertEquals(4, g.toStage);
        assertTrue(g.firstBloom);
        assertTrue(garden.awarded(6));
        PlaylumiGardenJourney.Growth next =
                PlaylumiGardenJourney.afterCompletedRound(garden);
        assertEquals(0, next.plant);
        assertEquals(0, next.fromStage);
        assertFalse(next.firstBloom);
    }

    @Test public void persistedGrowthDoesNotRepeatRewards() {
        PlaylumiGarden one = new PlaylumiGarden();
        for (int i = 0; i < 4; i++)
            PlaylumiGardenJourney.afterCompletedRound(one);
        PlaylumiGarden restored = new PlaylumiGarden();
        restored.restoreRewards(one.rewardMask());
        for (int i = 0; i < PlaylumiGarden.COUNT; i++)
            restored.restorePlant(i, one.stage(i));
        assertTrue(restored.awarded(0));
        PlaylumiGardenJourney.Growth next =
                PlaylumiGardenJourney.afterCompletedRound(restored);
        assertEquals(1, next.plant);
        assertFalse(next.firstBloom);
        assertEquals(1, restored.rewardMask());
    }
}
