package nl.playlumi.app;

/**
 * Four clear, consistent child-facing destinations.
 * The screens use native Android buttons: no web views or new permissions.
 */
public final class PlaylumiQuickNav {
    public static final int START=0, PLAY=1, CUSTOMIZE=2, GARDEN=3;
    public static final int COUNT=4;
    public static final String[] TITLES={"Start","Spelen","Aanpassen","Tuin"};
    public static final String[] ICONS={"🏠","🎮","🎨","🌱"};

    private PlaylumiQuickNav(){}

    /** All categories and items are visible in a scrollable grid, no pagination. */
    public static int columnsFor(int screenWidthDp) {
        return screenWidthDp>=600?3:2;
    }

    public static int rowsFor(int itemCount,int columnCount) {
        if(itemCount<0 || columnCount<1)throw new IllegalArgumentException("grid");
        return (itemCount+columnCount-1)/columnCount;
    }

    public static int categoryCount() {
        return PlaylumiWardrobe.SLOTS + PlaylumiRoom.SLOTS;
    }

    /** Text explicitly distinguishes worn, owned and locked items. */
    public static String wardrobeStatus(PlaylumiWardrobe wardrobe,int slot,int item) {
        if(wardrobe.equipped(slot)==item)return "✅ Nu aan";
        if(wardrobe.owned(slot,item))return "✨ Gratis aantrekken";
        return "⭐ "+wardrobe.price(slot,item)+" speelsterren";
    }

    public static String roomStatus(PlaylumiRoom room,int slot,int item) {
        if(room.selected(slot)==item)return "✅ Nu in je kamer";
        if(room.owned(slot,item))return "✨ Gratis gebruiken";
        return "⭐ "+room.cost(slot,item)+" speelsterren";
    }
}
