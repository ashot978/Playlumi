package nl.playlumi.app;

/**
 * Three independent local musical sketches belonging to one child profile.
 * Slot zero retains the legacy 0.49 storage key, without any migration loss.
 */
public final class PlaylumiMelodyAlbum {
    public static final int SLOTS=3;
    private static final String[] KEYS={
        "music_box_composer",
        "music_box_composer_2",
        "music_box_composer_3"
    };
    private static final String[] TITLES={
        "⭐ Sterrenliedje","🌼 Bloemenliedje","🌙 Maanliedje"
    };
    private final PlaylumiMusicComposer[] songs=new PlaylumiMusicComposer[SLOTS];
    private int selected;

    public PlaylumiMelodyAlbum(){
        for(int i=0;i<SLOTS;i++)songs[i]=new PlaylumiMusicComposer();
    }

    public static PlaylumiMelodyAlbum restore(String legacySong,
                                             String secondSong,String thirdSong,
                                             int currentSlot) {
        PlaylumiMelodyAlbum album=new PlaylumiMelodyAlbum();
        album.songs[0]=PlaylumiMusicComposer.restore(legacySong);
        album.songs[1]=PlaylumiMusicComposer.restore(secondSong);
        album.songs[2]=PlaylumiMusicComposer.restore(thirdSong);
        album.selected=safeSlot(currentSlot);
        return album;
    }

    public static int safeSlot(int slot) {
        return slot>=0 && slot<SLOTS?slot:0;
    }
    private static void checkSlot(int slot) {
        if(slot<0||slot>=SLOTS)throw new IllegalArgumentException("melody slot");
    }
    public static String key(int slot){checkSlot(slot);return KEYS[slot];}
    public static String title(int slot){checkSlot(slot);return TITLES[slot];}
    public void select(int slot){checkSlot(slot);selected=slot;}
    public int selected(){return selected;}
    public PlaylumiMusicComposer current(){return songs[selected];}
    public int noteCount(int slot){checkSlot(slot);return songs[slot].size();}
    public String savedSong(int slot){checkSlot(slot);return songs[slot].encode();}
}
