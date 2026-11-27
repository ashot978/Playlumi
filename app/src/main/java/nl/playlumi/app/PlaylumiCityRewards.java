package nl.playlumi.app;

/** Permanent city landmarks and gifts linked to existing stable achievement indices.
 * Older purchases remain owned. Landmark unlocks are never consumed or time-limited. */
public final class PlaylumiCityRewards {
    public static final class Landmark {
        public final int achievement,world,slot,item;
        public final String name,icon;
        Landmark(int a,int w,int s,int i,String n,String icon){
            achievement=a;world=w;slot=s;item=i;name=n;this.icon=icon;
        }
        public String requirement(){return PlaylumiAchievements.ALL[achievement].title;}
        public String gift(){return PlaylumiWardrobe.ITEM_NAMES[slot][item];}
    }
    public static final Landmark[] ALL={
        new Landmark(5,-1,PlaylumiWardrobe.FRIENDS,1,"Vriendjespark","🦋"),
        new Landmark(24,0,PlaylumiWardrobe.OUTFITS,6,"Observatorium","🔭"),
        new Landmark(25,1,PlaylumiWardrobe.HATS,3,"Toverbibliotheek","📚"),
        new Landmark(27,3,PlaylumiWardrobe.OUTFITS,4,"Muziekpodium","🎵"),
        new Landmark(28,4,PlaylumiWardrobe.FRIENDS,5,"Dierenpark","🦊"),
        new Landmark(29,5,PlaylumiWardrobe.OUTFITS,5,"Regenboogbrug","🌈"),
        new Landmark(19,-1,PlaylumiWardrobe.OUTFITS,2,"Lumi-standbeeld","🏆")
    };
    private PlaylumiCityRewards(){}
    public static boolean earned(long achievements,Landmark l){return (achievements&(1L<<l.achievement))!=0;}
    public static int count(long achievements){
        int count=0;for(Landmark l:ALL)if(earned(achievements,l))count++;return count;
    }
    public static Landmark forItem(int slot,int item){
        for(Landmark l:ALL)if(l.slot==slot&&l.item==item)return l;return null;
    }
    public static boolean grant(PlaylumiWardrobe wardrobe,long achievements){
        boolean changed=false;
        for(Landmark l:ALL)if(earned(achievements,l))changed=wardrobe.grant(l.slot,l.item)||changed;
        return changed;
    }
}
