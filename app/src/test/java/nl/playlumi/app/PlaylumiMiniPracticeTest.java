package nl.playlumi.app;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiMiniPracticeTest {
    @Test public void fourThemesAndAllLegacyCardsRemainAvailable(){
        Random r=new Random(119);
        Set<String> themes=new HashSet<>(),animals=new HashSet<>();
        for(int age=0;age<3;age++)for(int i=0;i<300;i++){
            PlaylumiMiniChallenge c=PlaylumiMiniChallenge.generate(PlaylumiMiniChallenge.MEMORY,age,r);
            themes.add(PlaylumiMiniPractice.theme(c.choices));animals.addAll(c.choices);
            assertEquals(age==0?3:age==1?4:6,c.pairCount());
            for(String a:c.choices){assertFalse(PlaylumiMiniPractice.animalName(a).isEmpty());assertEquals(2,Collections.frequency(c.choices,a));}
        }
        assertEquals(4,themes.size());assertEquals(32,animals.size());
        List<String> old=Arrays.asList("🐶","🐱","🐶","🐸","🐱","🐸");
        PlaylumiMiniChallenge c=PlaylumiMiniChallenge.restore(2,0,"🐾",old,-1,5,1);
        assertEquals(old,c.choices);assertTrue(c.matched(0));assertTrue(c.matched(2));assertEquals(1,c.firstOpen());
        assertEquals("Dieren",PlaylumiMiniPractice.theme(old));
        assertEquals("Gesloten kaart 2",PlaylumiMiniPractice.cardLabel(1,false,false,"🐱"));
    }
    @Test public void visualTrailsNeverRevealFutureMelodyNotes(){
        PlaylumiMelody m=PlaylumiMelody.restore(new int[]{0,2,1,3},0);
        assertEquals("○  ○  ○  ○",m.playedTrail());
        assertEquals(1,m.tap(0));assertEquals("🐸  ○  ○  ○",m.playedTrail());
        PlaylumiMelody restored=PlaylumiMelody.restore(m.sequence(),m.progress());
        assertEquals(m.playedTrail(),restored.playedTrail());
        assertEquals(0,m.tap(3));assertEquals("○  ○  ○  ○",m.playedTrail());
        assertEquals("●  ●  ○",PlaylumiMiniPractice.trail(2,3,"●","○"));
    }
    @Test public void compactBoardsHaveComfortableTargets(){
        for(int w:new int[]{240,300,340,400,700})for(int h:new int[]{400,600,800,1100}){
            for(int n:new int[]{6,8,12}){
                assertTrue(PlaylumiMiniPractice.memoryColumns(w,n)>=2);
                assertTrue(PlaylumiMiniPractice.memoryCardHeight(w,h,n)>=54);
                assertTrue(PlaylumiMiniPractice.memoryCardHeight(w,h,n)<=104);
            }
            for(int n=3;n<=12;n++){
                int c=PlaylumiMiniPractice.bubbleColumns(w,n),size=PlaylumiMiniPractice.bubbleSize(w,h,n);
                assertTrue(c*size+c*8<=w);assertTrue(size>=48);
            }
        }
        assertEquals(3,PlaylumiMiniPractice.memoryColumns(340,6));
        assertEquals(4,PlaylumiMiniPractice.memoryColumns(340,8));
        assertEquals(4,PlaylumiMiniPractice.memoryColumns(340,12));
    }
}
