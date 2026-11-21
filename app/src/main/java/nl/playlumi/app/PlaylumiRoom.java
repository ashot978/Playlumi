package nl.playlumi.app;

/** Offline, earnable room inventory. Old five-item masks remain intact. */
public final class PlaylumiRoom {
    public static final int WALL=0,FLOOR=1,FURNITURE=2,DECOR=3,WINDOW=4;
    public static final int SLOTS=5,OPTIONS=7;
    public static final String[] LABELS={"Muurkleuren","Vloerkleden","Meubels","Versieringen","Uitzicht"};
    public static final String[] SLOT_ICONS={"🎨","🧶","🛋️","🎈","🪟"};
    public static final String[][] NAMES={
        {"Witte wolken","Roze droom","Mintgroen","Lavendel","Zonneschijn",
                "Sterrennacht","Ochtendhemel"},
        {"Zacht kleed","Sterrenkleed","Regenboogkleed","Bloemenkleed","Ruimtekleed",
                "Wolkenkleed","Confettikleed"},
        {"Knus kussen","Lieve bank","Boekenkast","Speeltentje","Schommelstoel",
                "Hangmat","Glijbaan"},
        {"Klein plantje","Ballonnen","Feestlichtjes","Tekeningen","Schatkist",
                "Maanlamp","Kleine draak"},
        {"Zonnige dag","Maanlicht","Regenboog","Boszicht","Onder water",
                "Sterrenlucht","Magisch bos"}
    };
    public static final String[][] ICONS={
        {"☁️","🩷","💚","💜","🌞","🌌","🌅"},
        {"🧶","⭐","🌈","🌸","🚀","☁️","🎉"},
        {"🛏️","🛋️","📚","⛺","🪑","🏕️","🛝"},
        {"🌱","🎈","✨","🖼️","🎁","🌙","🐉"},
        {"☀️","🌙","🌈","🌳","🐠","🌌","🧚"}
    };
    public static final int[][] PRICES={
        {0,15,27,43,60,79,100},
        {0,18,33,50,68,88,112},
        {0,22,38,58,82,110,143},
        {0,14,27,43,64,89,113},
        {0,17,31,48,70,94,123}
    };
    public static final int[] WALL_COLORS={
        0xfff8f0e9,0xffffddea,0xffdaf4e4,0xffe7dbff,0xffffecb9,
        0xffd5d7f7,0xffffe8cb
    };
    public static final int[] FLOOR_COLORS={
        0xfff6e7ca,0xffd2d9fb,0xfffce3dc,0xffd5efcb,0xffa9badf,
        0xffe8efff,0xfff8e5bc
    };
    private final int[] ownedMasks=new int[SLOTS],selected=new int[SLOTS];
    public PlaylumiRoom() {
        for(int i=0;i<SLOTS;i++)ownedMasks[i]=1;
    }
    private static void check(int slot,int item){
        if(slot<0||slot>=SLOTS||item<0||item>=OPTIONS)
            throw new IllegalArgumentException("Unknown decoration");
    }
    public int selected(int slot){check(slot,0);return selected[slot];}
    public int ownedMask(int slot){check(slot,0);return ownedMasks[slot];}
    public boolean owned(int slot,int item){
        check(slot,item);return (ownedMasks[slot]&(1<<item))!=0;
    }
    public int cost(int slot,int item) {
        check(slot,item);return owned(slot,item)?0:PRICES[slot][item];
    }
    /** Call only after spending earned stars in the wallet. */
    public void unlockAndSelect(int slot,int item){
        check(slot,item);
        ownedMasks[slot]|=1<<item;
        selected[slot]=item;
    }
    public void select(int slot,int item){
        check(slot,item);
        if(!owned(slot,item))throw new IllegalStateException("Locked decoration");
        selected[slot]=item;
    }
    public void restore(int slot,int mask,int choice){
        check(slot,0);
        ownedMasks[slot]=(mask&((1<<OPTIONS)-1))|1;
        selected[slot]=choice>=0&&choice<OPTIONS&&owned(slot,choice)?choice:0;
    }
    public int ownedItems(){
        int total=0;
        for(int slot=0;slot<SLOTS;slot++)total+=Integer.bitCount(ownedMasks[slot]);
        return total;
    }
}
