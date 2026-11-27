package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/** Offline, curated Dutch grammar questions with an unambiguous single answer. */
public final class PlaylumiGrammarPractice {
    // Sentence with blank; correct completion; three deliberate grammatical confusions.
    private static final String[][] QUESTIONS={
        {"Ik … naar school.","fiets","fietst","fietsen","gefietst"},
        {"Jij … naar school.","fietst","fiets","fietsen","gefietst"},
        {"Wij … naar school.","fietsen","fiets","fietst","gefietst"},
        {"Hij … een appel.","eet","eten","eetst","gegeten"},
        {"De kinderen … buiten.","spelen","speelt","speel","gespeeld"},
        {"Het meisje … een boek.","leest","lees","lezen","gelezen"},
        {"Wij … een mooie tekening.","maken","maakt","maak","gemaakt"},
        {"De hond … in de tuin.","rent","rennen","ren","gerend"},
        {"Ik … mijn schoenen aan.","trek","trekt","trekken","getrokken"},
        {"Zij … de deur.","opent","openen","open","geopend"},
        {"Jullie … goed op.","letten","let","let op","gelet"},
        {"De vogels … in de lucht.","vliegen","vliegt","vlieg","gevlogen"},
        {"Morgen … wij naar oma.","gaan","gaat","ga","gegaan"},
        {"Gisteren … ik een boek.","las","lees","leest","lezen"},
        {"Gisteren … hij een bal.","gooide","gooi","gooit","gooien"},
        {"Vorige week … wij naar zee.","gingen","gaan","gaat","ging"},
        {"Ik heb een brief … .","geschreven","schrijf","schreef","schrijft"},
        {"Zij heeft de tafel … .","gedekt","dekt","dekken","dekte"},
        {"Het kind … een rode jas.","draagt","dragen","draag","gedragen"},
        {"Mijn ouders … thuis.","zijn","is","ben","bent"},
        {"Ik … zeven jaar.","ben","is","zijn","bent"},
        {"Jij … een blauwe fiets.","hebt","heeft","heb","hebben"},
        {"Wij … een grote tuin.","hebben","heeft","heb","hebt"},
        {"De boeken … op tafel.","liggen","ligt","lig","gelegen"}
    };
    private PlaylumiGrammarPractice(){}

    public static LearningGame.Round next(int level,Random random){
        if(level<0||level>2||random==null)throw new IllegalArgumentException("grammar");
        int length=level==0?8:level==1?16:QUESTIONS.length;
        String[] item=QUESTIONS[random.nextInt(length)];
        ArrayList<String> answers=new ArrayList<>(Arrays.asList(item).subList(1,5));
        Collections.shuffle(answers,random);
        ArrayList<Integer> inks=new ArrayList<>();
        for(int i=0;i<answers.size();i++)inks.add(0xff30484f);
        return new LearningGame.Round("Welk woord maakt de zin goed?","✏️\n"+item[0],
                0xff385b86,answers,inks,answers.indexOf(item[1]));
    }
}
