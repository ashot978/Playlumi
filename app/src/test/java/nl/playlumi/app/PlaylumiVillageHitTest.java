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
    @Test public void newHudButtonsDoNotOverlap() {
        assertEquals(PlaylumiVillageHit.STARS,PlaylumiVillageHit.hit(335,50));
        assertEquals(PlaylumiVillageHit.STARS,PlaylumiVillageHit.hit(502,50));
        assertEquals(PlaylumiVillageHit.DISCOVERY,PlaylumiVillageHit.hit(510,50));
        assertEquals(PlaylumiVillageHit.DISCOVERY,PlaylumiVillageHit.hit(680,50));
        assertEquals(PlaylumiVillageHit.PLAYER,PlaylumiVillageHit.hit(690,50));
        assertEquals(PlaylumiVillageHit.PLAYER,PlaylumiVillageHit.hit(840,50));
        assertEquals(PlaylumiVillageHit.SETTINGS,PlaylumiVillageHit.hit(875,50));
    }
    @Test public void tallPhoneArtCoordinatesMatchScreen() {
        int width=412,height=915;
        float x=285f*width/PlaylumiVillageHit.ART_W;
        float y=395f*height/PlaylumiVillageHit.ART_H;
        assertEquals(PlaylumiVillageHit.ROCKET,
                PlaylumiVillageHit.at(width,height,x,y));
        assertEquals(-1,PlaylumiVillageHit.at(width,height,-1,60));
    }
    @Test public void mascotRemainsCircularOnStretchedPhoneAndFold(){
        for(int[] screen:new int[][]{{412,915},{658,1380},{1800,1300},{360,640}}){
            float[] dst=PlaylumiVillageHit.destination(screen[0],screen[1]);
            float sx=dst[2]/PlaylumiVillageHit.ART_W,sy=dst[3]/PlaylumiVillageHit.ART_H;
            float[] corrected=PlaylumiVillageHit.avatarCompensation(sx,sy);
            assertEquals(126*sx*corrected[0],126*sy*corrected[1],.001f);
            assertTrue(corrected[0]<=1f&&corrected[1]<=1f);
        }
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
