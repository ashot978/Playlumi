package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class PlaylumiMagicLayoutTest {
    @Test public void portraitQuizFitsEightAnswers(){
        int h=800,w=360;
        int total=56+PlaylumiLayout.magicHeroHeight(w,h,false)+37+56+
                PlaylumiLayout.magicQuestionHeight(w,h,false)+23+4*(PlaylumiLayout.magicAnswerHeight(h,8,false)+6)+35;
        assertTrue(total<800);
    }
    @Test public void phoneAndFoldKeepGenerousTargets(){
        for(int w:new int[]{320,360,412,720,900}){
            for(int h:new int[]{640,800,1050}){
                assertTrue(PlaylumiLayout.magicAnswerHeight(h,8,w>=600)>=52);
                assertTrue(PlaylumiLayout.magicQuestionHeight(w,h,w>=600)>=108);
                assertTrue(PlaylumiLayout.magicAnimalHeight(w,h)>=102);
                assertTrue(PlaylumiLayout.magicHeroHeight(w,h,false)>=85);
            }
        }
    }
}
