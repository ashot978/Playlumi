package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Curated Dutch spelling in everyday sentences; no dictionary/network dependency. */
public final class PlaylumiSpellingPractice {
    private PlaylumiSpellingPractice(){}
    // Sentence, correct word, three plausible mistakes, strategy, explanation.
    private static final String[][] ITEMS={
        {"De … blaft in de tuin.","hond","hont","hondt","hontd","Maak het woord langer met -en.","Hond wordt honden. Je hoort de d."},
        {"Het … galoppeert in de wei.","paard","paart","pard","paardt","Maak het woord langer met -en.","Paard wordt paarden. Je hoort de d."},
        {"De … ligt in het water.","boot","bot","bood","bootd","Denk aan meer dan één.","Boot wordt boten. Je hoort de t."},
        {"Ik fiets over de … naar huis.","brug","bruch","brugg","bruh","Denk aan het meervoud.","Brug wordt bruggen."},
        {"We reizen met de … naar de stad.","trein","trijn","trejn","treinn","Let op ei of ij.","Trein schrijf je met ei."},
        {"Ik eet een … bij het ontbijt.","ei","ij","eij","ey","Let op ei of ij.","Een ei schrijf je met ei."},
        {"De … schijnt door het raam.","zon","son","zonn","zohn","Luister naar de eerste klank.","Zon begint met z."},
        {"De … springt in de vijver.","kikker","kiker","kikkur","kikerr","Hoor je een korte i?","Na de korte i schrijf je dubbel k."},
        {"We … door de plassen.","rennen","renen","rennun","rènnen","Hoor je een korte e?","Na de korte e schrijf je dubbel n."},
        {"De kinderen … in het zwembad.","zwemmen","zwemen","swemmen","zwemmun","Hoor je een korte e?","Na de korte e schrijf je dubbel m."},
        {"We eten samen een … ijs.","bakje","bakjuh","bakkje","bachje","Let op het stukje -je.","Bak en -je worden bakje."},
        {"De … groeit aan een boom.","appel","apel","appul","appell","Hoor je een korte a?","Na de korte a schrijf je dubbel p."},
        {"De … ligt naast het bed.","wekker","weker","wekkur","wekerr","Hoor je een korte e?","Na de korte e schrijf je dubbel k."},
        {"De … zit in zijn hol.","konijn","konein","konyn","konijjn","Let op ei of ij.","Konijn schrijf je met ij."},
        {"Het … loopt door het gras.","schaap","sgaap","schap","schaab","Let op sch en de lange aa.","Schaap begint met sch."},
        {"Ik zet mijn … op als het regent.","capuchon","kapuchon","capusjon","capuchón","Lees alle letters rustig.","Capuchon begint met c."},
        {"We eten een … met kaas.","boterham","boteram","boterhamm","booterham","Verdeel het woord in stukjes.","Boterham bestaat uit boter en ham."},
        {"De … heeft een lange nek.","giraf","giraff","girav","girafh","Maak het woord langer.","Giraf wordt giraffen."},
        {"Het … vliegt boven de bloemen.","vlindertje","vlindertjuh","vlintertje","vlindertije","Denk aan vlinder en -tje.","Vlinder en -tje worden vlindertje."},
        {"De … zwemt in zee.","dolfijn","dolfein","dolfyn","dolfain","Let op ei of ij.","Dolfijn schrijf je met ij."},
        {"De … heeft een hard schild.","schildpad","schiltpad","schildpat","schilpad","Verdeel het woord in twee woorden.","Schild en pad worden schildpad."},
        {"Het … staat op het schoolbord.","antwoord","antwoort","antword","antwooord","Maak het woord langer.","Antwoord wordt antwoorden. Je hoort de d."},
        {"Ik heb een … in mijn rugzak.","potlood","potloot","potlod","potloodt","Maak het woord langer.","Potlood wordt potloden. Je hoort de d."},
        {"De … ligt op mijn bureau.","schaar","sgaar","schar","schaarr","Let op sch en de lange aa.","Schaar begint met sch."},
        {"We gaan naar de … om brood te halen.","bakker","baker","bakkur","bakerr","Hoor je een korte a?","Na de korte a schrijf je dubbel k."},
        {"De … staan in de tuin.","bloemen","bloemmen","bloumen","bloemun","Denk aan één bloem.","Bloem en -en worden bloemen."},
        {"Het … is open en ik kijk naar buiten.","raam","ram","raamb","raamm","Luister naar de lange aa.","Raam schrijf je met aa."},
        {"Het … valt uit de wolken.","water","waater","watter","waaterh","Zeg wa-ter in stukjes.","Water heeft één a in de open lettergreep wa."}
    };
    private static final String PREFIX="✏️\n";
    public static boolean isContext(String display){return display!=null&&display.startsWith(PREFIX)&&display.contains("…");}
    private static String[] lookup(String display){
        if(!isContext(display))return null;
        String sentence=display.substring(PREFIX.length());
        for(String[] row:ITEMS)if(row[0].equals(sentence))return row;
        return null;
    }
    public static String hint(String display){String[] row=lookup(display);return row==null?"":row[5];}
    public static String explanation(String display,String chosen){
        String[] row=lookup(display);return row!=null&&row[1].equals(chosen)?row[6]:"";
    }
    static int count(){return ITEMS.length;}
    static String answerFor(String display){String[] row=lookup(display);return row==null?"":row[1];}
    public static LearningGame.Round next(Random random){
        String[] row=ITEMS[random.nextInt(ITEMS.length)];
        ArrayList<String> choices=new ArrayList<>(Arrays.asList(row).subList(1,5));
        Collections.shuffle(choices,random);
        List<Integer> colors=new ArrayList<>();for(String ignored:choices)colors.add(0xff30484f);
        return new LearningGame.Round("Welk woord maakt de zin goed?",PREFIX+row[0],0xff385b86,choices,colors,choices.indexOf(row[1]));
    }
}
