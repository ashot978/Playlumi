package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaylumiFavoritesTest {
    private static PlaylumiPlayHistory.Entry game(int mode,int variant) {
        return new PlaylumiPlayHistory.Entry(mode,variant,mode%3,false);
    }

    @Test public void pinAndUnpinPersistCorrectly() {
        PlaylumiFavorites f=new PlaylumiFavorites();
        assertTrue(f.toggle(game(1,0)));
        assertTrue(f.contains(1,0));
        assertFalse(f.toggle(game(1,0)));
        assertFalse(f.contains(1,0));
        assertEquals(0,f.size());
        for(int i=0;i<6;i++)assertTrue(f.pin(game(i,0)));
        assertEquals(6,f.size());
        assertFalse(f.pin(game(7,0)));
        assertEquals(6,f.size());
        assertTrue(f.pin(new PlaylumiPlayHistory.Entry(1,0,2,true)));
        assertEquals(6,f.size());
        PlaylumiFavorites restored=PlaylumiFavorites.restore(f.serialize());
        assertEquals(f.size(),restored.size());
        assertEquals(f.serialize(),restored.serialize());
        assertTrue(restored.contains(1,0));
    }

    @Test public void recoverMalformedPreferencesWithoutCrashing() {
        assertEquals(0,PlaylumiFavorites.restore(null).size());
        assertEquals(0,PlaylumiFavorites.restore(new String(new char[600])).size());
        PlaylumiFavorites f=PlaylumiFavorites.restore(
                "wrong;1,2,1,1;24,0,0,0;3,0,2,2;2,4,0,0;garbage");
        assertEquals(2,f.size());
        assertTrue(f.contains(1,2));
        assertTrue(f.contains(2,4));
        assertThrows(IllegalArgumentException.class,()->f.toggle(null));
    }
}
