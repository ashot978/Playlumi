package nl.playlumi.app;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class MatchingGameTest {
    @Test public void roundsStayInRangeAndDoNotRepeat() {
        for (int size : new int[]{4, 5}) {
            MatchingGame game = new MatchingGame(new Random(42), size);
            for (int i = 0; i < 1000; i++) {
                int answer = game.answer();
                assertTrue(answer >= 0 && answer < size);
                assertFalse(game.choose((answer + 1) % size));
                assertTrue(game.choose(answer));
                assertFalse(game.choose(answer));
                game.nextRound();
                assertNotEquals(answer, game.answer());
                assertFalse(game.isSolved());
            }
        }
    }
    @Test public void restoresSolvedRound() {
        MatchingGame game = new MatchingGame(new Random(1), 5);
        game.restore(3, true);
        assertEquals(3, game.answer());
        assertTrue(game.isSolved());
        assertFalse(game.choose(3));
    }
}
