package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;
public final class PlaylumiCityDailyTest {
    @Test public void dailyRewardsOnceAndResetWithoutLosingFinishedSets(){
        PlaylumiDailyChallenges d=new PlaylumiDailyChallenges(20000,Long.MIN_VALUE,0,0,0,0,0);
        assertNotEquals(d.world(0),d.world(1));
        int reward=0;
        for(int n=0;n<4;n++)reward+=d.correct(d.world(0),n==3);
        assertEquals(35,reward);
        for(int n=0;n<4;n++)reward+=d.correct(d.world(1),false);
        assertEquals(70,reward);assertEquals(3,d.completed());assertEquals(1,d.completedSets());
        assertEquals(0,d.correct(d.world(0),true));
        PlaylumiDailyChallenges restored=new PlaylumiDailyChallenges(19999,d.day,4,4,1,d.mask(),1);
        assertEquals(d.day,restored.day);assertEquals(0,restored.correct(d.world(0),true));
        PlaylumiDailyChallenges next=new PlaylumiDailyChallenges(20001,d.day,4,4,1,d.mask(),1);
        assertEquals(0,next.completed());assertEquals(1,next.completedSets());
    }
    @Test public void worldsAvailableToAllAges(){
        for(long day=20000;day<20100;day++)for(int age=0;age<3;age++){
            PlaylumiDailyChallenges d=new PlaylumiDailyChallenges(day,Long.MIN_VALUE,0,0,0,0,0);
            for(int i=0;i<2;i++){
                boolean found=false;
                for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age))
                    if(e.world==d.world(i)&&e.kind==PlaylumiGameCatalog.QUIZ)found=true;
                assertTrue(found);
            }
        }
    }
    @Test public void achievementGiftsArePermanentAndDoNotSpendStars(){
        PlaylumiWardrobe w=new PlaylumiWardrobe(500);
        assertEquals(PlaylumiWardrobe.ACHIEVEMENT_REQUIRED,w.buyOrEquip(PlaylumiWardrobe.OUTFITS,6));
        assertTrue(PlaylumiCityRewards.grant(w,1L<<24));
        assertFalse(PlaylumiCityRewards.grant(w,1L<<24));
        assertEquals(500,w.stars());
        assertEquals(PlaylumiWardrobe.EQUIPPED,w.buyOrEquip(PlaylumiWardrobe.OUTFITS,6));
        assertEquals(500,w.stars());
        PlaylumiWardrobe old=new PlaylumiWardrobe();
        old.restoreSlot(PlaylumiWardrobe.FRIENDS,1|(1<<5),5);
        assertEquals(PlaylumiWardrobe.EQUIPPED,old.buyOrEquip(PlaylumiWardrobe.FRIENDS,5));
    }
    @Test public void replayCountersDoNotTreatPartialRoundsAsCompleted(){
        assertEquals(0,PlaylumiCompletion.nextRoundCount(0,7,8));
        assertEquals(1,PlaylumiCompletion.nextRoundCount(0,8,8));
        assertEquals(3,PlaylumiCompletion.nextRoundCount(2,8,8));
        assertEquals("Brons",PlaylumiCompletion.mastery(3));
        assertEquals("Diamant",PlaylumiCompletion.mastery(20));
        assertEquals(6,PlaylumiCompletion.nextMastery(3));
    }
    @Test public void visibleDailyRewardTotalsOnlyDescribeExistingClaims(){
        PlaylumiDailyChallenges day=new PlaylumiDailyChallenges(42,42,0,0,0,0,0);
        assertEquals(70,day.totalReward());assertEquals(0,day.claimedReward());
        for(int i=0;i<4;i++)day.correct(day.world(0),false);
        assertEquals(15,day.claimedReward());
        for(int i=0;i<4;i++)day.correct(day.world(1),false);
        assertEquals(30,day.claimedReward());
        day.correct(day.world(0),true);
        assertEquals(70,day.claimedReward());
        PlaylumiDailyChallenges restored=new PlaylumiDailyChallenges(42,42,4,4,1,7,1);
        assertEquals(70,restored.claimedReward());assertEquals(0,restored.correct(restored.world(0),true));
        assertEquals(0,new PlaylumiDailyChallenges(43,42,4,4,1,7,1).claimedReward());
    }
}
