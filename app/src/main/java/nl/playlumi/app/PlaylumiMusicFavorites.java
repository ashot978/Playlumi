package nl.playlumi.app;

import java.util.Arrays;

/**
 * Compact, private bitset of favorite instrumental tunes for a single child.
 * Values are profile-local SharedPreferences, no account or remote storage.
 */
public final class PlaylumiMusicFavorites {
    public static final String PREF_KEY="music_box_favorites";
    private static final int ALLOWED=(1<<PlaylumiMusicBox.COUNT)-1;
    private int favorites;

    public PlaylumiMusicFavorites(int saved) {
        // Ignore damaged negative values and unused high bits.
        favorites=saved<0?0:(saved&ALLOWED);
    }

    private static void validate(int index) {
        if(index<0 || index>=PlaylumiMusicBox.COUNT)
            throw new IllegalArgumentException("music box track");
    }

    public boolean isFavorite(int index) {
        validate(index);
        return (favorites&(1<<index))!=0;
    }
    public boolean toggle(int index) {
        validate(index);
        favorites^=1<<index;
        return isFavorite(index);
    }
    public int mask() {return favorites;}
    public int count() {return Integer.bitCount(favorites);}
    public int visibleCount(boolean favoritesOnly) {
        return favoritesOnly?count():PlaylumiMusicBox.COUNT;
    }
    public int pages(boolean favoritesOnly) {
        int count=visibleCount(favoritesOnly);
        return Math.max(1,(count+PlaylumiMusicBox.PAGE_SIZE-1)/
                PlaylumiMusicBox.PAGE_SIZE);
    }
    public int[] onPage(boolean favoritesOnly,int page) {
        int[] listed=new int[visibleCount(favoritesOnly)];
        int n=0;
        for(int i=0;i<PlaylumiMusicBox.COUNT;i++)
            if(!favoritesOnly || isFavorite(i))listed[n++]=i;
        int offset=Math.max(0,page)*PlaylumiMusicBox.PAGE_SIZE;
        if(offset>=listed.length)return new int[0];
        return Arrays.copyOfRange(listed,offset,Math.min(listed.length,
                offset+PlaylumiMusicBox.PAGE_SIZE));
    }
}
