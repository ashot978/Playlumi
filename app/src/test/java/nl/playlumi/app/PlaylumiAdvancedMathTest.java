package nl.playlumi.app;
import java.util.Random;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiAdvancedMathTest {
    @Test public void newRoutesAreOnlyExposedAtOldestTier(){
        for(int age=0;age<3;age++){
            int count=0;
            for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age))
                if(e.kind==PlaylumiGameCatalog.QUIZ&&e.mode==ExtraGames.MATH&&e.variant>=PlaylumiAdvancedMath.TABLES){
                    count++;assertEquals(2,age);
                    assertEquals(e.id,PlaylumiGameCatalog.find(age,e.id).id);
                    assertEquals(2,PlaylumiLearningProfile.minimum(age,e.mode,e.variant));
                }
            assertEquals(age==2?3:0,count);
        }
    }
    @Test public void arithmeticIsUniqueAndRestorable(){
        LearningGame game=new LearningGame(new Random(47));
        for(int variant=5;variant<=7;variant++){
            game.configure(ExtraGames.MATH,variant,2);
            boolean up=false,down=false;
            for(int k=0;k<500;k++){
                LearningGame.Round r=game.round();Matcher m=Pattern.compile("\\d+").matcher(r.display);
                assertTrue(m.find());int x=Integer.parseInt(m.group());assertTrue(m.find());int y=Integer.parseInt(m.group());
                int answer=Integer.parseInt(r.answers.get(r.correctIndex));
                if(variant==6)assertEquals(0,x%y);
                int expected=variant==5?x*y:variant==6?x/y:r.display.contains("−")?x-y:x+y;
                assertEquals(expected,answer);assertTrue(answer>=0&&answer<=100);
                assertEquals(4,new HashSet<>(r.answers).size());
                up|=r.display.contains("+");down|=r.display.contains("−");
                int wrong=(r.correctIndex+1)%r.answers.size();
                assertFalse(r.choose(wrong));
                game.restoreRound(r.prompt,r.display,r.displayColor,r.answers,r.answerColors,r.correctIndex,r.fingerCount,false);
                game.round().restoreRejected(r.rejectedMask());
                assertTrue(game.round().isRejected(wrong));
                assertTrue(game.round().choose(game.round().correctIndex));
                assertFalse(PlaylumiQuizCoach.explain(ExtraGames.MATH,variant,game.round()).isEmpty());
                game.nextRound();
            }
            if(variant==7)assertTrue(up&&down);
        }
    }
    @Test public void speechKeepsOperators(){
        assertTrue(PlaylumiSpeechText.quiz(ExtraGames.MATH,5,"Reken uit","4 × 5 = ?").contains("4 keer 5"));
        assertTrue(PlaylumiSpeechText.quiz(ExtraGames.MATH,6,"Reken uit","20 ÷ 5 = ?").contains("20 gedeeld door 5"));
    }
}
