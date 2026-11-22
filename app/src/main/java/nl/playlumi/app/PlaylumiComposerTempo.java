package nl.playlumi.app;

/** Optional, child-safe timing for personally composed music, never autoplay. */
public final class PlaylumiComposerTempo {
    public static final int SLOW=0,NORMAL=1,FAST=2;
    private PlaylumiComposerTempo(){}
    public static int safe(int index){return index>=SLOW && index<=FAST?index:NORMAL;}
    public static int next(int index){return (safe(index)+1)%3;}
    public static String label(int index){
        switch(safe(index)){
            case SLOW:return "🐢 Rustig";
            case FAST:return "⚡ Snel";
            default:return "🎵 Normaal";
        }
    }
    public static int delayMillis(int index){
        switch(safe(index)){
            case SLOW:return 590;
            case FAST:return 300;
            default:return 420;
        }
    }
}
