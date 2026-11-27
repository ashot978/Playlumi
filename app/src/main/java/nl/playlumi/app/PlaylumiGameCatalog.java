package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Stable, age-filtered routes shared by the game library and native game screens. */
public final class PlaylumiGameCatalog {
    public static final int OVERVIEW=-1,FAVORITES=-2,ALL=-3;
    public static final int QUIZ=0,MINI=1,MUSIC=2,BUBBLES=3,STARS=4,DRAW=5,INTERACTIVE=6;
    public static final class Entry {
        public final int world,kind,mode,variant;
        public final String id,title,description,icon;
        Entry(int w,int k,int m,int v,String t,String d,String i){
            world=w;kind=k;mode=m;variant=v;title=t;description=d;icon=i;
            id=w+":"+k+":"+m+":"+v;
        }
    }
    private PlaylumiGameCatalog(){}
    private static void q(List<Entry> list,int w,int m,int v,String title,String desc,String icon){
        list.add(new Entry(w,QUIZ,m,v,title,desc,icon));
    }
    private static void g(List<Entry> list,int w,int kind,int mode,String title,String desc,String icon){
        list.add(new Entry(w,kind,mode,0,title,desc,icon));
    }
    public static List<Entry> entries(int age){
        int a=PlaylumiRocketCurriculum.level(age);
        ArrayList<Entry> list=new ArrayList<>();
        for(PlaylumiRocketCurriculum.Mission m:PlaylumiRocketCurriculum.missions(a))
            q(list,0,m.mode,m.variant,m.title,m.description,"✦");
        if(a==0)q(list,0,LearningGame.COUNT,2,"Stippen tellen","Tel de stippen","●");
        q(list,0,ExtraGames.COMPARE,PlaylumiThinkingPractice.EQUAL_GROUPS,"Evenveel","Vergelijk hoeveelheden: zoek het groepje met evenveel voorwerpen","● = ◆");
        if(a>=1){
            q(list,0,16,0,"Klokkijken","Lees hele en halve uren, kwartieren en minuten op de wijzerklok","◷");
            q(list,0,ExtraGames.SEQUENCES,0,"Getallenrij","Ontdek de sprong tussen getallen","123");
        }
        if(a>=2){
            q(list,0,16,1,"Een uur later","Reken een uur verder, ook met minuten","◷");
            q(list,0,16,3,"Tijd van de dag","Ochtend, middag, avond of nacht? Zet om naar de 24-uursklok","◷");
            q(list,0,ExtraGames.MATH,3,"Verdubbelen","Tel twee gelijke getallen op","✦");
            q(list,0,ExtraGames.MATH,PlaylumiAdvancedMath.TABLES,"Tafels oefenen","Vermenigvuldig met 2, 3, 4, 5 en 10","×");
            q(list,0,ExtraGames.MATH,PlaylumiAdvancedMath.SHARING,"Eerlijk verdelen","Deel in gelijke groepjes zonder rest","÷");
            q(list,0,ExtraGames.MATH,PlaylumiAdvancedMath.TO_HUNDRED,"Rekenen tot 100","Tel op en trek af met tientallen en eenheden","100");
            q(list,0,ExtraGames.MATH,PlaylumiThinkingPractice.PLACE_VALUE,"Tientallen en eenheden","Bouw een getal tot 99 uit tientallen en losse eenheden","123");
            q(list,0,22,0,"Meten","Vergelijk centimeters en millimeters","↔");
            q(list,0,22,4,"Lengteverschil","Bereken hoeveel centimeter het verschil is","↔");
            q(list,0,16,2,"Een uur eerder","Reken een uur terug, ook met minuten","◷");
            q(list,0,23,2,"Even of oneven","Ontdek getallenpatronen","123");
            q(list,0,ExtraGames.SEQUENCES,1,"Terugtellen","Ontdek de sprong en tel terug","123");
        }
        q(list,1,LearningGame.LETTERS,0,"Kleine letters","Vind de kleine letter","Aa");
        q(list,1,LearningGame.LETTERS,1,"Hoofdletters","Vind de grote letter","Aa");
        if(a>=1){
            q(list,1,LearningGame.WORDS,0,"Woorden maken","Vul de ontbrekende letter in","Ab");
            q(list,1,ExtraGames.FIRST_LAST,0,"Beginletter","Vul de eerste letter in of zoek een woord met dezelfde beginletter","A");
            q(list,1,ExtraGames.PICTURES,0,"Woord en plaatje","Lees het woord bij het dier","🐾");
            q(list,1,ExtraGames.RHYMES,0,"Rijmen","Zoek een rijmwoord","✦");
            q(list,1,ExtraGames.PICTURES,1,"Plaatje zoeken","Vind het dier bij het woord","🐾");
        }
        if(a>=2){
            q(list,1,ExtraGames.SPELLING,0,"Spelling","Maak een zin af met het goed gespelde woord","Ab");
            q(list,1,ExtraGames.FIRST_LAST,1,"Eindletter","Vul de laatste letter in of zoek een woord met dezelfde eindletter","Z");
            q(list,1,ExtraGames.OPPOSITES,0,"Tegenstellingen","Vind het tegenovergestelde","↔");
            q(list,1,ExtraGames.RHYMES,1,"Wat rijmt niet?","Ontdek welk woord anders klinkt","✦");
            q(list,1,LearningGame.LETTERS,4,"Vorige letter","Welke letter komt hiervoor?","Aa");
        }
        g(list,2,MINI,PlaylumiMiniChallenge.DRAG,"Sleep de vormen","Breng vormen naar hun poort","◆");
        q(list,2,LearningGame.SHAPES,0,"Vormen zoeken","Vind dezelfde vorm","▲");
        q(list,2,LearningGame.SHAPES,1,"Vormen benoemen","Herken de vorm","●");
        q(list,2,LearningGame.PATTERNS,0,"Patronen afmaken","Ontdek de regel en vul het ontbrekende vakje in","◆");
        if(a>=1){
            q(list,2,18,0,"Links of rechts","Ontdek waar de ster staat","↔");
            q(list,2,18,1,"Boven of onder","Waar staat de ster?","↕");
        }
        if(a>=2){
            q(list,2,23,0,"Slim nadenken","Ontdek wisselende sprongen in een getallenreeks","✦");
            q(list,2,LearningGame.SHAPES,4,"Vorm bij het woord","Kies de vorm die bij het woord past","◆");
        }
        g(list,3,MUSIC,0,"Speel het deuntje","Speel "+(a+2)+" tonen na","♪");
        if(a>=1)list.add(new Entry(3,MUSIC,0,1,"Lange melodie","Onthoud "+(a+3)+" tonen","♫"));
        if(a>=2)list.add(new Entry(3,MUSIC,0,2,"Geheugenconcert","Onthoud zes tonen","♪"));
        g(list,4,MINI,PlaylumiMiniChallenge.MEMORY,"Dierenmemory","Vind gelijke dieren","🐾");
        g(list,4,MINI,PlaylumiMiniChallenge.BALLOONS,"Tel de ballonnen","Tel echte ballonnen","🎈");
        g(list,4,BUBBLES,0,"Bellenfeest","Prik de zeepbellen","○");
        g(list,4,STARS,0,"Vang de sterren","Verzamel alleen gouden sterren","★");
        q(list,4,21,1,"Dierenwereld","Herken de dieren","🐾");
        q(list,4,ExtraGames.CATEGORIES,0,"Wat hoort erbij?","Zoek het passende plaatje","✦");
        if(a>=1){
            q(list,4,20,1,"Fruit herkennen","Welk fruit zie je?","🍎");
            q(list,4,20,2,"Groente herkennen","Ontdek de groente","🥕");
            q(list,4,19,0,"Dagelijkse dingen","Herken bekende voorwerpen","🏠");
            q(list,4,PlaylumiThinkingPractice.DAILY_THINGS,PlaylumiThinkingPractice.AFTER,"Eerst en daarna","Denk in volgorde: wat verandert er na een stap of handeling?","→");
        }
        if(a>=2){
            q(list,4,21,2,"Dieren en hun huis","Waar woont dit dier?","🐾");
            q(list,4,19,4,"Veilig kiezen","Wat is een veilige keuze?","✦");
            q(list,4,21,3,"Dierenbaby’s","Hoe heet het jong?","🐣");
        }
        q(list,5,LearningGame.COLORS,0,"Kleuren herkennen","Welke kleur zie je?","●");
        q(list,5,LearningGame.COLORS,1,"Kleuren zoeken","Vind de gevraagde kleur","●");
        g(list,5,DRAW,0,"Mijn kunstwerk","Teken en kleur vrij","✎");
        if(a>=1){
            q(list,5,17,2,"Weer herkennen","Ontdek het weer","☀");
            q(list,5,17,0,"Seizoenen","Welk seizoen zie je?","✿");
            q(list,5,LearningGame.PATTERNS,4,"Kleurenpatronen","Langere reeksen met afwisselende kleurengroepjes","✦");
        }
        if(a>=2){
            q(list,5,17,3,"Weer en spullen","Wat past bij het weer?","☂");
            q(list,5,17,4,"Seizoensplaatjes","Kies het juiste seizoen","✿");
        }
        g(list,0,INTERACTIVE,PlaylumiInteractive.NUMBERS,"Getallenrace",a==2?"Tel met sprongen vooruit en terug":a==1?"Tel verder en kies de juiste tegels":"Bouw de raket in de juiste volgorde","🚀");
        g(list,1,INTERACTIVE,PlaylumiInteractive.LETTERS,"Lettertrein",a==0?"Bouw een woord met lettertegels":"Los woordraadsels op met lettertegels","🚂");
        g(list,4,INTERACTIVE,PlaylumiInteractive.HABITATS,"Dierenwereld: thuis","Breng dieren naar hun leefomgeving","🐾");
        g(list,5,INTERACTIVE,PlaylumiInteractive.MOSAIC,"Kleurmozaïek","Bouw het kleurvoorbeeld na","🔷");
        // Stable world order, including Android 6 (API 23).
        ArrayList<Entry> grouped=new ArrayList<>();
        for(int world=0;world<6;world++)
            for(Entry entry:list)if(entry.world==world)grouped.add(entry);
        return Collections.unmodifiableList(grouped);
    }
    public static Entry find(int age,String id){
        if(id==null)return null;
        for(Entry e:entries(age))if(id.equals(e.id))return e;
        return null;
    }
    public static Entry route(int age,int world,int kind,int mode,int variant){
        return find(age,world+":"+kind+":"+mode+":"+variant);
    }
    public static List<Entry> filter(int age,int filter,Set<String> favorites){
        ArrayList<Entry> out=new ArrayList<>();
        for(Entry e:entries(age))
            if(filter==ALL||filter==e.world||(filter==FAVORITES&&favorites!=null&&favorites.contains(e.id)))out.add(e);
        return out;
    }
}
