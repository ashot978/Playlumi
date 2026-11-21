package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

/** New decorative motion must be bounded and never required for game results. */
public final class PlaylumiBubbleMotionTest {
    @Test public void bubblesAlwaysRemainWithinGentleComfortableRange() {
        Random random=new Random(382602);
        for(int i=0;i<16000;i++){
            long time=i==0?Long.MIN_VALUE:i==1?Long.MAX_VALUE:
                    random.nextInt(200000)-10000;
            PlaylumiBubbleMotion.Frame frame=PlaylumiBubbleMotion.sample(
                    time,i%18,false);
            assertTrue(Float.isFinite(frame.lift));
            assertTrue(Float.isFinite(frame.tilt));
            assertTrue(Float.isFinite(frame.scale));
            assertTrue(Float.isFinite(frame.sparkle));
            assertTrue(frame.lift>=-5.01f && frame.lift<=5.01f);
            assertTrue(frame.tilt>=-2.11f && frame.tilt<=2.11f);
            assertTrue(frame.scale>=.98f && frame.scale<=1.02f);
            assertTrue(frame.sparkle>=0f && frame.sparkle<=1f);
        }
    }

    @Test public void lessMotionStopsEveryBubbleAndKeepsAnAttractiveStaticScene() {
        for(long time:new long[]{Long.MIN_VALUE,0,100,999,4800,Long.MAX_VALUE}){
            PlaylumiBubbleMotion.Frame f=PlaylumiBubbleMotion.sample(time,4,true);
            assertEquals(0f,f.lift,0);
            assertEquals(0f,f.tilt,0);
            assertEquals(1f,f.scale,0);
            assertEquals(.5f,f.sparkle,0);
        }
    }

    @Test public void bubblesHaveDistinctPhasesAndLoopWithoutDrifting() {
        PlaylumiBubbleMotion.Frame a=PlaylumiBubbleMotion.sample(1300,0,false);
        PlaylumiBubbleMotion.Frame b=PlaylumiBubbleMotion.sample(1300,1,false);
        assertNotEquals(a.lift,b.lift,.0001f);
        assertEquals(a.lift,PlaylumiBubbleMotion.sample(
                1300+PlaylumiBubbleMotion.LOOP_MILLIS*500,0,false).lift,.0001f);
        assertEquals(a.tilt,PlaylumiBubbleMotion.sample(
                1300+PlaylumiBubbleMotion.LOOP_MILLIS*500,0,false).tilt,.0001f);
    }
}
