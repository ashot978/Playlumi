package nl.playlumi.app;

/** Naming contract that preserves the original player's storage during multi-profile rollout. */
public final class PlaylumiProfiles {
    public static final int COUNT=3;
    public static final String[] ICONS={"⭐","🌸","🚀"};
    private PlaylumiProfiles() {}
    public static int safe(int selected) {
        return selected>=0 && selected<COUNT?selected:0;
    }
    /** Null denotes legacy Activity.getPreferences() for player one. */
    public static String preferences(int profile) {
        validate(profile);
        return profile==0?null:"playlumi-child-"+profile;
    }
    public static String artworkFile(int profile) {
        validate(profile);
        return profile==0?"playlumi-kunstwerk.png":"playlumi-kunstwerk-"+profile+".png";
    }
    public static void validate(int profile) {
        if(profile<0 || profile>=COUNT)throw new IllegalArgumentException("profile");
    }
}
