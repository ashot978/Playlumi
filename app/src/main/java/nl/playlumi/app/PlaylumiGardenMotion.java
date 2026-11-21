package nl.playlumi.app;

/** Smooth, deterministic, optional animation for one garden growth step. */
public final class PlaylumiGardenMotion {
    public static final int DURATION_MILLIS = 1080;
    private PlaylumiGardenMotion() {}

    public static final class Frame {
        public final double progress;
        public final double oldAlpha;
        public final double newAlpha;
        public final double newScale;
        public final double sparkle;
        public final double waterOrSunshine;
        public final boolean finished;

        private Frame(double progress, double oldAlpha, double newAlpha,
                      double newScale, double sparkle,
                      double waterOrSunshine, boolean finished) {
            this.progress = progress;
            this.oldAlpha = oldAlpha;
            this.newAlpha = newAlpha;
            this.newScale = newScale;
            this.sparkle = sparkle;
            this.waterOrSunshine = waterOrSunshine;
            this.finished = finished;
        }
    }

    public static Frame sample(long elapsedMillis, boolean reducedMotion) {
        if (reducedMotion)
            return new Frame(1, 0, 1, 1, 0, 0, true);

        final double t = clamp((double) elapsedMillis / DURATION_MILLIS);
        final double eased = 1 - Math.pow(1 - t, 3);
        final double oldAlpha = 1 - smooth(t, .05, .55);
        final double newAlpha = smooth(t, .07, .48);
        final double newScale = .52 + .48 * eased + .055 * Math.sin(Math.PI * t);
        final double sparkle = smooth(t, .48, .69) *
                (1 - smooth(t, .79, 1));
        final double waterOrSun = smooth(t, .03, .22) *
                (1 - smooth(t, .40, .68));
        return new Frame(eased, oldAlpha, newAlpha, newScale,
                clamp(sparkle), clamp(waterOrSun), t >= 1);
    }

    private static double clamp(double t) {
        return Math.max(0, Math.min(1, t));
    }

    private static double smooth(double t, double a, double b) {
        final double s = clamp((t - a) / (b - a));
        return s * s * (3 - 2 * s);
    }
}
