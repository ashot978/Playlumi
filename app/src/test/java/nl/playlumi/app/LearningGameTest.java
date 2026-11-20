package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public class LearningGameTest {
    @Test public void everyGameVariantAndLevelProducesPlayableRound() {
        LearningGame game = new LearningGame(new Random(42));
        for (int mode = 0; mode < LearningGame.GAME_NAMES.length; mode++)
            for (int variant = 0; variant < 2; variant++)
                for (int level = 0; level < 3; level++) {
                    game.configure(mode, variant, level);
                    for (int trial = 0; trial < 20; trial++) {
                        LearningGame.Round round = game.round();
                        assertNotNull(round.prompt);
                        assertFalse(round.answers.isEmpty());
                        assertEquals(round.answers.size(), round.answerColors.size());
                        assertTrue(round.correctIndex >= 0);
                        assertTrue(round.correctIndex < round.answers.size());
                        assertFalse(round.isSolved());
                        for (int i = 0; i < round.answers.size(); i++)
                            if (i != round.correctIndex) assertFalse(round.choose(i));
                        assertTrue(round.choose(round.correctIndex));
                        assertTrue(round.isSolved());
                        assertFalse(round.choose(round.correctIndex));
                        game.nextRound();
                    }
                }
    }
    @Test(expected = IllegalArgumentException.class)
    public void invalidModeRejected() { new LearningGame(new Random()).configure(99, 0, 0); }
    @Test(expected = IllegalArgumentException.class)
    public void invalidLevelRejected() { new LearningGame(new Random()).configure(0, 0, 4); }
}