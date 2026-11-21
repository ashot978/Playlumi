package nl.playlumi.app;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public final class PlaylumiScoresTest {
    @Test public void correctAnswersCountInTheirOwnCategoryOnly() {
        PlaylumiScores board = new PlaylumiScores();
        board.answer(0,true); board.answer(0,true); board.answer(1,false);
        board.answer(1,true);
        assertEquals(2,board.get(0).correct);
        assertEquals(2,board.get(0).attempts);
        assertEquals(1,board.get(1).correct);
        assertEquals(2,board.get(1).attempts);
        assertEquals(0,board.get(23).correct);
    }
    @Test public void achievementsUnlockOnceAtExactlyTheirMilestone() {
        PlaylumiScores board=new PlaylumiScores();
        assertEquals(0,board.get(5).earnedBadges());
        assertEquals(0,board.answer(5,true).get(0).intValue());
        for(int i=2;i<=4;i++) assertTrue(board.answer(5,true).isEmpty());
        assertEquals(4,board.answer(5,true).get(0).intValue());
        for(int i=6;i<=9;i++) assertTrue(board.answer(5,true).isEmpty());
        assertEquals(1,board.answer(5,true).get(0).intValue());
        for(int i=11;i<=49;i++) board.answer(5,true);
        assertEquals(2,board.answer(5,true).get(0).intValue());
        for(int i=51;i<=199;i++) board.answer(5,true);
        assertEquals(3,board.answer(5,true).get(0).intValue());
        assertEquals(5,board.finishSession(5).get(0).intValue());
        assertTrue(board.finishSession(5).isEmpty());
        assertEquals(6,board.get(5).earnedBadges());
    }
    @Test public void incorrectAnswerBreaksStreakButDoesNotEraseEarnedPoints() {
        PlaylumiScores board=new PlaylumiScores();
        for(int i=0;i<5;i++)board.answer(2,true);
        assertEquals(5,board.get(2).bestStreak);
        board.answer(2,false);
        assertEquals(0,board.get(2).currentStreak);
        assertEquals(5,board.get(2).correct);
        assertTrue(board.get(2).hasBadge(4));
    }
    @Test public void restoredScoresContinueFromPastProgress() {
        PlaylumiScores board=new PlaylumiScores();
        board.restore(9,49,54,4,4,0);
        List<Integer> earned=board.answer(9,true);
        assertEquals(2,earned.size());
        assertTrue(earned.contains(2));
        assertTrue(earned.contains(4));
        assertEquals(50,board.get(9).correct);
        assertEquals(55,board.get(9).attempts);
        assertEquals(5,board.get(9).currentStreak);
    }
    @Test public void allCategoriesAreIndependent() {
        PlaylumiScores board=new PlaylumiScores();
        for(int c=0;c<PlaylumiScores.CATEGORIES;c++) {
            for(int i=0;i<c+1;i++)board.answer(c,true);
        }
        for(int c=0;c<PlaylumiScores.CATEGORIES;c++)
            assertEquals(c+1,board.get(c).correct);
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidCategoryIsRejected() {new PlaylumiScores().get(24);}
}