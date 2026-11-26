package nl.playlumi.app;

/** Permanent, offline, profile-specific milestones; no timers or random rewards. */
public final class PlaylumiCollection {
    public static final int[] GOALS={1,5,15};
    public static final String[][] NAMES={
        {"Maansteen","Sterrenkompas","Gouden raket"},
        {"Toverletter","Boekenjuweel","Gouden veer"},
        {"Kristalvorm","Vormensleutel","Kasteeljuweel"},
        {"Muzieknoot","Toverbel","Gouden harp"},
        {"Ballonschat","Dierenmedaille","Gouden ster"},
        {"Kleurkristal","Regenboogsteen","Gouden palet"}
    };
    public static final String[][] ICONS={
        {"🌙","🧭","🚀"},{"🔤","📚","🪶"},{"🔷","🗝️","🏰"},
        {"🎵","🔔","🎶"},{"🎈","🐾","⭐"},{"💎","🌈","🎨"}
    };
    private PlaylumiCollection(){}
    public static int clamp(int count){return Math.max(0,Math.min(GOALS[2],count));}
    public static int advance(int count,int gained){
        int safe=clamp(count);
        return safe+Math.min(GOALS[2]-safe,Math.max(0,gained));
    }
    public static int unlocked(int count){
        int n=0;for(int goal:GOALS)if(count>=goal)n++;return n;
    }
}
