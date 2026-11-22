package nl.playlumi.app;

/**
 * No-pressure question replacement for preschoolers. Does not know about
 * rewards, counters, stars, profiles or storage, so none can change on skip.
 */
public final class PlaylumiQuestionSkip {
    private PlaylumiQuestionSkip() {}

    /** @return true only when an unsolved question was actually replaced. */
    public static boolean skip(LearningGame game) {
        if(game==null)throw new IllegalArgumentException("game");
        LearningGame.Round before=game.round();
        if(before==null || before.isSolved())return false;
        game.nextRound();
        return game.round()!=before;
    }
}
