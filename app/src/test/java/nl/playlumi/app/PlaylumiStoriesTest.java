package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiStoriesTest {
    @Test public void allStoriesHaveFourCompleteMeaningfulScenes() {
        for(int id=0;id<PlaylumiStories.NAMES.length;id++) {
            for(int variant=0;variant<2;variant++) {
                PlaylumiStories story=new PlaylumiStories();
                story.start(id);
                for(int step=0;step<4;step++) {
                    PlaylumiStories.Scene scene=story.scene();
                    assertFalse(scene.emoji.isEmpty());
                    assertFalse(scene.prompt.isEmpty());
                    assertFalse(scene.first.isEmpty());
                    assertFalse(scene.second.isEmpty());
                    assertNotEquals(scene.first,scene.second);
                    assertFalse(story.choose(variant).isEmpty());
                    story.next();
                }
                assertTrue(story.finished());
                assertFalse(story.ending().isEmpty());
                assertTrue(story.awardOnce());
                assertFalse(story.awardOnce());
            }
        }
    }
    @Test public void choicesLeadToDifferentFinales() {
        PlaylumiStories a=new PlaylumiStories();
        PlaylumiStories b=new PlaylumiStories();
        a.start(3);b.start(3);
        for(int step=0;step<4;step++) {
            a.choose(0);a.next();
            b.choose(1);b.next();
        }
        assertNotEquals(a.ending(),b.ending());
    }
    @Test public void savedStoryStateResumesWithoutLosingProgress() {
        PlaylumiStories first=new PlaylumiStories();
        first.start(2);
        first.choose(1);first.next();
        first.choose(0);
        PlaylumiStories restored=new PlaylumiStories();
        restored.restore(first.completionMask(),first.currentStory(),first.step(),
                first.secondaryChoices(),first.choice());
        assertEquals(2,restored.currentStory());
        assertEquals(1,restored.step());
        assertEquals(0,restored.choice());
        restored.next();
        assertEquals(2,restored.step());
    }
    @Test public void completionOfOneStoryDoesNotUnlockOthers() {
        PlaylumiStories story=new PlaylumiStories();
        story.start(0);
        for(int i=0;i<4;i++) {story.choose(0);story.next();}
        story.awardOnce();
        assertEquals(1,story.completedCount());
        assertTrue(story.completed(0));
        assertFalse(story.completed(1));
    }
    @Test(expected=IllegalStateException.class)
    public void cannotAdvanceWithoutChoosing() {
        PlaylumiStories story=new PlaylumiStories();story.start(0);story.next();
    }

    @Test public void allTwelveStoryBooksAreConnectedAndSaveOldProgress() {
        assertEquals(12,PlaylumiStories.NAMES.length);
        assertEquals(12,PlaylumiStories.ICONS.length);
        assertEquals(12,PlaylumiStories.ENDINGS_A.length);
        assertEquals(12,PlaylumiStories.ENDINGS_B.length);
        PlaylumiStories stories=new PlaylumiStories();
        stories.restore((1<<0)|(1<<2)|(1<<5),-1,0,0,-1);
        assertEquals(3,stories.completedCount());
        assertTrue(stories.completed(0));
        assertTrue(stories.completed(2));
        assertTrue(stories.completed(5));
        for(int id=6;id<12;id++) {
            assertFalse(stories.completed(id));
            stories.start(id);
            for(int chapter=0;chapter<4;chapter++) {
                assertFalse(stories.scene().prompt.isEmpty());
                stories.choose((chapter+id)%2);
                stories.next();
            }
            assertTrue(stories.awardOnce());
        }
        assertEquals(9,stories.completedCount());
    }
}
