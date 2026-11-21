package nl.playlumi.app;

/**
 * Deterministic, presentation-only choreography for the third good answer
 * in each five-answer streak. The scoring system remains authoritative.
 */
public final class HighFiveMotion {
    public static final int DURATION_MILLIS = 680;

    private HighFiveMotion() {}

    /**
     * A high-five at streaks 3, 8, 13 ... when no session-completion
     * celebration will take precedence. Never alter the streak here.
     */
    public static boolean shouldPlay(int currentStreak, boolean sessionComplete) {
        return !sessionComplete && currentStreak >= 3 && (currentStreak - 3) % 5 == 0;
    }

    /** Immutable animation snapshot; no Android APIs or timers. */
    public static final class Frame {
        public final double hop;
        public final double handsMeet;
        public final double sparkle;
        public final double opacity;
        public final double tiltDegrees;
        public final boolean finished;

        private Frame(double hop, double handsMeet, double sparkle, double opacity,
                      double tiltDegrees, boolean finished) {
            this.hop = hop;
            this.handsMeet = handsMeet;
            this.sparkle = sparkle;
            this.opacity = opacity;
            this.tiltDegrees = tiltDegrees;
            this.finished = finished;
        }
    }

    /**
     * A non-looping animation. If motion is reduced, the existing progress
     * star already confirms success; no overlay frames are needed.
     */
    public static Frame sample(long elapsedMillis, boolean reduceMotion) {
        if (reduceMotion) return new Frame(0, 1, 0, 0, 0, true);
        double t = clamp01((double) elapsedMillis / DURATION_MILLIS);
        double hop = Math.pow(Math.sin(Math.PI * t), 2);
        double meet = smoothRange(t, .10, .47) *
                (1 - .68 * smoothRange(t, .64, .96));
        double sparkle = smoothRange(t, .46, .56) *
                (1 - smoothRange(t, .73, .94));
        double alpha = 1 - smoothRange(t, .86, 1);
        double tilt = 9 * Math.sin(Math.PI * t) * (1 - t);
        return new Frame(hop, clamp01(meet), clamp01(sparkle),
                clamp01(alpha), tilt, t >= 1);
    }

    private static double clamp01(double n) {
        if (Double.isNaN(n)) return 0;
        return Math.max(0, Math.min(1, n));
    }

    private static double smoothRange(double t, double from, double to) {
        double v = clamp01((t - from) / (to - from));
        return v * v * (3 - 2 * v);
    }
}
