package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class PlaylumiWorldStyleTest {
    @Test public void everyBuildingHasAUniqueTheme(){
        assertEquals(6,PlaylumiWorldStyle.NAMES.length);
        assertEquals(6,PlaylumiWorldStyle.SUBTITLES.length);
        for(int a=0;a<6;a++){
            assertTrue(PlaylumiWorldStyle.valid(a));
            for(int b=a+1;b<6;b++)assertNotEquals(PlaylumiWorldStyle.accent(a),PlaylumiWorldStyle.accent(b));
        }
        assertFalse(PlaylumiWorldStyle.valid(6));
    }
    @Test public void lumiReturnsWithoutRenumberingLegacyAvatars(){
        assertEquals(0,PlaylumiWorldStyle.LUMI);
        assertEquals("Lumi",PlaylumiWorldStyle.AVATAR_NAMES[0]);
        for(int i=0;i<4;i++)assertEquals(i,PlaylumiWorldStyle.safeAvatar(i));
        assertEquals(0,PlaylumiWorldStyle.safeAvatar(-1));
        assertEquals(0,PlaylumiWorldStyle.safeAvatar(4));
    }
    private static double channel(int value){
        double v=value/255.0;
        return v<=.04045?v/12.92:Math.pow((v+.055)/1.055,2.4);
    }
    private static double luminance(int color){
        return .2126*channel((color>>16)&255)+.7152*channel((color>>8)&255)+.0722*channel(color&255);
    }
    @Test public void everyDarkTileSubtitleHasReadableContrast(){
        for(int w=0;w<6;w++){
            for(int bg:new int[]{PlaylumiWorldStyle.plateTop(w),PlaylumiWorldStyle.plateMiddle(w),PlaylumiWorldStyle.deep(w)}){
                for(int fg:new int[]{PlaylumiWorldStyle.PRIMARY_ON_DARK,PlaylumiWorldStyle.SECONDARY_ON_DARK})
                    assertTrue("Contrast for world "+w,(luminance(fg)+.05)/(luminance(bg)+.05)>=4.5);
            }
        }
    }
    @Test public void swatchFillsMostOfTheSmallerDimension(){
        assertEquals(156f,PlaylumiWorldStyle.swatchDiameter(600,200),.01f);
        assertEquals(124.8f,PlaylumiWorldStyle.swatchDiameter(160,300),.01f);
    }
    @Test public void colorAnswersUseReadableTextOnBrightAndDarkColors(){
        assertEquals(0xff160e22,PlaylumiWorldStyle.contrastingInk(0xffffff00));
        assertEquals(0xff160e22,PlaylumiWorldStyle.contrastingInk(0xffffffff));
        assertEquals(0xffffffff,PlaylumiWorldStyle.contrastingInk(0xff172049));
        assertEquals(0xffffffff,PlaylumiWorldStyle.contrastingInk(0xff000000));
    }
}
