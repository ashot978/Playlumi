package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiHomeLayoutTest {
    @Test public void phonesKeepTheOriginalVillageAndWideWindowsGetNativeLayout(){
        assertEquals(PlaylumiHomeLayout.PHONE,PlaylumiHomeLayout.mode(360,760));
        assertEquals(PlaylumiHomeLayout.PHONE,PlaylumiHomeLayout.mode(412,915));
        assertEquals(PlaylumiHomeLayout.FOLD,PlaylumiHomeLayout.mode(673,745));
        assertEquals(PlaylumiHomeLayout.FOLD,PlaylumiHomeLayout.mode(800,400));
        assertEquals(PlaylumiHomeLayout.EXPANDED,PlaylumiHomeLayout.mode(1280,800));
    }
    @Test public void allSixWorldsFitWithoutLetterboxingTheWholeScreen(){
        for(int[] screen:new int[][]{{520,360},{600,800},{673,745},{850,900},{960,540},{1100,800},{1600,900}}){
            int w=screen[0],h=screen[1],mode=PlaylumiHomeLayout.mode(w,h);
            int columns=PlaylumiHomeLayout.worldColumns(w,h);
            int available=w-32-PlaylumiHomeLayout.actionWidth(w)
                    -(mode==PlaylumiHomeLayout.EXPANDED?PlaylumiHomeLayout.profileWidth(w)+8:0);
            assertTrue(available/columns>=145);
            assertTrue(columns==2||columns==3);
            assertEquals(0,6%columns);
            assertTrue(PlaylumiHomeLayout.artHeight(w,h)>=84);
            assertTrue(PlaylumiHomeLayout.artHeight(w,h)<=175);
        }
    }
}
