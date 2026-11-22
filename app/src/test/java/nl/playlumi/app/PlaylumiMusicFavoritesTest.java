package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public final class PlaylumiMusicFavoritesTest {
    @Test public void storingFavoritesCannotTouchOtherSongsOrAccounts() {
        PlaylumiMusicFavorites kidA=new PlaylumiMusicFavorites(0);
        PlaylumiMusicFavorites kidB=new PlaylumiMusicFavorites(0);
        assertTrue(kidA.toggle(0));
        assertTrue(kidA.toggle(17));
        assertEquals(2,kidA.count());
        assertEquals(0,kidB.count());
        PlaylumiMusicFavorites restored=new PlaylumiMusicFavorites(kidA.mask());
        assertTrue(restored.isFavorite(0));
        assertTrue(restored.isFavorite(17));
        assertFalse(restored.isFavorite(8));
        assertFalse(kidA.toggle(17));
        assertEquals(1,kidA.count());
        assertTrue(restored.isFavorite(17));
    }
    @Test public void musicBoxHasAllEighteenTracksAndNoFavoritesInitially() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        assertEquals(18,f.visibleCount(false));
        assertEquals(0,f.visibleCount(true));
        assertEquals(3,f.pages(false));
        assertEquals(1,f.pages(true));
        assertArrayEquals(new int[]{0,1,2,3,4,5},f.onPage(false,0));
        assertArrayEquals(new int[]{12,13,14,15,16,17},f.onPage(false,2));
        assertArrayEquals(new int[0],f.onPage(true,0));
    }
    @Test public void onlyFavoritesStaySortedAcrossPartialPages() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        int[] saved={0,2,5,6,8,9,13,14,17};
        for(int x:saved)f.toggle(x);
        assertEquals(9,f.count());
        assertEquals(2,f.pages(true));
        assertArrayEquals(new int[]{0,2,5,6,8,9},f.onPage(true,0));
        assertArrayEquals(new int[]{13,14,17},f.onPage(true,1));
        assertArrayEquals(new int[0],f.onPage(true,2));
    }
    @Test public void everyTrackMayBeStarredAndFoundExactlyOnce() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        Set<Integer> all=new HashSet<>();
        for(int i=0;i<PlaylumiMusicBox.COUNT;i++)f.toggle(i);
        assertEquals(18,f.count());
        for(int page=0;page<f.pages(true);page++)
            for(int track:f.onPage(true,page))assertTrue(all.add(track));
        assertEquals(18,all.size());
    }
    @Test public void corruptStorageAndHighBitsCannotCreateInvalidFavorites(){
        assertEquals(0,new PlaylumiMusicFavorites(-1).count());
        assertEquals(0,new PlaylumiMusicFavorites(1<<22).count());
        assertEquals(1,new PlaylumiMusicFavorites((1<<22)|(1<<17)).count());
    }
    @Test public void surpriseNeverLeavesFavoritesAndAvoidsPreviousWhenPossible() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        assertEquals(-1,f.surprise(true,-1,0));
        f.toggle(3);
        assertEquals(3,f.surprise(true,3,500));
        f.toggle(9);
        f.toggle(17);
        for(int draw=-20;draw<20;draw++) {
            int pick=f.surprise(true,9,draw);
            assertTrue(pick==3 || pick==17);
            assertTrue(f.isFavorite(pick));
        }
        assertEquals(3,f.surprise(true,-1,0));
        assertEquals(9,f.surprise(true,-1,1));
        assertEquals(17,f.surprise(true,-1,2));
    }
    @Test public void allTunesSurpriseAvoidsLastTrackAndStillWorks() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        for(int draw=-35;draw<35;draw++) {
            int pick=f.surprise(false,8,draw);
            assertTrue(pick>=0 && pick<18);
            assertNotEquals(8,pick);
        }
    }
    @Test public void adjacentAllTracksWrapsWithoutLosingSelection() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        assertEquals(0,f.adjacent(false,-1,1));
        assertEquals(17,f.adjacent(false,-1,-1));
        assertEquals(0,f.adjacent(false,17,1));
        assertEquals(17,f.adjacent(false,0,-1));
        assertEquals(9,f.adjacent(false,8,1));
        assertEquals(7,f.adjacent(false,8,-1));
    }
    @Test public void adjacentFavoritesSkipsOtherTunesAndHandlesEmptySingle() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        assertEquals(-1,f.adjacent(true,-1,1));
        assertEquals(-1,f.adjacent(true,3,-1));
        f.toggle(4);
        assertEquals(4,f.adjacent(true,4,1));
        assertEquals(4,f.adjacent(true,4,-1));
        f.toggle(11);
        f.toggle(17);
        assertEquals(11,f.adjacent(true,4,1));
        assertEquals(17,f.adjacent(true,4,-1));
        assertEquals(4,f.adjacent(true,17,1));
        assertEquals(17,f.adjacent(true,4, -1));
        assertEquals(4,f.adjacent(true,5,1));
        assertEquals(17,f.adjacent(true,5,-1));
        assertEquals(4,f.adjacent(true,-1,1));
        assertEquals(17,f.adjacent(true,-1,-1));
    }
    @Test(expected=IllegalArgumentException.class)
    public void adjacentRejectsUnsupportedDirection() {
        new PlaylumiMusicFavorites(0).adjacent(false,0,0);
    }
    @Test public void navigatingAllPagesWrapsInBothDirections() {
        PlaylumiMusicFavorites favorites=new PlaylumiMusicFavorites(0);
        assertEquals(3,favorites.pages(false));
        assertEquals(2,favorites.movePage(false,0,-1));
        assertEquals(0,favorites.movePage(false,2,1));
        assertEquals(1,favorites.movePage(false,0,1));
        assertEquals(0,favorites.movePage(false,1,-1));
    }
    @Test public void navigatingPartialAndEmptyFavoritesPagesIsSafe() {
        PlaylumiMusicFavorites favorites=new PlaylumiMusicFavorites(0);
        assertEquals(0,favorites.movePage(true,0,-1));
        assertEquals(0,favorites.movePage(true,42,1));
        for(int tune:new int[]{0,2,4,6,8,10,12})favorites.toggle(tune);
        assertEquals(2,favorites.pages(true));
        assertEquals(1,favorites.movePage(true,0,-1));
        assertEquals(0,favorites.movePage(true,1,1));
        assertEquals(1,favorites.movePage(true,42,1));
        assertEquals(1,favorites.onPage(true,1).length);
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidPageDirectionIsRejected() {
        new PlaylumiMusicFavorites(0).movePage(false,0,0);
    }
    @Test public void selectedTunePageMatchesFullCollection() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        for(int track=0;track<18;track++)
            assertEquals(track/6,f.pageOf(false,track));
        assertEquals(-1,f.pageOf(false,-1));
        assertEquals(-1,f.pageOf(false,18));
    }
    @Test public void selectedTunePageRespectsSparseFavorites() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        assertEquals(-1,f.pageOf(true,0));
        for(int track:new int[]{0,2,4,6,8,10,12,14})f.toggle(track);
        assertEquals(0,f.pageOf(true,10));
        assertEquals(1,f.pageOf(true,12));
        assertEquals(1,f.pageOf(true,14));
        assertEquals(-1,f.pageOf(true,13));
        assertEquals(-1,f.pageOf(true,-1));
        assertEquals(-1,f.pageOf(true,100));
        f.toggle(12);
        assertEquals(1,f.pageOf(true,14));
    }
    @Test public void navigationAcrossPagesFindsNewSelectedTune() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        int next=f.adjacent(false,5,1);
        assertEquals(6,next);
        assertEquals(1,f.pageOf(false,next));
        int previous=f.adjacent(false,12,-1);
        assertEquals(11,previous);
        assertEquals(1,f.pageOf(false,previous));
        assertEquals(0,f.pageOf(false,f.adjacent(false,17,1)));
        assertEquals(2,f.pageOf(false,f.adjacent(false,0,-1)));
        assertEquals(2,f.pageOf(false,f.surprise(false,0,16)));
    }
    @Test public void selectedFavoriteFollowsCompressedPageBoundaries() {
        PlaylumiMusicFavorites f=new PlaylumiMusicFavorites(0);
        for(int track:new int[]{0,2,4,6,8,10,12})f.toggle(track);
        assertEquals(1,f.pageOf(true,f.adjacent(true,10,1)));
        assertEquals(0,f.pageOf(true,f.adjacent(true,12,1)));
        assertEquals(1,f.pageOf(true,f.adjacent(true,0,-1)));
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidIndexIsRejected(){new PlaylumiMusicFavorites(0).toggle(18);}
}
