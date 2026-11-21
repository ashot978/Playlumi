package nl.playlumi.app;

/**
 * Gives one existing plant a small growth step after a completed learning
 * session. Uses the established water/sun rules and reward mask; it never
 * changes scores, awards stars or writes user preferences on its own.
 */
public final class PlaylumiGardenJourney {
    private PlaylumiGardenJourney() {}

    public static final class Growth {
        public final int plant, fromStage, toStage, action;
        public final boolean firstBloom;
        private Growth(int plant, int from, int to, int action, boolean bloom) {
            this.plant = plant; fromStage = from; toStage = to;
            this.action = action; firstBloom = bloom;
        }
    }

    /**
     * Grow an existing sprout first, then start the next seed, so children
     * see a plant bloom after four completed rounds. Manual care still
     * works between sessions. Returns null if all 12 plants are finished.
     */
    public static Growth afterCompletedRound(PlaylumiGarden garden) {
        if (garden == null) throw new IllegalArgumentException("garden");
        int candidate = -1;
        int mostGrown = -1;
        for (int plant = 0; plant < PlaylumiGarden.COUNT; plant++) {
            int stage = garden.stage(plant);
            if (stage < PlaylumiGarden.STAGES && stage > mostGrown) {
                mostGrown = stage;
                candidate = plant;
            }
        }
        if (candidate < 0) return null;
        int from = garden.stage(candidate);
        int action = garden.nextAction(candidate);
        int result = garden.grow(candidate, action);
        if (result != 0 && result != 1)
            throw new IllegalStateException("Valid garden action rejected");
        return new Growth(candidate, from, garden.stage(candidate),
                action, result == 1);
    }
}
