package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Random;
import static org.junit.Assert.*;

public class VariantGamesTest {
    @Test public void allNewVariantsCreatePlayableQuestions() {
        Random random = new Random(441);
        for (int mode = 0; mode < LearningGame.GAME_NAMES.length; mode++)
            for (int variant = 2; variant < 5; variant++)
                for (int difficulty = 0; difficulty < 3; difficulty++)
                    for (int sample = 0; sample < 100; sample++) {
                        LearningGame.Round q = VariantGames.next(mode, variant, difficulty, random);
                        assertNotNull(q.prompt);
                        assertFalse(q.display.isEmpty());
                        assertTrue(q.answers.size() >= 2);
                        assertEquals(q.answers.size(), new HashSet<>(q.answers).size());
                        assertEquals(q.answers.size(), q.answerColors.size());
                        assertTrue(q.correctIndex >= 0 && q.correctIndex < q.answers.size());
                        assertFalse(q.isSolved());
                        for (int i = 0; i < q.answers.size(); i++)
                            if (i != q.correctIndex) assertFalse(q.choose(i));
                        assertTrue(q.choose(q.correctIndex));
                        assertFalse(q.choose(q.correctIndex));
                    }
    }
    @Test public void allFiveVariantsWorkWithTheMainEngine() {
        LearningGame game = new LearningGame(new Random(92));
        for(int mode=0; mode<LearningGame.GAME_NAMES.length; mode++)
            for(int variant=0; variant<5; variant++)
                for(int level=0; level<3; level++) {
                    game.configure(mode,variant,level);
                    assertNotNull(game.round());
                    assertEquals(variant, game.variant());
                    game.nextRound();
                    assertTrue(game.round().answers.size()>=2);
                }
    }
    @Test public void historyRemembersProblemsAfterRestart() {
        LearningGame generator = new LearningGame(new Random(221));
        QuestionHistory history = new QuestionHistory();
        generator.setHistory(history);
        generator.configure(LearningGame.WORDS, 4, 2);
        LearningGame.Round first = generator.round();
        String fingerprint = history.fingerprint(first);
        assertTrue(history.size() > 0);
        QuestionHistory restored = new QuestionHistory(history.serialize());
        assertEquals(fingerprint, restored.fingerprint(first));
        assertTrue(restored.recency(first) > 0);
        generator.nextRound();
        assertNotEquals(fingerprint, history.fingerprint(generator.round()));
    }
    @Test public void recentHistoryIsSharedAcrossCategoriesAndBounded() {
        QuestionHistory history = new QuestionHistory();
        LearningGame game = new LearningGame(new Random(111));
        game.setHistory(history);
        for(int mode=0;mode<LearningGame.GAME_NAMES.length;mode++)
            for(int level=0;level<3;level++)
                for(int variant=0;variant<5;variant++) {
                    game.configure(mode,variant,level);
                    for(int i=0;i<3;i++) game.nextRound();
                }
        assertTrue(history.size() > 0);
        assertTrue(history.size() <= 600);
        assertEquals(history.size(),new QuestionHistory(history.serialize()).size());
    }
}
