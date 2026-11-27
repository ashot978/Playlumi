package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiPlayIdeasTest {
    private static final String LETTERS="1:0:2:0",SHAPES="2:0:3:0",COLORS="5:0:1:0";
    private static final String ADVANCED="0:0:12:8",DRAW="5:5:0:0";

    private static List<String> signature(List<PlaylumiPlayIdeas.Idea> ideas){
        List<String> result=new ArrayList<>();
        for(PlaylumiPlayIdeas.Idea idea:ideas)
            result.add(idea.entry.id+"/"+idea.kind+"/"+idea.rounds+"/"+idea.resume);
        return result;
    }
    private static void valid(int age,List<PlaylumiPlayIdeas.Idea> ideas){
        assertEquals(3,ideas.size());
        Set<String> ids=new HashSet<>();
        for(PlaylumiPlayIdeas.Idea idea:ideas){
            assertTrue(ids.add(idea.entry.id));
            assertNotNull(PlaylumiGameCatalog.find(age,idea.entry.id));
            assertNotEquals(PlaylumiGameCatalog.DRAW,idea.entry.kind);
            assertEquals(idea.kind==PlaylumiPlayIdeas.CONTINUE,idea.resume);
            assertTrue(idea.rounds>=0);
        }
    }

    @Test public void newProfilesGetThreeDifferentWorldsAndEveryTrackedRouteCanBeDiscovered(){
        for(int age=0;age<3;age++){
            Set<String> seen=new HashSet<>();
            int choices=PlaylumiGameCatalog.entries(age).size();
            for(long variation=0;variation<choices;variation++){
                List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(age,null,null,null,variation);
                valid(age,ideas);
                Set<Integer> worlds=new HashSet<>();
                for(PlaylumiPlayIdeas.Idea idea:ideas){
                    assertEquals(PlaylumiPlayIdeas.DISCOVER,idea.kind);assertEquals(0,idea.rounds);
                    worlds.add(idea.entry.world);seen.add(idea.entry.id);
                }
                assertEquals(3,worlds.size());
            }
            for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age))
                assertEquals(entry.kind!=PlaylumiGameCatalog.DRAW,seen.contains(entry.id));
        }
    }

    @Test public void mixedHistoryUsesTheNewestSaveThenACompletedAndAnUnfinishedFreeRoute(){
        Map<String,Long> saves=new HashMap<>();saves.put(LETTERS,50L);saves.put(SHAPES,500L);
        Set<String> completed=new HashSet<>(Arrays.asList(LETTERS,COLORS));
        Map<String,Integer> rounds=new HashMap<>();rounds.put(LETTERS,4);rounds.put(COLORS,7);
        for(long variation:new long[]{Long.MIN_VALUE,-1,0,1,23,Long.MAX_VALUE}){
            List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(0,saves,completed,rounds,variation);
            valid(0,ideas);
            assertEquals(SHAPES,ideas.get(0).entry.id);
            assertEquals(PlaylumiPlayIdeas.CONTINUE,ideas.get(0).kind);
            assertEquals(COLORS,ideas.get(1).entry.id);assertEquals(7,ideas.get(1).rounds);
            assertEquals(PlaylumiPlayIdeas.REPLAY,ideas.get(1).kind);
            assertEquals(PlaylumiPlayIdeas.DISCOVER,ideas.get(2).kind);
            assertFalse(saves.containsKey(ideas.get(2).entry.id));
            assertFalse(completed.contains(ideas.get(2).entry.id));
        }
    }

    @Test public void aSavedReplayAlwaysContinuesAndNeverDiscardsThatRound(){
        Map<String,Long> saves=Collections.singletonMap(LETTERS,900L);
        Set<String> completed=Collections.singleton(LETTERS);
        List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(0,saves,completed,
                Collections.singletonMap(LETTERS,12),0);
        valid(0,ideas);assertEquals(LETTERS,ideas.get(0).entry.id);
        assertTrue(ideas.get(0).resume);assertEquals(12,ideas.get(0).rounds);
        assertEquals(PlaylumiPlayIdeas.DISCOVER,ideas.get(1).kind);
        assertEquals(PlaylumiPlayIdeas.DISCOVER,ideas.get(2).kind);
    }

    @Test public void timestampsDetermineRecencyWithoutOverflowOrAClock(){
        Map<String,Long> saves=new HashMap<>();
        saves.put(LETTERS,Long.MIN_VALUE);saves.put(SHAPES,Long.MAX_VALUE);saves.put(COLORS,1L);
        for(long variation:new long[]{Long.MIN_VALUE,0,Long.MAX_VALUE})
            assertEquals(SHAPES,PlaylumiPlayIdeas.choose(0,saves,null,null,variation).get(0).entry.id);
        saves.put(SHAPES,null);
        assertEquals(COLORS,PlaylumiPlayIdeas.choose(0,saves,null,null,0).get(0).entry.id);
        saves.put(COLORS,0L);
        // Unknown/invalid dates are all old; the canonical catalog order settles the tie.
        assertEquals(LETTERS,PlaylumiPlayIdeas.choose(0,saves,null,null,Long.MAX_VALUE).get(0).entry.id);
    }

    @Test public void allSavedRoutesRemainAvailableAndTheThreeNewestAreKeptInOrder(){
        for(int age=0;age<3;age++){
            Map<String,Long> saves=new HashMap<>();List<String> expected=new ArrayList<>();
            long at=0;
            for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age)){
                saves.put(entry.id,++at);
                if(entry.kind!=PlaylumiGameCatalog.DRAW)expected.add(0,entry.id);
            }
            List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(age,saves,null,null,Long.MIN_VALUE);
            valid(age,ideas);
            for(int i=0;i<3;i++){
                assertEquals(expected.get(i),ideas.get(i).entry.id);
                assertEquals(PlaylumiPlayIdeas.CONTINUE,ideas.get(i).kind);
            }
            assertEquals(PlaylumiGameCatalog.entries(age).size(),saves.size());
        }
    }

    @Test public void aFullyCompletedCatalogOffersHonestReplaysAndNoInventedNewGame(){
        for(int age=0;age<3;age++){
            Set<String> completed=new HashSet<>();Map<String,Integer> rounds=new HashMap<>();
            for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age)){
                completed.add(entry.id);rounds.put(entry.id,3);
            }
            Set<String> seen=new HashSet<>();
            for(int variation=0;variation<80;variation++){
                List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(age,null,completed,rounds,variation);
                valid(age,ideas);Set<Integer> worlds=new HashSet<>();
                for(PlaylumiPlayIdeas.Idea idea:ideas){
                    assertEquals(PlaylumiPlayIdeas.REPLAY,idea.kind);assertEquals(3,idea.rounds);
                    seen.add(idea.entry.id);worlds.add(idea.entry.world);
                }
                assertEquals(3,worlds.size());
            }
            assertEquals(PlaylumiGameCatalog.entries(age).size()-1,seen.size());
        }
    }

    @Test public void legacyCompletionMarksNeverInventOneRecordedRound(){
        List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(0,null,
                Collections.singleton(LETTERS),null,0);
        assertEquals(LETTERS,ideas.get(0).entry.id);
        assertEquals(PlaylumiPlayIdeas.REPLAY,ideas.get(0).kind);assertEquals(0,ideas.get(0).rounds);
    }

    @Test public void positiveCountsAreRealReplayEvidenceEvenWithoutACompletionMark(){
        List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(0,null,null,
                Collections.singletonMap(COLORS,Integer.MAX_VALUE),Long.MIN_VALUE);
        assertEquals(COLORS,ideas.get(0).entry.id);
        assertEquals(PlaylumiPlayIdeas.REPLAY,ideas.get(0).kind);
        assertEquals(Integer.MAX_VALUE,ideas.get(0).rounds);
    }

    @Test public void invalidAndWrongAgeIdsNeverBecomeSuggestions(){
        Map<String,Long> saves=new HashMap<>();
        Map<String,Integer> rounds=new HashMap<>();Set<String> completed=new HashSet<>();
        for(String id:Arrays.asList(ADVANCED,DRAW,"unknown",null,"")){
            saves.put(id,Long.MAX_VALUE);rounds.put(id,Integer.MAX_VALUE);completed.add(id);
        }
        for(int age=0;age<2;age++)for(long variation:new long[]{Long.MIN_VALUE,0,Long.MAX_VALUE}){
            List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(age,saves,completed,rounds,variation);
            valid(age,ideas);
            for(PlaylumiPlayIdeas.Idea idea:ideas){
                assertFalse(completed.contains(idea.entry.id));
                assertEquals(PlaylumiPlayIdeas.DISCOVER,idea.kind);
            }
        }
        assertEquals(ADVANCED,PlaylumiPlayIdeas.choose(2,saves,completed,rounds,0).get(0).entry.id);
    }

    @Test public void negativeAndNullCountsDoNotPretendThatARoundWasCompleted(){
        Map<String,Integer> rounds=new HashMap<>();rounds.put(LETTERS,-8);rounds.put(SHAPES,null);
        assertEquals(signature(PlaylumiPlayIdeas.choose(0,null,null,null,3)),
                signature(PlaylumiPlayIdeas.choose(0,null,null,rounds,3)));
        List<PlaylumiPlayIdeas.Idea> legacy=PlaylumiPlayIdeas.choose(0,null,
                Collections.singleton(LETTERS),rounds,0);
        assertEquals(PlaylumiPlayIdeas.REPLAY,legacy.get(0).kind);assertEquals(0,legacy.get(0).rounds);
    }

    @Test public void replayChoiceUsesActualCountsForVarietyWithoutChangingThoseCounts(){
        Map<String,Integer> rounds=new HashMap<>();rounds.put(LETTERS,9);rounds.put(SHAPES,1);rounds.put(COLORS,5);
        Map<String,Integer> before=new HashMap<>(rounds);
        List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(0,null,null,rounds,Long.MAX_VALUE);
        assertEquals(SHAPES,ideas.get(0).entry.id);assertEquals(1,ideas.get(0).rounds);
        assertEquals(before,rounds);
    }

    @Test public void collectionOrderCannotChangeIdeasAndInputsRemainUntouched(){
        Map<String,Long> saves=new LinkedHashMap<>(),reverseSaves=new LinkedHashMap<>();
        Map<String,Integer> rounds=new LinkedHashMap<>(),reverseRounds=new LinkedHashMap<>();
        Set<String> completed=new LinkedHashSet<>(),reverseCompleted=new LinkedHashSet<>();
        List<PlaylumiGameCatalog.Entry> entries=PlaylumiGameCatalog.entries(2);
        for(int i=0;i<entries.size();i++){
            String id=entries.get(i).id;
            if(i%5==0)saves.put(id,100L+i%3);
            if(i%2==0)completed.add(id);
            if(i%3==0)rounds.put(id,i%4);
        }
        for(int i=entries.size()-1;i>=0;i--){
            String id=entries.get(i).id;
            if(saves.containsKey(id))reverseSaves.put(id,saves.get(id));
            if(completed.contains(id))reverseCompleted.add(id);
            if(rounds.containsKey(id))reverseRounds.put(id,rounds.get(id));
        }
        for(long variation:new long[]{Long.MIN_VALUE,-17,0,21,Long.MAX_VALUE}){
            List<PlaylumiPlayIdeas.Idea> first=PlaylumiPlayIdeas.choose(2,Collections.unmodifiableMap(saves),
                    Collections.unmodifiableSet(completed),Collections.unmodifiableMap(rounds),variation);
            valid(2,first);
            assertEquals(signature(first),signature(PlaylumiPlayIdeas.choose(2,reverseSaves,reverseCompleted,reverseRounds,variation)));
        }
        assertEquals(reverseSaves,saves);assertEquals(reverseCompleted,completed);assertEquals(reverseRounds,rounds);
    }

    @Test public void variationChangesOptionalChoicesButKeepsTheLatestSaveFirst(){
        Map<String,Long> saves=Collections.singletonMap(LETTERS,12L);
        Set<String> seen=new HashSet<>();
        for(long variation=0;variation<80;variation++){
            List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(0,saves,null,null,variation);
            assertEquals(LETTERS,ideas.get(0).entry.id);
            seen.add(ideas.get(1).entry.id);
        }
        assertTrue(seen.size()>3);
    }

    @Test public void anAgeChangeRebuildsChoicesFromItsOwnCatalog(){
        assertEquals(signature(PlaylumiPlayIdeas.choose(0,null,null,null,4)),
                signature(PlaylumiPlayIdeas.choose(Integer.MIN_VALUE,null,null,null,4)));
        assertEquals(signature(PlaylumiPlayIdeas.choose(2,null,null,null,4)),
                signature(PlaylumiPlayIdeas.choose(Integer.MAX_VALUE,null,null,null,4)));
    }

    @Test(expected=UnsupportedOperationException.class) public void resultCannotBeModified(){
        PlaylumiPlayIdeas.choose(0,null,null,null,0).clear();
    }
}
