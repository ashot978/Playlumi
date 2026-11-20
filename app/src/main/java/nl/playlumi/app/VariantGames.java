package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Three additional, deliberately distinct ways to practice every learning topic.
 * All exercises run offline and always contain one correct answer.
 */
public final class VariantGames {
    private VariantGames() {}
    public static final String[][] NAMES = {
        {"Tel de stippen","Eentje meer","Eentje minder"},
        {"Kleur bij het woord","Kleurenreeks","Kleurherinnering"},
        {"Welke beginletter?","Volgende letter","Vorige letter"},
        {"Noem de vorm","Tel de vormen","Vorm bij het woord"},
        {"Getal ervoor","Getal ertussen","Tel met sprongen"},
        {"Vormenpatroon","Getallenpatroon","Drie-kleurenpatroon"},
        {"Eerste letter weg","Laatste letter weg","Wisselende letter"},
        {"Spellingcontrole","Verschil ontdekken","Woord herstellen"},
        {"Letter in het midden","Kies de klinker","Lengte van woord"},
        {"Bijbehorend dier","Woord uit drie","Plaatje zoeken"},
        {"Rijmwoord zoeken","Rijmpaar afmaken","Welk rijmt niet?"},
        {"Grootste getal","Kleinste getal","Is het gelijk?"},
        {"Ontbrekend getal","Verdubbelen","Tel er twee bij"},
        {"Sprongen van twee","Welk getal ontbreekt?","Sprongen van drie"},
        {"Tegenovergesteld","Andere kant","Koppel woorden"},
        {"Zoek een dier","Zoek een fruitsoort","Welk hoort niet?"}
    };
    private static final String[][] ANIMALS = {
        {"🐮","koe"},{"🐱","kat"},{"🐟","vis"},{"🐶","hond"},{"🐔","kip"},
        {"🐵","aap"},{"🐻","beer"},{"🐭","muis"},{"🐸","kikker"},{"🐰","konijn"},
        {"🐴","paard"},{"🦁","leeuw"},{"🐯","tijger"},{"🐷","varken"},{"🐑","schaap"},
        {"🦆","eend"},{"🦋","vlinder"},{"🐌","slak"},{"🐝","bij"},{"🦊","vos"},
        {"🐘","olifant"},{"🐬","dolfijn"},{"🦒","giraf"},{"🦉","uil"},{"🐧","pinguïn"},
        {"🐢","schildpad"},{"🐊","krokodil"},{"🦓","zebra"},{"🐒","aapje"},{"🦀","krab"},
        {"🦇","vleermuis"},{"🐜","mier"},{"🦩","flamingo"},{"🦚","pauw"},{"🐿️","eekhoorn"},
        {"🦔","egel"},{"🦎","hagedis"},{"🦭","zeehond"},{"🐳","walvis"},{"🦅","adelaar"},
        {"🐏","ram"},{"🦃","kalkoen"},{"🐐","geit"},{"🦌","hert"},{"🐕","puppy"},
        {"🐈","poes"},{"🐤","kuiken"},{"🦗","sprinkhaan"},{"🐞","lieveheersbeestje"},{"🐙","octopus"}
    };
    private static final String[] COLORS = {"rood","blauw","groen","geel","paars","oranje"};
    private static final String[] COLOR_ICONS = {"🔴","🔵","🟢","🟡","🟣","🟠"};
    private static final String[] SHAPES = {"cirkel","vierkant","driehoek","ruit","ster","hart"};
    private static final String[] SHAPE_ICONS = {"●","■","▲","◆","★","♥"};
    private static final String[] RHYMES = {"kat","rat","mat","vat","maan","haan","baan","kraan",
        "huis","muis","pluis","buis","beer","peer","weer","meer","boom","room","droom","stroom",
        "roos","doos","boos","loos","bal","hal","stal","dal"};
    private static final String[][] OPPOSITES = {{"groot","klein"},{"warm","koud"},{"hoog","laag"},
        {"nat","droog"},{"licht","donker"},{"vol","leeg"},{"blij","verdrietig"},
        {"snel","langzaam"},{"open","dicht"},{"hard","zacht"},
        {"dik","dun"},{"lang","kort"},{"oud","jong"},{"boven","onder"},
        {"binnen","buiten"},{"vroeg","laat"},{"voor","achter"},{"dag","nacht"}};
    private static final String[] FRUIT = {"🍎","🍐","🍌","🍓","🍊","🍇"};
    private static final String[] TRANSPORT = {"🚗","🚌","🚲","🚕","🚂","✈️"};
    private static final String[] ALPHABET = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
    private static final String VOWELS = "aeiou";

