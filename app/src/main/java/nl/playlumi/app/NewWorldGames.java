package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Eight extra learning worlds with five distinct play styles each; 100% offline. */
public final class NewWorldGames {
    public static final int FIRST = 16;
    public static final String[] NAMES = {"Klokkijken","Weer & seizoenen","Plaats & richting",
            "Dagelijkse dingen","Eten & drinken","Dierenwereld","Meten & vergelijken","Slim nadenken"};
    public static final String[] ICONS = {"🕒","🌦️","🧭","🏠","🥕","🦁","📏","💡"};
    public static final String[][] VARIANTS = {
            {"Hele uren","Een uur later","Een uur eerder","Ochtend, middag, avond en nacht","Tijden vergelijken"},
            {"Welk seizoen?","Kleed je goed","Weer herkennen","Wat past bij het weer?","Seizoen kiezen"},
            {"Links of rechts","Boven of onder","Voor of achter","Waar staat het?","Richting volgen"},
            {"Wat gebruik je?","Ochtend en avond","Wat doe je eerst?","Waar hoort het?","Veilig kiezen"},
            {"Gezond kiezen","Fruit herkennen","Groente herkennen","Waar komt het vandaan?","Eten sorteren"},
            {"Dieren en geluid","Welk dier is het?","Woonplek van dieren","Jonge dieren","Dieren vergelijken"},
            {"Lang of kort","Zwaar of licht","Meer of minder","Grootte vergelijken","Meet het verschil"},
            {"Logische reeks","Wat ontbreekt?","Even of oneven","Welk getal past?","Eenvoudig raadsel"}
    };
    private static final String[] SEASONS = {"lente","zomer","herfst","winter"};
    private static final String[] SEASON_ICONS = {"🌷","☀️","🍂","❄️"};
    private static final String[] WEATHER = {"zonnig","regenachtig","sneeuw","winderig","bewolkt","onweer"};
    private static final String[] WEATHER_ICONS = {"☀️","🌧️","❄️","🌬️","☁️","⛈️"};
    private static final String[] WEATHER_WEAR = {"zonnebril","regenjas","muts","windjack","vest","binnen blijven"};
    private static final String[] WEATHER_ACT = {"zonnebrand","paraplu","sneeuwlaarzen","vlieger","jas","schuilen"};
    // The image and the corresponding names form curated, one-to-one answer pairs.
    private static final String[][] HOUSE = {
        {"🪥","tandenborstel","badkamer"},{"🛏️","bed","slaapkamer"},{"🍽️","bord","keuken"},
        {"🧼","zeep","badkamer"},{"📚","boek","woonkamer"},{"🧸","knuffel","slaapkamer"},
        {"🧹","bezem","berging"},{"🧦","sokken","slaapkamer"},{"🧻","toiletpapier","toilet"},
        {"🥄","lepel","keuken"},{"🍳","pan","keuken"},{"🛁","bad","badkamer"},
        {"🪑","stoel","woonkamer"},{"🖍️","krijt","speelhoek"},{"🧺","wasmand","wasruimte"},
        {"🧥","jas","hal"},{"🪞","spiegel","badkamer"},{"🛋️","bank","woonkamer"}
    };
    private static final String[][] ANIMALS = {
        {"🐶","hond","woef","huis","puppy"},{"🐱","kat","miauw","huis","kitten"},
        {"🐮","koe","boe","boerderij","kalf"},{"🐑","schaap","bèè","boerderij","lam"},
        {"🐴","paard","hinnik","boerderij","veulen"},{"🐷","varken","knor","boerderij","big"},
        {"🐔","kip","tok","boerderij","kuiken"},{"🦆","eend","kwaak","vijver","kuiken"},
        {"🦁","leeuw","brul","savanne","welp"},{"🐸","kikker","kwaak","vijver","kikkervisje"},
        {"🐻","beer","grom","bos","jong"},{"🐺","wolf","huil","bos","welp"},
        {"🐘","olifant","trompetter","savanne","kalf"},{"🐦","vogel","tjilp","boom","kuiken"},
        {"🐵","aap","roep","oerwoud","jong"},{"🦓","zebra","hinnik","savanne","veulen"}
    };
    private static final String[] HABITATS = {"huis","boerderij","vijver","savanne","bos","boom","oerwoud"};
    private static final String[][] FOOD = {
        {"🍎","appel","fruit"},{"🍌","banaan","fruit"},{"🍇","druiven","fruit"},
        {"🍓","aardbei","fruit"},{"🍊","sinaasappel","fruit"},{"🍐","peer","fruit"},
        {"🥕","wortel","groente"},{"🥦","broccoli","groente"},{"🌽","maïs","groente"},
        {"🥒","komkommer","groente"},{"🍅","tomaat","groente"},{"🫑","paprika","groente"},
        {"🥛","melk","drinken"},{"💧","water","drinken"},{"🧃","sap","drinken"},
        {"🍞","brood","graan"},{"🥔","aardappel","groente"},{"🧀","kaas","zuivel"}
    };
    private static final String[] CATEGORY_NAMES = {"fruit","groente","drinken","graan","zuivel"};
    private static final String[] YES_NO = {"Ja","Nee"};
    private static final String[] SIDES = {"Links","Rechts"};
    private static final String[] VERTICAL = {"Boven","Onder"};
    private static final String[] FRONT = {"Voor","Achter"};
    private static final String[] DAILY = {"wakker worden","ontbijten","tanden poetsen","aankleden",
            "naar school gaan","lunchen","buiten spelen","avondeten","douchen","slapen"};
    private static final String[] ACT_ICONS = {"🌅","🥣","🪥","👕","🎒","🥪","⚽","🍽️","🚿","😴"};
    private static final String[][] SAFE = {
        {"Je steekt over bij groen licht","Ja"},{"Je steekt over bij rood licht","Nee"},
        {"Je doet een helm op bij fietsen","Ja"},{"Je kijkt eerst links en rechts","Ja"},
        {"Je raakt een hete pan aan","Nee"},{"Je vraagt hulp als iets gevaarlijk is","Ja"},
        {"Je speelt op straat zonder op te letten","Nee"},{"Je wast je handen voor het eten","Ja"},
        {"Je pakt een onbekende pil","Nee"},{"Je zegt het als je verdwaald bent","Ja"},
        {"Je doet de gordel om in de auto","Ja"},{"Je loopt op een fietspad zonder te kijken","Nee"}
    };
    private NewWorldGames() {}

