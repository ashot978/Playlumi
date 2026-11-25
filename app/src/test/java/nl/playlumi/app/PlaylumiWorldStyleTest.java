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
    @Test public void avatarChoiceNeverUsesInvalidIndex(){
        assertEquals(4,PlaylumiWorldStyle.AVATAR_NAMES.length);
        for(int i=0;i<4;i++)assertEquals(i,PlaylumiWorldStyle.safeAvatar(i));
        assertEquals(0,PlaylumiWorldStyle.safeAvatar(-1));
        assertEquals(0,PlaylumiWorldStyle.safeAvatar(4));
    }
}
