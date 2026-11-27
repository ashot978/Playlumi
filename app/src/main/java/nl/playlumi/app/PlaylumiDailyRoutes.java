package nl.playlumi.app;

import java.util.ArrayList;
import java.util.List;

/** Daily goals can only be advanced by catalog quizzes. Routing shares that rule. */
public final class PlaylumiDailyRoutes {
    private PlaylumiDailyRoutes(){}
    public static boolean eligible(PlaylumiGameCatalog.Entry entry,PlaylumiDailyChallenges day,int goal){
        return entry!=null&&day!=null&&goal>=0&&goal<PlaylumiDailyChallenges.COUNT&&
                entry.kind==PlaylumiGameCatalog.QUIZ&&(goal==2||entry.world==day.world(goal));
    }
    /** Ordered unfinished ids come from real saved rounds, newest first. No save is discarded. */
    public static PlaylumiGameCatalog.Entry choose(int age,PlaylumiDailyChallenges day,int goal,List<String> unfinished){
        if(day==null||goal<0||goal>=PlaylumiDailyChallenges.COUNT)return null;
        if(unfinished!=null)for(String id:unfinished){
            PlaylumiGameCatalog.Entry entry=PlaylumiGameCatalog.find(age,id);
            if(eligible(entry,day,goal))return entry;
        }
        List<PlaylumiGameCatalog.Entry> candidates=new ArrayList<>();
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age))
            if(eligible(entry,day,goal))candidates.add(entry);
        if(candidates.isEmpty())return null;
        // Floor-mod also handles restored dates and avoids overflow near Long.MAX_VALUE.
        int pick=(int)((Math.floorMod(day.day,(long)candidates.size())+goal)%candidates.size());
        return candidates.get(pick);
    }
}