    private static int max(int level) { return level == 0 ? 5 : level == 1 ? 10 : 20; }
    private static int count(int level) { return level == 0 ? 3 : level == 1 ? 4 : 6; }
    private static int rand(Random r,int n) { return r.nextInt(n); }
    private static String repeat(String value, int n) {
        StringBuilder b = new StringBuilder();
        for (int i=0;i<n;i++) { if(i>0) b.append(i%5==0?"\n":"  "); b.append(value); }
        return b.toString();
    }
    private static void add(List<String> list,String text) { if(!list.contains(text)) list.add(text); }
    private static LearningGame.Round make(String prompt,String display,String answer,List<String> options,Random random) {
        Collections.shuffle(options,random);
        int index=options.indexOf(answer);
        if(index<0 || Collections.frequency(options,answer)!=1 || options.size()<2)
            throw new IllegalStateException("Invalid question: "+prompt+" "+answer);
        List<Integer> inks=new ArrayList<>();
        for(int i=0;i<options.size();i++) inks.add(0xff263e4b);
        return new LearningGame.Round(prompt,display,0xff276779,options,inks,index);
    }
    private static List<String> numeric(int correct,int min,int maximum,int options,Random random) {
        List<String> out=new ArrayList<>(); add(out,""+correct);
        int range=maximum-min+1;
        if(range<options) throw new IllegalArgumentException("Insufficient choices");
        while(out.size()<options) add(out,""+(min+rand(random,range)));
        return out;
    }
    private static List<String> choices(String correct,String[] source,int options,Random random) {
        List<String> out=new ArrayList<>();add(out,correct);
        while(out.size()<options) add(out,source[rand(random,source.length)]);
        return out;
    }
    private static String[] animal(int level, Random r) {
        // The easiest tier focuses on the shortest words; intermediate and advanced have broader pools.
        int start=level==0?0:level==1?8:18;
        int end=level==0?18:level==1?35:ANIMALS.length;
        return ANIMALS[start+rand(r,end-start)];
    }
    public static LearningGame.Round next(int mode,int variant,int level,Random r) {
        if(mode<0 || mode>=16 || variant<2 || variant>4 || level<0 || level>2)
            throw new IllegalArgumentException("Configuration");
        int v=variant-2;
        switch(mode) {
            case 0: return counting(v,level,r);
            case 1: return colors(v,level,r);
            case 2: return letters(v,level,r);
            case 3: return shapes(v,level,r);
            case 4: return numbers(v,level,r);
            case 5: return patterns(v,level,r);
            case 6: return words(v,level,r);
            case 7: return spelling(v,level,r);
            case 8: return fragments(v,level,r);
            case 9: return pictures(v,level,r);
            case 10: return rhymes(v,level,r);
            case 11: return compare(v,level,r);
            case 12: return math(v,level,r);
            case 13: return sequences(v,level,r);
            case 14: return opposites(v,level,r);
            case 15: return categories(v,level,r);
            default: throw new IllegalStateException("mode");
        }
    }
    private static LearningGame.Round counting(int v,int level,Random r) {
        int limit=max(level), n=1+rand(r,limit);
        int correct=v==0?n:v==1?Math.min(limit+1,n+1):Math.max(0,n-1);
        String prompt=v==0?"Hoeveel stippen tel je?":v==1?"Hoeveel zijn het met eentje erbij?":"Hoeveel zijn het als er eentje weggaat?";
        return make(prompt,repeat(v==0?"●":v==1?"🍎":"🐟",n),""+correct,
                numeric(correct,0,limit+1,count(level),r),r);
    }
    private static LearningGame.Round colors(int v,int level,Random r) {
        int size=level==0?3:level==1?5:6, n=rand(r,size), second=(n+1+rand(r,size-1))%size;
        String prompt=v==0?"Welke kleur hoort bij dit woord?":v==1?"Welke kleur komt hierna?":"Welke kleur heeft de eerste stip?";
        String shown=v==0?COLORS[n]:v==1?COLOR_ICONS[n]+" "+COLOR_ICONS[second]+" "+COLOR_ICONS[n]+"  ?":COLOR_ICONS[n]+" "+COLOR_ICONS[second]+" "+COLOR_ICONS[second];
        String correct=COLORS[v==1?second:n];
        List<String> answers=choices(correct,COLORS,count(level),r);
        return make(prompt,shown,correct,answers,r);
    }
    private static LearningGame.Round letters(int v,int level,Random r) {
        if(v==0) {
            String[] a=animal(level,r);String correct=a[1].substring(0,1);
            return make("Met welke letter begint dit dier?",a[0]+"  "+a[1],correct,choices(correct,ALPHABET,count(level),r),r);
        }
        int limit=level==0?8:level==1?16:26;
        int index=v==1?rand(r,limit-1):1+rand(r,limit-1);
        String correct=ALPHABET[index+(v==1?1:-1)];
        return make(v==1?"Welke letter komt hierna?":"Welke letter komt hiervoor?",
            "🔠 "+ALPHABET[index],correct,choices(correct,ALPHABET,count(level),r),r);
    }
    private static LearningGame.Round shapes(int v,int level,Random r) {
        int pool=level==0?3:level==1?5:6, selected=rand(r,pool);
        if(v==1) {
            int amount=1+rand(r,max(level));
            return make("Hoeveel vormen zie je?",repeat(SHAPE_ICONS[selected],amount),""+amount,
                numeric(amount,1,max(level),count(level),r),r);
        }
        String correct=v==0?SHAPES[selected]:SHAPE_ICONS[selected];
        List<String> options=choices(correct,v==0?SHAPES:SHAPE_ICONS,count(level),r);
        return make(v==0?"Welke vorm is dit?":"Kies de juiste vorm bij het woord",
            v==0?SHAPE_ICONS[selected]:SHAPES[selected],correct,options,r);
    }
    private static LearningGame.Round numbers(int v,int level,Random r) {
        int limit=max(level), n=v==1?1+rand(r,limit-2):v==0?2+rand(r,limit-1):1+rand(r,limit-2);
        int correct=v==0?n-1:v==1?n+1:n+2;
        String display=v==0?"?  →  "+n:v==1?n+"  →  ?  →  "+(n+2):n+"  →  ?  (sprong van 2)";
        String prompt=v==0?"Welk getal komt ervoor?":v==1?"Welk getal ontbreekt?":"Tel twee verder";
        return make(prompt,display,""+correct,numeric(correct,1,limit+1,count(level),r),r);
    }
    private static LearningGame.Round patterns(int v,int level,Random r) {
        int pool=level==0?3:level==1?5:6, a=rand(r,pool),b=(a+1+rand(r,pool-1))%pool;
        if(v==1) {
            int start=1+rand(r,max(level)-2);
            return make("Welk getal hoort op de lege plek?",start+" → "+(start+2)+" → ?",
                ""+(start+4),numeric(start+4,1,max(level)+3,count(level),r),r);
        }
        int c=(b+1+rand(r,pool-1))%pool;
        if(c==a) c=(c+1)%pool;
        String[] symbols=v==0?SHAPE_ICONS:COLOR_ICONS;
        String display=v==0?symbols[a]+" "+symbols[b]+" "+symbols[a]+" ?":
                symbols[a]+" "+symbols[b]+" "+symbols[c]+" "+symbols[a]+" "+symbols[b]+" ?";
        String correct=symbols[v==0?b:c];
        return make("Maak het patroon af",display,correct,choices(correct,symbols,count(level),r),r);
    }
    private static LearningGame.Round words(int v,int level,Random r) {
        String[] a=animal(level,r);String word=a[1];
        int index=v==0?0:v==1?word.length()-1:rand(r,word.length());
        String correct=word.substring(index,index+1);
        String shown=word.substring(0,index)+"_"+word.substring(index+1);
        return make("Welke letter ontbreekt in dit dier?",a[0]+"\n"+shown,correct,
                choices(correct,ALPHABET,count(level),r),r);
    }
    private static LearningGame.Round spelling(int v,int level,Random r) {
        String[] a=animal(level,r);String word=a[1];
        int pos=rand(r,word.length());
        String altered=word.substring(0,pos)+ALPHABET[(ALPHABET_INDEX(word.charAt(pos))+1)%26]+word.substring(pos+1);
        if(altered.equals(word)) altered=word+"x";
        if(v==2) {
            String missing=word.substring(0,pos)+"_"+word.substring(pos+1);
            return make("Herstel de juiste spelling",a[0]+"\n"+missing,
                word.substring(pos,pos+1),choices(word.substring(pos,pos+1),ALPHABET,count(level),r),r);
        }
        List<String> answers=new ArrayList<>();add(answers,word);add(answers,altered);
        add(answers,word+"x");
        if(level>0) add(answers,word.substring(0,word.length()-1));
        return make(v==0?"Welk woord is goed gespeld?":"Tik op het woord zonder fout",
            a[0]+"\n"+(v==1?"🔎 Let goed op":"✏️"),word,answers,r);
    }
    private static int ALPHABET_INDEX(char ch) {
        for(int i=0;i<ALPHABET.length;i++) if(ALPHABET[i].charAt(0)==ch)return i;
        return 0;
    }
    private static LearningGame.Round fragments(int v,int level,Random r) {
        String[] a=animal(level,r);String word=a[1];
        if(v==2)return make("Hoeveel letters heeft dit woord?",a[0]+"\n"+word,
            ""+word.length(),numeric(word.length(),1,Math.max(12,word.length()+2),count(level),r),r);
        if(v==1) {
            String vowels="";
            for(int i=0;i<word.length();i++) if(VOWELS.indexOf(word.charAt(i))>=0) {
                String letter=word.substring(i,i+1);
                if(vowels.indexOf(letter)<0) vowels+=letter;
            }
            if(vowels.isEmpty()) return fragments(0,level,r);
            String correct=vowels.substring(0,1);
            return make("Welke klinker zie je in dit woord?",a[0]+"\n"+word,
                correct,choices(correct,ALPHABET,count(level),r),r);
        }
        int pos=word.length()/2;
        String correct=word.substring(pos,pos+1);
        return make("Welke letter staat in het midden?",a[0]+"\n"+word.substring(0,pos)+"_"+word.substring(pos+1),
            correct,choices(correct,ALPHABET,count(level),r),r);
    }
    private static int randSafeIndex(String word,Random r) { return Math.min(word.length(),1+rand(r,word.length())); }
    private static LearningGame.Round pictures(int v,int level,Random r) {
        String[] a=animal(level,r);
        if(v==0 || v==2) {
            List<String> options=new ArrayList<>();add(options,a[1]);
            while(options.size()<count(level)) add(options,animal(level,r)[1]);
            return make(v==0?"Hoe heet dit dier?":"Welk woord hoort bij het plaatje?",a[0],a[1],options,r);
        }
        List<String> options=new ArrayList<>();add(options,a[0]);
        while(options.size()<count(level)) add(options,animal(level,r)[0]);
        return make("Welk plaatje hoort bij dit woord?",a[1],a[0],options,r);
    }
    private static LearningGame.Round rhymes(int v,int level,Random r) {
        int groups=level==0?3:level==1?5:7, g=rand(r,groups), start=g*4;
        int next=(g+1+rand(r,groups-1))%groups;
        String seed=RHYMES[start], correct=v==2?RHYMES[next*4+1]:RHYMES[start+1+rand(r,3)];
        List<String> options=new ArrayList<>();add(options,correct);
        if(v==2) {
            add(options,RHYMES[start+1]);add(options,RHYMES[start+2]);
        } else {
            while(options.size()<count(level)) {
                int wrong=(g+1+rand(r,groups-1))%groups;
                add(options,RHYMES[wrong*4+rand(r,4)]);
            }
        }
        return make(v==0?"Kies het rijmwoord":v==1?"Maak het rijmpaar af":"Welk woord rijmt niet?",
            "🎵 "+seed,correct,options,r);
    }
    private static LearningGame.Round compare(int v,int level,Random r) {
        int limit=max(level),a=1+rand(r,limit),b=1+rand(r,limit);
        if(v==2) {
            b=rand(r,2)==0?a:(a==limit?a-1:a+1);
            return make("Zijn deze getallen gelijk?",a+"  =  "+b+"  ?",a==b?"Ja":"Nee",
                choices(a==b?"Ja":"Nee",new String[]{"Ja","Nee"},2,r),r);
        }
        if(b==a)b=a==limit?a-1:a+1;
        return make(v==0?"Welk getal is groter?":"Welk getal is kleiner?",a+"   ⚖️   "+b,
            ""+(v==0?Math.max(a,b):Math.min(a,b)),
            numeric(v==0?Math.max(a,b):Math.min(a,b),1,limit,count(level),r),r);
    }
    private static LearningGame.Round math(int v,int level,Random r) {
        int limit=max(level), a=1+rand(r,Math.max(1,limit/2));
        int b=v==2?2:v==1?a:1+rand(r,limit-a);
        int result=a+b;
        if(v==0) return make("Welk getal ontbreekt?",a+" + ? = "+result,""+b,
                numeric(b,1,limit,count(level),r),r);
        return make(v==1?"Wat is het dubbele?":"Tel er twee bij",a+" + "+b+" = ?",
            ""+result,numeric(result,1,limit+2,count(level),r),r);
    }
    private static LearningGame.Round sequences(int v,int level,Random r) {
        int step=v==0?2:v==2?3:1;
        int limit=max(level)+6, start=1+rand(r,max(level));
        int correct=start+step*(v==1?1:2);
        String display=v==1?start+"  →  ?  →  "+(start+2):
            start+"  →  "+(start+step)+"  →  ?";
        return make(v==0?"Tel met sprongen van twee":v==1?"Welk getal ontbreekt?":"Tel met sprongen van drie",
            display,""+correct,numeric(correct,1,limit+8,count(level),r),r);
    }
    private static LearningGame.Round opposites(int v,int level,Random r) {
        int pool=level==0?6:level==1?12:OPPOSITES.length, n=rand(r,pool);
        String question=OPPOSITES[n][v==1?1:0], answer=OPPOSITES[n][v==1?0:1];
        List<String> options=new ArrayList<>();add(options,answer);
        while(options.size()<count(level)) {
            int another=rand(r,pool);
            add(options,OPPOSITES[another][v==1?0:1]);
        }
        return make(v==0?"Zoek het tegenovergestelde":v==1?"Wat is het omgekeerde?":"Welke woorden zijn elkaars tegenovergestelde?",
            "↔️ "+question,answer,options,r);
    }
    private static LearningGame.Round categories(int v,int level,Random r) {
        if(v==0 || v==1) {
            String[] correctPool=v==0?new String[]{"🐱","🐶","🐮","🐰","🦊","🐻"}:FRUIT;
            String[] wrongPool=v==0?TRANSPORT:new String[]{"🐱","🐶","🚗","🚌","🐸","🚲"};
            String correct=correctPool[rand(r,correctPool.length)];
            List<String> options=new ArrayList<>();add(options,correct);
            while(options.size()<count(level))add(options,wrongPool[rand(r,wrongPool.length)]);
            return make(v==0?"Welk plaatje is een dier?":"Welk plaatje is fruit?",
                v==0?"🐾 Dieren":"🍎 Fruit",correct,options,r);
        }
        String[] pool=rand(r,2)==0?FRUIT:TRANSPORT;
        String[] other=pool==FRUIT?TRANSPORT:FRUIT;
        String odd=other[rand(r,other.length)];
        List<String> options=new ArrayList<>();add(options,odd);
        while(options.size()<count(level))add(options,pool[rand(r,pool.length)]);
        return make("Welk plaatje hoort er niet bij?",pool[0]+"  "+pool[1],odd,options,r);
    }
}
