package nl.playlumi.app;

/** Offline, temporary visual previews and simple ownership filters. */
public final class PlaylumiTryOn {
    public static final int ALL=0, OWNED=1, TO_UNLOCK=2;
    public static final int FILTERS=3;
    public static final String[] FILTER_LABELS={"Alles","Van mij","Te sparen"};
    private PlaylumiTryOn() {}

    public static boolean showItem(boolean owned,int filter) {
        checkFilter(filter);
        return filter==ALL || (filter==OWNED && owned) ||
                (filter==TO_UNLOCK && !owned);
    }

    /** A separate in-memory copy; never purchases or equips the real wardrobe. */
    public static PlaylumiWardrobe previewWardrobe(PlaylumiWardrobe original,
                                                   int slot,int item) {
        if(original==null)throw new IllegalArgumentException("wardrobe");
        if(slot<0 || slot>=PlaylumiWardrobe.SLOTS || item<-1 ||
                item>=PlaylumiWardrobe.OPTIONS)
            throw new IllegalArgumentException("preview slot/item");
        PlaylumiWardrobe display=new PlaylumiWardrobe(original.stars());
        display.restoreBalance(original.stars(),original.allTimeStars());
        for(int i=0;i<PlaylumiWardrobe.SLOTS;i++) {
            int choice=(i==slot && item>=0)?item:original.equipped(i);
            int mask=original.ownedMask(i) | ((i==slot && item>=0)?(1<<item):0);
            display.restoreSlot(i,mask,choice);
        }
        return display;
    }

    /** A separate in-memory copy of the child's existing room. */
    public static PlaylumiRoom previewRoom(PlaylumiRoom original,int slot,int item) {
        if(original==null)throw new IllegalArgumentException("room");
        if(slot<0 || slot>=PlaylumiRoom.SLOTS || item<-1 ||
                item>=PlaylumiRoom.OPTIONS)
            throw new IllegalArgumentException("preview slot/item");
        PlaylumiRoom display=new PlaylumiRoom();
        for(int i=0;i<PlaylumiRoom.SLOTS;i++) {
            int choice=(i==slot && item>=0)?item:original.selected(i);
            int mask=original.ownedMask(i) | ((i==slot && item>=0)?(1<<item):0);
            display.restore(i,mask,choice);
        }
        return display;
    }

    public static int wardrobeCount(PlaylumiWardrobe wardrobe,int slot,int filter) {
        if(wardrobe==null)throw new IllegalArgumentException("wardrobe");
        checkFilter(filter);
        int count=0;
        for(int i=0;i<PlaylumiWardrobe.OPTIONS;i++)
            if(showItem(wardrobe.owned(slot,i),filter))count++;
        return count;
    }

    public static int roomCount(PlaylumiRoom room,int slot,int filter) {
        if(room==null)throw new IllegalArgumentException("room");
        checkFilter(filter);
        int count=0;
        for(int i=0;i<PlaylumiRoom.OPTIONS;i++)
            if(showItem(room.owned(slot,i),filter))count++;
        return count;
    }

    public static String previewLabel(boolean owned,String name) {
        return owned?"✨ Al van jou: "+name:"👀 Eerst bekijken: "+name;
    }

    private static void checkFilter(int filter) {
        if(filter<0 || filter>=FILTERS)
            throw new IllegalArgumentException("filter");
    }
}
