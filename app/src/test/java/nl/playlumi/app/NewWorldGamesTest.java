package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Random;
import static org.junit.Assert.*;

public final class NewWorldGamesTest {
    @Test public void eachNewCategoryHasFivePlayableVariantsAtEveryLevel() {
        Random random = new Random(871);
        for (int mode = NewWorldGames.FIRST; mode < LearningGame.GAME_NAMES.length; mode++)
            for (int variant = 0; variant < 5; variant++)
                for (int level = 0; level < 3; level++)
                    for (int trial = 0; trial < 220; trial++) {
                        LearningGame.Round r = NewWorldGames.next(mode, variant, level, random);
                        assertNotNull(r.prompt);
                        assertFalse(r.prompt.isEmpty());
                        assertFalse(r.display.isEmpty());
                        assertTrue(r.answers.size() >= 2);
                        assertEquals(r.answers.size(), new HashSet<>(r.answers).size());
                        assertEquals(r.answers.size(), r.answerColors.size());
                        assertTrue(r.correctIndex >= 0 && r.correctIndex < r.answers.size());
                        for (int i=0; i<r.answers.size(); i++)
                            if (i != r.correctIndex) assertFalse(r.choose(i));
                        assertTrue(r.choose(r.correctIndex));
                    }
    }
    @Test public void allNewModesWorkInMainGameEngine() {
        LearningGame game=new LearningGame(new Random(199));
        QuestionHistory history=new QuestionHistory();
        game.setHistory(history);
        for (int mode=NewWorldGames.FIRST;mode<LearningGame.GAME_NAMES.length;mode++)
            for(int variant=0;variant<5;variant++)
                for(int level=0;level<3;level++) {
                    game.configure(mode,variant,level);
                    for(int i=0;i<10;i++) {
                        assertNotNull(game.round());
                        game.nextRound();
                    }
                }
        assertTrue(history.size()>100);
    }
    @Test public void categoryMetadataCoversEntireMenu() {
        assertEquals(24,LearningGame.GAME_NAMES.length);
        assertEquals(8,NewWorldGames.NAMES.length);
        assertEquals(8,NewWorldGames.ICONS.length);
        assertEquals(8,NewWorldGames.VARIANTS.length);
        for(String[] names:NewWorldGames.VARIANTS)assertEquals(5,names.length);
    }
}