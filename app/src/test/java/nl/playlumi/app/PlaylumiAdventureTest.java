package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaylumiAdventureTest {
    @Test public void onlyNextAdventureIsUnlockedAndEachGivesRewardOnce() {
        PlaylumiAdventure story=new PlaylumiAdventure();
        for(int w=0;w<3;w++) {
            assertTrue(story.available(w,0));
            assertFalse(story.available(w,1));
            for(int i=0;i<PlaylumiAdventure.STAGES;i++) {
                assertTrue(story.available(w,i));
                assertTrue(story.complete(w,i));
                assertFalse(story.complete(w,i));
                assertEquals(i+1,story.progress(w));
                if(i+1<PlaylumiAdventure.STAGES)assertTrue(story.available(w,i+1));
            }
            assertTrue(story.worldFinished(w));
        }
    }
    @Test public void progressRestoresAndWorldsRemainIndependent() {
        PlaylumiAdventure story=new PlaylumiAdventure();
        story.restore(0,3);
        story.restore(1,0);
        story.restore(2,1);
        assertEquals(3,story.progress(0));
        assertEquals(0,story.progress(1));
        assertEquals(1,story.progress(2));
        assertTrue(story.completed(0,2));
        assertFalse(story.completed(0,3));
        assertTrue(story.available(0,3));
        assertFalse(story.available(0,4));
    }
    @Test public void everyAdventureMapsToValidLearningGame() {
        PlaylumiAdventure story=new PlaylumiAdventure();
        for(int w=0;w<PlaylumiAdventure.WORLDS;w++) {
            assertEquals(6,PlaylumiAdventure.TITLES[w].length);
            for(int stage=0;stage<PlaylumiAdventure.STAGES;stage++) {
                int mode=story.category(w,stage);
                assertTrue(mode>=0 && mode<LearningGame.GAME_NAMES.length);
                assertTrue(story.variant(stage)>=0 && story.variant(stage)<5);
                assertFalse(PlaylumiAdventure.ICONS[w][stage].isEmpty());
                assertFalse(PlaylumiAdventure.TITLES[w][stage].isEmpty());
            }
        }
    }
    @Test(expected=IllegalStateException.class)
    public void cannotSkipToLockedAdventure() {new PlaylumiAdventure().complete(2,3);}
}
