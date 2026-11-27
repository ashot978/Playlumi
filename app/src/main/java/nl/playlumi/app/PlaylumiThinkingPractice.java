package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Three distinct reasoning routes, appended to existing categories without reusing saved IDs. */
public final class PlaylumiThinkingPractice {
    public static final int EQUAL_GROUPS=5, PLACE_VALUE=8, AFTER=5;
    public static final int DAILY_THINGS=19;
    private static final String[] SHAPES={"★","●","◆","♥","✿"};
    private static final int INK=0xff3d195f;

    /** A stated action determines the answer; no assumptions about a child's own routine. */
    private static final String[][] STORIES={
        {"Het bakje is leeg.\nJe legt er een bal in.",
            "Bal in het bakje","Leeg bakje","Bal naast het bakje","Twee ballen in het bakje",
            "De bal zit nu in het bakje."},
        {"De doos is open.\nJe doet het deksel erop.",
            "Gesloten doos","Open doos","Doos zonder deksel","Doos op zijn kop",
            "Met het deksel erop is de doos gesloten."},
        {"De lamp is uit.\nJe doet de lamp aan.",
            "De lamp geeft licht","De lamp blijft uit","De lamp ligt op de grond","De lamp staat buiten",
            "Als je de lamp aan doet, geeft hij licht."},
        {"Het boek is dicht.\nJe slaat het boek open.",
            "Open boek","Dicht boek","Boek zonder bladzijden","Twee boeken",
            "Je kunt nu de bladzijden in het open boek zien."},
        {"De deur is open.\nJe doet de deur dicht.",
            "Gesloten deur","Open deur","Een raam","Twee deuren",
            "De deur is nu gesloten."},
        {"De knuffel zit op de stoel.\nJe legt hem op bed.",
            "Knuffel op bed","Knuffel op de stoel","Knuffel onder bed","Knuffel in de kast",
            "De knuffel is van de stoel naar het bed verhuisd."},
        {"Het glas is leeg.\nJe schenkt er water in.",
            "Glas met water","Leeg glas","Glas met zand","Glas met blokken",
            "Er zit nu water in het glas."},
        {"De tekening ligt op tafel.\nJe hangt hem aan de muur.",
            "Tekening aan de muur","Tekening op tafel","Tekening op de grond","Tekening in een doos",
            "De tekening hangt nu aan de muur."},
        {"De bal ligt in de doos.\nJe legt hem naast de doos.",
            "Bal naast de doos","Bal in de doos","Bal onder de doos","Bal op de doos",
            "De bal ligt buiten de doos, ernaast."},
        {"De klei is een bal.\nJe drukt de klei helemaal plat.",
            "Platte klei","Een ronde kleibal","Losse blokken","Een leeg bord",
            "Door het drukken veranderde de ronde bal in platte klei."},
        {"Je sokken liggen op bed.\nJe trekt ze aan.",
            "Sokken aan je voeten","Sokken op bed","Sokken aan je handen","Sokken in de la",
            "De sokken zitten nu aan je voeten."},
        {"Het potlood ligt in de la.\nJe legt het op tafel.",
            "Potlood op tafel","Potlood in de la","Potlood onder tafel","Potlood in een tas",
            "Het potlood ligt nu op tafel."},
        {"De bal ligt in de doos.\nJe legt hem op de stoel, dan onder de tafel.",
            "Bal onder de tafel","Bal in de doos","Bal op de stoel","Bal op de tafel",
            "Onder de tafel was de laatste plek waar je de bal neerlegde."},
        {"Er liggen een rode en een blauwe bal.\nJe haalt alleen de rode bal weg.",
            "De blauwe bal blijft","De rode bal blijft","Beide ballen blijven","Geen bal blijft",
            "Je haalde alleen de rode bal weg. De blauwe bleef liggen."},
        {"Het boek ligt op tafel.\nJe legt het in je tas en sluit de tas.",
            "Boek in gesloten tas","Boek op tafel","Boek in open tas","Boek onder de tas",
            "Het boek zit in de tas, en daarna heb je de tas gesloten."},
        {"Er staat een toren van blokken.\nJe haalt alle blokken los.",
            "Losse blokken","Dezelfde toren","Een hogere toren","Geen blokken meer",
            "De blokken zijn er nog, maar vormen geen toren meer."},
        {"Er liggen een blauwe en een gele kaart.\nJe schuift de gele onder de blauwe.",
            "Blauwe kaart bovenop","Gele kaart bovenop","Kaarten naast elkaar","Alleen een gele kaart",
            "De gele kaart ligt onder de blauwe. De blauwe ligt dus bovenop."},
        {"Op het papier staat een huis.\nJe tekent er een boom naast.",
            "Huis en boom","Alleen een huis","Alleen een boom","Leeg papier",
            "Het huis bleef staan en de boom kwam erbij."},
        {"De doos is leeg.\nJe stopt er een auto in en sluit de doos.",
            "Auto in gesloten doos","Lege gesloten doos","Auto naast de doos","Auto in open doos",
            "De auto zit erin en het deksel is dicht."},
        {"Een vel papier is heel.\nJe scheurt het in twee stukken.",
            "Twee stukken papier","Een heel vel","Geen papier meer","Drie stukken papier",
            "Het papier is niet weg: je hebt er twee stukken van gemaakt."},
        {"Je draagt een muts en een jas.\nJe doet alleen je muts af.",
            "Jas aan, muts af","Muts op, jas uit","Muts op, jas aan","Muts af, jas uit",
            "Alleen de muts ging af. Je jas bleef aan."},
        {"De lamp is aan.\nJe doet hem uit en daarna weer aan.",
            "De lamp geeft licht","De lamp is uit","De lamp is verdwenen","De lamp ligt op tafel",
            "De laatste stap was aan doen. De lamp geeft dus licht."},
        {"Het potlood ligt onder het boek.\nJe legt het boven op het boek.",
            "Potlood bovenop","Potlood onder het boek","Potlood naast het boek","Potlood in de la",
            "Het potlood ligt nu boven op het boek, niet eronder."},
        {"De deur is dicht.\nJe doet hem open en daarna weer dicht.",
            "Gesloten deur","Open deur","Een open raam","Geen deur meer",
            "De laatste stap was dicht doen. De deur is dus gesloten."}
    };

