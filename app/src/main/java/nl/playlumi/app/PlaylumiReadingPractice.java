package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/** Literal reading comprehension: every answer is supported by one explicit sentence. */
public final class PlaylumiReadingPractice {
    public static final int MODE=LearningGame.WORDS,VARIANT=5;
    private static final int INK=0xff3d195f;
    // Question, two-sentence story, answer, three distractors, supporting sentence.
    // Eight who, eight what and eight where questions; no knowledge outside the story is needed.
    private static final String[][] STORIES={
        {"Wie krijgt de bal?","Mila draagt de tas.\nSam krijgt de bal.",
            "Sam","Mila","Noor","Jules","Sam krijgt de bal."},
        {"Wie tekent het huis?","Noor tekent een boot.\nJules tekent het huis.",
            "Jules","Noor","Mila","Luca","Jules tekent het huis."},
        {"Wie leest het boek?","Sam leest het boek.\nMila bekijkt een foto.",
            "Sam","Mila","Noor","Luca","Sam leest het boek."},
        {"Wie haalt de borden?","Luca zet de bekers klaar.\nNoor haalt de borden.",
            "Noor","Luca","Jules","Sam","Noor haalt de borden."},
        {"Wie kiest de peer?","Jules pakt een appel.\nSam kiest de peer.",
            "Sam","Jules","Mila","Noor","Sam kiest de peer."},
        {"Wie bouwt de brug?","Mila bouwt de brug.\nLuca bouwt een toren.",
            "Mila","Luca","Sam","Jules","Mila bouwt de brug."},
        {"Wie vindt de sleutel?","Noor vindt de sleutel.\nJules zoekt zijn muts.",
            "Noor","Jules","Sam","Mila","Noor vindt de sleutel."},
        {"Wie draagt de rode tas?","Jules draagt de groene tas.\nNoor draagt de rode tas.",
            "Noor","Jules","Mila","Luca","Noor draagt de rode tas."},

        {"Wat pakt Mila?","Mila pakt de rode tas.\nSam pakt de blauwe jas.",
            "De rode tas","De blauwe jas","Het gele boek","De groene bal","Mila pakt de rode tas."},
        {"Wat tekent Noor?","Jules tekent een huis.\nNoor tekent een boot.",
            "Een boot","Een huis","Een boom","Een fiets","Noor tekent een boot."},
        {"Wat kiest Sam?","Sam kiest een peer.\nLuca kiest een appel.",
            "Een peer","Een appel","Een banaan","Een wortel","Sam kiest een peer."},
        {"Wat zoekt Jules?","Noor zoekt haar jas.\nJules zoekt zijn muts.",
            "Zijn muts","Haar jas","Een sleutel","Een bal","Jules zoekt zijn muts."},
        {"Wat draagt Luca?","Mila draagt een doos.\nLuca draagt een mand.",
            "Een mand","Een doos","Een tas","Een boek","Luca draagt een mand."},
        {"Wat leest Mila?","Mila leest een brief.\nSam leest een boek.",
            "Een brief","Een boek","Een kaart","Een krant","Mila leest een brief."},
        {"Wat vindt Noor?","Noor vindt een veer.\nJules vindt een steen.",
            "Een veer","Een steen","Een schelp","Een blad","Noor vindt een veer."},
        {"Wat ziet Sam?","Luca ziet een boot.\nSam ziet een trein.",
            "Een trein","Een boot","Een bus","Een fiets","Sam ziet een trein."},

        {"Waar ligt de bal?","De bal ligt onder de stoel.\nHet boek ligt op de tafel.",
            "Onder de stoel","Op de tafel","Op de stoel","Onder de tafel","De bal ligt onder de stoel."},
        {"Waar ligt de sjaal?","De jas hangt aan de kapstok.\nDe sjaal ligt in de mand.",
            "In de mand","Aan de kapstok","Naast de mand","Onder de jas","De sjaal ligt in de mand."},
        {"Waar staat de beker?","De beker staat naast de kan.\nDe kan staat op de tafel.",
            "Naast de kan","Onder de tafel","In de kan","Achter de deur","De beker staat naast de kan."},
        {"Waar ligt het potlood?","Het boek ligt in de tas.\nHet potlood ligt op de tafel.",
            "Op de tafel","In de tas","Onder het boek","Naast de tas","Het potlood ligt op de tafel."},
        {"Waar staat de doos?","De doos staat onder de tafel.\nDe mand staat naast de stoel.",
            "Onder de tafel","Naast de stoel","Op de tafel","In de mand","De doos staat onder de tafel."},
        {"Waar hangt de jas?","De jas hangt aan de kapstok.\nDe muts ligt op de stoel.",
            "Aan de kapstok","Op de stoel","Onder de muts","Naast de deur","De jas hangt aan de kapstok."},
        {"Waar ligt de sleutel?","Het potlood ligt op het boek.\nDe sleutel ligt in het bakje.",
            "In het bakje","Op het boek","Onder het boek","Naast het bakje","De sleutel ligt in het bakje."},
        {"Waar staat de plant?","De stoel staat naast het raam.\nDe plant staat op de kast.",
            "Op de kast","Naast het raam","Onder de stoel","In de kast","De plant staat op de kast."}
    };

    private PlaylumiReadingPractice(){}

    public static LearningGame.Round next(int level,Random random){
        if(level<0||level>2||random==null)throw new IllegalArgumentException("reading configuration");
        // The catalog and learning profile expose this route only to the oldest tier.
        String[] story=STORIES[random.nextInt(STORIES.length)];
        ArrayList<String> choices=new ArrayList<>();for(int i=2;i<=5;i++)choices.add(story[i]);
        Collections.shuffle(choices,random);
        ArrayList<Integer> colors=new ArrayList<>();for(String ignored:choices)colors.add(INK);
        return new LearningGame.Round(story[0],story[1],INK,choices,colors,choices.indexOf(story[2]));
    }

    /** Quote the relevant sentence after solving, rather than adding an unstated inference. */
    public static String explain(LearningGame.Round round){
        if(round==null||!round.isSolved())return "";
        for(String[] story:STORIES)
            if(story[0].equals(round.prompt)&&story[1].equals(round.display))return story[6];
        return "";
    }
}
