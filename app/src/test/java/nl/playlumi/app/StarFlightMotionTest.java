package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

/** Animation math regression tests; pure Java, no Android emulator required. */
public final class StarFlightMotionTest {
    @Test public void starBeginsAtActualTouchedAnswer() {
        StarFlightMotion.Frame frame = StarFlightMotion.sample(
                .18, .92, .85, .12, 0, false);
        assertEquals(.18, frame.x, 1e-12);
        assertEquals(.92, frame.y, 1e-12);
        assertFalse(frame.finished);
        assertEquals(1, frame.opacity, 1e-12);
    }

    @Test public void starLandsExactlyAtEarnedProgressSlot() {
        for (int i = 1; i <= 8; i++) {
            double target = StarFlightMotion.slotCenterFraction(i, 8);
            StarFlightMotion.Frame frame = StarFlightMotion.sample(
                    .25, .8, target, .08, StarFlightMotion.DURATION_MILLIS, false);
            assertEquals(target, frame.x, 1e-12);
            assertEquals(.08, frame.y, 1e-12);
            assertEquals(1, frame.scale, 1e-12);
            assertEquals(0, frame.opacity, 1e-12);
            assertEquals(0, frame.trailOpacity, 1e-12);
            assertTrue(frame.finished);
        }
        assertEquals(.0625, StarFlightMotion.slotCenterFraction(1,8), 1e-12);
        assertEquals(.9375, StarFlightMotion.slotCenterFraction(8,8), 1e-12);
        assertEquals(.5, StarFlightMotion.slotCenterFraction(0,0), 1e-12);
        assertEquals(.0625, StarFlightMotion.slotCenterFraction(-4,8), 1e-12);
        assertEquals(.9375, StarFlightMotion.slotCenterFraction(12,8), 1e-12);
    }

    @Test public void glidesAboveStraightLineThenSettles() {
        StarFlightMotion.Frame mid = StarFlightMotion.sample(
                .10, .92, .85, .15, StarFlightMotion.DURATION_MILLIS / 2, false);
        assertTrue(mid.x > .1 && mid.x < .85);
        assertTrue(mid.y < (.92 + .15) / 2);
        assertTrue(mid.rotationDegrees > 0);
        assertTrue(mid.scale > 1 && mid.scale < 1.3);
        assertTrue(mid.trailOpacity > 0);
        assertFalse(mid.finished);
    }

    @Test public void reducedMotionDoesNotMoveOrFlash() {
        for (long elapsed : new long[]{-800, 0, 10, 280, 720, Long.MAX_VALUE}) {
            StarFlightMotion.Frame still = StarFlightMotion.sample(
                    .10, .91, .80, .10, elapsed, true);
            assertEquals(.8, still.x, 1e-12);
            assertEquals(.1, still.y, 1e-12);
            assertEquals(1, still.scale, 1e-12);
            assertEquals(0, still.rotationDegrees, 1e-12);
            assertEquals(0, still.opacity, 1e-12);
            assertEquals(0, still.trailOpacity, 1e-12);
            assertTrue(still.finished);
        }
    }

    @Test public void elapsedIsClampedAndAnimationAlwaysEnds() {
        StarFlightMotion.Frame start = StarFlightMotion.sample(.1,.9,.85,.1,0,false);
        StarFlightMotion.Frame before = StarFlightMotion.sample(.1,.9,.85,.1,
                Long.MIN_VALUE,false);
        assertEquals(start.x, before.x, 0);
        assertEquals(start.y, before.y, 0);
        assertFalse(StarFlightMotion.sample(.1,.9,.85,.1,719,false).finished);
        assertTrue(StarFlightMotion.sample(.1,.9,.85,.1,720,false).finished);
        assertTrue(StarFlightMotion.sample(.1,.9,.85,.1,
                Long.MAX_VALUE,false).finished);
        assertTrue(StarFlightMotion.DURATION_MILLIS <= 1200);
        assertEquals(3, StarFlightMotion.TRAIL_PARTICLES);
    }

    @Test public void strangeCoordinatesCannotCrashAnimation() {
        double[] weird = {Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, -100, -1, 1.1, 1000};
        for (double v : weird) {
            for (long t : new long[]{Long.MIN_VALUE, 0, 350, 720, Long.MAX_VALUE}) {
                valid(StarFlightMotion.sample(v,v,v,v,t,false));
                valid(StarFlightMotion.sample(v,v,v,v,t,true));
            }
        }
    }

    @Test public void thousandsOfFramesStayFiniteAndDeterministic() {
        Random random = new Random(20261121);
        for (int trial=0; trial<200; trial++) {
            double sx=random.nextDouble(),sy=random.nextDouble();
            double tx=random.nextDouble(),ty=random.nextDouble();
            for (int millis=0;millis<=720;millis+=12) {
                StarFlightMotion.Frame a=StarFlightMotion.sample(
                        sx,sy,tx,ty,millis,false);
                StarFlightMotion.Frame b=StarFlightMotion.sample(
                        sx,sy,tx,ty,millis,false);
                valid(a);
                assertEquals(a.x,b.x,0);
                assertEquals(a.y,b.y,0);
                assertEquals(a.opacity,b.opacity,0);
            }
        }
    }

    private static void valid(StarFlightMotion.Frame f) {
        assertTrue(Double.isFinite(f.x));
        assertTrue(Double.isFinite(f.y));
        assertTrue(Double.isFinite(f.scale));
        assertTrue(Double.isFinite(f.rotationDegrees));
        assertTrue(Double.isFinite(f.opacity));
        assertTrue(Double.isFinite(f.trailOpacity));
        assertTrue(f.x>=0 && f.x<=1);
        assertTrue(f.y>=0 && f.y<=1);
        assertTrue(f.scale>=.95 && f.scale<=1.30);
        assertTrue(f.opacity>=0 && f.opacity<=1);
        assertTrue(f.trailOpacity>=0 && f.trailOpacity<=1);
    }
}
