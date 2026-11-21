package nl.playlumi.motion;

/**
 * A presentation-only, deterministic animation for a correctly matched shape.
 *
 * The existing game MUST decide whether a shape fits before calling this class.
 * This class never edits scores, drag state, profiles or saved progress. The four
 * coordinates are normalized to the SAME drawing area, from 0 to 1. Incorrect
 * or out-of-bounds coordinates are made safe rather than crashing the game.
 */
public final class ShapeMagnet {
    public static final int DURATION_MILLIS = 560;
    public static final int SPARKLE_COUNT = 3;

    private ShapeMagnet() {}

    /** Immutable presentation data; no Android dependencies or callbacks. */
    public static final class Frame {
        public final double x;
        public final double y;
        public final double scale;
        public final double rotationDegrees;
        public final double sparkleOpacity;
        public final double sparkleRadius;
        public final double targetGlow;
        public final boolean finished;

        private Frame(double x, double y, double scale, double rotationDegrees,
                      double sparkleOpacity, double sparkleRadius,
                      double targetGlow, boolean finished) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.rotationDegrees = rotationDegrees;
            this.sparkleOpacity = sparkleOpacity;
            this.sparkleRadius = sparkleRadius;
            this.targetGlow = targetGlow;
            this.finished = finished;
        }
    }

    /**
     * Call once for each rendered frame, after the game's successful-match event.
     * Elapsed milliseconds are relative to that event, not wall-clock time.
     * Invalid coordinates are sanitized to avoid taking down the host app.
     */
    public static Frame sample(double fromX, double fromY,
                               double targetX, double targetY,
                               long elapsedMillis, boolean reduceMotion) {
        double sx = safeCoordinate(fromX);
        double sy = safeCoordinate(fromY);
        double tx = safeCoordinate(targetX);
        double ty = safeCoordinate(targetY);

        if (reduceMotion) {
            // Static positive confirmation: do not animate, flash or spin.
            return new Frame(tx, ty, 1, 0, 0, 0, 1, true);
        }

        double t = clamp01((double) elapsedMillis / DURATION_MILLIS);
        double travel = 1 - Math.pow(1 - t, 3);
        double dx = tx - sx;
        double dy = ty - sy;
        double length = Math.hypot(dx, dy);

        // A short, curved glide. The arc fades completely at the destination.
        double arc = .025 * Math.sin(Math.PI * t) * Math.min(1, length * 2);
        double unitSideX = length > 0 ? -dy / length : 0;
        double unitSideY = length > 0 ? dx / length : 0;
        double x = clamp01(sx + dx * travel + unitSideX * arc);
        double y = clamp01(sy + dy * travel + unitSideY * arc);

        // A single soft landing bounce, without moving the interactive target.
        double bounce = Math.sin(Math.PI * t);
        double scale = 1 + .13 * bounce * bounce;
        double rotation = 5 * Math.sin(2 * Math.PI * t) * (1 - t);

        // Three small, once-only sparkles around the matched shape.
        // The renderer places them at 0, 120 and 240 degrees from the center.
        double sparkles = smoothRange(t, .47, .69) *
                (1 - smoothRange(t, .80, .99));
        double radius = .012 + .035 * smoothRange(t, .47, .94);
        double glow = smoothRange(t, .52, .70) *
                (1 - smoothRange(t, .89, 1));

        return new Frame(x, y, scale, rotation, clamp01(sparkles), radius,
                clamp01(glow), t >= 1);
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

    private static double smoothRange(double t, double start, double end) {
        double v = clamp01((t - start) / (end - start));
        return v * v * (3 - 2 * v);
    }
}
