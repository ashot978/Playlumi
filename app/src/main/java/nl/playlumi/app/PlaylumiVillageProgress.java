package nl.playlumi.app;

/** Profile-specific village discovery state; no network or account required. */
public final class PlaylumiVillageProgress {
    public static final int BUILDINGS=6;
    private PlaylumiVillageProgress() {}
    public static int markVisited(int mask,int building) {
        if(building<0 || building>=BUILDINGS) return mask;
        return mask | (1<<building);
    }
    public static boolean isVisited(int mask,int building) {
        return building>=0 && building<BUILDINGS && (mask & (1<<building))!=0;
    }
    public static int visitedCount(int mask) {
        return Integer.bitCount(mask & ((1<<BUILDINGS)-1));
    }
    public static int sanitize(int mask) {
        return mask & ((1<<BUILDINGS)-1);
    }
}
