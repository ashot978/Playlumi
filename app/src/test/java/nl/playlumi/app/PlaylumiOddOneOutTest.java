package nl.playlumi.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiOddOneOutTest {
    private static final String[] GROUPS={"Dieren","Fruit","Kleding","Voertuigen","Muziekinstrumenten","Vogels"};
    // Semantic ground truth, independent of the model's private membership flags.
    private static final String[][] MEMBERS={
        {"🐱","🐶","🐮","🐰","🐷","🐭","🐔","🐓","🐧","🐦","🐤"},
        {"🍎","🍌","🍇","🍓","🍊","🍐"},
        {"👕","👖","👗","👚"},
        {"🚗","🚌","🚲","🚂","🚢","✈️"},
        {"🎸","🎺","🎷","🎻","🎹"},
        {"🐔","🐓","🐧","🐦","🐤"}
    };
    private static final String[] PROMPTS={"Welk plaatje is geen dier?","Welk plaatje is geen fruit?",
        "Welk plaatje toont geen kleding?","Welk plaatje is geen voertuig?",
        "Welk plaatje is geen muziekinstrument?","Welk plaatje is geen vogel?"};

    private static int group(LearningGame.Round q){
        String[] context=q.display.split("\n");assertEquals(2,context.length);
        int group=Arrays.asList(GROUPS).indexOf(context[1]);assertTrue(group>=0);
        assertEquals(PROMPTS[group],q.prompt);return group;
    }
    private static LearningGame.Round example(int group,String display,String correct,String... members){
        java.util.ArrayList<String> choices=new java.util.ArrayList<>();choices.add(correct);
        Collections.addAll(choices,members);
        return new LearningGame.Round(PROMPTS[group],display,0xff333333,choices,
                Collections.nCopies(choices.size(),0xff333333),0);
    }

    @Test public void everyQuestionHasExactlyOneSemanticallyValidOutsider(){
        Set<String> known=new HashSet<>();for(String[] members:MEMBERS)Collections.addAll(known,members);
        for(int age=0;age<3;age++)for(int seed=0;seed<12;seed++){
            Random random=new Random(49979687L*(seed+1)+age);
            for(int i=0;i<80;i++){
                LearningGame.Round q=PlaylumiOddOneOut.next(age,random);int group=group(q);
                assertFalse(q.isSolved());assertEquals(age==0?3:4,q.answers.size());
                assertEquals(q.answers.size(),new HashSet<>(q.answers).size());
                assertEquals(q.answers.size(),q.answerColors.size());
                assertEquals(-1,q.letterBlank());assertEquals(0,q.fingerCount);
                int outsider=-1;
                for(int at=0;at<q.answers.size();at++){
                    assertTrue(known.contains(q.answers.get(at)));
                    if(!Arrays.asList(MEMBERS[group]).contains(q.answers.get(at))){
                        assertEquals("More than one outsider",-1,outsider);outsider=at;
                    }
                }
                assertTrue(outsider>=0);assertEquals(outsider,q.correctIndex);
                assertEquals("",PlaylumiQuizCoach.explain(15,5,q));
                for(int at=0;at<q.answers.size();at++)if(at!=outsider)assertFalse(q.choose(at));
                assertEquals("",PlaylumiQuizCoach.explain(15,5,q));assertTrue(q.choose(outsider));
                assertFalse(PlaylumiQuizCoach.explain(15,5,q).isEmpty());assertFalse(q.choose(outsider));
            }
        }
    }
    @Test public void ageVocabularyAndChoiceCountsAreBoundedAndVaried(){
        for(int age=0;age<3;age++){
            Random random=new Random(111150+age);Set<Integer> categories=new HashSet<>();
            Set<String> outsiders=new HashSet<>();
            for(int i=0;i<1800;i++){
                LearningGame.Round q=PlaylumiOddOneOut.next(age,random);int group=group(q);
                assertTrue(group<new int[]{4,5,6}[age]);categories.add(group);
                outsiders.add(q.answers.get(q.correctIndex));
                if(age==0)for(String option:q.answers)assertFalse(Arrays.asList(MEMBERS[4]).contains(option));
                assertTrue(q.display.length()<=22);assertTrue(q.prompt.length()<=40);
                q.choose(q.correctIndex);assertTrue(PlaylumiOddOneOut.explain(q).length()<=72);
            }
            assertEquals(new int[]{4,5,6}[age],categories.size());
            assertEquals(age==0?27:32,outsiders.size());
        }
    }
    @Test public void birdsIncludingPenguinsAreNeverAnOutsiderInTheAnimalGroup(){
        Random random=new Random(11132);int birdMembers=0,penguins=0;
        for(int i=0;i<2400;i++){
            LearningGame.Round q=PlaylumiOddOneOut.next(2,random);int group=group(q);
            if(group!=0&&group!=5)continue;
            for(int at=0;at<q.answers.size();at++)if(Arrays.asList(MEMBERS[5]).contains(q.answers.get(at))){
                assertNotEquals(q.correctIndex,at);birdMembers++;
                if(q.answers.get(at).equals("🐧"))penguins++;
            }
        }
        assertTrue(birdMembers>300);assertTrue(penguins>30);
    }
    @Test public void feedbackNamesTheObjectAndUsesCorrectPluralAndNestedCategory(){
        LearningGame.Round grapes=example(3,"🚗\nVoertuigen","🍇","🚗","🚌");
        assertEquals("",PlaylumiOddOneOut.explain(grapes));grapes.choose(grapes.correctIndex);
        assertEquals("Druiven zijn fruit, geen voertuig.",PlaylumiOddOneOut.explain(grapes));
        LearningGame.Round cat=example(5,"🐦\nVogels","🐱","🐔","🐧","🐓");
        cat.choose(cat.correctIndex);
        assertEquals("Een kat is een zoogdier, geen vogel.",PlaylumiOddOneOut.explain(cat));
        LearningGame.Round keyboard=example(2,"👕\nKleding","🎹","👕","👖","👗");
        keyboard.choose(keyboard.correctIndex);
        assertEquals("Een keyboard is een muziekinstrument, geen kleding.",PlaylumiOddOneOut.explain(keyboard));
    }
    @Test public void unsupportedOrInconsistentSavedContentDoesNotProduceFalseFeedback(){
        assertEquals("",PlaylumiOddOneOut.explain(null));
        LearningGame.Round wrongCategory=example(0,"🍎\nFruit","👕","🐶","🐱");
        wrongCategory.choose(wrongCategory.correctIndex);assertEquals("",PlaylumiOddOneOut.explain(wrongCategory));
        LearningGame.Round memberAsOutsider=example(0,"🐾\nDieren","🐧","🐶","🐱");
        memberAsOutsider.choose(memberAsOutsider.correctIndex);assertEquals("",PlaylumiOddOneOut.explain(memberAsOutsider));
        LearningGame.Round unknown=example(0,"🐾\nDieren","unknown","🐶","🐱");
        unknown.choose(unknown.correctIndex);assertEquals("",PlaylumiOddOneOut.explain(unknown));
    }
    @Test public void rejectedAndSolvedQuestionsRestoreWithoutChangingChoicesOrAwardingAgain(){
        for(int age=0;age<3;age++){
            LearningGame game=new LearningGame(new Random(1111500+age));game.configure(15,5,age);
            for(int i=0;i<40;i++){
                LearningGame.Round original=game.round();int wrong=(original.correctIndex+1)%original.answers.size();
                assertFalse(original.choose(wrong));
                game.restoreRound(original.prompt,original.display,original.displayColor,original.answers,
                        original.answerColors,original.correctIndex,0,false,original.rejectedMask());
                LearningGame.Round open=game.round();
                assertEquals(original.prompt,open.prompt);assertEquals(original.display,open.display);
                assertEquals(original.answers,open.answers);assertEquals(original.answerColors,open.answerColors);
                assertEquals(original.correctIndex,open.correctIndex);assertTrue(open.isRejected(wrong));
                assertEquals("",PlaylumiQuizCoach.explain(15,5,open));assertFalse(open.choose(wrong));
                assertTrue(open.choose(open.correctIndex));String explanation=PlaylumiQuizCoach.explain(15,5,open);
                game.restoreRound(open.prompt,open.display,open.displayColor,open.answers,open.answerColors,
                        open.correctIndex,0,true,open.rejectedMask());
                LearningGame.Round solved=game.round();assertTrue(solved.isSolved());assertTrue(solved.isRejected(wrong));
                assertFalse(solved.choose(solved.correctIndex));assertEquals(explanation,PlaylumiQuizCoach.explain(15,5,solved));
                game.nextRound();
            }
        }
    }
    @Test public void firstRoundCanOfferEightDistinctCategoryObjectQuestions(){
        for(int age=0;age<3;age++){
            LearningGame game=new LearningGame(new Random(15111+age));game.configure(15,5,age);
            QuestionHistory history=new QuestionHistory();game.setHistory(history);Set<String> seen=new HashSet<>();
            for(int i=0;i<8;i++){game.nextRound();assertTrue(seen.add(history.fingerprint(game.round())));}
            assertEquals(8,new QuestionHistory(history.serialize()).size());
        }
    }
    @Test public void catalogRouteIsStableInEveryAgeAlongsideTheExistingCategoryGame(){
        for(int age=0;age<3;age++){
            PlaylumiGameCatalog.Entry entry=PlaylumiGameCatalog.find(age,"4:0:15:5");
            assertNotNull(entry);assertEquals("Wat hoort er niet bij?",entry.title);
            assertEquals(4,entry.world);assertEquals(PlaylumiGameCatalog.QUIZ,entry.kind);
            assertEquals(entry.id,PlaylumiGameCatalog.route(age,4,0,15,5).id);
            assertEquals("Wat hoort erbij?",PlaylumiGameCatalog.find(age,"4:0:15:0").title);
            assertTrue(PlaylumiGameCatalog.filter(age,4,Collections.emptySet()).stream().anyMatch(e->e.id.equals(entry.id)));
            assertEquals(1,PlaylumiGameCatalog.filter(age,PlaylumiGameCatalog.FAVORITES,Collections.singleton(entry.id)).size());
            assertEquals(new int[]{37,62,89}[age],PlaylumiGameCatalog.entries(age).size());
        }
        assertEquals(5,LearningGame.maxVariant(15));assertEquals(24,PlaylumiScores.CATEGORIES);
    }
    @Test public void releasedCategoryVariantsStillResolveToTheirOriginalEngines(){
        for(int variant=0;variant<5;variant++)for(int age=0;age<3;age++){
            Random random=new Random(111+variant*3+age);
            LearningGame game=new LearningGame(random);game.configure(15,variant,age);
            assertNotEquals("Welk plaatje is geen dier?",game.round().prompt);
            assertNotEquals("Welk plaatje is geen fruit?",game.round().prompt);
            assertTrue(game.round().choose(game.round().correctIndex));
        }
    }
    @Test(expected=IllegalArgumentException.class) public void invalidAgeIsRejected(){PlaylumiOddOneOut.next(3,new Random());}
    @Test(expected=IllegalArgumentException.class) public void missingRandomIsRejected(){PlaylumiOddOneOut.next(1,null);}
}
