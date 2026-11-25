package nl.playlumi.app;
import org.junit.Test;
import java.util.*;
import java.util.regex.*;
import static org.junit.Assert.*;

public class PlaylumiRocketCurriculumTest {
    @Test public void ageMenusAreDifferentAndOlderChildrenGetArithmetic(){
        for(int age=0;age<3;age++){
            PlaylumiRocketCurriculum.Mission[] menu=PlaylumiRocketCurriculum.missions(age);
            Set<String> titles=new HashSet<>();
            for(PlaylumiRocketCurriculum.Mission m:menu){
                assertTrue(titles.add(m.title));
                if(age<2)assertNotEquals(ExtraGames.MATH,m.mode);
            }
            assertEquals(age,PlaylumiRocketCurriculum.level(age));
            assertEquals(age==0?3:5,menu.length);
            if(age==2){
                assertTrue(titles.contains("Optellen"));
                assertTrue(titles.contains("Aftrekken"));
                assertTrue(titles.contains("Getal aanvullen"));
                assertTrue(titles.contains("Verhaalsommen"));
                assertFalse(titles.contains("Voorwerpen tellen"));
            }
        }
    }
    @Test public void actualArithmeticHasUniqueCorrectAnswersAndAgeLimits(){
        Random r=new Random(137);
        for(int age=0;age<3;age++)for(int variant=0;variant<5;variant++){
            LearningGame game=new LearningGame(r);
            game.configure(ExtraGames.MATH,variant,age);
            boolean beyondTen=false;
            for(int sample=0;sample<200;sample++){
                LearningGame.Round round=game.round();
                int correct=Integer.parseInt(round.answers.get(round.correctIndex));
                Matcher digits=Pattern.compile("\\d+").matcher(round.display);
                assertTrue(digits.find());int a=Integer.parseInt(digits.group());
                assertTrue(digits.find());int b=Integer.parseInt(digits.group());
                int expected=variant==2?b-a:(variant==1||round.display.contains("weg")?a-b:a+b);
                assertEquals(expected,correct);
                assertTrue(correct>=0&&correct<=PlaylumiMathPractice.limit(age));
                assertTrue(a<=PlaylumiMathPractice.limit(age)&&b<=PlaylumiMathPractice.limit(age));
                assertEquals(round.answers.size(),new HashSet<>(round.answers).size());
                beyondTen|=Math.max(Math.max(a,b),correct)>10;
                for(int k=0;k<round.answers.size();k++)if(k!=round.correctIndex)assertFalse(round.choose(k));
                assertTrue(round.choose(round.correctIndex));
                assertFalse(round.choose(round.correctIndex));
                game.nextRound();
            }
            if(age==2)assertTrue("7+ questions must exceed ten",beyondTen);
        }
    }
}
