package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public final class PlaylumiTunesTest {
    private static int little(byte[] b,int offset) {
        return (b[offset]&255)|((b[offset+1]&255)<<8)|
                ((b[offset+2]&255)<<16)|((b[offset+3]&255)<<24);
    }
    @Test public void allJinglesProduceValidMonoPcmWavFiles() {
        for(int n=0;n<PlaylumiTunes.COUNT;n++) {
            byte[] wav=PlaylumiTunes.wav(n);
            assertTrue(wav.length>1000);
            assertEquals('R',wav[0]);assertEquals('I',wav[1]);
            assertEquals('W',wav[8]);assertEquals('A',wav[9]);
            assertEquals('d',wav[36]);assertEquals('a',wav[37]);
            assertEquals(wav.length-8,little(wav,4));
            assertEquals(wav.length-44,little(wav,40));
            assertEquals(PlaylumiTunes.RATE,little(wav,24));
            int energy=0;
            for(int i=44;i<wav.length;i+=17)energy+=Math.abs(wav[i]);
            assertTrue("effect "+n+" silent",energy>1000);
            assertTrue("effect too long",wav.length<170000);
        }
    }
    @Test public void effectsHaveDistinctWaveformsAndAreDeterministic() {
        Set<Integer> signatures=new HashSet<>();
        for(int n=0;n<PlaylumiTunes.COUNT;n++){
            byte[] a=PlaylumiTunes.wav(n);
            assertArrayEquals(a,PlaylumiTunes.wav(n));
            signatures.add(java.util.Arrays.hashCode(a));
        }
        assertEquals(PlaylumiTunes.COUNT,signatures.size());
    }
    @Test public void twelveAdditionalOriginalMelodiesArePlayable() {
        assertEquals(24,PlaylumiTunes.COUNT);
        java.util.Set<Integer> hashes=new java.util.HashSet<>();
        for(int id=PlaylumiTunes.BONUS;id<PlaylumiTunes.COUNT;id++) {
            byte[] wav=PlaylumiTunes.wav(id);
            assertTrue(wav.length>8000);
            assertTrue(wav.length<150000);
            assertTrue(hashes.add(java.util.Arrays.hashCode(wav)));
        }
        assertEquals(12,hashes.size());
        assertEquals(11,PlaylumiTunes.NOTE_PURPLE);
    }
    @Test(expected=IllegalArgumentException.class)
    public void unknownEffectCannotBeGenerated() {PlaylumiTunes.wav(99);}
}
