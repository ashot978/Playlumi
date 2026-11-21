package nl.playlumi.motion;

/**
 * Android-independent animation choreography. A renderer consumes Frame values.
 *
 * Coordinates are normalized to the effect's drawing area (0..1). An effect
 * must NEVER control scoring, touch handling, navigation, or stored progress.
 * See docs/INTEGRATION.md before wiring it to the existing game.
 */
public final class MotionEffects {
    private MotionEffects() {}

    public enum Kind {
        STAR_TRAIL, SHAPE_MAGNET, MASCOT_HIGH_FIVE, GARDEN_BLOOM,
        RAINBOW_BRIDGE, TREASURE_CHEST, MEMORY_FLIP, ROCKET_LAUNCH
    }

    /** Immutable render instruction. All fields are finite. */
    public static final class Frame {
        public final double x;
        public final double y;
        public final double scale;
        public final double rotationDegrees;
        public final double opacity;
        public final double accent;
        public final boolean finished;

        private Frame(double x, double y, double scale, double rotationDegrees,
                      double opacity, double accent, boolean finished) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.rotationDegrees = rotationDegrees;
            this.opacity = opacity;
            this.accent = accent;
            this.finished = finished;
        }
    }

    public static int durationMillis(Kind kind) {
        if (kind == null) throw new IllegalArgumentException("kind required");
        switch (kind) {
            case STAR_TRAIL: return 720;
            case SHAPE_MAGNET: return 560;
            case MASCOT_HIGH_FIVE: return 640;
            case GARDEN_BLOOM: return 900;
            case RAINBOW_BRIDGE: return 980;
            case TREASURE_CHEST: return 1100;
            case MEMORY_FLIP: return 480;
            case ROCKET_LAUNCH: return 1150;
            default: throw new IllegalArgumentException("unknown effect");
        }
    }

    /**
     * Sample an effect deterministically at elapsedMillis (no timers or IO).
     * Negative time is clamped to zero. Motion reduction gives a static result.
     */
    public static Frame sample(Kind kind, long elapsedMillis, boolean reduceMotion) {
        if (kind == null) throw new IllegalArgumentException("kind required");
        if (reduceMotion) return reducedFrame(kind);

        final int duration = durationMillis(kind);
        final double t = clamp((double) elapsedMillis / duration);
        final double ease = smooth(t);
        final double pi = Math.PI;
        switch (kind) {
            case STAR_TRAIL: {
                // A curved star flying from the answer card to the star counter.
                double x = bezier(.08, .34, .91, ease);
                double y = bezier(.84, -.11, .14, ease);
                return frame(x, y, 1.15 - .68 * ease, 420 * ease,
                        1 - .85 * smoothRange(t, .78, 1), Math.sin(pi * t), t);
            }
            case SHAPE_MAGNET: {
                // Elastic, non-blocking arrival in the matching shape outline.
                double settle = 1 - Math.pow(1 - t, 3);
                double spring = .075 * Math.sin(4 * pi * t) * (1 - t);
                return frame(.22 + .28 * settle + spring, .52 - .02 * Math.sin(pi * t),
                        1 + .12 * Math.sin(pi * t), 7 * (1 - t) * Math.sin(3 * pi * t),
                        1, ease, t);
            }
            case MASCOT_HIGH_FIVE: {
                double hop = Math.pow(Math.sin(pi * t), 1.5);
                return frame(.5 + .045 * Math.sin(2 * pi * t), .72 - .30 * hop,
                        1 + .14 * hop, 13 * Math.sin(2 * pi * t) * (1 - t),
                        1, hop, t);
            }
            case GARDEN_BLOOM: {
                // A flower sprouts a little at a time; no flashing or strobing.
                return frame(.5 + .014 * Math.sin(3 * pi * t) * (1 - t),
                        .85 - .31 * ease, .22 + .78 * ease,
                        5 * Math.sin(3 * pi * t) * (1 - t), 1, ease, t);
            }
            case RAINBOW_BRIDGE: {
                double x = .1 + .8 * ease;
                double y = .66 - .38 * Math.sin(pi * t);
                double opacity = smoothRange(t, 0, .18) *
                        (1 - smoothRange(t, .8, 1));
                return frame(x, y, .55 + .45 * Math.sin(pi * t),
                        20 * t, opacity, Math.sin(pi * t), t);
            }
            case TREASURE_CHEST: {
                // The lid opens, and a reward rises above the chest.
                double open = smoothRange(t, .08, .43);
                double rise = smoothRange(t, .34, .85);
                return frame(.5 + .10 * rise, .7 - .44 * rise,
                        .7 + .4 * Math.sin(pi * rise),
                        -24 * open * (1 - rise), 1, Math.max(open, rise), t);
            }
            case MEMORY_FLIP: {
                // A card face swap is done in the renderer at t >= 0.5.
                return frame(.5, .5, Math.abs(Math.cos(pi * t)), 180 * t,
                        1, t >= .5 ? 1 : 0, t);
            }
            case ROCKET_LAUNCH: {
                double launch = t * t * t;
                return frame(.5 + .035 * Math.sin(10 * pi * t) * (1 - t),
                        .83 - 1.65 * launch,
                        1 - .16 * t, -6 * Math.sin(2 * pi * t),
                        1 - smoothRange(t, .85, 1), t, t);
            }
            default: throw new IllegalArgumentException("unknown effect");
        }
    }

    private static Frame reducedFrame(Kind kind) {
        // One instantly available static visual confirmation, without movement.
        switch (kind) {
            case STAR_TRAIL: return new Frame(.91, .14, 1, 0, 1, 1, true);
            case SHAPE_MAGNET: return new Frame(.5, .5, 1, 0, 1, 1, true);
            case MASCOT_HIGH_FIVE: return new Frame(.5, .72, 1, 0, 1, 1, true);
            case GARDEN_BLOOM: return new Frame(.5, .54, 1, 0, 1, 1, true);
            case RAINBOW_BRIDGE: return new Frame(.5, .28, 1, 0, 1, 1, true);
            case TREASURE_CHEST: return new Frame(.6, .26, 1, 0, 1, 1, true);
            case MEMORY_FLIP: return new Frame(.5, .5, 1, 0, 1, 1, true);
            case ROCKET_LAUNCH: return new Frame(.5, .23, 1, 0, 1, 1, true);
            default: throw new IllegalArgumentException("unknown effect");
        }
    }

    private static Frame frame(double x, double y, double scale, double rotation,
                               double opacity, double accent, double t) {
        return new Frame(x, y, scale, rotation, clamp(opacity), clamp(accent), t >= 1);
    }

    private static double clamp(double n) {
        if (Double.isNaN(n)) return 0;
        return Math.max(0, Math.min(1, n));
    }

    private static double smooth(double t) {
        t = clamp(t);
        return t * t * (3 - 2 * t);
    }

    private static double smoothRange(double t, double from, double to) {
        return smooth((t - from) / (to - from));
    }

    private static double bezier(double start, double control, double end, double t) {
        return (1 - t) * (1 - t) * start +
                2 * (1 - t) * t * control + t * t * end;
    }
}
