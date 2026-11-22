package nl.playlumi.app;

/**
 * A tiny token gate that cancels all delayed sound jobs from earlier replays.
 * Used on Android's main thread, and stores no child data.
 */
public final class PlaylumiMelodyPlayback {
    private long generation;
    public long begin() {
        if(generation==Long.MAX_VALUE)generation=0;
        return ++generation;
    }
    public void stop() {
        if(generation==Long.MAX_VALUE)generation=0;
        generation++;
    }
    public boolean isActive(long token) {return token>0 && token==generation;}
}
