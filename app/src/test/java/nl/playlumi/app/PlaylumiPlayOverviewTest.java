package nl.playlumi.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiPlayOverviewTest {
    private static final String LETTERS="1:0:2:0",SHAPES="2:0:3:0",COLORS="5:0:1:0";

    @Test public void newProfilesShowSixEmptyWorldsWithRealAgeSpecificRouteTotals(){
        for(int age=0;age<3;age++){
            List<PlaylumiPlayOverview.WorldSummary> worlds=PlaylumiPlayOverview.summarize(age,null,null,null);
            assertEquals(6,worlds.size());int routes=0;
            for(int i=0;i<6;i++){
                PlaylumiPlayOverview.WorldSummary world=worlds.get(i);
                assertEquals(i,world.world);assertEquals(0L,world.countedRounds);
                assertEquals(0,world.completedRoutes);assertEquals(0,world.unfinishedRoutes);
                assertTrue(world.roundBasedRoutes>0);routes+=world.roundBasedRoutes;
            }
            assertEquals(new int[]{36,61,88}[age],routes);
        }
    }

    @Test public void legacyMarksAreCompletedRoutesWithoutFabricatedRoundCounts(){
        List<PlaylumiPlayOverview.WorldSummary> worlds=PlaylumiPlayOverview.summarize(0,null,
                new HashSet<>(Arrays.asList(LETTERS,SHAPES)),null);
        assertEquals(1,worlds.get(1).completedRoutes);assertEquals(1,worlds.get(2).completedRoutes);
        for(PlaylumiPlayOverview.WorldSummary world:worlds){
            assertEquals(0L,world.countedRounds);assertEquals(0,world.unfinishedRoutes);
        }
    }

    @Test public void anOpenReplayCountsOnceInEachRelevantMetric(){
        List<PlaylumiPlayOverview.WorldSummary> worlds=PlaylumiPlayOverview.summarize(0,
                Collections.singletonMap(LETTERS,200L),Collections.singleton(LETTERS),Collections.singletonMap(LETTERS,7));
        PlaylumiPlayOverview.WorldSummary letters=worlds.get(1);
        assertEquals(7L,letters.countedRounds);assertEquals(1,letters.completedRoutes);
        assertEquals(1,letters.unfinishedRoutes);
        for(int world:new int[]{0,2,3,4,5}){
            assertEquals(0L,worlds.get(world).countedRounds);
            assertEquals(0,worlds.get(world).completedRoutes);assertEquals(0,worlds.get(world).unfinishedRoutes);
        }
    }

    @Test public void positiveCountsAndCompletionMarksFormAUnionWithoutDoubleCounting(){
        Map<String,Integer> rounds=new HashMap<>();rounds.put(LETTERS,8);rounds.put("1:0:2:1",3);
        rounds.put(COLORS,2);rounds.put(SHAPES,null);
        List<PlaylumiPlayOverview.WorldSummary> worlds=PlaylumiPlayOverview.summarize(0,null,
                Collections.singleton(LETTERS),rounds);
        assertEquals(11L,worlds.get(1).countedRounds);assertEquals(2,worlds.get(1).completedRoutes);
        assertEquals(2L,worlds.get(5).countedRounds);assertEquals(1,worlds.get(5).completedRoutes);
        assertEquals(0,worlds.get(2).completedRoutes);
    }

    @Test public void multipleMaximumIntCountersRemainAnExactPositiveLong(){
        Map<String,Integer> rounds=new HashMap<>();
        rounds.put("0:0:12:0",Integer.MAX_VALUE);rounds.put("0:0:12:1",Integer.MAX_VALUE);
        rounds.put("0:0:12:2",Integer.MAX_VALUE);
        PlaylumiPlayOverview.WorldSummary math=PlaylumiPlayOverview.summarize(2,null,null,rounds).get(0);
        assertEquals(6442450941L,math.countedRounds);assertEquals(3,math.completedRoutes);
    }

    @Test public void invalidWrongAgeAndFreeDrawingIdsNeverBecomeProgress(){
        Map<String,Long> saves=new HashMap<>();Map<String,Integer> rounds=new HashMap<>();Set<String> completed=new HashSet<>();
        for(String id:Arrays.asList("0:0:12:8","5:5:0:0","unknown","",null)){
            saves.put(id,Long.MAX_VALUE);rounds.put(id,Integer.MAX_VALUE);completed.add(id);
        }
        for(int age=0;age<2;age++)for(PlaylumiPlayOverview.WorldSummary world:
                PlaylumiPlayOverview.summarize(age,saves,completed,rounds)){
            assertEquals(0L,world.countedRounds);assertEquals(0,world.completedRoutes);assertEquals(0,world.unfinishedRoutes);
        }
        PlaylumiPlayOverview.WorldSummary oldestMath=PlaylumiPlayOverview.summarize(2,saves,completed,rounds).get(0);
        assertEquals((long)Integer.MAX_VALUE,oldestMath.countedRounds);
        assertEquals(1,oldestMath.completedRoutes);assertEquals(1,oldestMath.unfinishedRoutes);
    }

    @Test public void unknownSaveDatesRemainSavesAndNegativeCountsNeverIncreaseProgress(){
        Map<String,Long> saves=new HashMap<>();saves.put(LETTERS,null);saves.put("1:0:2:1",Long.MIN_VALUE);saves.put(SHAPES,0L);
        Map<String,Integer> rounds=new HashMap<>();rounds.put(LETTERS,-1);rounds.put(SHAPES,null);
        List<PlaylumiPlayOverview.WorldSummary> worlds=PlaylumiPlayOverview.summarize(0,saves,Collections.singleton(LETTERS),rounds);
        assertEquals(2,worlds.get(1).unfinishedRoutes);assertEquals(1,worlds.get(2).unfinishedRoutes);
        assertEquals(1,worlds.get(1).completedRoutes);assertEquals(0,worlds.get(2).completedRoutes);
        for(PlaylumiPlayOverview.WorldSummary world:worlds)assertEquals(0L,world.countedRounds);
    }

    @Test public void aFullCatalogCanBeCompletedAndHaveOneOpenReplayPerRoute(){
        for(int age=0;age<3;age++){
            Map<String,Long> saves=new HashMap<>();Set<String> completed=new HashSet<>();Map<String,Integer> rounds=new HashMap<>();
            for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age)){
                saves.put(entry.id,4L);completed.add(entry.id);rounds.put(entry.id,12);
            }
            for(PlaylumiPlayOverview.WorldSummary world:PlaylumiPlayOverview.summarize(age,saves,completed,rounds)){
                assertEquals(world.roundBasedRoutes,world.completedRoutes);
                assertEquals(world.roundBasedRoutes,world.unfinishedRoutes);
                assertEquals(12L*world.roundBasedRoutes,world.countedRounds);
            }
        }
    }

    @Test public void sourceCollectionsAreNeverChanged(){
        Map<String,Long> saves=new HashMap<>();saves.put(LETTERS,10L);
        Map<String,Integer> rounds=new HashMap<>();rounds.put(COLORS,2);
        Set<String> completed=new HashSet<>(Arrays.asList(LETTERS,COLORS));
        for(int i=0;i<3;i++)PlaylumiPlayOverview.summarize(i,Collections.unmodifiableMap(saves),
                Collections.unmodifiableSet(completed),Collections.unmodifiableMap(rounds));
        assertEquals(Collections.singletonMap(LETTERS,10L),saves);
        assertEquals(Collections.singletonMap(COLORS,2),rounds);
        assertEquals(new HashSet<>(Arrays.asList(LETTERS,COLORS)),completed);
    }

    @Test(expected=UnsupportedOperationException.class) public void resultCannotBeModified(){
        PlaylumiPlayOverview.summarize(0,null,null,null).clear();
    }
}
