package nl.playlumi.app;

/**
 * Pure Java choreography for a star flying from a correct answer to the
 * earned slot of the existing star-progress view.
 *
 * No view, score, preference, sound, network or Android dependencies.
 * Coordinates are fractions of the SAME overlay viewport [0..1].
 */
public final class StarFlightMotion {
    public static final int DURATION_MILLIS = 720;
    public static final int TRAIL_PARTICLES = 3;

    private StarFlightMotion() {}

    public static final class Frame {
        public final double x, y, scale, rotationDegrees;
        public final double opacity, trailOpacity;
        public final boolean finished;

        private Frame(double x, double y, double scale, double rotationDegrees,
                      double opacity, double trailOpacity, boolean finished) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.rotationDegrees = rotationDegrees;
            this.opacity = opacity;
            this.trailOpacity = trailOpacity;
            this.finished = finished;
        }
    }

    /**
     * A completed count between 1 and total maps to a zero-based star slot.
     * Invalid values are clamped; animation data must never crash gameplay.
     */
    public static double slotCenterFraction(int completed, int total) {
        if (total <= 0) return .5;
        int slot = Math.max(1, Math.min(completed, total));
        return (slot - .5) / total;
    }

    /**
     * Deterministic animation sample with no timers or side effects.
     * When movement is disabled, the already updated progress star is the
     * visible feedback, so this decorative overlay is immediately finished.
     */
    public static Frame sample(double fromX, double fromY,
                               double toX, double toY,
                               long elapsedMillis, boolean reduceMotion) {
        final double sx = safeCoordinate(fromX);
        final double sy = safeCoordinate(fromY);
        final double tx = safeCoordinate(toX);
        final double ty = safeCoordinate(toY);

        if (reduceMotion) return new Frame(tx, ty, 1, 0, 0, 0, true);

        final double t = clamp01((double) elapsedMillis / DURATION_MILLIS);
        final double eased = smooth(t);

        // Curved glide that always starts and finishes at the actual view positions.
        final double side = Math.signum(tx - sx);
        final double controlX = clamp01((sx + tx) * .5 + side * .065);
        final double rise = Math.min(.18, .05 + Math.abs(ty - sy) * .22);
        final double controlY = clamp01(Math.min(sy, ty) - rise);

        final double x = clamp01(bezier(sx, controlX, tx, eased));
        final double y = clamp01(bezier(sy, controlY, ty, eased));

        // Gentle growing glow, one turn, then disappear into the progress star.
        final double scale = 1.10 + .15 * Math.sin(Math.PI * t) - .10 * eased;
        final double opacity = 1 - smoothRange(t, .84, 1);
        final double trail = smoothRange(t, .08, .24) *
                (1 - smoothRange(t, .63, .96));
        return new Frame(x, y, scale, 360 * eased,
                clamp01(opacity), clamp01(trail), t >= 1);
    }

    private static double safeCoordinate(double value) {
        if (Double.isNaN(value)) return .5;
        if (value == Double.NEGATIVE_INFINITY) return 0;
        if (value == Double.POSITIVE_INFINITY) return 1;
        return clamp01(value);
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static double smooth(double t) {
        t = clamp01(t);
        return t * t * (3 - 2 * t);
    }

    private static double smoothRange(double t, double start, double end) {
        return smooth((t - start) / (end - start));
    }

    private static double bezier(double start, double control, double end, double t) {
        final double u = 1 - t;
        return u * u * start + 2 * u * t * control + t * t * end;
    }
}
