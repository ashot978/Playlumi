package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Random;
import static org.junit.Assert.*;

public class ExtraGamesTest {
    @Test public void everyModeVariantAgeHasUniqueCorrectAnswer() {
        Random random = new Random(8173);
        for (int mode = ExtraGames.SPELLING; mode <= ExtraGames.CATEGORIES; mode++) {
            for (int variant = 0; variant < 2; variant++) {
                for (int age = 0; age < 3; age++) {
                    for (int sample = 0; sample < 200; sample++) {
                        LearningGame.Round round = ExtraGames.next(mode, variant, age, random);
                        assertNotNull(round.prompt);
                        assertFalse(round.prompt.isEmpty());
                        assertFalse(round.display.isEmpty());
                        assertTrue(round.answers.size() >= 2);
                        assertEquals(round.answers.size(), new HashSet<>(round.answers).size());
                        assertEquals(round.answers.size(), round.answerColors.size());
                        assertTrue(round.correctIndex >= 0 && round.correctIndex < round.answers.size());
                        for (int i = 0; i < round.answers.size(); i++) {
                            if (i != round.correctIndex) assertFalse(round.choose(i));
                        }
                        assertTrue(round.choose(round.correctIndex));
                        assertFalse(round.choose(round.correctIndex));
                    }
                }
            }
        }
    }

    @Test public void spellsRealAnimalAndPicksCorrectWord() {
        Random random = new Random(220);
        for (int age = 0; age < 3; age++) {
            for (int variant = 0; variant < 2; variant++) {
                for (int i = 0; i < 60; i++) {
                    LearningGame.Round round = ExtraGames.next(ExtraGames.SPELLING, variant, age, random);
                    String correct = round.answers.get(round.correctIndex);
                    assertTrue(correct.length() >= 3);
                    assertTrue(round.display.contains("\n"));
                }
            }
        }
    }

    @Test public void ageLevelsControlMathDifficulty() {
        Random random = new Random(92);
        for (int age = 0; age < 3; age++) {
            int limit = age == 0 ? 5 : age == 1 ? 10 : 20;
            for (int variant = 0; variant < 2; variant++) {
                for (int i = 0; i < 100; i++) {
                    LearningGame.Round round = ExtraGames.next(ExtraGames.MATH, variant, age, random);
                    int answer = Integer.parseInt(round.answers.get(round.correctIndex));
                    assertTrue(answer >= 0 && answer <= limit);
                }
            }
        }
    }
}