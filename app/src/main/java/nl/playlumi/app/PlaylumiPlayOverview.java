package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Factual per-world play history for the current profile and age. Rendering earns no progress. */
public final class PlaylumiPlayOverview {
    public static final class WorldSummary {
        public final int world,completedRoutes,unfinishedRoutes,roundBasedRoutes;
        public final long countedRounds;

        private WorldSummary(int world,long countedRounds,int completedRoutes,int unfinishedRoutes,int roundBasedRoutes){
            this.world=world;this.countedRounds=countedRounds;this.completedRoutes=completedRoutes;
            this.unfinishedRoutes=unfinishedRoutes;this.roundBasedRoutes=roundBasedRoutes;
        }
    }

    private PlaylumiPlayOverview(){}

    /**
     * Inputs use the same age-specific history as PlaylumiPlayIdeas.choose. unfinishedUpdated
     * contains only caller-validated saved rounds; timestamp presence, rather than its value,
     * identifies a saved round. A completed game may also have an unfinished replay.
     */
    public static List<WorldSummary> summarize(int age,Map<String,Long> unfinishedUpdated,
            Set<String> completedIds,Map<String,Integer> roundCounts){
        long[] totals=new long[6];int[] completed=new int[6],unfinished=new int[6],available=new int[6];
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age)){
            if(!PlaylumiCompletion.eligible(entry))continue;
            int world=entry.world;available[world]++;
            Integer recorded=roundCounts==null?null:roundCounts.get(entry.id);
            int rounds=recorded==null?0:Math.max(0,recorded);
            // Widen each route before addition; multiple Integer.MAX_VALUE counters stay exact.
            totals[world]+=(long)rounds;
            if(rounds>0||(completedIds!=null&&completedIds.contains(entry.id)))completed[world]++;
            if(unfinishedUpdated!=null&&unfinishedUpdated.containsKey(entry.id))unfinished[world]++;
        }
        ArrayList<WorldSummary> result=new ArrayList<>();
        for(int world=0;world<6;world++)
            result.add(new WorldSummary(world,totals[world],completed[world],unfinished[world],available[world]));
        return Collections.unmodifiableList(result);
    }
}
