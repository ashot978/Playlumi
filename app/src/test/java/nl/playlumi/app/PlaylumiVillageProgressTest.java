package nl.playlumi.app;

import static org.junit.Assert.*;
import org.junit.Test;
public final class PlaylumiVillageProgressTest {
    @Test public void visitingEachBuildingCountsOnce() {
        int state=0;
        for(int i=0;i<6;i++) state=PlaylumiVillageProgress.markVisited(state,i);
        assertEquals(6,PlaylumiVillageProgress.visitedCount(state));
        state=PlaylumiVillageProgress.markVisited(state,4);
        assertEquals(6,PlaylumiVillageProgress.visitedCount(state));
    }
    @Test public void ignoredNavDoesNotModifyDiscovery() {
        assertEquals(3,PlaylumiVillageProgress.markVisited(3,-1));
        assertEquals(3,PlaylumiVillageProgress.markVisited(3,6));
        assertFalse(PlaylumiVillageProgress.isVisited(3,6));
        assertEquals(63,PlaylumiVillageProgress.sanitize(0xFF));
    }
    @Test public void masksDoNotAffectEachOther() {
        int childOne=PlaylumiVillageProgress.markVisited(0,0);
        int childTwo=PlaylumiVillageProgress.markVisited(0,3);
        assertEquals(1,PlaylumiVillageProgress.visitedCount(childOne));
        assertEquals(1,PlaylumiVillageProgress.visitedCount(childTwo));
        assertFalse(PlaylumiVillageProgress.isVisited(childOne,3));
    }
}
