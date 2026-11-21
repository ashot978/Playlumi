package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiGardenTest {
    @Test public void everyPlantGrowsThroughFourMeaningfulActions(){
        PlaylumiGarden garden=new PlaylumiGarden();
        for(int plant=0;plant<PlaylumiGarden.COUNT;plant++){
            assertEquals(0,garden.stage(plant));
            assertEquals("🫘",garden.picture(plant));
            for(int step=0;step<4;step++){
                assertEquals(step%2, garden.nextAction(plant));
                assertEquals(-1,garden.grow(plant,1-step%2));
                assertEquals(step==3?1:0,garden.grow(plant,step%2));
            }
            assertTrue(garden.bloomed(plant));
            assertEquals(2,garden.grow(plant,PlaylumiGarden.WATER));
        }
        assertEquals(PlaylumiGarden.COUNT,garden.bloomedCount());
        assertEquals((1<<PlaylumiGarden.COUNT)-1,garden.rewardMask());
    }
    @Test public void newCollectionRestoresEveryBloomAndDoesNotRepay(){
        PlaylumiGarden first=new PlaylumiGarden();
        for(int step=0;step<4;step++)first.grow(9,step%2);
        first.grow(2,0);
        PlaylumiGarden later=new PlaylumiGarden();
        later.restoreRewards(first.rewardMask());
        for(int plant=0;plant<PlaylumiGarden.COUNT;plant++)
            later.restorePlant(plant,first.stage(plant));
        assertEquals(1,later.bloomedCount());
        assertEquals(2,later.growingCount());
        assertEquals(2,later.grow(9,1));
        assertEquals(0,later.stage(1));
        assertTrue(later.awarded(9));
    }
    @Test public void everyPlantHasFactNameAndPicture(){
        assertEquals(PlaylumiGarden.COUNT,PlaylumiGarden.NAMES.length);
        assertEquals(PlaylumiGarden.COUNT,PlaylumiGarden.FACTS.length);
        assertEquals(PlaylumiGarden.COUNT,PlaylumiGarden.ICONS.length);
        for(int plant=0;plant<PlaylumiGarden.COUNT;plant++){
            assertTrue(PlaylumiGarden.NAMES[plant].length()>=3);
            assertTrue(PlaylumiGarden.FACTS[plant].length()>=15);
            assertFalse(PlaylumiGarden.ICONS[plant].isEmpty());
        }
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidPlantRejected(){new PlaylumiGarden().grow(12,PlaylumiGarden.WATER);}
}
