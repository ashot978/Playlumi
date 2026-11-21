package nl.playlumi.app;

/** Six short unlockable learning missions per world, stored locally on the device. */
public final class PlaylumiAdventure {
    public static final int WORLDS=3, STAGES=6, QUESTIONS_PER_MISSION=6, BONUS=18;
    public static final String[][] TITLES={
        {"Eerste stappen","Kleurenfeest","Vormenbos","Cijferbrug","Patronenpad","Sterrenkasteel"},
        {"Letterland","Dierenwoorden","Spellingbos","Rijmrivier","Plaatjespad","Woordkasteel"},
        {"Weeravontuur","Tijdreis","Dierenwereld","Eetfeest","Meetbrug","Wereldfeest"}
    };
    public static final String[][] ICONS={
        {"🌱","🎨","🔷","🔢","🧩","🏰"},
        {"🔤","🐾","✏️","🎶","🖼️","👑"},
        {"🌦️","🕒","🦁","🥕","📏","🌍"}
    };
    private static final int[][] CATEGORIES={
        {0,1,3,4,5,2},
        {6,7,8,10,9,11},
        {17,16,21,20,22,23}
    };
    private final int[] unlocked = {0,0,0};
    public void restore(int world,int latest) {
        check(world,0);
        unlocked[world]=Math.max(0,Math.min(STAGES,latest));
    }
    private void check(int world,int stage) {
        if(world<0 || world>=WORLDS || stage<0 || stage>=STAGES)
            throw new IllegalArgumentException("Invalid adventure stage");
    }
    public int progress(int world) {check(world,0);return unlocked[world];}
    public boolean available(int world,int stage) {
        check(world,stage);
        return stage<=unlocked[world];
    }
    public boolean completed(int world,int stage) {
        check(world,stage);
        return stage<unlocked[world];
    }
    /** Return true for first completion; repeat stages are always welcome but not rewarded twice. */
    public boolean complete(int world,int stage) {
        check(world,stage);
        if(!available(world,stage))throw new IllegalStateException("Locked stage");
        if(stage<unlocked[world])return false;
        unlocked[world]=Math.min(STAGES,stage+1);
        return true;
    }
    public int category(int world,int stage) {check(world,stage);return CATEGORIES[world][stage];}
    public int variant(int stage) {
        if(stage<0 || stage>=STAGES)throw new IllegalArgumentException("stage");
        return stage%5;
    }
    public boolean worldFinished(int world) {return progress(world)==STAGES;}
}