    private static int max(int level) { return level == 0 ? 10 : level == 1 ? 30 : 60; }
    private static int choiceCount(int level) { return level == 0 ? 3 : level == 1 ? 4 : 6; }
    private static int choose(Random r,int n) { return r.nextInt(n); }
    private static String hour(int h) { return String.format(java.util.Locale.ROOT,"%02d:00",((h%24)+24)%24); }
    private static String minute(int m) { return String.format(java.util.Locale.ROOT,"%02d:%02d",(((m/60)%24)+24)%24,((m%60)+60)%60); }
    private static List<String> opts(String answer,String[] choices,int size,Random r) {
        List<String> out=new ArrayList<>(); out.add(answer);
        if(choices.length<size)throw new IllegalArgumentException("small choices");
        int tries=0;
        while(out.size()<size && tries++<1000) {
            String value=choices[choose(r,choices.length)];
            if(!out.contains(value))out.add(value);
        }
        if(out.size()<size)throw new IllegalArgumentException("not enough distinct answers");
        return out;
    }
    private static String[] numbers(int min,int max) {
        String[] arr=new String[max-min+1];
        for(int i=min;i<=max;i++)arr[i-min]=""+i;
        return arr;
    }
    private static LearningGame.Round make(String prompt,String visual,String right,List<String> answers,Random r) {
        Collections.shuffle(answers,r);
        int index=answers.indexOf(right);
        if(index<0 || Collections.frequency(answers,right)!=1)throw new IllegalStateException("ambiguous "+prompt);
        List<Integer> inks=new ArrayList<>();
        for(int i=0;i<answers.size();i++) inks.add(0xff244454);
        return new LearningGame.Round(prompt,visual,0xff376c98,answers,inks,index);
    }
    private static String[] column(String[][] data,int col) {
        List<String> values=new ArrayList<>();
        for(String[] row:data)if(!values.contains(row[col]))values.add(row[col]);
        return values.toArray(new String[0]);
    }
    public static LearningGame.Round next(int mode,int variant,int level,Random r) {
        if(mode<FIRST || mode>=FIRST+NAMES.length || variant<0 || variant>=5 || level<0 || level>2)
            throw new IllegalArgumentException("game settings");
        switch(mode-FIRST) {
            case 0:return time(variant,level,r);
            case 1:return weather(variant,level,r);
            case 2:return direction(variant,level,r);
            case 3:return daily(variant,level,r);
            case 4:return food(variant,level,r);
            case 5:return animals(variant,level,r);
            case 6:return measure(variant,level,r);
            case 7:return logic(variant,level,r);
            default:throw new IllegalStateException("game");
        }
    }
    private static LearningGame.Round time(int v,int lvl,Random r) {
        int base=choose(r,lvl==0?12:24);
        if(v==0) {
            String correct=hour(1+choose(r,12));
            String[] pool=new String[12];for(int i=0;i<12;i++)pool[i]=hour(i+1);
            return make("Hoe laat is het op de wijzerklok?","clock:"+correct,correct,
                    opts(correct,pool,choiceCount(lvl),r),r);
        }
        if(v==1 || v==2) {
            String correct=hour(base+(v==1?1:-1));
            String[] pool=new String[24];for(int i=0;i<24;i++)pool[i]=hour(i);
            return make(v==1?"Hoe laat is het een uur later?":"Hoe laat was het een uur eerder?",
                    "⏰ "+hour(base),correct,opts(correct,pool,choiceCount(lvl),r),r);
        }
        if(v==3) {
            // The 12-hour wording is ambiguous without a day part. Always show it,
            // and include the matching AM/PM distractor rather than random times.
            int h=choose(r,24);
            int step=lvl==0?30:lvl==1?15:5;
            int m=choose(r,60/step)*step;
            return PlaylumiDaytimeClock.round(h,m,lvl,r);
        }
        int other=(base+1+choose(r,11))%24;
        String correct=base>other?"Links":"Rechts";
        return make("Welke tijd is later op dezelfde dag?","⬅️ "+hour(base)+"     "+hour(other)+" ➡️",
                correct,opts(correct,SIDES,2,r),r);
    }
    private static LearningGame.Round weather(int v,int lvl,Random r) {
        int index=choose(r,WEATHER.length), season=choose(r,4);
        if(v==0 || v==4) {
            String correct=SEASONS[season];
            String display=v==0?SEASON_ICONS[season]:season==0?"🌷 🐣 🌼":season==1?"☀️ 🏖️ 🍦":season==2?"🍁 🌧️ 🍂":"❄️ 🧣 ⛄";
            return make(v==0?"Welk seizoen zie je?":"Bij welk seizoen passen deze plaatjes?",
                    display,correct,opts(correct,SEASONS,v==0?3:4,r),r);
        }
        String correct=v==1?WEATHER_WEAR[index]:v==2?WEATHER[index]:WEATHER_ACT[index];
        String[] pool=v==1?WEATHER_WEAR:v==2?WEATHER:WEATHER_ACT;
        return make(v==1?"Wat trek je aan bij dit weer?":v==2?"Hoe heet dit weer?":"Wat past bij dit weer?",
                WEATHER_ICONS[index]+"  "+(v==2?"":WEATHER[index]),correct,
                opts(correct,pool,choiceCount(lvl),r),r);
    }
    private static LearningGame.Round direction(int v,int lvl,Random r) {
        if(v<3) {
            String[] pool=v==0?SIDES:v==1?VERTICAL:FRONT;
            String correct=pool[choose(r,2)];
            String visual=v==0?(correct.equals("Links")?"⭐  ←  🐻":"🐻  →  ⭐"):
                    v==1?(correct.equals("Boven")?"⭐\n🐻":"🐻\n⭐"):
                    (correct.equals("Voor")?"🚗  →  🐻":"🐻  →  🚗");
            return make(v==0?"Waar staat de ster ten opzichte van de beer?":
                    v==1?"Waar staat de ster?":"Waar staat de auto in het rijtje?",
                    visual,correct,opts(correct,pool,2,r),r);
        }
        int steps=1+choose(r,max(lvl)), target=1+choose(r,max(lvl));
        if(v==3) {
            String correct=steps>target?"Links":steps<target?"Rechts":"Gelijk";
            return make("Waar staan meer sterren?","⬅️ "+steps+" ★    "+target+" ★ ➡️",
                    correct,opts(correct,new String[]{"Links","Rechts","Gelijk"},3,r),r);
        }
        String[] pool={"Noord","Oost","Zuid","West"};
        int where=choose(r,4);
        return make("Welke richting wijst de pijl?",
                new String[]{"⬆️","➡️","⬇️","⬅️"}[where],pool[where],
                opts(pool[where],pool,4,r),r);
    }
    private static LearningGame.Round daily(int v,int lvl,Random r) {
        if(v==0 || v==3) {
            String[] row=HOUSE[choose(r,HOUSE.length)];
            String correct=v==0?row[1]:row[2], prompt=v==0?"Wat is dit voor een voorwerp?":"Waar hoort dit voorwerp?";
            String[] pool=v==0?column(HOUSE,1):column(HOUSE,2);
            return make(prompt,row[0],correct,opts(correct,pool,choiceCount(lvl),r),r);
        }
        if(v==1) {
            int index=choose(r,DAILY.length);
            String correct=index<4?"Ochtend":index<7?"Middag":"Avond";
            return make("Wanneer doe je dit meestal?",ACT_ICONS[index]+"\n"+DAILY[index],
                correct,opts(correct,new String[]{"Ochtend","Middag","Avond"},3,r),r);
        }
        if(v==2) {
            int n=choose(r,DAILY.length-1);
            String correct=DAILY[n];
            return make("Wat doe je eerst?",DAILY[n]+"  →  "+DAILY[n+1],
                correct,opts(correct,DAILY,choiceCount(lvl),r),r);
        }
        String[] row=SAFE[choose(r,SAFE.length)];
        return make("Is dit een veilige keuze?","🛡️ "+row[0],row[1],opts(row[1],YES_NO,2,r),r);
    }
    private static LearningGame.Round food(int v,int lvl,Random r) {
        String[] food=FOOD[choose(r,FOOD.length)];
        if(v==0) {
            boolean healthy=choose(r,2)==0;
            String visual=healthy?"🥕 🍎 🥦":"🍬 🍭 🍩";
            String correct=healthy?"Gezond":"Soms";
            return make("Welke soort tussendoortjes zie je?",visual,correct,
                    opts(correct,new String[]{"Gezond","Soms"},2,r),r);
        }
        if(v==1 || v==2) {
            int from=v==1?0:6, amount=6;
            String[] row=FOOD[from+choose(r,amount)];
            String[] pool=new String[amount];for(int i=0;i<amount;i++)pool[i]=FOOD[from+i][1];
            return make(v==1?"Welk fruit is dit?":"Welke groente is dit?",row[0],row[1],
                    opts(row[1],pool,choiceCount(lvl),r),r);
        }
        if(v==3) {
            String correct=food[2];
            return make("Bij welke groep hoort dit eten?",food[0]+"\n"+food[1],correct,
                    opts(correct,CATEGORY_NAMES,lvl==0?3:lvl==1?4:5,r),r);
        }
        String correct=food[1];
        return make("Kies de naam bij het plaatje",food[0],correct,
                opts(correct,column(FOOD,1),choiceCount(lvl),r),r);
    }
    private static LearningGame.Round animals(int v,int lvl,Random r) {
        String[] row=ANIMALS[choose(r,ANIMALS.length)];
        String correct=v==0?row[2]:v==1?row[1]:v==2?row[3]:v==3?row[4]:row[1];
        String[] pool=column(ANIMALS,v==0?2:v==1?1:v==2?3:v==3?4:1);
        String prompt=v==0?"Welk geluid maakt het dier?":v==1?"Hoe heet dit dier?":
                v==2?"Waar woont dit dier?":v==3?"Hoe heet het jonge dier?":"Welk dier past bij dit plaatje?";
        return make(prompt,row[0]+(v==4?"  🐾":""),correct,
                opts(correct,pool,Math.min(choiceCount(lvl),pool.length),r),r);
    }
    private static LearningGame.Round measure(int v,int lvl,Random r) {
        int n=1+choose(r,max(lvl)),other=1+choose(r,max(lvl));
        if(other==n)other=n==max(lvl)?n-1:n+1;
        if(v==0 || v==1 || v==3) {
            String correct=v==1? (n>other?"Links":"Rechts"):(n>other?"Links":"Rechts");
            String name=v==0?"Welk lint is langer?":v==1?"Welke doos is zwaarder?":"Welke toren is groter?";
            return make(name,"⬅️ "+n+(v==1?" kg":" cm")+"    "+other+(v==1?" kg":" cm")+" ➡️",
                    correct,opts(correct,SIDES,2,r),r);
        }
        if(v==2) {
            int target=n+other;
            return make("Hoeveel heb je samen?","📏 "+n+" + "+other+" = ?",
                    ""+target,opts(""+target,numbers(1,max(lvl)*2),choiceCount(lvl),r),r);
        }
        int diff=Math.abs(n-other);
        return make("Hoeveel is het verschil?","📏 "+Math.max(n,other)+" − "+Math.min(n,other)+" = ?",
                ""+diff,opts(""+diff,numbers(0,max(lvl)),choiceCount(lvl),r),r);
    }
    private static LearningGame.Round logic(int v,int lvl,Random r) {
        int max=max(lvl);
        if(v==0 || v==1 || v==3) {
            int step=v==0?2:v==1?3:4, first=1+choose(r,max);
            int correct=first+2*step;
            return make(v==0?"Maak de reeks af":v==1?"Welk getal ontbreekt?":"Welk getal past in de reeks?",
                    first+"  →  "+(first+step)+"  →  ?", ""+correct,
                    opts(""+correct,numbers(1,max+12),choiceCount(lvl),r),r);
        }
        if(v==2) {
            int n=1+choose(r,max);
            String answer=n%2==0?"Even":"Oneven";
            return make("Is het getal even of oneven?","🔢 "+n,answer,
                    opts(answer,new String[]{"Even","Oneven"},2,r),r);
        }
        int n=1+choose(r,max);
        String answer=n>max/2?"Ja":"Nee";
        return make("Is dit groter dan "+(max/2)+"?","💡 "+n,answer,opts(answer,YES_NO,2,r),r);
    }
}
