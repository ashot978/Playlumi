package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiInteractiveDepthTest {
    @Test public void oldSeedIsNotReinterpreted(){
        for(int kind=0;kind<4;kind++)for(int level=0;level<3;level++)for(long seed=0;seed<60;seed++){
            PlaylumiInteractive old=new PlaylumiInteractive(kind,level,seed,1);
            PlaylumiInteractive restored=new PlaylumiInteractive(kind,level,seed,1,PlaylumiInteractive.LEGACY);
            assertEquals(old.options(),restored.options());assertEquals(old.expected(),restored.expected());
            for(int j=0;j<old.total();j++)assertEquals(old.target(j),restored.target(j));
        }
    }
    @Test public void advancedNumberRaceHasForwardAndBackwardSteps(){
        boolean up=false,down=false;
        for(long seed=0;seed<200;seed++){
            PlaylumiInteractive r=PlaylumiInteractive.start(PlaylumiInteractive.NUMBERS,2,seed);
            up|=r.step()>0;down|=r.step()<0;
            assertTrue(Math.abs(r.step())>=2);
            assertEquals(r.total()+2,r.options().size());
            for(int j=1;j<r.total();j++)assertEquals(r.step(),Integer.parseInt(r.target(j))-Integer.parseInt(r.target(j-1)));
            assertTrue(r.choose(r.expected()));
            PlaylumiInteractive restored=new PlaylumiInteractive(r.kind,r.level,r.seed,r.progress(),r.version);
            assertEquals(r.options(),restored.options());assertEquals(r.expected(),restored.expected());
        }
        assertTrue(up&&down);
    }
    @Test public void wordsAreCluedNotFullyDisplayedUntilRevealed(){
        for(int level=1;level<=2;level++)for(long seed=0;seed<100;seed++){
            PlaylumiInteractive r=PlaylumiInteractive.start(PlaylumiInteractive.LETTERS,level,seed);
            assertTrue(r.wordPuzzle());assertFalse(r.clue().isEmpty());assertEquals("?",r.example(1,false));
            assertEquals(r.target(1),r.example(1,true));
            while(!r.solved())assertTrue(r.choose(r.expected()));
            for(int j=0;j<r.total();j++)assertEquals(r.target(j),r.example(j,false));
        }
    }
}
