package nl.playlumi.app;
import java.util.*;

public final class ParentProgressCheck {
    private static void require(boolean good,String message){if(!good)throw new AssertionError(message);}
    public static void main(String[] args){
        Random random=new Random(118);
        int checked=0;
        for(int age=0;age<3;age++)for(int scenario=0;scenario<300;scenario++){
            Map<String,Integer> rounds=new HashMap<>();
            Map<String,Long> saved=new HashMap<>();
            Set<String> marks=new HashSet<>();
            Map<String,PlaylumiLearningProfile> profiles=new HashMap<>();
            List<PlaylumiGameCatalog.Entry> entries=PlaylumiGameCatalog.entries(age);
            for(PlaylumiGameCatalog.Entry e:entries){
                if(random.nextBoolean())rounds.put(e.id,random.nextInt(8));
                if(random.nextBoolean())marks.add(e.id);
                if(random.nextBoolean())saved.put(e.id,random.nextLong());
                int samples=random.nextInt(13),bits=random.nextInt(1<<12);
                profiles.put(e.id,new PlaylumiLearningProfile(age,0,age,bits,samples,samples+random.nextInt(100),0));
            }
            // Deliberately inject an invalid route, which must never enter a visible report.
            rounds.put("9:0:9:9",Integer.MAX_VALUE);marks.add("9:0:9:9");saved.put("9:0:9:9",0L);
            List<PlaylumiParentProgress.World> reports=PlaylumiParentProgress.summarize(age,saved,marks,rounds,profiles);
            require(reports.size()==6,"Six worlds");
            for(PlaylumiParentProgress.World report:reports){
                int world=report.summary.world,expected=0,completed=0,unfinished=0,good=0,samples=0;
                long total=0,counted=0;
                Set<String> ids=new HashSet<>();
                for(PlaylumiGameCatalog.Entry e:entries){
                    if(e.world!=world||e.kind==PlaylumiGameCatalog.DRAW)continue;
                    expected++;int n=rounds.containsKey(e.id)?rounds.get(e.id):0;counted+=n;
                    if(n>0||marks.contains(e.id))completed++;
                    if(saved.containsKey(e.id))unfinished++;
                    if(e.kind==PlaylumiGameCatalog.QUIZ){
                        PlaylumiLearningProfile p=profiles.get(e.id);
                        good+=Integer.bitCount(p.bits);samples+=p.samples;total+=p.total;
                    }
                    ids.add(e.id);
                }
                require(report.games.size()==expected&&report.summary.roundBasedRoutes==expected,"Route count");
                require(report.summary.completedRoutes==completed&&report.summary.unfinishedRoutes==unfinished&&report.summary.countedRounds==counted,"Round totals");
                require(report.samples==samples&&report.firstTry==good&&report.total==total,"Per-quiz aggregation");
                require(report.percent()==(samples<4?-1:(good*100+samples/2)/samples),"Weighted percentage");
                boolean emptySeen=false;
                for(PlaylumiParentProgress.Game game:report.games){
                    require(ids.remove(game.entry.id),"Missing/duplicate game");
                    require(game.entry.kind==PlaylumiGameCatalog.QUIZ||game.samples==0&&game.total==0,"Nonquiz counted as quiz");
                    if(!game.hasHistory())emptySeen=true;else require(!emptySeen,"Actual history not first");
                }
                require(ids.isEmpty(),"Route omitted");checked++;
            }
        }
        for(int mask=0;mask<8;mask++){
            PlaylumiDailyChallenges day=new PlaylumiDailyChallenges(10,10,0,0,0,mask,0);
            int expected=(mask&1)!=0?15:0;expected+=(mask&2)!=0?15:0;expected+=(mask&4)!=0?20:0;expected+=mask==7?20:0;
            require(day.claimedReward()==expected&&day.totalReward()==70,"Reward summary");
        }
        System.out.println("PASS: "+checked+" randomized world reports, exact route and round totals, weighted recent quiz data, and all eight daily reward masks.");
    }
}
