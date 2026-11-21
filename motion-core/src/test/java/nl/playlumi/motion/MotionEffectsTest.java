package nl.playlumi.motion;

public final class MotionEffectsTest {
    private static int assertions;

    public static void main(String[] args) {
        MotionEffects.Kind[] kinds = MotionEffects.Kind.values();
        eq(8, kinds.length, "all eight effects");
        for (MotionEffects.Kind kind : kinds) {
            int duration = MotionEffects.durationMillis(kind);
            yes(duration >= 400 && duration <= 1500, kind + " duration");
            MotionEffects.Frame start = MotionEffects.sample(kind, -90, false);
            MotionEffects.Frame zero = MotionEffects.sample(kind, 0, false);
            same(start.x, zero.x, kind + " clamps negative time");
            same(start.opacity, zero.opacity, kind + " start opacity");
            yes(!start.finished, kind + " not finished at start");

            for (int i = 0; i <= 30; i++) {
                MotionEffects.Frame f = MotionEffects.sample(kind, duration * i / 30, false);
                finite(f.x, kind + " x");
                finite(f.y, kind + " y");
                finite(f.scale, kind + " scale");
                finite(f.rotationDegrees, kind + " rotation");
                finite(f.opacity, kind + " opacity");
                finite(f.accent, kind + " accent");
                yes(f.opacity >= 0 && f.opacity <= 1, kind + " opacity 0..1");
                yes(f.accent >= 0 && f.accent <= 1, kind + " accent 0..1");
                yes(f.scale >= 0 && f.scale <= 2, kind + " scale reasonable");
            }
            yes(MotionEffects.sample(kind, duration, false).finished, kind + " finishes");
            yes(MotionEffects.sample(kind, duration + 9000, false).finished,
                    kind + " remains finished");
            yes(!MotionEffects.sample(kind, duration - 1, false).finished,
                    kind + " not prematurely finished");

            MotionEffects.Frame stillA = MotionEffects.sample(kind, 0, true);
            MotionEffects.Frame stillB = MotionEffects.sample(kind, 9000, true);
            same(stillA.x, stillB.x, kind + " reduced motion x");
            same(stillA.y, stillB.y, kind + " reduced motion y");
            same(stillA.scale, stillB.scale, kind + " reduced motion scale");
            same(0, stillA.rotationDegrees, kind + " reduced motion no spin");
            same(1, stillA.opacity, kind + " reduced motion visible");
            yes(stillA.finished && stillB.finished, kind + " reduced motion immediate");
        }
        yes(MotionEffects.sample(MotionEffects.Kind.STAR_TRAIL, 0, false).y >
                MotionEffects.sample(MotionEffects.Kind.STAR_TRAIL, 720, false).y,
                "star moves upward to counter");
        yes(MotionEffects.sample(MotionEffects.Kind.MASCOT_HIGH_FIVE, 320, false).y <
                MotionEffects.sample(MotionEffects.Kind.MASCOT_HIGH_FIVE, 0, false).y,
                "mascot visibly hops");
        yes(MotionEffects.sample(MotionEffects.Kind.MEMORY_FLIP, 240, false).scale < .001,
                "memory card midpoint is edge-on");
        boolean failed = false;
        try { MotionEffects.sample(null, 0, false); }
        catch (IllegalArgumentException expected) { failed = true; }
        yes(failed, "null effect rejected");
        System.out.println("PASS " + assertions + " assertions; " + kinds.length +
                " procedural animations; no Android runtime required");
    }

    private static void finite(double value, String message) {
        yes(!Double.isNaN(value) && !Double.isInfinite(value), message);
    }
    private static void same(double expected, double actual, String message) {
        yes(Math.abs(expected - actual) < 1e-10, message);
    }
    private static void eq(int expected, int actual, String message) {
        yes(expected == actual, message);
    }
    private static void yes(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
