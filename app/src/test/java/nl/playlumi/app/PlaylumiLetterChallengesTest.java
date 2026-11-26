package nl.playlumi.app;
import org.junit.Test;
import java.util.HashSet;
import java.util.Random;
import static org.junit.Assert.*;

public final class PlaylumiLetterChallengesTest {
    @Test public void noCompleteWordOrAmbiguousAnswerBeforeSolving(){
        int matched=0;
        for(int level=0;level<=2;level++)for(int direction=0;direction<=1;direction++){
            boolean last=direction==1;Random r=new Random(31+level*2+direction);
            for(int i=0;i<600;i++){
                LearningGame.Round q=PlaylumiLetterChallenges.next(last,level,r);
                assertTrue(q.letterBlank()>=0);
                assertTrue(q.visibleDisplay().contains("_"));
                assertEquals(q.answers.size(),new HashSet<>(q.answers).size());
                String letter=q.blankReplacement();
                assertEquals(1,letter.length());
                String word=PlaylumiLetterChallenges.word(q.display,letter);
                assertFalse(word.contains("_"));
                boolean match=q.prompt.equals(PlaylumiLetterChallenges.MATCH_FIRST)||q.prompt.equals(PlaylumiLetterChallenges.MATCH_LAST);
                if(match){
                    matched++;
                    int count=0;
                    for(String option:q.answers){
                        String edge=last?option.substring(option.length()-1):option.substring(0,1);
                        if(edge.equals(letter))count++;
                        assertNotEquals(word,option);
                    }
                    assertEquals(1,count);
                }
                int wrong=(q.correctIndex+1)%q.answers.size();
                assertFalse(q.choose(wrong));assertTrue(q.visibleDisplay().contains("_"));
                assertTrue(q.choose(q.correctIndex));assertFalse(q.visibleDisplay().contains("_"));
                assertTrue(q.visibleDisplay().endsWith(word));
            }
        }
        assertTrue(matched>400);
    }
    @Test public void legacyQuestionsAreReplacedWithoutChangingOtherGames(){
        assertTrue(PlaylumiLetterChallenges.legacy(ExtraGames.FIRST_LAST,1,"🐊\nkrokodil"));
        assertFalse(PlaylumiLetterChallenges.legacy(ExtraGames.FIRST_LAST,1,"🐊\nkrokodi_"));
        assertFalse(PlaylumiLetterChallenges.legacy(LearningGame.WORDS,0,"🐊\nkrokodil"));
    }
}
