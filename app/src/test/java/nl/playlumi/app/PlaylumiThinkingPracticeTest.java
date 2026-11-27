package nl.playlumi.app;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiThinkingPracticeTest {
    private static int count(String text){
        int number=0;
        for(int cp:text.codePoints().toArray())if(!Character.isWhitespace(cp)&&!Character.isSpaceChar(cp))number++;
        return number;
    }

    @Test public void equivalenceUsesQuantitiesAcrossDifferentShapes(){
        for(int age=0;age<3;age++){
            LearningGame game=new LearningGame(new Random(108+age));game.configure(11,5,age);
            Set<Integer> coverage=new HashSet<>();
            for(int i=0;i<400;i++){
                LearningGame.Round q=game.round();int wanted=count(q.display),matches=0;
                coverage.add(wanted);assertTrue(wanted<=new int[]{3,6,10}[age]);
                assertEquals(q.answers.size(),new HashSet<>(q.answers).size());
                for(int j=0;j<q.answers.size();j++){
                    assertNotEquals(q.display.codePointAt(0),q.answers.get(j).codePointAt(0));
                    if(count(q.answers.get(j))==wanted){matches++;assertEquals(q.correctIndex,j);}
                }
                assertEquals(1,matches);game.nextRound();
            }
            assertEquals(new int[]{3,6,10}[age],coverage.size());
        }
    }

    @Test public void placeValueUsesDecimalPositionsIncludingZeroUnits(){
        LearningGame game=new LearningGame(new Random(208));game.configure(12,8,2);
        Set<Integer> numbers=new HashSet<>();
        for(int i=0;i<1800;i++){
            LearningGame.Round q=game.round();Matcher digits=Pattern.compile("\\d+").matcher(q.display);
            assertTrue(digits.find());String tens=digits.group();assertTrue(digits.find());String units=digits.group();
            int expected=Integer.parseInt(tens+units);numbers.add(expected);
            assertEquals(expected,Integer.parseInt(q.answers.get(q.correctIndex)));
            assertEquals("",PlaylumiQuizCoach.explain(12,8,q));
            assertTrue(q.choose(q.correctIndex));
            assertEquals(tens+" × 10 + "+units+" = "+expected+".",PlaylumiQuizCoach.explain(12,8,q));
            game.nextRound();
        }
        assertEquals(90,numbers.size());assertTrue(numbers.contains(10));assertTrue(numbers.contains(90));
    }

    @Test public void storySequenceRespectsLastActionAndUnchangedObjects(){
        LearningGame game=new LearningGame(new Random(308));game.configure(19,5,2);
        boolean location=false,removal=false,lamp=false,coat=false;
        Set<String> contexts=new HashSet<>();
        for(int i=0;i<700;i++){
            LearningGame.Round q=game.round();contexts.add(q.display);String answer=q.answers.get(q.correctIndex);
            if(q.display.contains("op de stoel, dan onder de tafel")){
                assertEquals("Bal onder de tafel",answer);location=true;
            }
            if(q.display.contains("alleen de rode bal weg")){
                assertEquals("De blauwe bal blijft",answer);removal=true;
            }
            if(q.display.contains("uit en daarna weer aan")){
                assertEquals("De lamp geeft licht",answer);lamp=true;
            }
            if(q.display.contains("alleen je muts af")){
                assertEquals("Jas aan, muts af",answer);coat=true;
            }
            assertEquals(q.answers.size(),new HashSet<>(q.answers).size());
            assertTrue(q.choose(q.correctIndex));assertFalse(PlaylumiQuizCoach.explain(19,5,q).isEmpty());
            game.nextRound();
        }
        assertTrue(location&&removal&&lamp&&coat);assertEquals(24,contexts.size());
    }

    @Test public void newRoutesRespectAgeAndExistingCategoryIdentity(){
        for(int age=0;age<3;age++){
            assertNotNull(PlaylumiGameCatalog.find(age,"0:0:11:5"));
            assertEquals(age==2,PlaylumiGameCatalog.find(age,"0:0:12:8")!=null);
            assertEquals(age>=1,PlaylumiGameCatalog.find(age,"4:0:19:5")!=null);
        }
        assertEquals(24,LearningGame.GAME_NAMES.length);assertEquals(24,PlaylumiScores.CATEGORIES);
        assertFalse(PlaylumiThinkingPractice.supports(11,4));assertFalse(PlaylumiThinkingPractice.supports(12,7));
    }

    @Test public void newDailyThingsVariantIsValid(){
        LearningGame game=new LearningGame(new Random(1));game.configure(19,6,1);
        assertEquals(6,game.variant());assertEquals(6,LearningGame.maxVariant(19));
        assertTrue(game.round().answers.size()>=3);
    }
    @Test(expected=IllegalArgumentException.class) public void unknownNewVariantIsRejected(){
        new LearningGame(new Random(1)).configure(19,7,1);
    }
}
