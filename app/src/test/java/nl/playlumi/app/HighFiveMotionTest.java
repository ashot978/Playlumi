package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

/** The high-five never affects scoring and needs no Android runtime. */
public final class HighFiveMotionTest {
    @Test public void streakRuleDoesNotCompeteWithBigCelebrations() {
        for (int streak = -12; streak <= 32; streak++) {
            boolean expected = streak >= 3 && (streak - 3) % 5 == 0;
            assertEquals(expected, HighFiveMotion.shouldPlay(streak, false));
            assertFalse(HighFiveMotion.shouldPlay(streak, true));
        }
        assertTrue(HighFiveMotion.shouldPlay(3, false));
        assertTrue(HighFiveMotion.shouldPlay(8, false));
        assertFalse(HighFiveMotion.shouldPlay(5, false));
    }

    @Test public void firstFrameIsVisibleButDoesNotMove() {
        HighFiveMotion.Frame start = HighFiveMotion.sample(0, false);
        assertEquals(0, start.hop, 1e-12);
        assertEquals(0, start.handsMeet, 1e-12);
        assertEquals(0, start.sparkle, 1e-12);
        assertEquals(1, start.opacity, 1e-12);
        assertFalse(start.finished);
    }

    @Test public void handsMeetBeforeSparklesGlow() {
        HighFiveMotion.Frame middle = HighFiveMotion.sample(340, false);
        assertTrue(middle.hop > .9);
        assertTrue(middle.handsMeet > .9);
        HighFiveMotion.Frame flash = HighFiveMotion.sample(450, false);
        assertTrue(flash.sparkle > .5);
        assertTrue(flash.opacity > .5);
    }

    @Test public void finishesAndDisappears() {
        HighFiveMotion.Frame last = HighFiveMotion.sample(
                HighFiveMotion.DURATION_MILLIS, false);
        assertTrue(last.finished);
        assertEquals(0, last.opacity, 1e-12);
        assertEquals(0, last.sparkle, 1e-12);
        assertTrue(HighFiveMotion.sample(Long.MAX_VALUE, false).finished);
        assertFalse(HighFiveMotion.sample(
                HighFiveMotion.DURATION_MILLIS - 1, false).finished);
        assertTrue(HighFiveMotion.DURATION_MILLIS < 1000);
    }

    @Test public void motionReductionSkipsTheDecorationEntirely() {
        for (long millis : new long[]{Long.MIN_VALUE, 0, 100, 400, 680, Long.MAX_VALUE}) {
            HighFiveMotion.Frame still = HighFiveMotion.sample(millis, true);
            assertEquals(0, still.hop, 1e-12);
            assertEquals(1, still.handsMeet, 1e-12);
            assertEquals(0, still.sparkle, 1e-12);
            assertEquals(0, still.opacity, 1e-12);
            assertEquals(0, still.tiltDegrees, 1e-12);
            assertTrue(still.finished);
        }
    }

    @Test public void extremeTimesAndManyFramesStaySafeAndDeterministic() {
        Random random = new Random(20261121);
        for (int sample = 0; sample < 12000; sample++) {
            long elapsed = random.nextInt(1600) - 300;
            HighFiveMotion.Frame f = HighFiveMotion.sample(elapsed, false);
            HighFiveMotion.Frame again = HighFiveMotion.sample(elapsed, false);
            assertEquals(f.hop, again.hop, 0);
            assertEquals(f.handsMeet, again.handsMeet, 0);
            assertEquals(f.opacity, again.opacity, 0);
            assertEquals(f.sparkle, again.sparkle, 0);
            assertTrue(Double.isFinite(f.hop));
            assertTrue(Double.isFinite(f.handsMeet));
            assertTrue(Double.isFinite(f.sparkle));
            assertTrue(Double.isFinite(f.opacity));
            assertTrue(Double.isFinite(f.tiltDegrees));
            assertTrue(f.hop >= 0 && f.hop <= 1);
            assertTrue(f.handsMeet >= 0 && f.handsMeet <= 1);
            assertTrue(f.sparkle >= 0 && f.sparkle <= 1);
            assertTrue(f.opacity >= 0 && f.opacity <= 1);
            assertTrue(Math.abs(f.tiltDegrees) <= 9);
        }
        HighFiveMotion.Frame negative = HighFiveMotion.sample(Long.MIN_VALUE, false);
        HighFiveMotion.Frame zero = HighFiveMotion.sample(0, false);
        assertEquals(zero.hop, negative.hop, 0);
        assertEquals(zero.handsMeet, negative.handsMeet, 0);
    }
}
