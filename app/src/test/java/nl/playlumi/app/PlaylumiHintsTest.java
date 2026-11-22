package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

/** Hints never expose the right answer or change the underlying game. */
public final class PlaylumiHintsTest {
    @Test public void allPossibleCorrectPositionsKeepTwoAnswers() {
        for(int options=2;options<=16;options++) {
            for(int correct=0;correct<options;correct++) {
                int hidden=0,removed=0;
                while(true) {
                    int next=PlaylumiHints.nextWrongOption(correct,options,hidden);
                    if(next<0)break;
                    assertNotEquals(correct,next);
                    assertEquals(0,hidden&(1<<next));
                    hidden|=1<<next;
                    removed++;
                    assertTrue(removed<=options-2);
                }
                assertEquals(Math.max(0,options-2),removed);
                assertEquals(0,hidden&(1<<correct));
                assertEquals(2,options-Integer.bitCount(hidden));
                assertEquals(-1,PlaylumiHints.nextWrongOption(
                        correct,options,hidden));
            }
        }
    }

    @Test public void hintDoesNotSolveGameOrChangeCorrectAnswer() {
        LearningGame.Round round=new LearningGame.Round("Wat zie je?","🐶",
                0xff112233,java.util.Arrays.asList("Kat","Hond","Vis","Koe"),
                java.util.Arrays.asList(1,2,3,4),1);
        int original=round.correctIndex;
        int hidden=0;
        while(true) {
            int next=PlaylumiHints.nextWrongOption(
                    round.correctIndex,round.answers.size(),hidden);
            if(next<0)break;
            hidden|=1<<next;
            assertFalse(round.isSolved());
            assertEquals(original,round.correctIndex);
        }
        assertEquals("Hond",round.answers.get(original));
        assertTrue(round.choose(original));
        assertTrue(round.isSolved());
    }

    @Test(expected=IllegalArgumentException.class)
    public void cannotRemoveCorrectAnswer() {
        PlaylumiHints.nextWrongOption(1,4,1<<1);
    }
    @Test(expected=IllegalArgumentException.class)
    public void cannotUseInvalidMask() {
        PlaylumiHints.nextWrongOption(0,3,1<<7);
    }
    @Test(expected=IllegalArgumentException.class)
    public void cannotUseInvalidCorrectIndex() {
        PlaylumiHints.nextWrongOption(3,3,0);
    }
    @Test(expected=IllegalArgumentException.class)
    public void cannotUseSingleAnswer() {
        PlaylumiHints.nextWrongOption(0,1,0);
    }
}
