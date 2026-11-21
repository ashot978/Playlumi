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
}