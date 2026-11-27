package nl.playlumi.app;

import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiDrawingHistoryTest {
    private static PlaylumiDrawingHistory.Action stroke(int color){
        PlaylumiDrawingHistory.StrokeBuilder b=new PlaylumiDrawingHistory.StrokeBuilder(color,.02f,false,.1f,.2f);
        b.append(.8f,.7f);return b.build();
    }

    @Test public void oneWholeGestureIsOneUndoStepAndRestoresTheSameAction(){
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(false);
        PlaylumiDrawingHistory.StrokeBuilder b=new PlaylumiDrawingHistory.StrokeBuilder(123,.02f,false,0,0);
        for(int i=1;i<=100;i++)b.append(i/100f,i/100f);
        PlaylumiDrawingHistory.Action action=b.build();history.append(action,null);
        assertEquals(1,history.appliedCount());assertTrue(history.hasArtwork());
        assertTrue(history.undo());assertFalse(history.canUndo());assertTrue(history.canRedo());
        assertFalse(history.hasArtwork());assertTrue(history.redo());
        assertSame(action,history.applied(0));assertEquals(101,action.pointCount());
    }
    @Test public void clearCanRestoreAnImportedImageWithoutPretendingItHasOldUndoSteps(){
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(true);
        assertFalse(history.canUndo());assertTrue(history.hasArtwork());
        history.append(PlaylumiDrawingHistory.Action.clear(),null);
        assertFalse(history.hasArtwork());assertTrue(history.undo());assertTrue(history.hasArtwork());
        assertTrue(history.redo());assertFalse(history.hasArtwork());assertFalse(history.redo());
    }
    @Test public void aNewStrokeAfterUndoDiscardsOnlyTheRedoBranch(){
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(false);
        PlaylumiDrawingHistory.Action first=stroke(1),second=stroke(2),replacement=stroke(3);
        history.append(first,null);history.append(second,null);history.undo();
        history.append(replacement,null);
        assertEquals(2,history.retainedCount());assertSame(first,history.applied(0));
        assertSame(replacement,history.applied(1));assertFalse(history.contains(second));assertFalse(history.canRedo());
    }
    @Test public void clearAfterUndoAlsoDiscardsTheRedoBranch(){
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(true);
        history.append(stroke(1),null);history.append(stroke(2),null);history.undo();
        history.append(PlaylumiDrawingHistory.Action.clear(),null);
        assertFalse(history.hasArtwork());assertFalse(history.canRedo());
        history.undo();assertTrue(history.hasArtwork());assertEquals(1,history.appliedCount());
    }
    @Test public void realHistoryOverflowPreservesEveryOlderActionInOrder(){
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(false);
        ArrayList<PlaylumiDrawingHistory.Action> submitted=new ArrayList<>(),flattened=new ArrayList<>();
        for(int i=0;i<97;i++){
            PlaylumiDrawingHistory.Action action=stroke(i);submitted.add(action);
            history.append(action,flattened::add);
            assertTrue(history.retainedCount()<=PlaylumiDrawingHistory.MAX_ACTIONS);
        }
        assertEquals(65,flattened.size());
        for(int i=0;i<65;i++)assertSame(submitted.get(i),flattened.get(i));
        for(int i=0;i<32;i++)assertSame(submitted.get(i+65),history.applied(i));
        for(int i=0;i<32;i++)assertTrue(history.undo());
        assertFalse(history.undo());assertTrue(history.hasArtwork());
        for(int i=0;i<32;i++)assertTrue(history.redo());
        assertFalse(history.redo());assertEquals(65,flattened.size());
    }
    @Test public void eraserAndClearAlsoCrossTheHistoryBoundaryInTheirOriginalOrder(){
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(true);
        ArrayList<PlaylumiDrawingHistory.Action> flattened=new ArrayList<>();
        history.append(new PlaylumiDrawingHistory.StrokeBuilder(5,.1f,true,.5f,.5f).build(),flattened::add);
        history.append(PlaylumiDrawingHistory.Action.clear(),flattened::add);
        for(int i=0;i<32;i++)history.append(stroke(i),flattened::add);
        assertEquals(2,flattened.size());assertEquals(PlaylumiDrawingHistory.ERASE,flattened.get(0).kind);
        assertEquals(PlaylumiDrawingHistory.CLEAR,flattened.get(1).kind);
        while(history.undo()){}
        assertFalse(history.hasArtwork());
    }
    @Test public void failedCompactionDoesNotLoseTheOldestAction(){
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(false);
        PlaylumiDrawingHistory.Action first=stroke(0);history.append(first,null);
        for(int i=1;i<32;i++)history.append(stroke(i),null);
        try{history.append(stroke(99),action->{throw new IllegalStateException("cannot compose");});fail();}
        catch(IllegalStateException expected){assertEquals("cannot compose",expected.getMessage());}
        assertEquals(32,history.appliedCount());assertEquals(32,history.retainedCount());
        assertSame(first,history.applied(0));assertEquals(31,history.applied(31).color);
    }
    @Test public void branchingWithinFullHistoryDoesNotFlattenUnneededOperations(){
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(false);
        for(int i=0;i<32;i++)history.append(stroke(i),null);
        for(int i=0;i<17;i++)history.undo();
        history.append(stroke(50),action->{fail("unexpected compaction");});
        assertEquals(16,history.retainedCount());assertEquals(50,history.applied(15).color);
        assertFalse(history.canRedo());
    }
    @Test public void anEraserOnEmptyPaperDoesNotInventArtwork(){
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(false);
        history.append(new PlaylumiDrawingHistory.StrokeBuilder(1,.05f,true,.4f,.4f).build(),null);
        assertFalse(history.hasArtwork());assertTrue(history.canUndo());
        history.undo();assertTrue(history.canRedo());
    }
    @Test public void aTapRemainsOnePointAndActionGeometryIsAnImmutableSnapshot(){
        PlaylumiDrawingHistory.StrokeBuilder builder=new PlaylumiDrawingHistory.StrokeBuilder(7,.04f,false,.2f,.3f);
        assertFalse(builder.append(.2f,.3f));
        PlaylumiDrawingHistory.Action tap=builder.build();builder.append(.9f,.9f);
        assertEquals(1,tap.pointCount());assertEquals(.2f,tap.x(0),0);assertEquals(.3f,tap.y(0),0);
        assertEquals(7,tap.color);assertEquals(.04f,tap.width,0);assertFalse(tap.simplified);
    }
    @Test public void aLongGestureIsBoundedContinuesAndReportsItsSimplification(){
        PlaylumiDrawingHistory.StrokeBuilder builder=new PlaylumiDrawingHistory.StrokeBuilder(8,.02f,false,.05f,.1f);
        for(int i=1;i<PlaylumiDrawingHistory.MAX_POINTS*5;i++){
            builder.append((i%503)/502f,(i%307)/306f);
            assertTrue(builder.pointCount()<=PlaylumiDrawingHistory.MAX_POINTS);
        }
        builder.append(.876f,.654f);
        PlaylumiDrawingHistory.Action action=builder.build();
        assertTrue(action.simplified);assertTrue(action.pointCount()>1);
        assertEquals(.05f,action.x(0),0);assertEquals(.1f,action.y(0),0);
        assertEquals(.876f,action.x(action.pointCount()-1),0);
        assertEquals(.654f,action.y(action.pointCount()-1),0);
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(false);history.append(action,null);
        assertTrue(history.undo());assertFalse(history.canUndo());assertTrue(history.redo());
        assertSame(action,history.applied(0));
    }
    @Test public void invalidMotionSamplesAreIgnoredAndOffPaperPointsAreClamped(){
        PlaylumiDrawingHistory.StrokeBuilder builder=new PlaylumiDrawingHistory.StrokeBuilder(1,.02f,false,-3,8);
        assertFalse(builder.append(Float.NaN,.5f));assertFalse(builder.append(.5f,Float.POSITIVE_INFINITY));
        builder.append(8,-4);PlaylumiDrawingHistory.Action action=builder.build();
        assertEquals(2,action.pointCount());assertEquals(0,action.x(0),0);assertEquals(1,action.y(0),0);
        assertEquals(1,action.x(1),0);assertEquals(0,action.y(1),0);
    }
    @Test(expected=IllegalArgumentException.class) public void anInvalidWidthCannotCreateAnUnrenderableStroke(){
        new PlaylumiDrawingHistory.StrokeBuilder(1,Float.NaN,false,.2f,.2f);
    }
}
