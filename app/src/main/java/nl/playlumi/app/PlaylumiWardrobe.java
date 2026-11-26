package nl.playlumi.app;

/**
 * Offline earned-star wallet and persistent mascot wardrobe.
 * Previously owned options (0–4) and their saved bit masks remain compatible.
 */
public final class PlaylumiWardrobe {
    public static final int COLORS=0,HATS=1,GLASSES=2,FRIENDS=3,OUTFITS=4,AURAS=5;
    public static final int SLOTS=6,OPTIONS=7;
    public static final int STARS_FOR_ANSWER=3,STARS_FOR_BADGE=12,STARTER_STARS=18;
    public static final String[] SLOT_NAMES={
            "Medaillonkleur","Hoedjes","Brillen","Vriendjes","Kleding","Magische gloed"
    };
    public static final String[] SLOT_ICONS={"🎨","🎩","🕶️","🐾","👗","✨"};
    public static final String[][] ITEM_NAMES={
            {"Zonnig geel","Suikerspinroze","Muntgroen","Lila","Hemelsblauw",
                    "Koraalrood","Nachtblauw"},
            {"Zonder hoed","Koningskroon","Feesthoed","Toverhoed","Bloemenkrans",
                    "Feestkroon","Ruimtehelm"},
            {"Zonder bril","Zonnebril","Ronde bril","Hartjesbril","Regenboogbril",
                    "Sterrenbril","Vlinderbril"},
            {"Lekker alleen","Vlindervriend","Maantje","Konijntje","Wolkje",
                    "Vosje","Puppy"},
            {"Gewoon mezelf","Strikje","Superheldencape","Warme sjaal","Sterrenpak",
                    "Regenboogtrui","Astronautenpak"},
            {"Zonder glitter","Zacht fonkelen","Hartjesregen","Sterrenstof",
                    "Regenbooglicht","Vuurvliegjes","Maanlicht"}
    };
    public static final String[][] ITEM_ICONS={
            {"💛","🩷","💚","💜","💙","❤️","🩵"},
            {"🌟","👑","🥳","🧙","🌸","🎊","🪐"},
            {"😊","🕶️","👓","💗","🌈","⭐","🦋"},
            {"⭐","🦋","🌙","🐰","☁️","🦊","🐶"},
            {"✨","🎀","🦸","🧣","🚀","🌈","👩‍🚀"},
            {"🌟","✨","💗","⭐","🌈","🪄","🌙"}
    };
    public static final int[][] PRICES={
            {0,12,24,36,50,66,82},
            {0,18,35,55,75,100,130},
            {0,15,30,48,65,89,118},
            {0,16,34,52,72,96,125},
            {0,18,38,58,78,105,135},
            {0,23,43,67,91,117,145}
    };
    public static final int[] BODY_COLORS={
            0xffffd557,0xffffabd8,0xff91e3ad,0xffc2a2ef,0xff8dd7f6,
            0xffff8c8a,0xff7c8eca
    };
    public static final int PURCHASED=2,EQUIPPED=1,NOT_ENOUGH=0;
    private final int[] ownedMasks=new int[SLOTS];
    private final int[] selected=new int[SLOTS];
    private int stars,allTimeStars;
    public PlaylumiWardrobe(){this(STARTER_STARS);}
    public PlaylumiWardrobe(int start){
        if(start<0)throw new IllegalArgumentException("negative stars");
        stars=start;allTimeStars=start;
        for(int i=0;i<SLOTS;i++)ownedMasks[i]=1;
    }
    private static void check(int slot,int item){
        if(slot<0 || slot>=SLOTS || item<0 || item>=OPTIONS)
            throw new IllegalArgumentException("unknown outfit");
    }
    public int stars(){return stars;}
    public int allTimeStars(){return allTimeStars;}
    public int equipped(int slot){check(slot,0);return selected[slot];}
    public int[] equippedItems(){return selected.clone();}
    public int ownedMask(int slot){check(slot,0);return ownedMasks[slot];}
    public boolean owned(int slot,int item) {
        check(slot,item);return (ownedMasks[slot]&(1<<item))!=0;
    }
    public int price(int slot,int item){check(slot,item);return PRICES[slot][item];}
    public void restoreBalance(int balance,int earned){
        stars=Math.max(0,balance);allTimeStars=Math.max(stars,earned);
    }
    public void restoreSlot(int slot,int owned,int worn){
        check(slot,0);
        ownedMasks[slot]=(owned&((1<<OPTIONS)-1))|1;
        selected[slot]=(worn>=0 && worn<OPTIONS && owned(slot,worn))?worn:0;
    }
    public void earnAnswer(){add(STARS_FOR_ANSWER);}
    public void earnAdventureBonus(){add(PlaylumiAdventure.BONUS);}
    public void earnStoryBonus(){add(15);}
    public void earnCreativeBonus(){add(9);}
    public void earnFriendBonus(){add(PlaylumiFriends.FIRST_COMPLETE_BONUS);}
    public void earnGardenBonus(){add(PlaylumiGarden.FIRST_BLOOM_STARS);}
    public void earnBadges(int badges){
        if(badges<0 || badges>Integer.MAX_VALUE/STARS_FOR_BADGE)
            throw new IllegalArgumentException("badges out of range");
        add(STARS_FOR_BADGE*badges);
    }
    private void add(int count){
        if(count<0 || stars>Integer.MAX_VALUE-count || allTimeStars>Integer.MAX_VALUE-count)
            throw new IllegalArgumentException("wallet full");
        stars+=count;allTimeStars+=count;
    }
    public boolean spendStars(int cost){
        if(cost<0)throw new IllegalArgumentException("negative cost");
        if(stars<cost)return false;
        stars-=cost;return true;
    }
    /** Buying and equipping once never requires payment again. */
    public int buyOrEquip(int slot,int item){
        check(slot,item);
        if(owned(slot,item)){selected[slot]=item;return EQUIPPED;}
        int cost=price(slot,item);
        if(stars<cost)return NOT_ENOUGH;
        stars-=cost;
        ownedMasks[slot]|=1<<item;
        selected[slot]=item;
        return PURCHASED;
    }
    public int unlockedItems(){
        int count=0;
        for(int m:ownedMasks)count+=Integer.bitCount(m);
        return count;
    }
}
