package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public final class PlaylumiGardenMotionTest {
    @Test public void startsAsOldPlantAndEndsAsFullyGrownNewPlant() {
        PlaylumiGardenMotion.Frame first =
                PlaylumiGardenMotion.sample(0, false);
        assertEquals(1, first.oldAlpha, 0);
        assertEquals(0, first.newAlpha, 0);
        assertFalse(first.finished);
        PlaylumiGardenMotion.Frame last =
                PlaylumiGardenMotion.sample(1080, false);
        assertEquals(0, last.oldAlpha, 0);
        assertEquals(1, last.newAlpha, 0);
        assertEquals(1, last.newScale, 1e-12);
        assertEquals(0, last.sparkle, 1e-12);
        assertEquals(0, last.waterOrSunshine, 1e-12);
        assertTrue(last.finished);
    }

    @Test public void oneStepHasNoticeableGrowingAndGentleSparkles() {
        PlaylumiGardenMotion.Frame middle =
                PlaylumiGardenMotion.sample(600, false);
        assertTrue(middle.newScale > .5 && middle.newScale <= 1.1);
        assertTrue(middle.newAlpha > .95);
        assertTrue(middle.sparkle > 0);
        assertFalse(middle.finished);
    }

    @Test public void reducedMotionIsInstantAndHasNoParticles() {
        for (long t : new long[]{Long.MIN_VALUE, 0, 250, 1080, Long.MAX_VALUE}) {
            PlaylumiGardenMotion.Frame f =
                    PlaylumiGardenMotion.sample(t, true);
            assertTrue(f.finished);
            assertEquals(0, f.oldAlpha, 0);
            assertEquals(1, f.newAlpha, 0);
            assertEquals(1, f.newScale, 0);
            assertEquals(0, f.sparkle, 0);
            assertEquals(0, f.waterOrSunshine, 0);
        }
    }

    @Test public void extremeDurationsNeverYieldInvalidFrames() {
        Random random = new Random(20261121);
        for (int i = 0; i < 10000; i++) {
            long t = i < 2 ? (i == 0 ? Long.MIN_VALUE : Long.MAX_VALUE) :
                    random.nextInt(2000) - 500;
            PlaylumiGardenMotion.Frame f =
                    PlaylumiGardenMotion.sample(t, false);
            assertTrue(Double.isFinite(f.progress));
            assertTrue(Double.isFinite(f.oldAlpha));
            assertTrue(Double.isFinite(f.newAlpha));
            assertTrue(Double.isFinite(f.newScale));
            assertTrue(Double.isFinite(f.sparkle));
            assertTrue(Double.isFinite(f.waterOrSunshine));
            assertTrue(f.progress >= 0 && f.progress <= 1);
            assertTrue(f.oldAlpha >= 0 && f.oldAlpha <= 1);
            assertTrue(f.newAlpha >= 0 && f.newAlpha <= 1);
            assertTrue(f.newScale >= .5 && f.newScale <= 1.1);
            assertTrue(f.sparkle >= 0 && f.sparkle <= 1);
            assertTrue(f.waterOrSunshine >= 0 && f.waterOrSunshine <= 1);
        }
        assertFalse(PlaylumiGardenMotion.sample(1079,false).finished);
        assertTrue(PlaylumiGardenMotion.sample(Long.MAX_VALUE,false).finished);
    }
}
