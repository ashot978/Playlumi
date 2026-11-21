package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public final class PlaylumiBubblesTest {
    @Test public void everyAgeProducesPlayableUniqueCountingGoals() {
        Random r=new Random(1019);
        for(int age=0;age<3;age++)for(int n=0;n<500;n++){
            PlaylumiBubbles b=PlaylumiBubbles.generate(age,r);
            assertTrue(b.target>=2);
            assertTrue(b.target<b.total);
            assertTrue(b.total<=12);
            assertEquals(0,b.popped());
            for(int i=0;i<b.target;i++){
                assertTrue(b.pop(i));
                assertFalse(b.pop(i));
            }
            assertTrue(b.solved());
            assertFalse(b.pop(b.target));
            assertEquals(b.target,b.popped());
        }
    }
    @Test public void canResumeSameBubblesAfterRotation() {
        PlaylumiBubbles b=PlaylumiBubbles.generate(1,new Random(4));
        b.pop(0);
        b.pop(1);
        PlaylumiBubbles restored=PlaylumiBubbles.restore(1,b.target,b.total,b.mask());
        assertEquals(2,restored.popped());
        assertTrue(restored.isPopped(0));
        assertTrue(restored.isPopped(1));
        for(int index=2;restored.popped()<restored.target;index++)
            restored.pop(index);
        assertTrue(restored.solved());
    }
    @Test(expected=IllegalArgumentException.class)
    public void refuseOutOfRangeBubbleIndex() {
        PlaylumiBubbles b=PlaylumiBubbles.generate(0,new Random(4));
        b.pop(b.total);
    }
    @Test(expected=IllegalArgumentException.class)
    public void refuseInvalidRestoreWithTooManyPops() {
        PlaylumiBubbles.restore(0,2,5,31);
    }
}
