package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiAnimalSudokuTest {
    @Test public void eachTierHasDistinctAnimalChoicesAndIncreasingChallenge(){
        for(int age=0;age<3;age++){
            PlaylumiPuzzle puzzle=PlaylumiPuzzle.start(13,age,163L);
            assertEquals(age==0?2:4,puzzle.cols);assertEquals(puzzle.cols,puzzle.rows);
            java.util.Set<String> names=new java.util.HashSet<>();
            for(int i=0;i<puzzle.optionCount();i++)names.add(puzzle.optionName(i));
            assertEquals(puzzle.cols,names.size());
            assertTrue(puzzle.total()>=(age==0?2:age==1?5:7));
        }
    }
    @Test public void moveStoresFilledCellRatherThanAnimalOption(){
        PlaylumiInteractive game=PlaylumiInteractive.start(13,2,412L);
        while(!game.solved()){
            PlaylumiPuzzle puzzle=game.puzzle();int cell=puzzle.currentCell(),before=game.mask();
            String correct=game.expected();assertTrue(game.choose(correct));
            assertEquals(before|(1<<cell),game.mask());
            PlaylumiInteractive restored=new PlaylumiInteractive(13,2,412L,game.progress(),4,game.mask());
            assertEquals(game.expected(),restored.expected());
            for(int i=0;i<puzzle.cells();i++)assertEquals(puzzle.display(i),restored.puzzle().display(i));
        }
        assertFalse(game.choose("0"));
    }
    @Test public void wrongAnimalDoesNotFillOrAdvance(){
        PlaylumiPuzzle puzzle=PlaylumiPuzzle.start(13,1,918L);
        int correct=puzzle.expected(),cell=puzzle.currentCell();
        assertFalse(puzzle.tap((correct+1)%puzzle.optionCount()));
        assertEquals(0,puzzle.progress());assertEquals(0,puzzle.mask());assertEquals(cell,puzzle.currentCell());
        assertEquals("·",puzzle.display(cell));
    }
    @Test(expected=IllegalArgumentException.class) public void corruptRestoreCannotFillGivenCell(){
        PlaylumiPuzzle puzzle=PlaylumiPuzzle.start(13,1,7L);
        int given=0;while(!puzzle.blocked(given))given++;
        new PlaylumiPuzzle(13,1,7L,1,1<<given);
    }
}
