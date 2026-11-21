package nl.playlumi.app;

/** Twelve offline, gently educational plants; each needs water and sunlight. */
public final class PlaylumiGarden {
    public static final int COUNT=12,STAGES=4,FIRST_BLOOM_STARS=9;
    public static final int WATER=0,SUN=1;
    public static final String[] NAMES={
        "Tulp","Zonnebloem","Roos","Madeliefje","Cactus","Aardbei",
        "Paddenstoel","Lavendel","Bamboe","Palmboom","Appelboom","Kersenbloesem"
    };
    public static final String[] ICONS={
        "🌷","🌻","🌹","🌼","🌵","🍓","🍄","🪻","🎋","🌴","🍎","🌸"
    };
    public static final String[] FACTS={
        "Tulpen groeien uit een bol in de grond.",
        "Zonnebloemen draaien hun bloemen naar het licht.",
        "Rozen hebben vaak stekels op hun takken.",
        "Madeliefjes groeien graag in het gras.",
        "Een cactus kan water bewaren in zijn dikke stengel.",
        "Aardbeien zijn vruchten met kleine zaadjes aan de buitenkant.",
        "Paddenstoelen groeien vaak in vochtige plekken.",
        "Lavendel ruikt heerlijk en trekt bijen aan.",
        "Bamboe is een grassoort die heel snel kan groeien.",
        "Palmbomen groeien op veel warme plaatsen.",
        "Appelbomen krijgen eerst bloemen en daarna appels.",
        "Kersenbloesem bloeit vaak in de lente."
    };
    private final int[] stages=new int[COUNT];
    private int rewardedMask;
    private static void check(int plant){
        if(plant<0 || plant>=COUNT)throw new IllegalArgumentException("plant");
    }
    public int stage(int plant){check(plant);return stages[plant];}
    public boolean bloomed(int plant){return stage(plant)==STAGES;}
    public int growingCount(){
        int count=0;
        for(int stage:stages)if(stage>0)count++;
        return count;
    }
    public int bloomedCount(){
        int count=0;
        for(int stage:stages)if(stage==STAGES)count++;
        return count;
    }
    public int rewardMask(){return rewardedMask;}
    public boolean awarded(int plant){
        check(plant);
        return (rewardedMask&(1<<plant))!=0;
    }
    public void restoreRewards(int mask){rewardedMask=mask&((1<<COUNT)-1);}
    public void restorePlant(int plant,int stage){
        check(plant);
        stages[plant]=Math.max(0,Math.min(STAGES,stage));
    }
    /** Return the required next action: water then sunlight then water then sunlight. */
    public int nextAction(int plant){
        return stage(plant)%2==0?WATER:SUN;
    }
    /** Returns -1 for the wrong action, 0 for growing, 1 first bloom, 2 already bloomed. */
    public int grow(int plant,int action){
        check(plant);
        if(action!=WATER && action!=SUN)throw new IllegalArgumentException("action");
        if(bloomed(plant)){
            if(!awarded(plant)){rewardedMask|=1<<plant;return 1;}
            return 2;
        }
        if(action!=nextAction(plant))return -1;
        stages[plant]++;
        if(bloomed(plant)&&!awarded(plant)){
            rewardedMask|=1<<plant;return 1;
        }
        return 0;
    }
    public String picture(int plant){
        int stage=stage(plant);
        if(stage==0)return "🫘";
        if(stage==1)return "🌱";
        if(stage==2)return "🌿";
        if(stage==3)return "🌾";
        return ICONS[plant];
    }
}
