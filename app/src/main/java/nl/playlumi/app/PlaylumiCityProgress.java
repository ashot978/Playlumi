package nl.playlumi.app;

/** Presentation of earned city milestones. Never owns stars, purchases or an unlock timer. */
public final class PlaylumiCityProgress {
    private PlaylumiCityProgress(){}
    public static int builtMask(long achievements){
        int mask=0;
        for(int i=0;i<PlaylumiCityRewards.ALL.length;i++)
            if(PlaylumiCityRewards.earned(achievements,PlaylumiCityRewards.ALL[i]))mask|=1<<i;
        return mask;
    }
    public static int unseen(long achievements,int seen){return builtMask(achievements)&~seen;}
    public static int progress(int index,long achievements,int[] stats){
        if(index<0||index>=PlaylumiCityRewards.ALL.length)return 0;
        PlaylumiCityRewards.Landmark landmark=PlaylumiCityRewards.ALL[index];
        PlaylumiAchievements.Milestone milestone=PlaylumiAchievements.ALL[landmark.achievement];
        if(PlaylumiCityRewards.earned(achievements,landmark))return milestone.goal;
        int raw=stats!=null&&milestone.metric<stats.length?stats[milestone.metric]:0;
        return Math.max(0,Math.min(milestone.goal,raw));
    }
    /** Suggest a nearby goal from actual achievement progress, keeping stable ties. */
    public static int next(long achievements,int[] stats){
        int best=-1,bestProgress=0,bestGoal=1;
        for(int i=0;i<PlaylumiCityRewards.ALL.length;i++){
            PlaylumiCityRewards.Landmark landmark=PlaylumiCityRewards.ALL[i];
            if(PlaylumiCityRewards.earned(achievements,landmark))continue;
            int p=progress(i,achievements,stats),goal=PlaylumiAchievements.ALL[landmark.achievement].goal;
            if(best<0||(long)p*bestGoal>(long)bestProgress*goal){best=i;bestProgress=p;bestGoal=goal;}
        }
        return best;
    }
}
