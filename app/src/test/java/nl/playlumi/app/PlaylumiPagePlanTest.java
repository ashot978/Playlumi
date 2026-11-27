package nl.playlumi.app;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiPagePlanTest {
    private static PlaylumiPagePlan.Block block(int start,int end){return new PlaylumiPagePlan.Block(start,end);}
    @Test public void actualBlocksAreNeverClippedAcrossTwoPages(){
        List<PlaylumiPagePlan.Page> pages=PlaylumiPagePlan.pack(Arrays.asList(
                block(0,100),block(104,180),block(190,285),block(290,365)),180,4);
        assertEquals(2,pages.size());
        assertEquals(0,pages.get(0).start);assertEquals(180,pages.get(0).end);
        assertEquals(190,pages.get(1).start);assertEquals(365,pages.get(1).end);
        assertEquals(1f,pages.get(1).scale,0f);
    }
    @Test public void overlapsStayTogetherAndOversizedBoardFitsOnOwnPage(){
        List<PlaylumiPagePlan.Page> pages=PlaylumiPagePlan.pack(Arrays.asList(
                block(10,85),block(50,150),block(145,220),block(240,300)),180,4);
        assertEquals(2,pages.size());assertEquals(10,pages.get(0).start);assertEquals(220,pages.get(0).end);
        assertTrue(pages.get(0).scale<1f);assertEquals(180f,210*pages.get(0).scale,.01f);
        assertEquals(1f,pages.get(1).scale,0f);
    }
    @Test public void calmMenusHaveLimitedContentEvenOnLargeScreens(){
        List<PlaylumiPagePlan.Page> pages=PlaylumiPagePlan.pack(Arrays.asList(
                block(0,20),block(30,50),block(60,80),block(90,110),block(120,140)),1000,3);
        assertEquals(2,pages.size());assertEquals(3,pages.get(0).blocks);
        assertEquals(1,PlaylumiPagePlan.pageFor(pages,120));
        assertEquals(1,PlaylumiPagePlan.safePage(99,pages.size()));
        assertEquals(0,PlaylumiPagePlan.safePage(-2,pages.size()));
    }
    @Test public void onlyDeliberateHorizontalSwipesChangePages(){
        assertEquals(0,PlaylumiPagePlan.swipe(0,500,48,true));
        assertEquals(0,PlaylumiPagePlan.swipe(20,500,48,true));
        assertEquals(0,PlaylumiPagePlan.swipe(70,80,48,true));
        assertEquals(0,PlaylumiPagePlan.swipe(45,0,48,true));
        assertEquals(0,PlaylumiPagePlan.swipe(-200,0,48,false));
        assertEquals(1,PlaylumiPagePlan.swipe(-200,15,48,true));
        assertEquals(-1,PlaylumiPagePlan.swipe(200,15,48,true));
    }
    @Test public void stableAnchorSurvivesEarlierContentGrowthAndExplicitNavigationWins(){
        List<PlaylumiPagePlan.Page> changed=PlaylumiPagePlan.pack(Arrays.asList(
                block(0,95),block(100,190),block(195,290),block(295,390)),100,4);
        assertEquals(2,PlaylumiPagePlan.resolvePage(changed,1,-1,195,-1));
        assertEquals(0,PlaylumiPagePlan.resolvePage(changed,1,0,195,-1));
        assertEquals(3,PlaylumiPagePlan.resolvePage(changed,1,0,195,295));
        assertEquals(1,PlaylumiPagePlan.resolvePage(changed,1,-1,-1,-1));
    }
    @Test public void aVerticalGestureCannotBecomeAnAccidentalPageTurn(){
        int vertical=PlaylumiPagePlan.lockAxis(0,3,40,14);
        assertEquals(PlaylumiPagePlan.AXIS_VERTICAL,vertical);
        assertEquals(vertical,PlaylumiPagePlan.lockAxis(vertical,250,40,14));
        int horizontal=PlaylumiPagePlan.lockAxis(0,-40,2,14);
        assertEquals(PlaylumiPagePlan.AXIS_HORIZONTAL,horizontal);
        assertEquals(horizontal,PlaylumiPagePlan.lockAxis(horizontal,-45,180,14));
        assertEquals(0,PlaylumiPagePlan.lockAxis(0,10,10,14));
    }
}
