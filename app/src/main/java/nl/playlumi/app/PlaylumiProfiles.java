package nl.playlumi.app;

/** Fixed anonymous local players; display helpers never change their legacy storage identity. */
public final class PlaylumiProfiles {
    public static final int COUNT=3;
    public static final String[] ICONS={"⭐","🌸","🚀"};
    private static final String[] AGE_LABELS={"3–4 jaar","5–6 jaar","7+ jaar"};
    private PlaylumiProfiles() {}
    public static int safe(int selected) {
        return selected>=0 && selected<COUNT?selected:0;
    }
    public static String label(int profile) {
        validate(profile);
        return "Speler "+(profile+1);
    }
    /** Missing or damaged age values require a choice, rather than selecting a different age. */
    public static boolean hasChosenAge(int age) {
        return age>=0 && age<AGE_LABELS.length;
    }
    public static String ageLabel(int age) {
        return hasChosenAge(age)?AGE_LABELS[age]:"Leeftijd nog kiezen";
    }
    /**
     * Only describe the supplied saved balance. Null means no stored wallet is known;
     * this preview must not invent starter stars or perform the legacy wallet migration.
     * An unknown age does not mean the player's other saved progress is empty.
     */
    public static String summary(int age,Integer storedStars) {
        String description=ageLabel(age);
        if(storedStars==null)return description;
        int stars=Math.max(0,storedStars);
        return description+" · "+stars+(stars==1?" speelster":" speelsterren");
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
