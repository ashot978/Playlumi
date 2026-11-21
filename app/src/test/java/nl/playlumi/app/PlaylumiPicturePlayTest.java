package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.*;

public final class PlaylumiPicturePlayTest {
    @Test public void sixIllustratedChoicesExistForEachAge() {
        for(int age=0;age<3;age++){
            Set<Integer> unique=new HashSet<>();
            for(int i=0;i<PlaylumiPicturePlay.COUNT;i++){
                int mode=PlaylumiPicturePlay.game(age,i);
                assertTrue(mode>=0 && mode<LearningGame.GAME_NAMES.length);
                assertTrue(unique.add(mode));
                assertFalse(PlaylumiPicturePlay.label(age,i).isEmpty());
                assertFalse(PlaylumiPicturePlay.icon(age,i).isEmpty());
                assertEquals(age,PlaylumiPicturePlay.difficulty(age));
                assertEquals(0,PlaylumiPicturePlay.variant(age,i));
            }
            assertEquals(6,unique.size());
        }
    }

    @Test public void oneTapCreatesRealPlayableRoundsInAllEighteenShortcuts() {
        LearningGame engine=new LearningGame(new Random(129));
        for(int age=0;age<3;age++)
            for(int i=0;i<PlaylumiPicturePlay.COUNT;i++){
                engine.configure(PlaylumiPicturePlay.game(age,i),
                        PlaylumiPicturePlay.variant(age,i),
                        PlaylumiPicturePlay.difficulty(age));
                assertEquals(300,engine.questionPoolSize());
                for(int n=0;n<8;n++){
                    LearningGame.Round round=engine.round();
                    assertNotNull(round);
                    assertFalse(round.prompt.isEmpty());
                    assertTrue(round.correctIndex>=0);
                    assertTrue(round.correctIndex<round.answers.size());
                    assertTrue(round.choose(round.correctIndex));
                    assertTrue(round.isSolved());
                    engine.nextRound();
                }
            }
    }

    @Test public void layoutIsEasyToTapOnPhoneAndTablet() {
        assertEquals(2,PlaylumiPicturePlay.columns(320));
        assertEquals(2,PlaylumiPicturePlay.columns(599));
        assertEquals(3,PlaylumiPicturePlay.columns(600));
        assertEquals(3,PlaylumiPicturePlay.columns(850));
    }

    @Test public void invalidAgeAndShortcutCannotStartAChildGame() {
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiPicturePlay.game(-1,0));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiPicturePlay.game(3,0));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiPicturePlay.icon(0,6));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiPicturePlay.label(1,-1));
    }
}