    private PlaylumiThinkingPractice(){}

    public static boolean supports(int mode,int variant){
        return mode==ExtraGames.COMPARE&&variant==EQUAL_GROUPS ||
                mode==ExtraGames.MATH&&variant==PLACE_VALUE || mode==DAILY_THINGS&&variant==AFTER;
    }

    public static LearningGame.Round next(int mode,int variant,int level,Random random){
        if(!supports(mode,variant)||level<0||level>2||random==null)
            throw new IllegalArgumentException("thinking practice configuration");
        if(mode==ExtraGames.COMPARE)return equalGroups(level,random);
        if(mode==ExtraGames.MATH)return placeValue(level,random);
        return after(level,random);
    }

    private static LearningGame.Round round(String prompt,String display,String correct,List<String> answers,Random r){
        if(new LinkedHashSet<>(answers).size()!=answers.size()||Collections.frequency(answers,correct)!=1)
            throw new IllegalArgumentException("ambiguous thinking choices");
        Collections.shuffle(answers,r);
        ArrayList<Integer> colors=new ArrayList<>();for(String ignored:answers)colors.add(INK);
        return new LearningGame.Round(prompt,display,INK,answers,colors,answers.indexOf(correct));
    }

    private static String group(String shape,int number,int columns){
        StringBuilder value=new StringBuilder();
        for(int i=0;i<number;i++){
            if(i>0)value.append(i%columns==0?'\n':'\u2009');
            value.append(shape);
        }
        return value.toString();
    }

    private static LearningGame.Round equalGroups(int level,Random r){
        int max=level==0?3:level==1?6:10,count=level==0?3:4;
        int answer=1+r.nextInt(max),shape=r.nextInt(SHAPES.length);
        String shownShape=SHAPES[shape],choiceShape=SHAPES[(shape+1+r.nextInt(SHAPES.length-1))%SHAPES.length];
        ArrayList<Integer> wrong=new ArrayList<>();
        for(int n=1;n<=max;n++)if(n!=answer)wrong.add(n);
        Collections.shuffle(wrong,r);
        ArrayList<String> answers=new ArrayList<>();
        String correct=group(choiceShape,answer,5);answers.add(correct);
        for(int i=0;i<count-1;i++)answers.add(group(choiceShape,wrong.get(i),5));
        // The shape changes, but the number stays the same. The layout is not a cue.
        String display=group(shownShape,answer,answer>5?5:2+r.nextInt(4));
        return round("Waar zie je evenveel?",display,correct,answers,r);
    }

    private static LearningGame.Round placeValue(int level,Random r){
        int maxTens=level==0?1:level==1?4:9;
        int tens=1+r.nextInt(maxTens),units=r.nextInt(10),answer=10*tens+units;
        int max=10*maxTens+9,count=level==0?3:4;
        LinkedHashSet<Integer> candidates=new LinkedHashSet<>();
        // Include meaningful mistakes: reversing the digits and treating a ten as one.
        for(int n:new int[]{10*units+tens,tens+units,10*tens,answer+10,answer-10,answer+1,answer-1})
            if(n>=0&&n<=max&&n!=answer)candidates.add(n);
        ArrayList<Integer> wrong=new ArrayList<>(candidates);Collections.shuffle(wrong,r);
        ArrayList<String> choices=new ArrayList<>();String correct=Integer.toString(answer);choices.add(correct);
        for(int n:wrong){if(choices.size()==count)break;choices.add(Integer.toString(n));}
        for(int n=0;choices.size()<count&&n<=max;n++)if(!choices.contains(Integer.toString(n)))choices.add(Integer.toString(n));
        String display=tens+(tens==1?" tiental":" tientallen")+"\n"+units+(units==1?" eenheid":" eenheden");
        return round("Welk getal is dit?",display,correct,choices,r);
    }

    private static LearningGame.Round after(int level,Random r){
        String[] story=STORIES[r.nextInt(level==0?8:level==1?12:STORIES.length)];
        ArrayList<String> answers=new ArrayList<>();
        for(int i=1;i<=4;i++)answers.add(story[i]);
        if(level==0)answers.remove(3);
        return round("Wat zie je na deze stap?",story[0],story[1],answers,r);
    }

    /** Feedback is shown only after the learner solves the question. */
    public static String explain(int mode,int variant,LearningGame.Round round){
        if(round==null||!round.isSolved()||!supports(mode,variant))return "";
        if(mode==ExtraGames.COMPARE){
            int total=0;
            for(int at=0;at<round.display.length();){
                int cp=round.display.codePointAt(at);at+=Character.charCount(cp);
                if(!Character.isWhitespace(cp)&&!Character.isSpaceChar(cp))total++;
            }
            return "Beide groepjes hebben "+total+(total==1?" voorwerp.":" voorwerpen.");
        }
        if(mode==ExtraGames.MATH){
            Matcher digits=Pattern.compile("\\d+").matcher(round.display);
            if(!digits.find())return "";int tens=Integer.parseInt(digits.group());
            if(!digits.find())return "";int units=Integer.parseInt(digits.group());
            return tens+" × 10 + "+units+" = "+(tens*10+units)+".";
        }
        for(String[] story:STORIES)if(story[0].equals(round.display))return story[5];
        return "";
    }
}
