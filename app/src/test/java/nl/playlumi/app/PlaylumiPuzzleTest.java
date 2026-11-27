package nl.playlumi.app;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public final class PlaylumiPuzzleTest {
    @Test public void unorderedSelectionsSurviveInTheirActualOrder(){
        for(int kind:new int[]{PlaylumiPuzzle.SORT,PlaylumiPuzzle.PIXEL,PlaylumiPuzzle.SYMMETRY}){
            PlaylumiPuzzle p=PlaylumiPuzzle.start(kind,2,123);
            ArrayList<Integer> valid=new ArrayList<>();for(int i=0;i<p.cells();i++)if(p.accepts(i))valid.add(i);
            assertTrue(valid.size()>=2);int chosen=valid.get(valid.size()-1);
            assertTrue(p.tap(chosen));assertFalse(p.tap(chosen));
            PlaylumiPuzzle restored=new PlaylumiPuzzle(kind,2,123,1,p.mask());
            assertTrue(restored.selected(chosen));assertEquals(p.mask(),restored.mask());
            for(int i=0;i<p.cells();i++)assertEquals(p.display(i),restored.display(i));
        }
    }
    @Test public void snapshotsCannotInventCompletedCells(){
        try{new PlaylumiPuzzle(PlaylumiPuzzle.PIXEL,0,12,1,0);fail();}catch(IllegalArgumentException expected){}
        try{new PlaylumiPuzzle(PlaylumiPuzzle.MAZE,0,12,1,0);fail();}catch(IllegalArgumentException expected){}
        try{PlaylumiPuzzle.start(PlaylumiPuzzle.MAP,0,12);fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void oldGeneratorVersionsRemainIndependent(){
        for(int age=0;age<3;age++)for(int kind=0;kind<4;kind++){
            PlaylumiInteractive old=PlaylumiInteractive.start(kind,age,24);
            assertEquals(3,old.version);assertNull(old.puzzle());assertEquals(0,old.mask());
        }
    }
}
