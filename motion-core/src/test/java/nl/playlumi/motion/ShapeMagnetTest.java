package nl.playlumi.motion;

import java.util.Random;

/** Pure Java tests for the A02 Shape Magnet; run in CI without Android. */
public final class ShapeMagnetTest {
    private static int assertions;

    public static void main(String[] args) {
        beginningAndEnd();
        midFlight();
        reducedMotion();
        bizarreInputMustNeverCrash();
        repeatabilityAndStress();
        System.out.println("PASS " + assertions + " ShapeMagnet assertions");
    }

    private static void beginningAndEnd() {
        ShapeMagnet.Frame a = ShapeMagnet.sample(.17, .91, .84, .24, 0, false);
        ShapeMagnet.Frame negative = ShapeMagnet.sample(.17, .91, .84, .24, -100, false);
        ShapeMagnet.Frame b = ShapeMagnet.sample(.17, .91, .84, .24,
                ShapeMagnet.DURATION_MILLIS, false);
        ShapeMagnet.Frame after = ShapeMagnet.sample(.17, .91, .84, .24,
                100000L, false);
        near(.17, a.x, "start x");
        near(.91, a.y, "start y");
        near(negative.x, a.x, "negative clamped x");
        near(negative.y, a.y, "negative clamped y");
        yes(!a.finished, "not finished at start");
        near(.84, b.x, "exact destination x");
        near(.24, b.y, "exact destination y");
        near(1, b.scale, "normal scale at end");
        near(0, b.rotationDegrees, "no rotation at end");
        near(0, b.sparkleOpacity, "no lingering sparkles");
        near(0, b.targetGlow, "no lingering glow");
        yes(b.finished && after.finished, "finished stays true");
        near(after.x, b.x, "end x stable");
        near(after.y, b.y, "end y stable");
        yes(!ShapeMagnet.sample(.17, .91, .84, .24,
                ShapeMagnet.DURATION_MILLIS - 1, false).finished,
                "no premature finish");
        yes(ShapeMagnet.DURATION_MILLIS >= 450 &&
                ShapeMagnet.DURATION_MILLIS <= 1200, "child-friendly duration");
        yes(ShapeMagnet.SPARKLE_COUNT == 3, "exactly three gentle sparkles");
    }

    private static void midFlight() {
        ShapeMagnet.Frame a = ShapeMagnet.sample(.10, .75, .88, .24, 280, false);
        yes(a.x > .10 && a.x < .88, "shape travels horizontally");
        yes(a.y < .75 && a.y > .24, "shape travels vertically");
        yes(a.scale > 1 && a.scale < 1.15, "gentle squash-and-stretch");
        yes(Math.abs(a.rotationDegrees) <= 5, "rotation bounded");
        ShapeMagnet.Frame spark = ShapeMagnet.sample(.10, .75, .88, .24,
                385, false);
        yes(spark.sparkleOpacity > .85, "sparkles visible on arrival");
        yes(spark.targetGlow >= 0 && spark.targetGlow <= 1,
                "target glow bounded");
    }

    private static void reducedMotion() {
        for (long at : new long[]{-1000, 0, 10, 280, 560, 9000}) {
            ShapeMagnet.Frame a = ShapeMagnet.sample(.05, .8, .9, .1, at, true);
            near(.9, a.x, "reduced motion at target x");
            near(.1, a.y, "reduced motion at target y");
            near(1, a.scale, "reduced scale stable");
            near(0, a.rotationDegrees, "no rotation with reduce motion");
            near(0, a.sparkleOpacity, "no sparkles with reduce motion");
            near(0, a.sparkleRadius, "no moving ring with reduce motion");
            near(1, a.targetGlow, "static positive feedback");
            yes(a.finished, "no animation frames needed");
        }
    }

    private static void bizarreInputMustNeverCrash() {
        double[] weird = {Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, -100, 200, -.00001, 1.00001};
        for (double value : weird) {
            valid(ShapeMagnet.sample(value, value, value, value,
                    Long.MIN_VALUE, false), "invalid input at start");
            valid(ShapeMagnet.sample(value, value, value, value,
                    Long.MAX_VALUE, false), "invalid input at end");
            valid(ShapeMagnet.sample(value, value, value, value,
                    Long.MAX_VALUE, true), "invalid input reduced motion");
        }
    }

    private static void repeatabilityAndStress() {
        Random rng = new Random(1977);
        for (int scene = 0; scene < 220; scene++) {
            double sx = rng.nextDouble();
            double sy = rng.nextDouble();
            double tx = rng.nextDouble();
            double ty = rng.nextDouble();
            for (int time = 0; time <= 560; time += 14) {
                ShapeMagnet.Frame a = ShapeMagnet.sample(sx, sy, tx, ty, time, false);
                ShapeMagnet.Frame b = ShapeMagnet.sample(sx, sy, tx, ty, time, false);
                valid(a, "random scene " + scene);
                near(a.x, b.x, "repeatable x");
                near(a.y, b.y, "repeatable y");
                near(a.scale, b.scale, "repeatable scale");
                near(a.sparkleOpacity, b.sparkleOpacity, "repeatable sparkle");
            }
        }
    }

    private static void valid(ShapeMagnet.Frame f, String where) {
        finite(f.x, where + " x");
        finite(f.y, where + " y");
        finite(f.scale, where + " scale");
        finite(f.rotationDegrees, where + " rotation");
        finite(f.sparkleOpacity, where + " sparkle opacity");
        finite(f.sparkleRadius, where + " sparkle radius");
        finite(f.targetGlow, where + " target glow");
        yes(f.x >= 0 && f.x <= 1, where + " x stays within viewport");
        yes(f.y >= 0 && f.y <= 1, where + " y stays within viewport");
        yes(f.scale >= .9 && f.scale <= 1.15, where + " safe scale");
        yes(f.sparkleOpacity >= 0 && f.sparkleOpacity <= 1,
                where + " safe sparkle alpha");
        yes(f.targetGlow >= 0 && f.targetGlow <= 1,
                where + " safe glow");
        yes(f.sparkleRadius >= 0 && f.sparkleRadius <= .1,
                where + " safe sparkle radius");
    }

    private static void finite(double value, String where) {
        yes(!Double.isNaN(value) && !Double.isInfinite(value), where);
    }

    private static void near(double expected, double actual, String where) {
        yes(Math.abs(expected - actual) < 1e-9, where +
                ": expected " + expected + " actual " + actual);
    }

    private static void yes(boolean ok, String where) {
        assertions++;
        if (!ok) throw new AssertionError(where);
    }
}
