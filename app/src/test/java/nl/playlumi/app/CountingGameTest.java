package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public class CountingGameTest {
    @Test public void roundsStayInRangeAndChange() {
        CountingGame game = new CountingGame(new Random(42));
        for (int i = 0; i < 1000; i++) {
            int before = game.answer(); assertTrue(before >= 1 && before <= 5);
            game.nextRound(); assertNotEquals(before, game.answer());
        }
    }
    @Test public void wrongAnswerCanBeRetriedAndCorrectAnswerCountsOnce() {
        CountingGame game = new CountingGame(new Random(1));
        int answer = game.answer(); assertFalse(game.choose(answer % 5 + 1));
        assertFalse(game.isSolved()); assertTrue(game.choose(answer));
        assertTrue(game.isSolved()); assertFalse(game.choose(answer));
        game.nextRound(); assertFalse(game.isSolved());
    }
    @Test public void restoresRound() {
        CountingGame game = new CountingGame(new Random(2)); game.restore(3, true);
        assertEquals(3, game.answer()); assertTrue(game.isSolved());
    }
    @Test public void levelsStayWithinTheirRange() {
        CountingGame game = new CountingGame(new Random(9));
        for (int max : new int[]{3, 5, 10}) {
            game.setMaximum(max);
            for (int i = 0; i < 500; i++) {
                assertTrue(game.answer() >= 1 && game.answer() <= max);
                game.nextRound();
            }
        }
    }
}
