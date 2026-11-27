package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Three optional play choices from actual, age-specific catalog history. No progress is written. */
public final class PlaylumiPlayIdeas {
    public static final int CONTINUE=0,REPLAY=1,DISCOVER=2;
    public static final int COUNT=3;

    public static final class Idea {
        public final PlaylumiGameCatalog.Entry entry;
        public final int kind,rounds;
        public final boolean resume;

        private Idea(PlaylumiGameCatalog.Entry entry,int kind,int rounds){
            this.entry=entry;this.kind=kind;this.rounds=rounds;
            resume=kind==CONTINUE;
        }
    }

    private static final class Candidate {
        final Idea idea;
        final long updated;
        Candidate(PlaylumiGameCatalog.Entry entry,int kind,int rounds,long updated){
            idea=new Idea(entry,kind,rounds);this.updated=updated;
        }
    }

    private PlaylumiPlayIdeas(){}

    /**
     * unfinishedUpdated contains only sessions the caller has validated for the current profile
     * and age. A missing/negative timestamp is treated as an old save, not a reason to lose it.
     * completedIds and roundCounts are the existing age-specific completion stores. A legacy
     * completion mark is useful evidence, but never invents a numeric round count.
     * variation is an explicit profile-local counter; selection never depends on the clock.
     *
     * One choice of each available kind comes first. Missing kinds are filled with other honest
     * choices, preferring discovery, then replay, then another saved round. Active saves are
     * always CONTINUE, even when the same game has older completed rounds.
     */
    public static List<Idea> choose(int age,Map<String,Long> unfinishedUpdated,
            Set<String> completedIds,Map<String,Integer> roundCounts,long variation){
        ArrayList<Candidate> candidates=new ArrayList<>();
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age)){
            // Free drawing has no tracked round completion or resumable session.
            if(!PlaylumiCompletion.eligible(entry))continue;
            Integer recorded=roundCounts==null?null:roundCounts.get(entry.id);
            int rounds=recorded==null?0:Math.max(0,recorded);
            boolean unfinished=unfinishedUpdated!=null&&unfinishedUpdated.containsKey(entry.id);
            Long updated=unfinished?unfinishedUpdated.get(entry.id):null;
            boolean completed=rounds>0||(completedIds!=null&&completedIds.contains(entry.id));
            int kind=unfinished?CONTINUE:completed?REPLAY:DISCOVER;
            candidates.add(new Candidate(entry,kind,rounds,updated==null?0:Math.max(0L,updated)));
        }

        ArrayList<Idea> result=new ArrayList<>();
        Set<String> chosenIds=new HashSet<>();
        Set<Integer> chosenWorlds=new HashSet<>();
        for(int kind:new int[]{CONTINUE,REPLAY,DISCOVER})
            add(pick(candidates,kind,chosenIds,chosenWorlds,variation,result.size()),result,chosenIds,chosenWorlds);
        while(result.size()<COUNT){
            Candidate next=null;
            for(int kind:new int[]{DISCOVER,REPLAY,CONTINUE}){
                next=pick(candidates,kind,chosenIds,chosenWorlds,variation,result.size());
                if(next!=null)break;
            }
            if(next==null)break;
            add(next,result,chosenIds,chosenWorlds);
        }
        return Collections.unmodifiableList(result);
    }

    private static void add(Candidate selected,List<Idea> result,Set<String> chosenIds,Set<Integer> chosenWorlds){
        if(selected==null)return;
        result.add(selected.idea);
        chosenIds.add(selected.idea.entry.id);
        chosenWorlds.add(selected.idea.entry.world);
    }

    private static Candidate pick(List<Candidate> candidates,int kind,Set<String> chosenIds,
            Set<Integer> chosenWorlds,long variation,int slot){
        ArrayList<Candidate> available=new ArrayList<>();
        for(Candidate candidate:candidates)
            if(candidate.idea.kind==kind&&!chosenIds.contains(candidate.idea.entry.id))available.add(candidate);
        if(available.isEmpty())return null;
        if(kind==CONTINUE){
            Candidate newest=available.get(0);
            for(Candidate candidate:available)
                if(candidate.updated>newest.updated)newest=candidate;
            // Catalog order breaks equal-timestamp ties, independent of hash-map order or variation.
            return newest;
        }

        ArrayList<Candidate> differentWorld=new ArrayList<>();
        for(Candidate candidate:available)
            if(!chosenWorlds.contains(candidate.idea.entry.world))differentWorld.add(candidate);
        if(!differentWorld.isEmpty())available=differentWorld;
        if(kind==REPLAY){
            // Spread replay opportunities among actual recorded rounds, without judging ability.
            int fewest=Integer.MAX_VALUE;
            for(Candidate candidate:available)fewest=Math.min(fewest,candidate.idea.rounds);
            ArrayList<Candidate> lessPlayed=new ArrayList<>();
            for(Candidate candidate:available)if(candidate.idea.rounds==fewest)lessPlayed.add(candidate);
            available=lessPlayed;
        }
        // Normalize before addition; this also works on API 23 without Math.floorMod(long,long).
        int size=available.size();
        long remainder=variation%size;
        if(remainder<0)remainder+=size;
        int at=(int)((remainder+slot)%size);
        return available.get(at);
    }
}
