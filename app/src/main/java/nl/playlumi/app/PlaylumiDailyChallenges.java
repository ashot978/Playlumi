package nl.playlumi.app;

/** Three optional local-calendar challenges; no login, server, streak penalty or timer.
 * One event = one genuinely credited catalog quiz answer. Persist with its wallet update. */
public final class PlaylumiDailyChallenges {
    public static final int COUNT=3, ANSWERS=4, BONUS=20;
    private static final int[] WORLDS={0,1,2,4,5}; // each offers quizzes at every supported age
    private static final int[] REWARDS={15,15,20};
    public final long day;
    private final int[] progress=new int[COUNT];
    private int mask,completedSets;
    public PlaylumiDailyChallenges(long today,long savedDay,int a,int b,int rounds,int claimed,int sets){
        day=Math.max(today,savedDay);
        completedSets=Math.max(0,sets);
        if(day==savedDay){
            int[] old={a,b,rounds};
            for(int i=0;i<COUNT;i++)progress[i]=Math.max(0,Math.min(goal(i),old[i]));
            mask=claimed&7;
            // Never pay again if a restored snapshot already records completion.
            for(int i=0;i<COUNT;i++)if(progress[i]==goal(i)||(mask&(1<<i))!=0){
                progress[i]=goal(i);mask|=1<<i;
            }
        }
    }
    public int world(int index){
        int first=(int)Math.floorMod(day,5L);
        if(index==0)return WORLDS[first];
        if(index==1)return WORLDS[(first+1+(int)Math.floorMod(Math.floorDiv(day,5L),4L))%5];
        return -1;
    }
    public int goal(int i){return i==2?1:ANSWERS;}
    public int reward(int i){return REWARDS[i];}
    public int progress(int i){return progress[i];}
    public int mask(){return mask;}
    public int completed(){return Integer.bitCount(mask);}
    public int totalReward(){int total=BONUS;for(int reward:REWARDS)total+=reward;return total;}
    public int claimedReward(){int total=mask==7?BONUS:0;for(int i=0;i<COUNT;i++)if((mask&(1<<i))!=0)total+=reward(i);return total;}
    public int completedSets(){return completedSets;}
    public String title(int i){return i==2?"Speel een hele quizronde":PlaylumiWorldStyle.NAMES[world(i)];}
    public String description(int i){return i==2?"Beantwoord alle vragen in een quizronde.":"Geef "+ANSWERS+" goede antwoorden in deze wereld.";}
    /** Returns only newly earned stars; repeated events after a goal give no challenge bonus. */
    public int correct(int world,boolean finished){
        if(!PlaylumiWorldStyle.valid(world))return 0;
        int before=mask,earned=0;
        for(int i=0;i<COUNT;i++){
            if((mask&(1<<i))!=0)continue;
            if(i==2?finished:world(i)==world){
                progress[i]=Math.min(goal(i),progress[i]+1);
                if(progress[i]==goal(i)){mask|=1<<i;earned+=reward(i);}
            }
        }
        if(before!=7&&mask==7){
            earned+=BONUS;
            if(completedSets<Integer.MAX_VALUE)completedSets++;
        }
        return earned;
    }
}
