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
    @Test public void libraryShowsAtMostFourLargeTilesAndShortScreensUseOneRow(){
        assertEquals(2,PlaylumiPagePlan.libraryColumns(360,1f));
        assertEquals(1,PlaylumiPagePlan.libraryColumns(360,1.5f));
        assertEquals(1,PlaylumiPagePlan.libraryRows(420,1f));
        assertEquals(2,PlaylumiPagePlan.libraryRows(780,1f));
        assertEquals(218,PlaylumiPagePlan.libraryTileHeight(850,2));
    }
    @Test public void semanticPageLabelsAndCompactShelvesRemainStable(){
        assertEquals("Bouwtekening · 2/5",PlaylumiPagePlan.caption("Bouwtekening",1,5));
        assertEquals("Pagina 5 van 5",PlaylumiPagePlan.caption("",8,5));
        assertEquals("Accessoires · 1/1",PlaylumiPagePlan.caption("Accessoires",-1,0));
        assertEquals(1,PlaylumiPagePlan.shelfRows(540,1f));
        assertEquals(2,PlaylumiPagePlan.shelfRows(740,1f));
        assertEquals(1,PlaylumiPagePlan.shelfRows(740,1.5f));
    }
    @Test public void reflowKeepsTheVisibleBlockAndVerticalGestureStaysVertical(){
        List<PlaylumiPagePlan.Page> pages=PlaylumiPagePlan.pack(Arrays.asList(block(0,50),block(100,160),block(200,280)),120,1);
        assertEquals(2,PlaylumiPagePlan.resolvePage(pages,0,-1,205,-1));
        assertEquals(1,PlaylumiPagePlan.resolvePage(pages,0,1,205,-1));
        assertEquals(0,PlaylumiPagePlan.resolvePage(pages,2,1,205,20));
        int axis=PlaylumiPagePlan.lockAxis(0,3,28,14);
        assertEquals(PlaylumiPagePlan.AXIS_VERTICAL,axis);
        assertEquals(axis,PlaylumiPagePlan.lockAxis(axis,180,4,14));
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
    @Test public void hiddenPagesCannotBeTappedThroughThePinnedChrome(){
        List<PlaylumiPagePlan.Page> pages=PlaylumiPagePlan.pack(Arrays.asList(
                block(0,100),block(120,220),block(240,340)),100,1);
        PlaylumiPagePlan.Page visible=pages.get(1);
        assertFalse(PlaylumiPagePlan.containsPoint(visible,320,160,80));
        assertFalse(PlaylumiPagePlan.containsPoint(visible,320,160,280));
        assertTrue(PlaylumiPagePlan.containsPoint(visible,320,0,120));
        assertTrue(PlaylumiPagePlan.containsPoint(visible,320,319.5f,219.5f));
        assertFalse(PlaylumiPagePlan.containsPoint(visible,320,-.5f,160));
        assertFalse(PlaylumiPagePlan.containsPoint(visible,320,320,160));
        assertFalse(PlaylumiPagePlan.containsPoint(visible,320,160,220));
    }
    @Test public void scaledBoardsRemainInteractiveAcrossTheirWholeVisiblePage(){
        PlaylumiPagePlan.Page page=PlaylumiPagePlan.pack(
                Arrays.asList(block(100,700)),300,1).get(0);
        assertEquals(.5f,page.scale,0f);
        for(float screenY:new float[]{0,1,149,299.5f})
            assertTrue(PlaylumiPagePlan.containsPoint(page,320,160,page.start+screenY/page.scale));
        assertFalse(PlaylumiPagePlan.containsPoint(page,320,160,page.start-1));
        assertFalse(PlaylumiPagePlan.containsPoint(page,320,160,page.start+300/page.scale));
    }
}
