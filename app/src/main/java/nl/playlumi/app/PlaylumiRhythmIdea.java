package nl.playlumi.app;

/** Four original, offline rhythm ideas with intentional silent beats. */
public final class PlaylumiRhythmIdea {
    private static final int[][] IDEAS={
        {0,1,PlaylumiMusicComposer.REST,2},
        {2,PlaylumiMusicComposer.REST,3,2},
        {0,0,PlaylumiMusicComposer.REST,3},
        {3,2,1,PlaylumiMusicComposer.REST}
    };
    public static final int COUNT=4;
    private PlaylumiRhythmIdea(){}

    public static int[] notes(int index){
        if(index<0 || index>=COUNT)throw new IllegalArgumentException("idea");
        return java.util.Arrays.copyOf(IDEAS[index],IDEAS[index].length);
    }

    /**
     * Atomic append: if the current song lacks space, do not touch its notes.
     * No sound is played and nothing is saved here.
     */
    public static boolean append(PlaylumiMusicComposer song,int ideaIndex){
        if(song==null)throw new IllegalArgumentException("song");
        int[] pattern=notes(ideaIndex);
        if(song.size()>PlaylumiMusicComposer.MAX_NOTES-pattern.length)
            return false;
        for(int note:pattern) {
            if(!song.add(note))throw new IllegalStateException("rhythm capacity");
        }
        return true;
    }
}
