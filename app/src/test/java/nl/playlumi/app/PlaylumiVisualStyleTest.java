package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiVisualStyleTest {
    @Test public void blendingIsStableAndOpaque() {
        assertEquals(0xff123456,PlaylumiVisualStyle.blend(0xff123456,0xffffffff,0));
        assertEquals(0xffffffff,PlaylumiVisualStyle.blend(0xff123456,0xffffffff,1000));
        assertEquals(0xff808080,PlaylumiVisualStyle.blend(0xff000000,0xffffffff,500));
        assertEquals(0xff000000,PlaylumiVisualStyle.blend(0xff000000,0xffffffff,-7));
        assertEquals(0xffffffff,PlaylumiVisualStyle.blend(0xff000000,0xffffffff,12000));
        for(int i=0;i<=1000;i+=7)
            assertEquals(255,PlaylumiVisualStyle.lightened(0xffa283d3,i)>>>24);
    }
    @Test public void mainLabelsMeetComfortableContrast() {
        assertTrue(PlaylumiVisualStyle.contrastRatio(
                PlaylumiVisualStyle.PRIMARY_START,0xffffffff)>=4.5);
        assertTrue(PlaylumiVisualStyle.contrastRatio(
                PlaylumiVisualStyle.PRIMARY_END,0xffffffff)>=4.5);
        assertTrue(PlaylumiVisualStyle.contrastRatio(
                PlaylumiVisualStyle.INK,PlaylumiVisualStyle.PAGE_TOP)>=7);
        assertTrue(PlaylumiVisualStyle.contrastRatio(
                PlaylumiVisualStyle.MUTED,PlaylumiVisualStyle.PAGE_BOTTOM)>=4.5);
    }
    @Test public void softlyGradedCardsRetainLegibleText() {
        int[] cards={0xffdceeff,0xffd7f3e4,0xffffe7bb,0xffece5ff,0xffd9f3ff,
                0xffffe4bd,0xffe8d9ff,0xffd5f5df,0xffffdded,0xfffff1b6};
        for(int c:cards) {
            assertTrue(PlaylumiVisualStyle.contrastRatio(
                    PlaylumiVisualStyle.INK,PlaylumiVisualStyle.lightened(c,100))>=7);
            assertTrue(PlaylumiVisualStyle.contrastRatio(
                    PlaylumiVisualStyle.INK,PlaylumiVisualStyle.lightened(c,470))>=7);
        }
    }
}
