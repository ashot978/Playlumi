package nl.playlumi.app;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
public final class PlaylumiPlayExperienceTest {
    @Test public void searchIsLocalAgeFilteredAndAccentInsensitive(){
        assertTrue(PlaylumiLibrarySearch.find(0,"klok").isEmpty());
        assertEquals("Klokkijken",PlaylumiLibrarySearch.find(2,"klokkijken").get(0).title);
        assertEquals("Dierenmemory",PlaylumiLibrarySearch.find(0,"géheugen").get(0).title);
        assertTrue(PlaylumiLibrarySearch.find(2,"xxxx").isEmpty());
    }
    @Test public void answerLayoutsKeepDirectionAndLongWordsReadable(){
        assertEquals(3,PlaylumiQuizCoach.columns(Arrays.asList("1","2","3","4","5","6"),360,false,false,false));
        assertEquals(2,PlaylumiQuizCoach.columns(Arrays.asList("1","2","3","4","5","6"),320,false,false,false));
        assertEquals(1,PlaylumiQuizCoach.columns(Arrays.asList("Boven","Onder"),700,false,true,false));
        assertEquals(2,PlaylumiQuizCoach.columns(Arrays.asList("Links","Rechts","Gelijk"),700,false,true,false));
        assertEquals(2,PlaylumiQuizCoach.columns(Arrays.asList("krokodil","olifant","schildpad","pinguïn","kangoeroe","dolfijn"),360,false,false,false));
    }
    @Test public void wrongChoiceDoesNotLeakAnswerAndIsRestorable(){
        LearningGame g=new LearningGame(new Random(7));g.configure(ExtraGames.MATH,1,2);
        LearningGame.Round r=g.round();
        int wrong=(r.correctIndex+1)%r.answers.size();
        assertFalse(r.choose(wrong));assertTrue(r.isRejected(wrong));assertFalse(r.isSolved());
        assertEquals("",PlaylumiQuizCoach.explain(g.mode(),g.variant(),r));
        g.restoreRound(r.prompt,r.display,r.displayColor,r.answers,r.answerColors,r.correctIndex,r.fingerCount,false);
        g.round().restoreRejected(r.rejectedMask()|(1<<r.correctIndex));
        assertTrue(g.round().isRejected(wrong));assertFalse(g.round().isRejected(r.correctIndex));
        assertTrue(g.round().choose(r.correctIndex));
        assertTrue(PlaylumiQuizCoach.explain(g.mode(),g.variant(),g.round()).contains(" = "));
    }
}
