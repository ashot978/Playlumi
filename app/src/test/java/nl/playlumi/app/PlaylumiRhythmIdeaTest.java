package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiRhythmIdeaTest {
    @Test public void allIdeasCreatePlayableOriginalRhythms(){
        assertEquals(4,PlaylumiRhythmIdea.COUNT);
        java.util.Set<String> seen=new java.util.HashSet<>();
        for(int i=0;i<PlaylumiRhythmIdea.COUNT;i++){
            PlaylumiMusicComposer song=new PlaylumiMusicComposer();
            assertTrue(PlaylumiRhythmIdea.append(song,i));
            assertEquals(4,song.size());
            assertEquals(4,song.encode().length());
            assertTrue("Must include a quiet rhythmic pause",
                    song.description().contains("🤫"));
            assertTrue("All patterns should differ",seen.add(song.encode()));
            assertEquals(song.encode(),PlaylumiMusicComposer.restore(song.encode()).encode());
        }
    }
    @Test public void ideasNeverOverwriteFullOrNearlyFullSongs(){
        PlaylumiMusicComposer song=PlaylumiMusicComposer.restore("012301230");
        String unchanged=song.encode();
        assertFalse(PlaylumiRhythmIdea.append(song,0));
        assertEquals(unchanged,song.encode());
        song.clear();
        for(int i=0;i<12;i++)assertTrue(song.add(i%5));
        unchanged=song.encode();
        assertFalse(PlaylumiRhythmIdea.append(song,1));
        assertEquals(unchanged,song.encode());
    }
    @Test public void twelveNotesCanContainThreeIndependentIdeas(){
        PlaylumiMusicComposer song=new PlaylumiMusicComposer();
        for(int i=0;i<3;i++)assertTrue(PlaylumiRhythmIdea.append(song,i));
        assertEquals(12,song.size());
        assertFalse(PlaylumiRhythmIdea.append(song,3));
        assertEquals(12,PlaylumiMusicComposer.restore(song.encode()).size());
    }
    @Test public void callersCannotMutatePatternLibrary(){
        int[] modified=PlaylumiRhythmIdea.notes(0);
        modified[0]=999;
        assertEquals(0,PlaylumiRhythmIdea.notes(0)[0]);
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidIdeaRejected(){PlaylumiRhythmIdea.notes(-1);}
}
