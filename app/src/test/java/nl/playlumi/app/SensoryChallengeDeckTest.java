package nl.playlumi.app;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import static org.junit.Assert.*;

/** Checks 2 games x 3 difficulty levels x 300 actual, playable layouts. */
public final class SensoryChallengeDeckTest {
    @Test public void threeHundredDifferentBubbleTasksPerDifficulty(){
        for(int level=0;level<3;level++){
            SensoryChallengeDeck<PlaylumiBubbles> deck=
                    SensoryChallengeDeck.bubbles(level,20261121L+level);
            Set<String> seen=new HashSet<>();
            for(int n=0;n<300;n++){
                PlaylumiBubbles b=deck.next();
                assertEquals(level,b.level);
                assertTrue(b.target>=2 && b.total>b.target && b.total<=12);
                assertTrue(seen.add(SensoryChallengeDeck.bubbleKey(b)));
                PlaylumiBubbles restored=PlaylumiBubbles.restore(
                        level,b.target,b.total,b.mask(),b.layoutCode());
                assertEquals(SensoryChallengeDeck.bubbleKey(b),
                        SensoryChallengeDeck.bubbleKey(restored));
                for(int i=0;i<b.total;i++){
                    assertTrue(b.paletteIndex(i)>=0 && b.paletteIndex(i)<6);
                    if(i<b.target)assertTrue(b.pop(i));
                }
                assertTrue(b.solved());
                assertFalse(b.pop(b.target));
            }
            assertEquals(300,seen.size());
            assertFalse(deck.next().solved());
            assertEquals(299,deck.remaining());
        }
    }

    @Test public void threeHundredDifferentMusicTasksPerDifficulty(){
        for(int level=0;level<3;level++){
            SensoryChallengeDeck<PlaylumiMelody> deck=
                    SensoryChallengeDeck.melodies(level,20261122L+level);
            Set<String> seen=new HashSet<>();
            for(int n=0;n<300;n++){
                PlaylumiMelody m=deck.next();
                assertEquals(level+2,m.length());
                assertTrue(seen.add(SensoryChallengeDeck.melodyKey(m)));
                Set<Integer> positions=new HashSet<>();
                for(int b:m.buttonOrder())assertTrue(positions.add(b));
                assertEquals(4,positions.size());
                PlaylumiMelody restored=PlaylumiMelody.restore(
                        m.sequence(),m.progress(),m.buttonOrder());
                assertEquals(SensoryChallengeDeck.melodyKey(m),
                        SensoryChallengeDeck.melodyKey(restored));
                for(int i=0;i<m.length();i++)
                    assertEquals(i==m.length()-1?2:1,m.tap(m.noteAt(i)));
                assertTrue(m.completed());
            }
            assertEquals(300,seen.size());
            assertFalse(deck.next().completed());
        }
    }

    @Test public void sameSeedAndCursorResumeExactly(){
        for(int level=0;level<3;level++){
            SensoryChallengeDeck<PlaylumiBubbles> before=
                    SensoryChallengeDeck.bubbles(level,90000L+level);
            for(int i=0;i<13;i++)before.next();
            SensoryChallengeDeck<PlaylumiBubbles> after=
                    SensoryChallengeDeck.bubbles(level,before.seed());
            after.skip(before.dealt());
            assertEquals(SensoryChallengeDeck.bubbleKey(before.next()),
                    SensoryChallengeDeck.bubbleKey(after.next()));
            SensoryChallengeDeck<PlaylumiMelody> music=
                    SensoryChallengeDeck.melodies(level,400L+level);
            for(int i=0;i<9;i++)music.next();
            SensoryChallengeDeck<PlaylumiMelody> restored=
                    SensoryChallengeDeck.melodies(level,music.seed());
            restored.skip(music.dealt());
            assertEquals(SensoryChallengeDeck.melodyKey(music.next()),
                    SensoryChallengeDeck.melodyKey(restored.next()));
        }
    }

    @Test public void newOpeningsActuallyReshuffle(){
        SensoryChallengeDeck<PlaylumiBubbles> first=
                SensoryChallengeDeck.bubbles(0,21L);
        SensoryChallengeDeck<PlaylumiBubbles> second=
                SensoryChallengeDeck.bubbles(0,22L);
        int changed=0;
        for(int i=0;i<20;i++)if(!SensoryChallengeDeck.bubbleKey(first.next())
                .equals(SensoryChallengeDeck.bubbleKey(second.next())))changed++;
        assertTrue(changed>=16);
        SensoryChallengeDeck<PlaylumiMelody> a=SensoryChallengeDeck.melodies(0,12L);
        SensoryChallengeDeck<PlaylumiMelody> b=SensoryChallengeDeck.melodies(0,13L);
        changed=0;
        for(int i=0;i<20;i++)if(!SensoryChallengeDeck.melodyKey(a.next())
                .equals(SensoryChallengeDeck.melodyKey(b.next())))changed++;
        assertTrue(changed>=16);
    }

    @Test public void scoresAndPreviousSavesStillWork(){
        PlaylumiMelody m=PlaylumiMelody.restore(
                new int[]{0,1,3},0,new int[]{3,1,0,2});
        assertEquals(1,m.tap(0));
        assertEquals(0,m.tap(2));
        assertEquals(0,m.progress());
        assertEquals(1,m.tap(0));
        assertEquals(1,m.tap(1));
        assertEquals(2,m.tap(3));
        assertTrue(m.completed());
        int[] order=m.buttonOrder();
        order[0]=999;
        assertEquals(3,m.buttonOrder()[0]);
        PlaylumiBubbles bubbles=PlaylumiBubbles.generate(1,new Random(7));
        assertTrue(bubbles.pop(0));
        PlaylumiBubbles rotated=PlaylumiBubbles.restore(1,
                bubbles.target,bubbles.total,bubbles.mask(),bubbles.layoutCode());
        assertEquals(1,rotated.popped());
        assertEquals(bubbles.paletteIndex(0),rotated.paletteIndex(0));
    }

    @Test public void invalidStatesAreRejected(){
        assertThrows(IllegalArgumentException.class,
                ()->SensoryChallengeDeck.bubbles(3,0));
        assertThrows(IllegalArgumentException.class,
                ()->SensoryChallengeDeck.melodies(-1,0));
        assertThrows(IllegalArgumentException.class,
                ()->SensoryChallengeDeck.melodies(0,1).skip(301));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiBubbles.restore(0,2,4,0,46656));
        assertThrows(IllegalArgumentException.class,
                ()->PlaylumiMelody.restore(new int[]{0,1},0,
                        new int[]{0,0,2,3}));
    }
}
