package nl.playlumi.app;

import java.util.Locale;

/** Pure Dutch voice preference logic; does not download voices. */
public final class PlaylumiVoiceChoice {
    private PlaylumiVoiceChoice() {}
    public static int rank(Locale locale,boolean requiresNetwork,int quality) {
        if(locale==null || requiresNetwork ||
                !"nl".equalsIgnoreCase(locale.getLanguage()))return -1;
        int localeScore="NL".equalsIgnoreCase(locale.getCountry())?10000:
                "BE".equalsIgnoreCase(locale.getCountry())?5000:1000;
        return localeScore+Math.max(0,Math.min(500,quality));
    }
}
