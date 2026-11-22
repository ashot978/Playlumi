package nl.playlumi.app;

/** Conservative local speech-rate choices; never triggers playback. */
public final class PlaylumiSpeechSettings {
    public static final int SLOW=0, NORMAL=1, BRISK=2;
    private PlaylumiSpeechSettings() {}

    public static int normalize(int index) {
        return index>=SLOW && index<=BRISK?index:NORMAL;
    }

    public static int next(int index) {
        return (normalize(index)+1)%3;
    }

    public static float rate(int index) {
        switch(normalize(index)) {
            case SLOW:return .85f;
            case BRISK:return 1.12f;
            default:return 1.0f;
        }
    }

    public static String label(int index) {
        switch(normalize(index)) {
            case SLOW:return "Rustig";
            case BRISK:return "Vlot";
            default:return "Normaal";
        }
    }
}
