package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaylumiSavedLooksTest {
    @Test public void savedAndWornLooksNeverCostStars() {
        PlaylumiWardrobe w=new PlaylumiWardrobe(600);
        PlaylumiSavedLooks looks=new PlaylumiSavedLooks();
        assertFalse(looks.wear(0,w));
        assertEquals(PlaylumiWardrobe.PURCHASED,w.buyOrEquip(0,4));
        assertEquals(PlaylumiWardrobe.PURCHASED,w.buyOrEquip(1,3));
        assertEquals(PlaylumiWardrobe.PURCHASED,w.buyOrEquip(4,2));
        looks.save(0,w);
        int stars=w.stars();
        for(int i=0;i<PlaylumiWardrobe.SLOTS;i++)w.buyOrEquip(i,0);
        assertTrue(looks.wear(0,w));
        assertEquals(4,w.equipped(0));
        assertEquals(3,w.equipped(1));
        assertEquals(2,w.equipped(4));
        assertEquals(stars,w.stars());
        assertEquals(stars,w.stars());
        PlaylumiSavedLooks restored=PlaylumiSavedLooks.restore(looks.serialize());
        assertEquals(looks.describe(0),restored.describe(0));
        assertTrue(restored.isFilled(0));
        assertFalse(restored.isFilled(1));
    }

    @Test public void incompleteAndMalformedLooksAreSafe() {
        assertEquals("-;-;-",new PlaylumiSavedLooks().serialize());
        assertEquals(0,new PlaylumiSavedLooks().describe(1).indexOf("Nog"));
        PlaylumiSavedLooks loaded=PlaylumiSavedLooks.restore("123456;999999;001100");
        assertTrue(loaded.isFilled(0));
        assertFalse(loaded.isFilled(1));
        assertTrue(loaded.isFilled(2));
        PlaylumiWardrobe w=new PlaylumiWardrobe(19);
        // Items the child does not own are never equipped or purchased.
        int original=w.stars();
        loaded.wear(0,w);
        assertEquals(original,w.stars());
        for(int i=0;i<PlaylumiWardrobe.SLOTS;i++)assertEquals(0,w.equipped(i));
        assertThrows(IllegalArgumentException.class,()->loaded.save(3,w));
        assertThrows(IllegalArgumentException.class,()->loaded.wear(-1,w));
    }
}
