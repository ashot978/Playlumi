package nl.playlumi.app;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/** Specific weather problems instead of asking which of several sensible actions is 'right'. */
public final class PlaylumiWeatherPractice {
    private static final String[][] TASKS={
        {"☀️ zonnig","Wat beschermt je ogen tegen fel zonlicht?","zonnebril","regenlaarzen","sjaal","handschoenen","zwembandjes","rugzak"},
        {"🌧️ regen","Wat houdt je hoofd droog als het regent?","paraplu","zonnebril","sneeuwschep","slee","voetbal","schepnet"},
        {"❄️ sneeuw","Wat kun je gebruiken om sneeuw weg te schuiven?","sneeuwschep","zonnebril","zwemband","vlieger","paraplu","badhanddoek"},
        {"🌬️ wind","Welk speelgoed laat je door de wind vliegen?","vlieger","puzzel","knikker","trein","boek","blok"},
        {"☁️ bewolkt","Wat bedekt de zon in een bewolkte lucht?","wolken","sneeuwpoppen","bomen","zand","gras","sterren"},
        {"⛈️ onweer","Wat doe je als je buiten dichtbij onweer hoort?","Naar binnen gaan","Onder een hoge boom staan","In het water blijven","Een vlieger oplaten","Op een open veld blijven","Een metalen hek vasthouden"}
    };
    private static final String[][] CLOTHES={
        {"☀️ zonnig","Wat beschermt je ogen tegen fel zonlicht?","zonnebril","regenlaarzen","sjaal","handschoenen","zwembandjes","rugzak"},
        {"🌧️ regen","Welke jas is gemaakt om regen tegen te houden?","regenjas","trui","T-shirt","zwemvest","badjas","pyjamajas"},
        {"❄️ koud","Wat houdt je handen warm bij vorst?","handschoenen","sandalen","zonnebril","zwembroek","pet","sokken"},
        {"🌬️ harde wind","Welke jas beschermt speciaal tegen wind?","windjack","T-shirt","zwemvest","badjas","pyjamajas","hesje"}
    };
    private PlaylumiWeatherPractice(){}
    public static boolean legacy(int mode,int variant,String prompt){
        return mode==17&&(variant==1||variant==3)&&("Wat trek je aan bij dit weer?".equals(prompt)||"Wat past bij dit weer?".equals(prompt));
    }
    public static LearningGame.Round next(boolean clothes,int level,Random r){
        String[][] pool=clothes?CLOTHES:TASKS;
        String[] row=pool[r.nextInt(pool.length)];String right=row[2];
        ArrayList<String> wrong=new ArrayList<>(Arrays.asList(row).subList(3,row.length));
        Collections.shuffle(wrong,r);ArrayList<String> options=new ArrayList<>();options.add(right);
        int size=level==0?3:level==1?4:6;
        for(int i=0;i<size-1;i++)options.add(wrong.get(i));
        Collections.shuffle(options,r);ArrayList<Integer> inks=new ArrayList<>();
        for(String ignored:options)inks.add(0xff244454);
        return new LearningGame.Round(row[1],row[0],0xff376c98,options,inks,options.indexOf(right));
    }
}
