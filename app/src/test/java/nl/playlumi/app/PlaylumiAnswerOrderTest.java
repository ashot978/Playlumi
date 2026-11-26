package nl.playlumi.app;
import org.junit.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.Assert.*;
public final class PlaylumiAnswerOrderTest {
    @Test public void allComparisonAndDirectionRoundsKeepSidesStable(){
        Random r=new Random(91);
        for(int level=0;level<3;level++)for(int n=0;n<200;n++){
            check(ExtraGames.next(ExtraGames.COMPARE,0,level,r));
            check(ExtraGames.next(ExtraGames.COMPARE,1,level,r));
            for(int v=0;v<5;v++){
                check(NewWorldGames.next(16,v,level,r));
                check(NewWorldGames.next(18,v,level,r));
                check(NewWorldGames.next(22,v,level,r));
            }
        }
    }
    private static void check(LearningGame.Round round){
        if(PlaylumiAnswerOrder.horizontalPair(round.answers)){
            assertEquals("Links",round.answers.get(0));assertEquals("Rechts",round.answers.get(1));
        }
        if(PlaylumiAnswerOrder.verticalPair(round.answers)){
            assertEquals("Boven",round.answers.get(0));assertEquals("Onder",round.answers.get(1));
        }
        assertTrue(round.choose(round.correctIndex));assertFalse(round.choose(round.correctIndex));
    }
    @Test public void restoredAnswersPreserveCorrectAnswerAndColors(){
        LearningGame game=new LearningGame(new Random(91));
        game.restoreRound("Welke is langer?","1 cm 16 cm",0,Arrays.asList("Rechts","Links"),Arrays.asList(11,22),0,0,true);
        assertEquals(Arrays.asList("Links","Rechts"),game.round().answers);
        assertEquals(Arrays.asList(22,11),game.round().answerColors);
        assertEquals(1,game.round().correctIndex);assertTrue(game.round().isSolved());
    }
    @Test public void nonSpatialOptionsKeepOriginalOrder(){
        assertArrayEquals(new int[]{0,1,2},PlaylumiAnswerOrder.indices(Arrays.asList("B","A","C")));
    }
}
