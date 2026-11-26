package nl.playlumi.app;

/** Only read the instruction and suitable problem text; never announce a correct answer. */
public final class PlaylumiSpeechText {
    private PlaylumiSpeechText(){}
    public static String quiz(int mode,int variant,String prompt,String display){
        String question=clean(prompt);
        if(display==null||display.startsWith("clock:")||display.startsWith("pattern:"))return question;
        if(mode==ExtraGames.MATH||mode==ExtraGames.SEQUENCES||mode==22||
                mode==23&&variant!=2||mode==16&&variant>=1&&variant<=3||mode==19&&variant==4||
                mode==ExtraGames.SPELLING&&PlaylumiSpellingPractice.isContext(display)){
            String problem=clean(display.replace("…"," leeg vak "));
            if(!problem.isEmpty())return question+". "+problem;
        }
        return question;
    }
    static String clean(String source){
        if(source==null)return "";
        return source.replace("−"," min ").replace("+"," plus ").replace("="," is gelijk aan ")
                .replace("→",", ").replace("←"," links ").replace("|"," tegenover ")
                .replaceAll("(?<=\\d)\\s*cm\\b"," centimeter").replaceAll("(?<=\\d)\\s*mm\\b"," millimeter")
                .replace("?", " ? ").replaceAll("[^\\p{L}\\p{N}\\p{P}\\s]"," ")
                .replaceAll("\\s+"," ").trim();
    }
}
