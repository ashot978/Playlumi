package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public final class PlaylumiQuestionSkipTest {
    @Test public void skippingDrawsFreshCardsWithoutSolvingOrScoring() {
        LearningGame game=new LearningGame(new Random(413));
        game.configure(LearningGame.COLORS,0,0);
        PlaylumiScores scores=new PlaylumiScores();
        PlaylumiScores.Score score=scores.get(LearningGame.COLORS);
        int initialPool=game.questionPoolSize();
        assertEquals(300,initialPool);

        Set<String> seen=new HashSet<>();
        for(int i=0;i<40;i++) {
            LearningGame.Round current=game.round();
            assertTrue("Not a repeated shuffled card",seen.add(
                    QuestionDeck.identity(current)));
            assertFalse(current.isSolved());
            assertTrue(PlaylumiQuestionSkip.skip(game));
            assertNotSame(current,game.round());
            assertFalse("Skip must never solve a question",current.isSolved());
            assertEquals(0,score.correct);
            assertEquals(0,score.attempts);
            assertEquals(0,score.completedSessions);
            assertEquals(0,score.earnedBadges());
        }
    }

    @Test public void worksForEachAgeAndDifferentLearningWorlds() {
        for(int age=0;age<3;age++)for(int mode:new int[]{
                LearningGame.COUNT,LearningGame.SHAPES,LearningGame.WORDS,
                ExtraGames.SPELLING,NewWorldGames.FIRST}) {
            LearningGame game=new LearningGame(new Random(50+age+mode));
            game.configure(mode,0,age);
            LearningGame.Round first=game.round();
            assertTrue(PlaylumiQuestionSkip.skip(game));
            assertNotSame(first,game.round());
            assertFalse(game.round().isSolved());
            assertEquals(300,game.questionPoolSize());
        }
    }

    @Test public void solvedQuestionCannotBeSkippedOrLoseProgress() {
        LearningGame game=new LearningGame(new Random(3));
        game.configure(LearningGame.COUNT,0,0);
        LearningGame.Round solved=game.round();
        assertTrue(solved.choose(solved.correctIndex));
        assertFalse(PlaylumiQuestionSkip.skip(game));
        assertSame(solved,game.round());
        assertTrue(solved.isSolved());
    }

    @Test(expected=IllegalArgumentException.class)
    public void missingGameRejected() { PlaylumiQuestionSkip.skip(null); }
}
