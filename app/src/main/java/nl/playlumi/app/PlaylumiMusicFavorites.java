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
    /**
     * Pick a track from the visible collection without replaying the previous
     * tune when another choice exists. Return -1 for empty favorites.
     * The caller supplies a draw so selection is easy to test.
     */
    public int surprise(boolean favoritesOnly,int previous,int draw) {
        int count=visibleCount(favoritesOnly);
        if(count==0)return -1;
        int available=count-(count>1 && contains(favoritesOnly,previous)?1:0);
        int choice=Math.floorMod(draw,available);
        for(int track=0;track<PlaylumiMusicBox.COUNT;track++) {
            if(!favoritesOnly || isFavorite(track)) {
                if(count>1 && track==previous)continue;
                if(choice--==0)return track;
            }
        }
        throw new IllegalStateException("No music box track found");
    }

    private boolean contains(boolean favoritesOnly,int track) {
        return track>=0 && track<PlaylumiMusicBox.COUNT &&
                (!favoritesOnly || isFavorite(track));
    }

    /**
     * Navigate the current collection, wrapping at both ends. Missing
     * selections start at the first/last visible track depending on direction.
     * Returning -1 lets the UI handle an empty favorites collection safely.
     */
    public int adjacent(boolean favoritesOnly,int previous,int direction) {
        if(direction!=1 && direction!=-1)
            throw new IllegalArgumentException("direction");
        int count=visibleCount(favoritesOnly);
        if(count==0)return -1;
        int[] tracks=onAll(favoritesOnly);
        int position=-1;
        for(int i=0;i<tracks.length;i++) {
            if(tracks[i]==previous) {position=i;break;}
        }
        if(position<0)return direction>0?tracks[0]:tracks[count-1];
        return tracks[Math.floorMod(position+direction,count)];
    }

    private int[] onAll(boolean favoritesOnly) {
        int[] tracks=new int[visibleCount(favoritesOnly)];
        int n=0;
        for(int track=0;track<PlaylumiMusicBox.COUNT;track++) {
            if(!favoritesOnly || isFavorite(track))tracks[n++]=track;
        }
        return tracks;
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
    /** Move between pages in either direction, including partial last pages. */
    public int movePage(boolean favoritesOnly,int currentPage,int direction) {
        if(direction!=1 && direction!=-1)
            throw new IllegalArgumentException("direction");
        int total=pages(favoritesOnly);
        int safe=currentPage<0 || currentPage>=total?0:currentPage;
        return Math.floorMod(safe+direction,total);
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
