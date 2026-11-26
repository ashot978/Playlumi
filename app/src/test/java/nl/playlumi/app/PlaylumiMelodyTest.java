package nl.playlumi.app;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public final class PlaylumiMelodyTest {
    @Test public void allAgesHaveFairAudibleTonesAndAreCompletable(){
        Random random=new Random(97);
        for(int age=0;age<3;age++)for(int trial=0;trial<350;trial++){
            PlaylumiMelody melody=PlaylumiMelody.generate(age,random);
            assertEquals(age+2,melody.length());
            for(int i=0;i<melody.length();i++){
                int note=melody.noteAt(i);
                assertTrue(note>=0 && note<PlaylumiMelody.NOTES);
                assertEquals(i==melody.length()-1?2:1,melody.tap(note));
            }
            assertTrue(melody.completed());
            assertEquals(3,melody.tap(0));
        }
    }
    @Test public void ageSpecificLongerConcertsCanRestoreAndComplete(){
        for(int age=0;age<3;age++)for(int variant=0;variant<=age;variant++){
            PlaylumiMelody m=PlaylumiMelody.generate(age,variant,new Random(87));
            assertEquals(variant==2?6:age+2+variant,m.length());
            assertEquals(1,m.tap(m.noteAt(0)));
            m=PlaylumiMelody.restore(m.sequence(),m.progress());
            while(!m.completed())m.tap(m.noteAt(m.progress()));
            assertEquals(m.length(),m.progress());
        }
    }
    @Test public void wrongAnswersOnlyRestartTune(){
        PlaylumiMelody melody=PlaylumiMelody.restore(new int[]{0,1,3},0);
        assertEquals(1,melody.tap(0));
        assertEquals(0,melody.tap(2));
        assertEquals(0,melody.progress());
        assertEquals(1,melody.tap(0));
        assertEquals(1,melody.tap(1));
        assertEquals(2,melody.tap(3));
        assertEquals(3,melody.progress());
    }
    @Test public void reopeningScreenCanResumeSameMelody(){
        PlaylumiMelody before=PlaylumiMelody.restore(new int[]{1,2,0,3},0);
        before.tap(1);
        PlaylumiMelody after=PlaylumiMelody.restore(before.sequence(),before.progress());
        assertEquals(1,after.progress());
        after.tap(2);
        after.tap(0);
        assertEquals(2,after.tap(3));
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidLengthRejected(){PlaylumiMelody.restore(new int[]{0},0);}
    @Test(expected=IllegalArgumentException.class)
    public void invalidNoteRejected(){new PlaylumiMelodyTest().badNote();}
    private void badNote(){PlaylumiMelody.restore(new int[]{0,5},0);}
}
