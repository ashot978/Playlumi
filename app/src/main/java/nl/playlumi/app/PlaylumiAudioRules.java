package nl.playlumi.app;

/**
 * Child-friendly sound policy: silence decorative effects by default, but
 * never accidentally silence a melody the child explicitly asked to hear.
 */
public final class PlaylumiAudioRules {
    private PlaylumiAudioRules() {}

    public static boolean shouldPlay(boolean soundOn,boolean effectsOn,
                                     boolean intentionalMusic){
        return soundOn && (intentionalMusic || effectsOn);
    }
}
