package nl.playlumi.app;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiDailyRoutesTest {
    @Test public void everyGoalOpensAQuizThatCanActuallyAdvanceIt(){
        for(long date=20000;date<20365;date++)for(int age=0;age<3;age++){
            PlaylumiDailyChallenges day=new PlaylumiDailyChallenges(date,Long.MIN_VALUE,0,0,0,0,0);
            for(int goal=0;goal<3;goal++){
                PlaylumiGameCatalog.Entry entry=PlaylumiDailyRoutes.choose(age,day,goal,Collections.emptyList());
                assertNotNull(entry);assertEquals(PlaylumiGameCatalog.QUIZ,entry.kind);
                assertEquals(entry.id,PlaylumiDailyRoutes.choose(age,day,goal,null).id);
                PlaylumiDailyChallenges fresh=new PlaylumiDailyChallenges(date,Long.MIN_VALUE,0,0,0,0,0);
                fresh.correct(entry.world,goal==2);
                assertEquals(1,fresh.progress(goal));
            }
        }
    }
    @Test public void unfinishedMatchingGamesWinAndWrongWorldOrAgeNeverLeaks(){
        PlaylumiDailyChallenges day=new PlaylumiDailyChallenges(20000,Long.MIN_VALUE,0,0,0,0,0);
        PlaylumiGameCatalog.Entry first=PlaylumiDailyRoutes.choose(0,day,0,null),other=null;
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(0))
            if(entry.kind==PlaylumiGameCatalog.QUIZ&&entry.world!=first.world){other=entry;break;}
        assertNotNull(other);
        assertEquals(first.id,PlaylumiDailyRoutes.choose(0,day,0,Arrays.asList("0:0:12:8",other.id,first.id)).id);
        assertEquals(other.id,PlaylumiDailyRoutes.choose(0,day,2,Arrays.asList(other.id,first.id)).id);
        assertNull(PlaylumiDailyRoutes.choose(0,day,-1,null));
        assertNull(PlaylumiDailyRoutes.choose(0,day,3,null));
    }
    @Test public void completedGoalsStayPlayableWithoutAnotherBonus(){
        PlaylumiDailyChallenges day=new PlaylumiDailyChallenges(20000,20000,4,4,1,7,1);
        for(int goal=0;goal<3;goal++){
            PlaylumiGameCatalog.Entry entry=PlaylumiDailyRoutes.choose(2,day,goal,null);
            assertNotNull(entry);assertEquals(0,day.correct(entry.world,true));
        }
    }
    @Test public void cityPresentationOnlyUsesEarnedMilestones(){
        int[] stats=new int[PlaylumiAchievements.METRICS];
        assertEquals(0,PlaylumiCityProgress.builtMask(1L));
        assertEquals(0,PlaylumiCityProgress.next(0,stats));
        stats[12]=2;assertEquals(1,PlaylumiCityProgress.next(0,stats));
        long mask=(1L<<5)|(1L<<24);
        assertEquals(3,PlaylumiCityProgress.builtMask(mask));
        assertEquals(2,PlaylumiCityProgress.unseen(mask,1));
        assertEquals(3,PlaylumiCityProgress.progress(1,mask,new int[0]));
        assertEquals(0,PlaylumiCityProgress.unseen(mask,3));
        assertEquals(-1,PlaylumiCityProgress.next(-1L,stats));
    }
}
