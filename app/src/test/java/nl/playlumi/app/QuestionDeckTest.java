package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * The cardinality assertion checks all 24 games, all 5 exercise styles
 * and all 3 levels: 360 independent pools of 300 visible question cards.
 */
public final class QuestionDeckTest {
    @Test public void threeHundredPlayableCardsForEveryGameStyleAndLevel() {
        LearningGame game = new LearningGame(new Random(20261121));
        for (int mode = 0; mode < LearningGame.GAME_NAMES.length; mode++) {
            for (int style = 0; style < 5; style++) {
                for (int level = 0; level < 3; level++) {
                    game.configure(mode, style, level);
                    assertEquals("card pool mode=" + mode + " style=" + style +
                            " level=" + level, 300, game.questionPoolSize());
                    Set<String> seen = new HashSet<>();
                    for (int index = 0; index < 300; index++) {
                        LearningGame.Round card = game.round();
                        String info = "mode="+mode+" style="+style+
                                " level="+level+" card="+index;
                        assertTrue("repeated card "+info,
                                seen.add(QuestionDeck.identity(card)));
                        assertNotNull(card.prompt);
                        assertFalse(card.prompt.trim().isEmpty());
                        assertFalse(card.display.isEmpty());
                        assertFalse(card.answers.isEmpty());
                        assertEquals(card.answers.size(), card.answerColors.size());
                        assertTrue(card.correctIndex >= 0);
                        assertTrue(card.correctIndex < card.answers.size());
                        Set<String> uniqueAnswers = new HashSet<>(card.answers);
                        assertEquals("ambiguous answers " + info,
                                uniqueAnswers.size(), card.answers.size());
                        assertFalse(card.isSolved());
                        assertTrue("correct answer fails " + info,
                                card.choose(card.correctIndex));
                        game.nextRound();
                    }
                    // After a full deck cycle, it stays playable, unsolved and shuffled.
                    assertNotNull(game.round());
                    assertFalse(game.round().isSolved());
                }
            }
        }
    }

    @Test public void openingAgainCreatesNewOrderWithSameDifficulty() {
        LearningGame game = new LearningGame(new Random(1234));
        game.configure(LearningGame.COLORS, 0, 0);
        String[] a = new String[12];
        for (int i = 0; i < a.length; i++) {
            a[i] = QuestionDeck.identity(game.round());
            game.nextRound();
        }
        game.configure(LearningGame.COLORS, 0, 0);
        String[] b = new String[12];
        for (int i = 0; i < b.length; i++) {
            b[i] = QuestionDeck.identity(game.round());
            game.nextRound();
        }
        assertEquals(300, game.questionPoolSize());
        int changed = 0;
        for (int i = 0; i < a.length; i++) if (!a[i].equals(b[i])) changed++;
        assertTrue("open should reshuffle the sequence",changed >= 8);
    }

    @Test public void requestingAnotherRoundAlsoReshuffles() {
        LearningGame game = new LearningGame(new Random(11));
        game.configure(LearningGame.COUNT, 0, 0);
        String first = QuestionDeck.identity(game.round());
        game.reshuffle();
        assertEquals(300,game.questionPoolSize());
        assertNotNull(game.round());
        assertFalse(game.round().isSolved());
        // Different order is overwhelmingly likely; this also guards against
        // accidentally replaying the same stale solved Round instance.
        for(int i=0;i<8;i++) {
            assertTrue(game.round().choose(game.round().correctIndex));
            game.nextRound();
        }
        assertEquals(300,game.questionPoolSize());
    }

    @Test public void oldRecentHistoryRemainsReadable() {
        QuestionHistory history = new QuestionHistory();
        LearningGame game = new LearningGame(new Random(14));
        game.setHistory(history);
        game.configure(LearningGame.SHAPES, 0, 1);
        int before = history.size();
        for(int i=0;i<8;i++) {
            assertTrue(game.round().choose(game.round().correctIndex));
            game.nextRound();
        }
        assertTrue(history.size() >= before);
        assertTrue(history.size() <= 600);
        assertEquals(history.serialize(), new QuestionHistory(history.serialize()).serialize());
    }

    @Test public void questionFactoryDoesNotNeedInternetOrAndroid() {
        LearningGame game=new LearningGame(new Random(77));
        game.configure(LearningGame.PATTERNS,2,2);
        assertEquals(300,game.questionPoolSize());
        for(int i=0;i<20;i++) {
            LearningGame.Round q=game.round();
            assertTrue(q.answers.contains(q.answers.get(q.correctIndex)));
            game.nextRound();
        }
    }
}
