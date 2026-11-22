package nl.playlumi.app;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public final class PlaylumiMusicComposerTest {
    @Test public void allFourAnimalNotesCanBeRecordedAndRestored() {
        PlaylumiMusicComposer composer=new PlaylumiMusicComposer();
        for(int i=0;i<PlaylumiMelody.NOTES;i++)
            assertTrue(composer.add(i));
        assertEquals("0123",composer.encode());
        assertArrayEquals(new int[]{0,1,2,3},composer.snapshot());
        assertTrue(composer.description().contains("4 / 12"));
        PlaylumiMusicComposer loaded=PlaylumiMusicComposer.restore(composer.encode());
        assertArrayEquals(composer.snapshot(),loaded.snapshot());
    }

    @Test public void maxTwelveNotesWithoutOverwritingExistingMusic() {
        PlaylumiMusicComposer composer=new PlaylumiMusicComposer();
        for(int i=0;i<PlaylumiMusicComposer.MAX_NOTES;i++)
            assertTrue(composer.add(i%4));
        String original=composer.encode();
        assertFalse(composer.add(2));
        assertEquals(12,composer.size());
        assertEquals(original,composer.encode());
        assertTrue(composer.undo());
        assertEquals(11,composer.size());
        assertTrue(composer.add(3));
        assertEquals(12,composer.size());
    }

    @Test public void undoAndClearDoNotAffectOtherProfiles() {
        PlaylumiMusicComposer first=PlaylumiMusicComposer.restore("120");
        PlaylumiMusicComposer second=PlaylumiMusicComposer.restore("3331");
        assertTrue(first.undo());
        assertEquals("12",first.encode());
        first.clear();
        assertEquals("",first.encode());
        assertEquals("3331",second.encode());
        assertFalse(first.undo());
    }

    @Test public void badOrCorruptedSettingsAreIgnoredSafely() {
        for(String bad:Arrays.asList("0125","-1","abc"," 12","0123012301230")) {
            assertEquals(0,PlaylumiMusicComposer.restore(bad).size());
        }
        assertEquals(0,PlaylumiMusicComposer.restore(null).size());
        assertEquals(0,PlaylumiMusicComposer.restore("").size());
    }

    @Test public void snapshotsDoNotExposeInternalMutableNotes() {
        PlaylumiMusicComposer composer=PlaylumiMusicComposer.restore("0012");
        int[] copy=composer.snapshot();
        copy[0]=3;
        assertEquals("0012",composer.encode());
    }

    @Test public void silentRestCanBeRecordedPersistedUndoneAndShown() {
        PlaylumiMusicComposer song=PlaylumiMusicComposer.restore("0123");
        assertTrue(song.add(PlaylumiMusicComposer.REST));
        assertEquals("01234",song.encode());
        assertEquals("🤫",PlaylumiMusicComposer.symbolFor(PlaylumiMusicComposer.REST));
        assertTrue(song.description().contains("🤫"));
        assertArrayEquals(new int[]{0,1,2,3,4},
                PlaylumiMusicComposer.restore(song.encode()).snapshot());
        assertTrue(song.undo());
        assertEquals("0123",song.encode());
    }

    @Test(expected=IllegalArgumentException.class)
    public void notesMustBeValidAnimalsOrRest() {
        new PlaylumiMusicComposer().add(5);
    }
}
