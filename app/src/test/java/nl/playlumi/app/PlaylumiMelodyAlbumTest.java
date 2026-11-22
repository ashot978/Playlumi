package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

/** The original 0.49 melody must survive the three-song album upgrade. */
public final class PlaylumiMelodyAlbumTest {
    @Test public void originalSongStaysInFirstSlotWithoutChangingItsKey(){
        PlaylumiMelodyAlbum album=PlaylumiMelodyAlbum.restore(
                "012301","22","3001",0);
        assertEquals("music_box_composer",PlaylumiMelodyAlbum.key(0));
        assertEquals("012301",album.current().encode());
        assertEquals(6,album.noteCount(0));
        assertEquals("22",album.savedSong(1));
        assertEquals("3001",album.savedSong(2));
    }

    @Test public void changesToOneSongCannotOverwriteAnother(){
        PlaylumiMelodyAlbum album=PlaylumiMelodyAlbum.restore("012","11","323",0);
        assertTrue(album.current().add(3));
        assertEquals("0123",album.savedSong(0));
        album.select(1);
        album.current().clear();
        album.current().add(0);
        assertEquals("0",album.savedSong(1));
        assertEquals("323",album.savedSong(2));
        album.select(2);
        album.current().undo();
        assertEquals("32",album.savedSong(2));
        assertEquals("0123",album.savedSong(0));
    }

    @Test public void selectingSlotPersistsThroughRestore(){
        PlaylumiMelodyAlbum album=PlaylumiMelodyAlbum.restore("0","12","333",2);
        assertEquals(2,album.selected());
        PlaylumiMelodyAlbum loaded=PlaylumiMelodyAlbum.restore(
                album.savedSong(0),album.savedSong(1),album.savedSong(2),
                album.selected());
        assertEquals(2,loaded.selected());
        assertEquals("333",loaded.current().encode());
    }

    @Test public void corruptedSlotsDoNotEraseValidOnes(){
        PlaylumiMelodyAlbum album=PlaylumiMelodyAlbum.restore(
                "031", "INVALID", "002", 500);
        assertEquals(0,album.selected());
        assertEquals("031",album.savedSong(0));
        assertEquals("",album.savedSong(1));
        assertEquals("002",album.savedSong(2));
    }

    @Test public void threeSlotNamesKeysAndBoundsAreDistinct(){
        assertEquals(3,PlaylumiMelodyAlbum.SLOTS);
        java.util.Set<String> keys=new java.util.HashSet<>();
        java.util.Set<String> titles=new java.util.HashSet<>();
        for(int i=0;i<3;i++){
            assertTrue(keys.add(PlaylumiMelodyAlbum.key(i)));
            assertTrue(titles.add(PlaylumiMelodyAlbum.title(i)));
        }
        assertEquals(0,PlaylumiMelodyAlbum.safeSlot(-1));
        assertEquals(0,PlaylumiMelodyAlbum.safeSlot(3));
        assertEquals(2,PlaylumiMelodyAlbum.safeSlot(2));
    }

    @Test(expected=IllegalArgumentException.class)
    public void cannotSelectFourthSong(){
        new PlaylumiMelodyAlbum().select(3);
    }
}
