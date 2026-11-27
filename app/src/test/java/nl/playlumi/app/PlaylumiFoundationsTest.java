package nl.playlumi.app;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiFoundationsTest {
    private static final String[] DAYS={"maandag","dinsdag","woensdag","donderdag","vrijdag","zaterdag","zondag"};

    private static void choices(LearningGame.Round q,int amount){
        assertFalse(q.isSolved());assertEquals(amount,q.answers.size());
        assertEquals(amount,new HashSet<>(q.answers).size());assertEquals(amount,q.answerColors.size());
        assertTrue(q.correctIndex>=0&&q.correctIndex<amount);
        assertEquals(-1,q.letterBlank());assertEquals(0,q.fingerCount);
    }
    private static LearningGame.Round example(String prompt,String display,String correct,String... alternatives){
        List<String> options=new ArrayList<>();options.add(correct);Collections.addAll(options,alternatives);
        return new LearningGame.Round(prompt,display,0xff333333,options,
                Collections.nCopies(options.size(),0xff333333),0);
    }
    private static int targetCount(LearningGame.Round q){
        Matcher symbol=Pattern.compile("\\(([●▲■◆★♥])\\)").matcher(q.prompt);
        assertTrue(symbol.find());char target=symbol.group(1).charAt(0);int amount=0;
        assertFalse(symbol.find());
        for(char glyph:q.display.toCharArray())if(glyph==target)amount++;
        return amount;
    }

    @Test public void focusedCountingCountsOnlyTheNamedShapeAcrossDeterministicSeeds(){
        for(int age=0;age<3;age++)for(int seed=0;seed<18;seed++){
            Random random=new Random(982451653L*(seed+1)+age);
            for(int i=0;i<80;i++){
                LearningGame.Round q=PlaylumiFocusedCounting.next(age,random);choices(q,age==0?3:4);
                int expected=targetCount(q),total=0;
                String allowed=age==0?"●▲■":age==1?"●▲■◆★":"●▲■◆★♥";
                for(char glyph:q.display.toCharArray())if(!Character.isSpaceChar(glyph)&&!Character.isWhitespace(glyph)){
                    assertTrue(allowed.indexOf(glyph)>=0);total++;
                }
                assertEquals(expected,Integer.parseInt(q.answers.get(q.correctIndex)));
                assertTrue(total>expected);assertTrue(total<=new int[]{5,9,15}[age]);
                assertTrue(expected>=(age==0?1:0));assertTrue(expected<=new int[]{3,6,10}[age]);
                assertEquals("",PlaylumiFocusedCounting.explain(q));
            }
        }
    }
    @Test public void youngestFieldsStaySpacedAndSmallWhileOlderCountsIncludeZero(){
        for(int age=0;age<3;age++){
            Random random=new Random(23110+age);Set<Integer> counts=new HashSet<>();
            for(int i=0;i<700;i++){
                LearningGame.Round q=PlaylumiFocusedCounting.next(age,random);counts.add(targetCount(q));
                String[] rows=q.display.split("\n");assertTrue(rows.length<=new int[]{2,2,3}[age]);
                for(String row:rows){
                    String[] tokens=row.split("\u2003");assertTrue(tokens.length<=(age==0?3:5));
                    for(String token:tokens)assertEquals(1,token.codePointCount(0,token.length()));
                }
                for(String answer:q.answers){
                    int n=Integer.parseInt(answer);assertTrue(n>=(age==0?1:0));
                    assertTrue(n<=new int[]{3,9,15}[age]);
                }
            }
            assertEquals(new int[]{3,7,11}[age],counts.size());assertEquals(age>0,counts.contains(0));
        }
    }
    @Test public void countingFeedbackUsesActualQuantityAndDutchSingular(){
        LearningGame.Round one=example("Hoeveel cirkels (●) zie je?","▲\u2003●\u2003■","1","2","3");
        assertEquals("",PlaylumiFocusedCounting.explain(one));assertTrue(one.choose(one.correctIndex));
        assertEquals("Er is 1 cirkel. Je telt alleen deze vorm.",PlaylumiFocusedCounting.explain(one));
        LearningGame.Round zero=example("Hoeveel sterren (★) zie je?","●\u2003▲","0","1","2");
        zero.choose(zero.correctIndex);
        assertEquals("Er zijn 0 sterren. Je telt alleen deze vorm.",PlaylumiFocusedCounting.explain(zero));
        LearningGame.Round two=example("Hoeveel driehoeken (▲) zie je?","▲\u2003■\u2003▲","2","1","3");
        two.choose(two.correctIndex);
        assertEquals("Er zijn 2 driehoeken. Je telt alleen deze vorm.",PlaylumiFocusedCounting.explain(two));
    }

    @Test public void weekdayAnswersMatchTheIndependentIsoCalendar(){
        for(int age=0;age<3;age++)for(int seed=0;seed<18;seed++){
            Random random=new Random(961748941L*(seed+1)+age);
            for(int i=0;i<90;i++){
                LearningGame.Round q=PlaylumiWeekdays.next(age,random);choices(q,age==0?3:4);
                String[] lines=q.display.split("\n");assertEquals(2,lines.length);
                int start=Arrays.asList(DAYS).indexOf(lines[0]);assertTrue(start>=0);
                Matcher shift=Pattern.compile("([1-6]) (dag|dagen) (eerder|later)").matcher(lines[1]);
                assertTrue(shift.matches());int n=Integer.parseInt(shift.group(1));
                assertEquals(n==1?"dag":"dagen",shift.group(2));if(age<2)assertEquals(1,n);
                int delta=shift.group(3).equals("later")?n:-n;
                String expected=DAYS[DayOfWeek.of(start+1).plus(delta).getValue()-1];
                assertEquals(expected,q.answers.get(q.correctIndex));
                for(String answer:q.answers)assertTrue(Arrays.asList(DAYS).contains(answer));
                assertEquals("",PlaylumiWeekdays.explain(q));
            }
        }
    }
    @Test public void weekdayPoolCoversEveryDayDistanceAndDirection(){
        for(int age=0;age<3;age++){
            Set<String> seen=new HashSet<>();Random random=new Random(110160+age);
            for(int i=0;i<2400;i++)seen.add(PlaylumiWeekdays.next(age,random).display);
            for(String day:DAYS)for(int distance=1;distance<=(age==2?6:1);distance++)
                for(String direction:new String[]{"eerder","later"})
                    assertTrue(seen.contains(day+"\n"+distance+(distance==1?" dag ":" dagen ")+direction));
            assertEquals(age==2?84:14,seen.size());
        }
    }
    @Test public void weekdayFeedbackHandlesBothWeekBoundariesAndSixDayShifts(){
        String[][] cases={
            {"zondag\n1 dag later","maandag","1 dag na zondag is maandag."},
            {"maandag\n1 dag eerder","zondag","1 dag vóór maandag is zondag."},
            {"vrijdag\n3 dagen later","maandag","3 dagen na vrijdag is maandag."},
            {"woensdag\n6 dagen eerder","donderdag","6 dagen vóór woensdag is donderdag."}
        };
        for(String[] row:cases){
            LearningGame.Round q=example("Welke dag is het dan?",row[0],row[1],"dinsdag","zaterdag");
            assertEquals("",PlaylumiWeekdays.explain(q));q.choose(q.correctIndex);
            assertEquals(row[2],PlaylumiWeekdays.explain(q));
        }
    }

    private static String capitalized(String text){return text.substring(0,1).toUpperCase(Locale.ROOT)+text.substring(1);}
    /** Derive the answer from the visible grammar; no generator answer tables are consulted. */
    private static String[] literalFact(LearningGame.Round q){
        assertTrue(q.prompt.endsWith("?"));String query=q.prompt.substring(0,q.prompt.length()-1);
        String answer=null,evidence=null;
        for(String sentence:q.display.split("\n")){
            assertTrue(sentence.endsWith("."));String statement=sentence.substring(0,sentence.length()-1);
            String candidate=null;
            if(query.startsWith("Wie ")){
                int split=statement.indexOf(' ');
                if(statement.substring(split+1).equals(query.substring(4)))candidate=statement.substring(0,split);
            }else if(query.startsWith("Wat ")){
                String[] request=query.substring(4).split(" ",2);
                String prefix=request[1]+" "+request[0]+" ";
                if(statement.startsWith(prefix))candidate=capitalized(statement.substring(prefix.length()));
            }else if(query.startsWith("Waar ")){
                String[] request=query.substring(5).split(" ",2);
                String prefix=capitalized(request[1])+" "+request[0]+" ";
                if(statement.startsWith(prefix))candidate=capitalized(statement.substring(prefix.length()));
            }else fail("Unknown reading question");
            if(candidate!=null){assertNull("Multiple supported answers",answer);answer=candidate;evidence=sentence;}
        }
        assertNotNull("Answer requires unstated knowledge",answer);return new String[]{answer,evidence};
    }
    @Test public void everyReadingAnswerAndExplanationAreSupportedByItsVisibleSentence(){
        Set<String> seen=new HashSet<>();int[] kinds=new int[3];
        for(int seed=0;seed<24;seed++){
            Random random=new Random(86028121L*(seed+1));
            for(int i=0;i<70;i++){
                LearningGame.Round q=PlaylumiReadingPractice.next(2,random);choices(q,4);
                String[] fact=literalFact(q);assertEquals(fact[0],q.answers.get(q.correctIndex));
                assertEquals(1,Collections.frequency(q.answers,fact[0]));
                assertEquals("",PlaylumiReadingPractice.explain(q));
                if(seen.add(q.prompt+"\n"+q.display))kinds[q.prompt.startsWith("Wie ")?0:q.prompt.startsWith("Wat ")?1:2]++;
                String display=q.display;assertTrue(q.choose(q.correctIndex));assertEquals(display,q.visibleDisplay());
                assertEquals(fact[1],PlaylumiReadingPractice.explain(q));assertFalse(q.choose(q.correctIndex));
            }
        }
        assertEquals(24,seen.size());assertArrayEquals(new int[]{8,8,8},kinds);
    }
    @Test public void readingContextsAndAnswersStayWithinTheNativeTextBudget(){
        Random random=new Random(1106);
        for(int i=0;i<700;i++){
            LearningGame.Round q=PlaylumiReadingPractice.next(2,random);
            assertEquals(2,q.display.split("\n").length);assertTrue(q.display.length()<=84);
            assertTrue(q.prompt.length()<=30);
            for(String answer:q.answers)assertTrue(answer.length()<=18);
        }
    }
    @Test public void everyNewRoutePreservesTheExactRejectedAndSolvedQuestion(){
        for(int[] config:new int[][]{{0,0},{0,1},{0,2},{16,1},{16,2},{6,2}}){
            LearningGame game=new LearningGame(new Random(110+config[0]));game.configure(config[0],5,config[1]);
            LearningGame.Round q=game.round();int wrong=(q.correctIndex+1)%q.answers.size();
            assertFalse(q.choose(wrong));assertTrue(q.isRejected(wrong));
            game.restoreRound(q.prompt,q.display,q.displayColor,q.answers,q.answerColors,q.correctIndex,0,false,q.rejectedMask());
            LearningGame.Round open=game.round();
            assertEquals(q.prompt,open.prompt);assertEquals(q.display,open.display);assertEquals(q.answers,open.answers);
            assertEquals(q.correctIndex,open.correctIndex);assertTrue(open.isRejected(wrong));
            assertEquals("",PlaylumiQuizCoach.explain(config[0],5,open));assertFalse(open.choose(wrong));
            assertTrue(open.choose(open.correctIndex));String explanation=PlaylumiQuizCoach.explain(config[0],5,open);
            assertFalse(explanation.isEmpty());assertFalse(open.choose(open.correctIndex));
            game.restoreRound(open.prompt,open.display,open.displayColor,open.answers,open.answerColors,open.correctIndex,0,true,open.rejectedMask());
            assertTrue(game.round().isSolved());assertTrue(game.round().isRejected(wrong));
            assertFalse(game.round().choose(game.round().correctIndex));
            assertEquals(explanation,PlaylumiQuizCoach.explain(config[0],5,game.round()));
        }
    }
    @Test public void firstEightQuestionsCanBeDistinctAndHistorySurvivesReload(){
        for(int[] config:new int[][]{{0,0},{16,1},{6,2}}){
            LearningGame game=new LearningGame(new Random(6110+config[0]));game.configure(config[0],5,config[1]);
            QuestionHistory history=new QuestionHistory();game.setHistory(history);Set<String> seen=new HashSet<>();
            for(int i=0;i<8;i++){game.nextRound();assertTrue(seen.add(history.fingerprint(game.round())));}
            assertEquals(8,new QuestionHistory(history.serialize()).size());
        }
    }
    @Test public void catalogAgesAndExistingScoreCategoriesRemainExplicit(){
        for(int age=0;age<3;age++){
            assertEquals(new int[]{37,62,89}[age],PlaylumiGameCatalog.entries(age).size());
            assertNotNull(PlaylumiGameCatalog.find(age,"0:0:0:5"));
            assertEquals(age>=1,PlaylumiGameCatalog.find(age,"0:0:16:5")!=null);
            assertEquals(age==2,PlaylumiGameCatalog.find(age,"1:0:6:5")!=null);
            assertNotNull(PlaylumiGameCatalog.find(age,"0:0:11:5"));
            assertNotNull(PlaylumiGameCatalog.find(age,"2:0:3:0"));
        }
        assertEquals(2,PlaylumiLearningProfile.minimum(2,6,5));
        assertEquals(6,LearningGame.maxVariant(0));assertEquals(5,LearningGame.maxVariant(6));assertEquals(5,LearningGame.maxVariant(16));
        assertEquals(24,PlaylumiScores.CATEGORIES);assertEquals(24,LearningGame.GAME_NAMES.length);
    }
    @Test public void nullOrUnrelatedSolvedQuestionsHaveNoFoundationFeedback(){
        assertEquals("",PlaylumiFocusedCounting.explain(null));assertEquals("",PlaylumiWeekdays.explain(null));
        assertEquals("",PlaylumiReadingPractice.explain(null));
        LearningGame.Round unrelated=example("Welke kleur?","●","rood","blauw","groen");
        unrelated.choose(unrelated.correctIndex);
        assertEquals("",PlaylumiFocusedCounting.explain(unrelated));assertEquals("",PlaylumiWeekdays.explain(unrelated));
        assertEquals("",PlaylumiReadingPractice.explain(unrelated));
    }
    @Test(expected=IllegalArgumentException.class) public void focusedCountingRejectsInvalidAge(){PlaylumiFocusedCounting.next(3,new Random());}
    @Test(expected=IllegalArgumentException.class) public void weekdaysRejectMissingRandom(){PlaylumiWeekdays.next(1,null);}
    @Test(expected=IllegalArgumentException.class) public void readingRejectsNegativeAge(){PlaylumiReadingPractice.next(-1,new Random());}
}
