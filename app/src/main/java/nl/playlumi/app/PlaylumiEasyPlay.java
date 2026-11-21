package nl.playlumi.app;

/** Tiny, safe rules for one-tap play. Parents can still choose any level. */
public final class PlaylumiEasyPlay {
    private PlaylumiEasyPlay() {}
    public static int category(int saved) {
        return saved>=0 && saved<PlaylumiScores.CATEGORIES?saved:0;
    }
    public static int variant(int saved) {
        return saved>=0 && saved<5?saved:0;
    }
    public static boolean autoReadDefault(int age) {
        return age==0;
    }
}
