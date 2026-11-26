package nl.playlumi.app;
import java.util.HashSet;
import java.util.Random;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiDaytimeClockTest {
    @Test public void allHoursAndMinutesHaveUnambiguousSpokenTimes(){
        Random random=new Random(92);
        for(int level=0;level<3;level++)for(int h=0;h<24;h++)for(int m=0;m<60;m+=5){
            LearningGame.Round r=PlaylumiDaytimeClock.round(h,m,level,random);
            String right=String.format(Locale.ROOT,"%02d:%02d",h,m);
            String opposite=String.format(Locale.ROOT,"%02d:%02d",(h+12)%24,m);
            assertEquals(right,r.answers.get(r.correctIndex));
            assertTrue(r.answers.contains(opposite));
            assertTrue(r.display.contains(PlaylumiDaytimeClock.period(h)));
            assertTrue(r.display.startsWith((h%12==0?12:h%12)+" uur"));
            assertEquals(level==0?3:level==1?4:6,r.answers.size());
            assertEquals(r.answers.size(),new HashSet<>(r.answers).size());
            assertFalse(r.display.contains(right));
            for(int i=0;i<r.answers.size();i++)if(i!=r.correctIndex)assertFalse(r.choose(i));
            assertTrue(r.choose(r.correctIndex));assertFalse(r.choose(r.correctIndex));
        }
    }
    @Test public void midnightNoonAndEveningAreDistinguished(){
        assertEquals("12 uur\n🌙 ’s nachts",PlaylumiDaytimeClock.display(0,0));
        assertEquals("12 uur\n☀️ ’s middags",PlaylumiDaytimeClock.display(12,0));
        assertEquals("9 uur en 35 minuten\n🌆 ’s avonds",PlaylumiDaytimeClock.display(21,35));
        assertEquals("9 uur en 35 minuten\n🌅 ’s ochtends",PlaylumiDaytimeClock.display(9,35));
        assertEquals("’s nachts",PlaylumiDaytimeClock.period(5));
        assertEquals("’s avonds",PlaylumiDaytimeClock.period(18));
    }
}
