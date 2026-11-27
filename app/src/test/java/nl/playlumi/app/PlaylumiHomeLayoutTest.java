package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiHomeLayoutTest {
    @Test public void phonesKeepTheOriginalVillageAndWideWindowsGetNativeLayout(){
        assertEquals(PlaylumiHomeLayout.PHONE,PlaylumiHomeLayout.mode(360,760));
        assertEquals(PlaylumiHomeLayout.PHONE,PlaylumiHomeLayout.mode(412,915));
        assertEquals(PlaylumiHomeLayout.PHONE,PlaylumiHomeLayout.mode(360,360));
        assertEquals(PlaylumiHomeLayout.FOLD,PlaylumiHomeLayout.mode(360,300));
        assertEquals(PlaylumiHomeLayout.FOLD,PlaylumiHomeLayout.mode(500,330));
        assertEquals(PlaylumiHomeLayout.FOLD,PlaylumiHomeLayout.mode(673,745));
        assertEquals(PlaylumiHomeLayout.FOLD,PlaylumiHomeLayout.mode(800,400));
        assertEquals(PlaylumiHomeLayout.EXPANDED,PlaylumiHomeLayout.mode(1280,800));
    }
    @Test public void allSixWorldsFitWithoutLetterboxingTheWholeScreen(){
        for(int[] screen:new int[][]{{360,300},{412,280},{500,330},{520,360},{599,360},{600,800},{673,745},{850,900},{960,540},{1100,800},{1600,900}}){
            int w=screen[0],h=screen[1],mode=PlaylumiHomeLayout.mode(w,h);
            int columns=PlaylumiHomeLayout.worldColumns(w,h);
            int available=w-32-PlaylumiHomeLayout.actionWidth(w)
                    -(mode==PlaylumiHomeLayout.EXPANDED?PlaylumiHomeLayout.profileWidth(w)+8:0);
            assertTrue(available/columns>=145);
            assertTrue(columns>=1&&columns<=3);
            assertEquals(0,6%columns);
            assertTrue(PlaylumiHomeLayout.artHeight(w,h)>=84);
            assertTrue(PlaylumiHomeLayout.artHeight(w,h)<=175);
        }
    }
    @Test public void narrowLandscapeKeepsRoomForCardsAndTheActionPanel(){
        for(int width=360;width<600;width++){
            assertEquals(PlaylumiHomeLayout.FOLD,PlaylumiHomeLayout.mode(width,300));
            assertEquals(1,PlaylumiHomeLayout.worldColumns(width,300));
            int sidebar=PlaylumiHomeLayout.actionWidth(width);
            assertTrue(sidebar>=150&&sidebar<=199);
            assertTrue(width-32-sidebar>=145);
            assertTrue(PlaylumiHomeLayout.artHeight(width,300)>=84);
        }
        assertEquals(2,PlaylumiHomeLayout.worldColumns(600,800));
        assertEquals(2,PlaylumiHomeLayout.worldColumns(673,745));
    }
}
