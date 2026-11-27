package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Offline contract checks for honest, age-correct optional play suggestions. */
public final class PlayIdeasCheck {
    private static int cases,ideasChecked,worldsChecked;
    private static void require(boolean okay,String message){if(!okay)throw new AssertionError(message);}

    private static final class History {
        final Map<String,Long> saves=new LinkedHashMap<>();
        final Set<String> completed=new LinkedHashSet<>();
        final Map<String,Integer> rounds=new LinkedHashMap<>();
    }
    private static int rounds(History history,String id){
        Integer value=history.rounds.get(id);return value==null||value<0?0:value;
    }
    private static int kind(History history,String id){
        if(history.saves.containsKey(id))return PlaylumiPlayIdeas.CONTINUE;
        return history.completed.contains(id)||rounds(history,id)>0?PlaylumiPlayIdeas.REPLAY:PlaylumiPlayIdeas.DISCOVER;
    }
    private static long updated(History history,String id){
        Long value=history.saves.get(id);return value==null||value<0?0:value;
    }
    private static List<String> signature(List<PlaylumiPlayIdeas.Idea> ideas){
        List<String> result=new ArrayList<>();
        for(PlaylumiPlayIdeas.Idea idea:ideas)
            result.add(idea.entry.id+"/"+idea.kind+"/"+idea.rounds+"/"+idea.resume);
        return result;
    }
    private static History reverse(History original){
        History copy=new History();
        List<String> ids=new ArrayList<>(original.saves.keySet());Collections.reverse(ids);
        for(String id:ids)copy.saves.put(id,original.saves.get(id));
        ids=new ArrayList<>(original.completed);Collections.reverse(ids);copy.completed.addAll(ids);
        ids=new ArrayList<>(original.rounds.keySet());Collections.reverse(ids);
        for(String id:ids)copy.rounds.put(id,original.rounds.get(id));
        return copy;
    }
    private static void verifyOverview(int age,History history,History reversed){
        long[] expectedRounds=new long[6];int[] expectedCompleted=new int[6],expectedSaved=new int[6],available=new int[6];
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age)){
            if(entry.kind==PlaylumiGameCatalog.DRAW)continue;
            int world=entry.world;available[world]++;
            int actual=rounds(history,entry.id);expectedRounds[world]+=(long)actual;
            if(history.completed.contains(entry.id)||actual>0)expectedCompleted[world]++;
            if(history.saves.containsKey(entry.id))expectedSaved[world]++;
        }
        List<PlaylumiPlayOverview.WorldSummary> worlds=PlaylumiPlayOverview.summarize(age,
                Collections.unmodifiableMap(history.saves),Collections.unmodifiableSet(history.completed),
                Collections.unmodifiableMap(history.rounds));
        List<PlaylumiPlayOverview.WorldSummary> other=PlaylumiPlayOverview.summarize(age,reversed.saves,reversed.completed,reversed.rounds);
        require(worlds.size()==6&&other.size()==6,"missing world summary");
        for(int i=0;i<6;i++){
            PlaylumiPlayOverview.WorldSummary world=worlds.get(i),reordered=other.get(i);
            require(world.world==i&&world.roundBasedRoutes==available[i],"wrong world or age-specific route total");
            require(world.countedRounds==expectedRounds[i],"invented, missing or overflowing round total");
            require(world.completedRoutes==expectedCompleted[i],"completed routes double-counted or omitted");
            require(world.unfinishedRoutes==expectedSaved[i],"saved replays omitted or double-counted");
            require(world.countedRounds==reordered.countedRounds&&world.completedRoutes==reordered.completedRoutes&&
                    world.unfinishedRoutes==reordered.unfinishedRoutes,"history iteration order changes overview");
            worldsChecked++;
        }
    }
    private static List<PlaylumiPlayIdeas.Idea> verify(int age,History history,long variation){
        History before=reverse(history);
        List<PlaylumiPlayIdeas.Idea> ideas=PlaylumiPlayIdeas.choose(age,
                Collections.unmodifiableMap(history.saves),Collections.unmodifiableSet(history.completed),
                Collections.unmodifiableMap(history.rounds),variation);
        require(ideas.size()==3,"missing useful fallback");
        Set<String> unique=new HashSet<>();Set<Integer> offeredKinds=new HashSet<>(),availableKinds=new HashSet<>();
        String newest=null;long latest=-1;
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age)){
            if(entry.kind==PlaylumiGameCatalog.DRAW)continue;
            availableKinds.add(kind(history,entry.id));
            if(history.saves.containsKey(entry.id)&&updated(history,entry.id)>latest){
                newest=entry.id;latest=updated(history,entry.id);
            }
        }
        for(PlaylumiPlayIdeas.Idea idea:ideas){
            require(unique.add(idea.entry.id),"duplicate idea");
            PlaylumiGameCatalog.Entry entry=PlaylumiGameCatalog.find(age,idea.entry.id);
            require(entry!=null&&entry.kind!=PlaylumiGameCatalog.DRAW,"unknown, wrong-age or untracked route");
            require(entry.title.equals(idea.entry.title)&&entry.world==idea.entry.world,"noncanonical route");
            require(idea.kind==kind(history,entry.id),"idea misrepresents actual history");
            require(idea.rounds==rounds(history,entry.id),"fabricated or changed round count");
            require(idea.resume==history.saves.containsKey(entry.id),"saved round would restart");
            offeredKinds.add(idea.kind);ideasChecked++;
        }
        require(offeredKinds.containsAll(availableKinds),"an available kind was omitted");
        if(newest!=null)require(ideas.get(0).entry.id.equals(newest)&&ideas.get(0).resume,"latest valid save not first");
        require(signature(ideas).equals(signature(PlaylumiPlayIdeas.choose(age,before.saves,before.completed,before.rounds,variation))),
                "hash-map or set order changes result");
        verifyOverview(age,history,before);
        require(history.saves.equals(before.saves)&&history.completed.equals(before.completed)&&history.rounds.equals(before.rounds),
                "suggestions mutated real history");
        cases++;
        return ideas;
    }
    private static History fixture(int age,int scenario){
        History h=new History();int i=0;
        List<PlaylumiGameCatalog.Entry> catalog=PlaylumiGameCatalog.entries(age);
        for(PlaylumiGameCatalog.Entry entry:catalog){
            String id=entry.id;
            switch(scenario){
                case 1:h.completed.add(id);h.rounds.put(id,3);break;
                case 2:h.saves.put(id,100L+i);break;
                case 3:
                    if(i%5==0)h.saves.put(id,100L+i%3);
                    if(i%2==0)h.completed.add(id);
                    if(i%3==0)h.rounds.put(id,i%9);
                    break;
                case 4:h.completed.add(id);break;
                case 5:h.rounds.put(id,1+i%8);break;
                case 6:
                    h.rounds.put(id,i%2==0?-i:null);
                    if(i%7==0)h.saves.put(id,i%2==0?null:Long.MIN_VALUE);
                    break;
                case 7:if(i!=0){h.completed.add(id);h.rounds.put(id,2);}break;
                case 8:h.saves.put(id,i%2==0?Long.MAX_VALUE:0L);h.completed.add(id);h.rounds.put(id,Integer.MAX_VALUE);break;
                case 9:h.rounds.put(id,0);break;
                default:break;
            }
            i++;
        }
        for(String invalid:new String[]{null,"","not-a-route","99:0:12:8","5:5:0:0"}){
            h.saves.put(invalid,Long.MAX_VALUE);h.completed.add(invalid);h.rounds.put(invalid,Integer.MAX_VALUE);
        }
        if(age<2){h.saves.put("0:0:12:8",Long.MAX_VALUE);h.completed.add("0:0:12:8");h.rounds.put("0:0:12:8",20);}
        return h;
    }
    private static void variedCoverage(){
        for(int age=0;age<3;age++)for(int scenario:new int[]{0,1,4}){
            History history=fixture(age,scenario);Set<String> seen=new HashSet<>();
            for(int variation=0;variation<100;variation++){
                List<PlaylumiPlayIdeas.Idea> ideas=verify(age,history,variation);
                Set<Integer> worlds=new HashSet<>();
                for(PlaylumiPlayIdeas.Idea idea:ideas){seen.add(idea.entry.id);worlds.add(idea.entry.world);}
                require(worlds.size()==3,"same-world cards crowd out available variety");
            }
            Set<String> expected=new HashSet<>();
            for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age))
                if(entry.kind!=PlaylumiGameCatalog.DRAW)expected.add(entry.id);
            require(seen.equals(expected),"some tracked route cannot be suggested");
        }
    }
    private static void specificRegressions(){
        History h=new History();
        h.saves.put("1:0:2:0",100L);h.completed.add("1:0:2:0");h.rounds.put("1:0:2:0",12);
        h.completed.add("5:0:1:0");
        List<PlaylumiPlayIdeas.Idea> ideas=verify(0,h,Long.MIN_VALUE);
        require(ideas.get(0).resume&&ideas.get(0).rounds==12,"completed route lost its current saved replay");
        require(ideas.get(1).entry.id.equals("5:0:1:0")&&ideas.get(1).rounds==0,"legacy completion invented a count");
        h=new History();h.rounds.put("1:0:2:0",9);h.rounds.put("2:0:3:0",1);h.rounds.put("5:0:1:0",5);
        require(verify(0,h,Long.MAX_VALUE).get(0).entry.id.equals("2:0:3:0"),"replay ignores actual count variety");
        List<PlaylumiPlayIdeas.Idea> empty=PlaylumiPlayIdeas.choose(0,null,null,null,0);
        require(empty.size()==3,"null history has no fallback");
        try{empty.clear();throw new AssertionError("modifiable result");}catch(UnsupportedOperationException expected){}
        require(signature(PlaylumiPlayIdeas.choose(Integer.MIN_VALUE,null,null,null,4))
                .equals(signature(PlaylumiPlayIdeas.choose(0,null,null,null,4))),"negative age not normalized");
        require(signature(PlaylumiPlayIdeas.choose(Integer.MAX_VALUE,null,null,null,4))
                .equals(signature(PlaylumiPlayIdeas.choose(2,null,null,null,4))),"large age not normalized");
        h=new History();h.rounds.put("0:0:12:0",Integer.MAX_VALUE);h.rounds.put("0:0:12:1",Integer.MAX_VALUE);
        h.rounds.put("0:0:12:2",Integer.MAX_VALUE);
        verifyOverview(2,h,reverse(h));
        require(PlaylumiPlayOverview.summarize(2,null,null,h.rounds).get(0).countedRounds==6442450941L,
                "three maximum counters do not retain their exact long sum");
        List<PlaylumiPlayOverview.WorldSummary> emptyWorlds=PlaylumiPlayOverview.summarize(0,null,null,null);
        require(emptyWorlds.size()==6,"null history has no world overview");
        try{emptyWorlds.clear();throw new AssertionError("modifiable overview");}catch(UnsupportedOperationException expected){}
    }
    public static void main(String[] args){
        for(int age=0;age<3;age++)for(int scenario=0;scenario<10;scenario++){
            History history=fixture(age,scenario);
            for(long variation=-40;variation<40;variation++)verify(age,history,variation);
            for(long variation:new long[]{Long.MIN_VALUE,Long.MIN_VALUE+1,Long.MAX_VALUE-1,Long.MAX_VALUE})
                verify(age,history,variation);
        }
        variedCoverage();specificRegressions();
        System.out.println("PASS: "+cases+" play-idea histories and variations; "+ideasChecked+
                " unique age-correct, history-backed choices; newest-save priority, legacy counts and immutable inputs.");
        System.out.println("PASS: "+worldsChecked+" factual world summaries, including legacy completion, open replays and exact long round totals.");
    }
}
