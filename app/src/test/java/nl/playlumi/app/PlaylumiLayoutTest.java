package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiLayoutTest {
    @Test public void miniSessionsAreAchievableWithoutErasingLifetimeProgress() {
        assertEquals(8,PlaylumiLayout.SHORT_ROUND_QUESTIONS);
    }
    @Test public void smallerPhonesKeepComfortableTouchTargets() {
        assertFalse(PlaylumiLayout.useWideLayout(360,false));
        assertEquals(12,PlaylumiLayout.sidePadding(360));
        assertTrue(PlaylumiLayout.tileHeight(48,false)>=60);
        assertTrue(PlaylumiLayout.tileHeight(80,true)>=56);
    }

    @Test public void unfoldedFoldAndLandscapeAreTreatedAsWide() {
        assertTrue(PlaylumiLayout.useWideLayout(700,false));
        assertTrue(PlaylumiLayout.useWideLayout(400,true));
        assertFalse(PlaylumiLayout.useWideLayout(599,false));
        assertEquals(45,PlaylumiLayout.sidePadding(900));
        assertEquals(24,PlaylumiLayout.sidePadding(650));
    }

    @Test public void giantHeadersCannotTakeOverTheGame() {
        assertTrue(PlaylumiLayout.heroHeight(240,false,false,780)<=108);
        assertTrue(PlaylumiLayout.heroHeight(240,true,false,780)<=84);
        assertTrue(PlaylumiLayout.heroHeight(240,false,true,780)<=174);
        assertTrue(PlaylumiLayout.heroHeight(240,false,false,600)<=76);
    }

    @Test public void shapeBoardUsesBalancedRowsAndResponsiveGate(){
        for(int[] screen:new int[][]{{320,570},{360,760},{412,915},{600,800},{900,1000},{800,380}}){
            int w=screen[0],h=screen[1];
            for(int count:new int[]{3,4,6}){
                int cols=PlaylumiLayout.shapeColumns(w,h,count);
                int row=PlaylumiLayout.shapeTileHeight(w,h,count);
                int gate=PlaylumiLayout.shapeTargetHeight(w,h,count);
                assertTrue(cols>=2&&cols<=3);
                assertTrue(row>=54&&row<=118);
                assertTrue(gate>=100&&gate<=248);
                if(PlaylumiLayout.shapeWide(w,h)&&count>=4)
                    assertTrue(Math.abs(gate-(count+cols-1)/cols*(row+8))<=24);
            }
        }
        assertFalse(PlaylumiLayout.shapeWide(360,800));
        assertTrue(PlaylumiLayout.shapeWide(900,1000));
        assertTrue(PlaylumiLayout.shapeWide(800,380));
        assertTrue(PlaylumiLayout.shapeTileHeight(900,1000,6)>PlaylumiLayout.shapeTileHeight(360,760,6));
    }
    @Test public void roomsDrawingsAndButtonsRemainUsable() {
        assertEquals(215,PlaylumiLayout.drawingHeight(true,900));
        assertEquals(225,PlaylumiLayout.drawingHeight(false,650));
        assertTrue(PlaylumiLayout.sceneHeight(310,true,800)<=218);
        assertTrue(PlaylumiLayout.sceneHeight(310,false,650)<=205);
        for (int height=30;height<400;height+=7) {
            assertTrue(PlaylumiLayout.tileHeight(height,false)>=60);
            assertTrue(PlaylumiLayout.tileHeight(height,false)<=102);
            assertTrue(PlaylumiLayout.tileHeight(height,true)<=80);
        }
    }
    @Test public void libraryFillsFoldAndTabletScreens(){
        assertEquals(1,PlaylumiLayout.libraryColumns(360));
        assertEquals(2,PlaylumiLayout.libraryColumns(600));
        assertEquals(2,PlaylumiLayout.libraryColumns(800));
        assertEquals(3,PlaylumiLayout.libraryColumns(900));
        assertTrue(1000-2*PlaylumiLayout.librarySidePadding(1000)>940);
        assertTrue(1400-2*PlaylumiLayout.librarySidePadding(1400)<=1320);
    }
    @Test public void widerQuizIsCenteredAndContentWidthIsCapped(){
        assertTrue(PlaylumiLayout.centeredMagicQuiz(700,780));
        assertTrue(PlaylumiLayout.centeredMagicQuiz(900,1000));
        assertFalse(PlaylumiLayout.centeredMagicQuiz(800,400));
        assertFalse(PlaylumiLayout.centeredMagicQuiz(360,800));
        assertTrue(1400-2*PlaylumiLayout.magicSidePadding(1400)<=760);
        assertEquals(3,PlaylumiLayout.centeredAnswerColumns(6));
        assertEquals(2,PlaylumiLayout.centeredAnswerColumns(4));
        assertTrue(PlaylumiLayout.centeredAnswerHeight(700,6)>=50);
    }
}
