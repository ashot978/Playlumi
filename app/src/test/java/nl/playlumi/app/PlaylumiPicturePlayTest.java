package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

/** Spoken previews should match the real choices for all three ages. */
public final class PlaylumiPicturePlayTest {
    @Test public void eachPictureHasAnAccurateSpeakableHint() {
        for(int age=0;age<3;age++) {
            assertEquals(6,PlaylumiPicturePlay.COUNT);
            for(int item=0;item<PlaylumiPicturePlay.COUNT;item++) {
                String name=PlaylumiPicturePlay.label(age,item);
                String hint=PlaylumiPicturePlay.spokenLabel(age,item);
                assertEquals("Dit is "+name+". Tik om te spelen.",hint);
                assertTrue(hint.contains(name));
                assertFalse(hint.contains("null"));
                assertFalse(name.trim().isEmpty());
                assertEquals("Luister naar "+name+" zonder het spel te starten",
                        PlaylumiPicturePlay.listenDescription(age,item));
            }
        }
    }
    @Test public void eachAgeHasSixDistinctAccessiblePlayAndListenOptions() {
        for(int age=0;age<3;age++) {
            java.util.Set<String> choices=new java.util.HashSet<>();
            for(int item=0;item<PlaylumiPicturePlay.COUNT;item++) {
                String title=PlaylumiPicturePlay.label(age,item);
                assertTrue(choices.add(title));
                assertTrue(PlaylumiPicturePlay.listenDescription(age,item)
                        .contains(title));
            }
            assertEquals(6,choices.size());
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidListenSelectionIsRejected() {
        PlaylumiPicturePlay.listenDescription(2,-1);
    }

    @Test public void stillUsesTwoColumnsOnPhonesThreeOnTablets() {
        assertEquals(2,PlaylumiPicturePlay.columns(320));
        assertEquals(2,PlaylumiPicturePlay.columns(599));
        assertEquals(3,PlaylumiPicturePlay.columns(600));
        assertEquals(3,PlaylumiPicturePlay.columns(880));
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidAgeCannotBeSpoken() {
        PlaylumiPicturePlay.spokenLabel(3,0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidChoiceCannotBeSpoken() {
        PlaylumiPicturePlay.spokenLabel(1,PlaylumiPicturePlay.COUNT);
    }
}
