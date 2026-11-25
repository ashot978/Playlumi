package nl.playlumi.app;

import static org.junit.Assert.*;
import org.junit.Test;

public final class PlaylumiVillageHitTest {
    @Test public void allSixRealGameBuildingsAreReachable() {
        assertEquals(PlaylumiVillageHit.ROCKET,PlaylumiVillageHit.hit(285,395));
        assertEquals(PlaylumiVillageHit.BOOKS,PlaylumiVillageHit.hit(725,398));
        assertEquals(PlaylumiVillageHit.SHAPES,PlaylumiVillageHit.hit(185,690));
        assertEquals(PlaylumiVillageHit.MUSIC,PlaylumiVillageHit.hit(770,699));
        assertEquals(PlaylumiVillageHit.PLAYGROUND,PlaylumiVillageHit.hit(185,1040));
        assertEquals(PlaylumiVillageHit.COLORS,PlaylumiVillageHit.hit(785,1050));
    }
    @Test public void topAndBottomNavigationRemainTouchable() {
        assertEquals(PlaylumiVillageHit.STARS,PlaylumiVillageHit.hit(417,52));
        assertEquals(PlaylumiVillageHit.DISCOVERY,PlaylumiVillageHit.hit(610,52));
        assertEquals(PlaylumiVillageHit.PLAYER,PlaylumiVillageHit.hit(80,155));
        assertEquals(PlaylumiVillageHit.PLAYER,PlaylumiVillageHit.hit(770,52));
        assertEquals(PlaylumiVillageHit.SETTINGS,PlaylumiVillageHit.hit(877,55));
        assertEquals(PlaylumiVillageHit.PARENTS,PlaylumiVillageHit.hit(840,135));
        assertEquals(PlaylumiVillageHit.FRIEND,PlaylumiVillageHit.hit(470,761));
        assertEquals(PlaylumiVillageHit.MISSIONS,PlaylumiVillageHit.hit(84,1550));
        assertEquals(PlaylumiVillageHit.REWARDS,PlaylumiVillageHit.hit(261,1540));
        assertEquals(PlaylumiVillageHit.HOME,PlaylumiVillageHit.hit(466,1540));
        assertEquals(PlaylumiVillageHit.COLLECTION,PlaylumiVillageHit.hit(670,1540));
        assertEquals(PlaylumiVillageHit.PROFILE,PlaylumiVillageHit.hit(841,1540));
    }
    @Test public void tallPhoneArtCoordinatesMatchScreen() {
        int width=412,height=915;
        float x=285f*width/PlaylumiVillageHit.ART_W;
        float y=395f*height/PlaylumiVillageHit.ART_H;
        assertEquals(PlaylumiVillageHit.ROCKET,
                PlaylumiVillageHit.at(width,height,x,y));
        assertEquals(-1,PlaylumiVillageHit.at(width,height,-1,60));
    }
    @Test public void foldableLandscapeUsesContainedArt() {
        float[] box=PlaylumiVillageHit.destination(1800,1300);
        assertTrue(box[0]>0);
        assertTrue(box[2]<1800);
        float x=box[0]+(725/PlaylumiVillageHit.ART_W)*box[2];
        float y=box[1]+(398/PlaylumiVillageHit.ART_H)*box[3];
        assertEquals(PlaylumiVillageHit.BOOKS,PlaylumiVillageHit.at(1800,1300,x,y));
        assertEquals(-1,PlaylumiVillageHit.at(1800,1300,5,650));
    }
}
