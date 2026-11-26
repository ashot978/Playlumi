package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;
public final class PlaylumiPremiumTest {
    @Test public void bannerDoesNotConsumeShortScreens(){
        assertEquals(50,PlaylumiPremiumLayout.quizBannerHeight(400,true));
        assertTrue(PlaylumiPremiumLayout.quizBannerHeight(800,true)<=84);
        for(int i=0;i<24;i++)assertFalse(PlaylumiPremiumLayout.emblem(i,0).isEmpty());
    }
    @Test public void filterOnlyChangesVisibility(){
        assertTrue(PlaylumiPremiumLayout.showAchievement(0,true));assertTrue(PlaylumiPremiumLayout.showAchievement(0,false));
        assertTrue(PlaylumiPremiumLayout.showAchievement(1,true));assertFalse(PlaylumiPremiumLayout.showAchievement(1,false));
        assertTrue(PlaylumiPremiumLayout.showAchievement(2,false));assertFalse(PlaylumiPremiumLayout.showAchievement(2,true));
    }
    @Test public void speechDoesNotReadHiddenClockOrPatternValues(){
        assertEquals("Hoe laat is het ?",PlaylumiSpeechText.quiz(16,0,"Hoe laat is het?","clock:9:30"));
        assertEquals("Welke kleur zie je ?",PlaylumiSpeechText.quiz(LearningGame.COLORS,0,"Welke kleur zie je?","rood"));
        String math=PlaylumiSpeechText.quiz(ExtraGames.MATH,1,"Reken uit","8 − 3 = ?");
        assertTrue(math.contains("8 min 3 is gelijk aan"));
        assertTrue(PlaylumiSpeechText.quiz(22,0,"Vergelijk","7 cm | 72 mm").contains("7 centimeter tegenover 72 millimeter"));
    }
}
