package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class PlaylumiCollectionTest {
    @Test public void sixWorldsEachHaveThreePermanentTreasures(){
        assertEquals(6,PlaylumiCollection.NAMES.length);
        assertEquals(6,PlaylumiCollection.ICONS.length);
        for(int w=0;w<6;w++){
            assertEquals(3,PlaylumiCollection.NAMES[w].length);
            assertEquals(3,PlaylumiCollection.ICONS[w].length);
        }
        assertEquals(0,PlaylumiCollection.unlocked(0));
        assertEquals(1,PlaylumiCollection.unlocked(1));
        assertEquals(1,PlaylumiCollection.unlocked(4));
        assertEquals(2,PlaylumiCollection.unlocked(5));
        assertEquals(3,PlaylumiCollection.unlocked(15));
        assertEquals(15,PlaylumiCollection.advance(14,Integer.MAX_VALUE));
        assertEquals(5,PlaylumiCollection.advance(5,-1));
    }
}
