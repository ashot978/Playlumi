package nl.playlumi.app;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiParentProgressTest {
    @Test public void sixWorldsMatchTheExactCurrentAgeCatalogue(){
        for(int age=0;age<3;age++){
            List<PlaylumiParentProgress.World> reports=PlaylumiParentProgress.summarize(age,null,null,null,null);
            assertEquals(6,reports.size());int count=0;
            for(int w=0;w<6;w++){
                PlaylumiParentProgress.World report=reports.get(w);
                assertEquals(w,report.summary.world);assertEquals(-1,report.percent());
                assertEquals(report.summary.roundBasedRoutes,report.games.size());
                for(PlaylumiParentProgress.Game game:report.games){
                    assertEquals(w,game.entry.world);assertFalse(game.hasHistory());
                    assertNotNull(PlaylumiGameCatalog.find(age,game.entry.id));
                    assertFalse(game.entry.kind==PlaylumiGameCatalog.DRAW);count++;
                }
            }
            assertEquals(new int[]{36,61,88}[age],count);
        }
    }
    @Test public void reportsUseOnlyRealPerRouteSamplesAndKeepLegacyCompletion(){
        String math="0:0:0:5",board="0:6:0:0",legacy="1:0:2:0",otherAge="0:0:12:8";
        Map<String,Integer> rounds=new HashMap<>();rounds.put(board,2);rounds.put(otherAge,99);
        Map<String,Long> open=new HashMap<>();open.put(math,5L);open.put(board,0L);
        Set<String> complete=new HashSet<>(Arrays.asList(legacy,otherAge));
        Map<String,PlaylumiLearningProfile> data=new HashMap<>();
        data.put(math,new PlaylumiLearningProfile(0,0,0,0b1101,4,10,0));
        data.put(board,new PlaylumiLearningProfile(0,0,0,15,4,99,0));
        data.put(otherAge,new PlaylumiLearningProfile(0,0,0,15,4,99,0));
        List<PlaylumiParentProgress.World> reports=PlaylumiParentProgress.summarize(0,open,complete,rounds,data);
        PlaylumiParentProgress.World rocket=reports.get(0);
        assertEquals(2L,rocket.summary.countedRounds);assertEquals(1,rocket.summary.completedRoutes);
        assertEquals(2,rocket.summary.unfinishedRoutes);
        assertEquals(4,rocket.samples);assertEquals(3,rocket.firstTry);assertEquals(75,rocket.percent());assertEquals(10L,rocket.total);
        assertTrue(rocket.games.get(0).hasHistory());assertTrue(rocket.games.get(1).hasHistory());
        PlaylumiParentProgress.World letters=reports.get(1);
        assertEquals(0L,letters.summary.countedRounds);assertEquals(1,letters.summary.completedRoutes);
        assertEquals(-1,letters.percent());assertTrue(letters.games.get(0).completed);
        assertEquals(0,letters.games.get(0).rounds);
        assertEquals(3, data.size());assertEquals(2,rounds.size());assertEquals(2,complete.size());
    }
    @Test public void percentagesAndPaginationAreSafe(){
        assertEquals(-1,PlaylumiParentProgress.percentage(3,3));
        assertEquals(75,PlaylumiParentProgress.percentage(3,4));
        assertEquals(67,PlaylumiParentProgress.percentage(4,6));
        assertEquals(100,PlaylumiParentProgress.percentage(Integer.MAX_VALUE,Integer.MAX_VALUE));
        assertEquals(4,PlaylumiParentProgress.pageSize(360,1f));
        assertEquals(8,PlaylumiParentProgress.pageSize(900,1f));
        assertEquals(4,PlaylumiParentProgress.pageSize(900,1.3f));
        assertEquals(1,PlaylumiParentProgress.pageCount(0,4));
        assertEquals(3,PlaylumiParentProgress.pageCount(9,4));
        assertEquals(2,PlaylumiParentProgress.safePage(99,9,4));
        assertEquals(0,PlaylumiParentProgress.safePage(-2,9,4));
    }
}
