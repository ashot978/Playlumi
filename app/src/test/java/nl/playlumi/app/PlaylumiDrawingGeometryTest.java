package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiDrawingGeometryTest {
    @Test public void newPaperHasFourToThreeProportionsAndIsCenteredInsideItsInset(){
        PlaylumiDrawingGeometry.Paper paper=PlaylumiDrawingGeometry.fit(430,320,1024,768,8);
        assertTrue(paper.valid());assertEquals(4f/3f,(paper.right-paper.left)/(paper.bottom-paper.top),.00001f);
        assertEquals(215,(paper.left+paper.right)/2,.00001f);assertEquals(160,(paper.top+paper.bottom)/2,.00001f);
        assertTrue(paper.left>=8&&paper.top>=8&&paper.right<=422&&paper.bottom<=312);
    }
    @Test public void aWideImportedFoldPictureKeepsItsProportionsOnANarrowPhone(){
        PlaylumiDrawingGeometry.Paper paper=PlaylumiDrawingGeometry.fit(390,300,1520,430,8);
        assertEquals(1520f/430f,(paper.right-paper.left)/(paper.bottom-paper.top),.00001f);
        assertEquals(paper.scale,(paper.right-paper.left)/1520,.00001f);
        assertEquals(paper.scale,(paper.bottom-paper.top)/430,.00001f);
        assertTrue(paper.top>80);assertFalse(paper.contains(195,10));assertTrue(paper.contains(195,150));
    }
    @Test public void documentPointsRoundTripAcrossPhoneFoldAndLandscapeWithoutDrift(){
        int[][] viewports={{390,300},{430,320},{900,220},{280,800}};
        int[][] documents={{1024,768},{1520,430},{600,1400}};
        for(int[] view:viewports)for(int[] doc:documents){
            PlaylumiDrawingGeometry.Paper paper=PlaylumiDrawingGeometry.fit(view[0],view[1],doc[0],doc[1],8);
            for(float fraction:new float[]{0,.12f,.5f,.83f,1}){
                assertEquals(fraction,paper.xFraction(paper.viewX(fraction)),.00001f);
                assertEquals(fraction,paper.yFraction(paper.viewY(fraction)),.00001f);
            }
        }
    }
    @Test public void outOfPaperMovesStopAtTheEdgeAndInvalidBoundsCannotStartDrawing(){
        PlaylumiDrawingGeometry.Paper paper=PlaylumiDrawingGeometry.fit(390,300,1024,768,8);
        assertEquals(0,paper.xFraction(-100),0);assertEquals(1,paper.yFraction(900),0);
        for(PlaylumiDrawingGeometry.Paper invalid:new PlaylumiDrawingGeometry.Paper[]{
                PlaylumiDrawingGeometry.fit(0,300,1024,768,8),
                PlaylumiDrawingGeometry.fit(10,10,1024,768,8),
                PlaylumiDrawingGeometry.fit(Float.NaN,300,1024,768,8),
                PlaylumiDrawingGeometry.fit(390,300,0,768,8)}){
            assertFalse(invalid.valid());assertFalse(invalid.contains(0,0));
            assertEquals(0,invalid.xFraction(30),0);assertEquals(0,invalid.yFraction(30),0);
        }
    }
    @Test public void legacyPngDecodeIsBoundedEvenAtExactEdgesAndVeryLargeDimensions(){
        int[][] pictures={{1024,768},{4000,500},{4001,500},{8193,5000},{1,Integer.MAX_VALUE},
                {Integer.MAX_VALUE,1},{Integer.MAX_VALUE,Integer.MAX_VALUE}};
        for(int[] picture:pictures){
            int sample=PlaylumiDrawingGeometry.sampleSize(picture[0],picture[1]);
            assertTrue(sample>0);assertEquals(0,sample&(sample-1));
            long width=((long)picture[0]+sample-1)/sample,height=((long)picture[1]+sample-1)/sample;
            assertTrue(width*height<=PlaylumiDrawingGeometry.MAX_PIXELS);
            assertTrue(width<=PlaylumiDrawingGeometry.MAX_EDGE&&height<=PlaylumiDrawingGeometry.MAX_EDGE);
        }
        assertEquals(1,PlaylumiDrawingGeometry.sampleSize(4000,500));
        assertEquals(2,PlaylumiDrawingGeometry.sampleSize(4001,500));
    }
    @Test public void brushPresetsKeepTheExistingEightDpDefault(){
        assertEquals(4,PlaylumiDrawingGeometry.brushWidthDp(0));
        assertEquals(8,PlaylumiDrawingGeometry.brushWidthDp(1));
        assertEquals(16,PlaylumiDrawingGeometry.brushWidthDp(2));
        for(int invalid:new int[]{-1,3,Integer.MIN_VALUE,Integer.MAX_VALUE}){
            assertEquals(1,PlaylumiDrawingGeometry.safeBrushSize(invalid));
            assertEquals(8,PlaylumiDrawingGeometry.brushWidthDp(invalid));
        }
    }
}
