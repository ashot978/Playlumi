package nl.playlumi.app;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public final class PlaylumiSkillVisualTest {
    @Test public void visualModelsNeverRevealTheAnswerBeforeSolving(){
        Random r=new Random(116);
        for(int age=0;age<3;age++)for(int mode:new int[]{0,2,3,4,5}){
            LearningGame game=new LearningGame(r);game.configure(mode,6,age);
            for(int i=0;i<200;i++){
                LearningGame.Round q=game.round();
                boolean supported=PlaylumiSkillVisual.supported(mode,6,q);
                if(mode==0&&age>0){assertFalse(supported);game.nextRound();continue;}
                assertTrue(supported);assertEquals(q.display,PlaylumiSkillVisual.description(mode,q));
                assertEquals(-1,PlaylumiSkillVisual.filledIndex(q));assertEquals(0,PlaylumiSkillVisual.dotDelta(q));
                int wrong=(q.correctIndex+1)%q.answers.size();
                assertFalse(q.choose(wrong));assertEquals(q.display,PlaylumiSkillVisual.description(mode,q));
                assertTrue(q.choose(q.correctIndex));
                if(PlaylumiSkillPractice.revealsBlank(mode,6)){
                    assertFalse(Arrays.asList(PlaylumiSkillVisual.tokens(q)).contains("?"));
                    assertEquals(q.answers.get(q.correctIndex),PlaylumiSkillVisual.tokens(q)[PlaylumiSkillVisual.filledIndex(q)]);
                }else if(mode==0)assertEquals(1,Math.abs(PlaylumiSkillVisual.dotDelta(q)));
                else if(mode==3)assertEquals(PlaylumiSkillVisual.corners(q.display),Integer.parseInt(q.answers.get(q.correctIndex)));
                game.nextRound();
            }
        }
    }
    @Test public void rowsAreBoundedOnPhonesAndTablets(){
        assertEquals(0,PlaylumiSkillVisual.corners("●"));assertEquals(3,PlaylumiSkillVisual.corners("▲"));
        assertEquals(4,PlaylumiSkillVisual.corners("▭"));assertEquals(-1,PlaylumiSkillVisual.corners("x"));
        assertEquals(3,PlaylumiSkillVisual.dots("●  ●  ●"));assertEquals(0,PlaylumiSkillVisual.dots("● x"));
        for(int width:new int[]{240,320,360,600,900})for(int count=1;count<=9;count++){
            int cols=PlaylumiSkillVisual.columns(width,count),rows=PlaylumiSkillVisual.rows(width,count);
            assertTrue(cols>=1&&cols<=5&&cols<=count);assertTrue(rows*cols>=count);
            assertTrue((rows-1)*cols<count);
        }
    }
}
