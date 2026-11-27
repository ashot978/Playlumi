package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Read-only age-specific reporting. Never infer route results from shared category scores. */
public final class PlaylumiParentProgress {
    private PlaylumiParentProgress(){}
    public static final int MIN_SAMPLES=4;
    public static final class Game {
        public final PlaylumiGameCatalog.Entry entry;
        public final int rounds,samples,firstTry,total,level;
        public final boolean completed,unfinished;
        private Game(PlaylumiGameCatalog.Entry entry,int rounds,boolean completed,boolean unfinished,
                     PlaylumiLearningProfile profile){
            this.entry=entry;this.rounds=Math.max(0,rounds);
            this.completed=this.rounds>0||completed;this.unfinished=unfinished;
            this.samples=profile==null?0:profile.samples;
            this.firstTry=profile==null?0:Integer.bitCount(profile.bits);
            this.total=profile==null?0:profile.total;
            this.level=profile==null?-1:profile.level;
        }
        public int percent(){return percentage(firstTry,samples);}
        public boolean hasHistory(){return completed||unfinished||total>0;}
    }
    public static final class World {
        public final PlaylumiPlayOverview.WorldSummary summary;
        public final List<Game> games;
        public final int samples,firstTry;
        public final long total;
        private World(PlaylumiPlayOverview.WorldSummary summary,List<Game> games){
            this.summary=summary;this.games=Collections.unmodifiableList(new ArrayList<>(games));
            int seen=0,good=0;long solved=0;
            for(Game game:games){seen+=game.samples;good+=game.firstTry;solved+=game.total;}
            samples=seen;firstTry=good;total=solved;
        }
        public int percent(){return percentage(firstTry,samples);}
    }
    public static int percentage(int good,int samples){
        if(samples<MIN_SAMPLES)return -1;
        return (int)((100L*Math.max(0,Math.min(samples,good))+samples/2)/samples);
    }
    public static List<World> summarize(int age,Map<String,Long> unfinished,Set<String> completed,
                                      Map<String,Integer> rounds,Map<String,PlaylumiLearningProfile> profiles){
        List<PlaylumiPlayOverview.WorldSummary> summary=PlaylumiPlayOverview.summarize(age,unfinished,completed,rounds);
        List<PlaylumiGameCatalog.Entry> entries=PlaylumiGameCatalog.entries(age);
        ArrayList<World> result=new ArrayList<>();
        for(PlaylumiPlayOverview.WorldSummary world:summary){
            ArrayList<Game> games=new ArrayList<>();
            for(PlaylumiGameCatalog.Entry entry:entries){
                if(entry.world!=world.world||!PlaylumiCompletion.eligible(entry))continue;
                Integer n=rounds==null?null:rounds.get(entry.id);
                PlaylumiLearningProfile profile=entry.kind==PlaylumiGameCatalog.QUIZ&&profiles!=null?profiles.get(entry.id):null;
                games.add(new Game(entry,n==null?0:n,completed!=null&&completed.contains(entry.id),
                        unfinished!=null&&unfinished.containsKey(entry.id),profile));
            }
            // Show actual played games first, then the original stable curriculum order.
            Collections.sort(games,(a,b)->Boolean.compare(b.hasHistory(),a.hasHistory()));
            result.add(new World(world,games));
        }
        return Collections.unmodifiableList(result);
    }
    public static int pageSize(int width,float fontScale){return width>=700&&fontScale<1.2f?8:4;}
    public static int pageCount(int count,int pageSize){return Math.max(1,(Math.max(0,count)+Math.max(1,pageSize)-1)/Math.max(1,pageSize));}
    public static int safePage(int page,int count,int pageSize){return Math.max(0,Math.min(page,pageCount(count,pageSize)-1));}
}
