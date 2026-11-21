package nl.playlumi.app;

/** Optional, child-friendly playtime break reminders. Does not lock or punish children. */
public final class PlaylumiUsage {
    private int todaySeconds,limitMinutes;
    private boolean breakShown;
    public void restore(int seconds,int limit,boolean shown) {
        todaySeconds=Math.max(0,seconds);
        setLimit(limit);
        breakShown=shown;
    }
    public void addSeconds(int seconds) {
        if(seconds<0)throw new IllegalArgumentException("negative time");
        todaySeconds+=Math.min(seconds,Integer.MAX_VALUE-todaySeconds);
    }
    public int seconds(){return todaySeconds;}
    public int minutes(){return todaySeconds/60;}
    public int limit(){return limitMinutes;}
    public boolean breakShown(){return breakShown;}
    public void setLimit(int value) {
        if(value!=0 && value!=15 && value!=30 && value!=45 && value!=60)
            throw new IllegalArgumentException("unsupported playtime limit");
        limitMinutes=value;
    }
    public void startNewDay() {
        todaySeconds=0;
        breakShown=false;
    }
    public boolean shouldSuggestBreak() {
        if(limitMinutes==0 || breakShown || todaySeconds<limitMinutes*60)return false;
        breakShown=true;
        return true;
    }
}
