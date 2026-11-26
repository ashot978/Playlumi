package nl.playlumi.app;
import java.util.HashSet;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
public final class PlaylumiLearningDepthTest {
    @Test public void spellingUsesContextAndOnlyExplainsAfterCorrectAnswer(){
        Random r=new Random(49);HashSet<String> contexts=new HashSet<>();
        for(int i=0;i<1200;i++){
            LearningGame.Round q=ExtraGames.next(ExtraGames.SPELLING,0,2,r);
            assertTrue(PlaylumiSpellingPractice.isContext(q.display));contexts.add(q.display);
            assertEquals(4,new HashSet<>(q.answers).size());
            assertEquals("",PlaylumiQuizCoach.explain(ExtraGames.SPELLING,0,q));
            assertFalse(PlaylumiSpellingPractice.hint(q.display).isEmpty());
            assertTrue(q.choose(q.correctIndex));
            assertFalse(PlaylumiQuizCoach.explain(ExtraGames.SPELLING,0,q).isEmpty());
        }
        assertEquals(PlaylumiSpellingPractice.count(),contexts.size());
    }
    @Test public void oldestLettersCoverEntireAlphabetWithoutTwentySixChoices(){
        LearningGame g=new LearningGame(new Random(53));
        for(int v=0;v<2;v++){
            g.configure(LearningGame.LETTERS,v,2);HashSet<String> letters=new HashSet<>();
            for(int i=0;i<800;i++){
                LearningGame.Round q=g.round();letters.add(q.display);
                assertEquals(6,q.answers.size());
                assertTrue(q.answers.get(q.correctIndex).equalsIgnoreCase(q.display));
                assertNotEquals(q.display,q.answers.get(q.correctIndex));
                g.nextRound();
            }
            assertEquals(26,letters.size());
        }
    }
    @Test public void quizPaceIsBoundedAndManualDisablesTimer(){
        assertEquals(PlaylumiQuizPace.AUTO,PlaylumiQuizPace.safe(99));
        assertEquals(-1,PlaylumiQuizPace.delay(PlaylumiQuizPace.MANUAL,true,"uitleg"));
        assertTrue(PlaylumiQuizPace.delay(PlaylumiQuizPace.CALM,true,"uitleg")>PlaylumiQuizPace.delay(PlaylumiQuizPace.AUTO,true,"uitleg"));
        assertEquals(2,PlaylumiLearningProfile.minimum(2,ExtraGames.SPELLING,0));
    }
}
