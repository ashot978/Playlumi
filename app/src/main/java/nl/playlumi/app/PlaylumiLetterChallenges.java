package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Complete an actual missing letter, or apply it to another word. */
public final class PlaylumiLetterChallenges {
    public static final String FILL_FIRST="Welke eerste letter ontbreekt?";
    public static final String FILL_LAST="Welke laatste letter ontbreekt?";
    public static final String MATCH_FIRST="Welk woord begint met dezelfde letter?";
    public static final String MATCH_LAST="Welk woord eindigt met dezelfde letter?";
    private static final String[][] WORDS={
        {"🐱","kat"},{"🐟","vis"},{"🐔","kip"},{"🐵","aap"},{"🐻","beer"},
        {"🐭","muis"},{"🦊","vos"},{"🐝","bij"},{"🦆","eend"},{"🐶","hond"},
        {"🐸","kikker"},{"🐰","konijn"},{"🐴","paard"},{"🦁","leeuw"},{"🐯","tijger"},
        {"🐷","varken"},{"🐑","schaap"},{"🦋","vlinder"},{"🐌","slak"},{"🐘","olifant"},
        {"🐬","dolfijn"},{"🦉","uil"},{"🐧","pinguïn"},{"🐢","schildpad"},{"🐊","krokodil"},
        {"🦓","zebra"},{"🦒","giraf"},{"🦀","krab"},{"🦘","kangoeroe"},{"🦔","egel"},
        {"🍎","appel"},{"🌙","maan"},{"🚲","fiets"},{"🌳","boom"},{"☀️","zon"},
        {"⭐","ster"},{"🏠","huis"},{"🧸","beer"},{"🌷","tulp"},{"🍐","peer"}
    };
    private static final String[] OPTIONS={
        "appel","maan","vis","fiets","boom","ster","huis","tulp","peer","boek","stoel","deur",
        "raam","jas","tas","pen","kom","bal","bel","kam","kap","hap","hek","hoek","dag",
        "tafel","vogel","kasteel","winkel","regen","water","sneeuw","broek","broekzak","brug",
        "hond","paard","schild","konijn","kip","kat","slak","kikker","varken","schaap",
        "bloem","kaars","schip","beker","vos","leeuw","zebra","giraf","krab","bij",
        "eend","egel","olifant","dolfijn","uil","pinguïn","vlag","potlood","pot","wolk",
        "tak","dak","bak","vaas","kaas","mes","taart","roos","rood","geel","groen",
        "blauw","wit","zwart","bruin","grijs","muts","neus","ring","thee","koe","zee"
    };
    private PlaylumiLetterChallenges(){}
    private static String edge(String word,boolean last){return last?word.substring(word.length()-1):word.substring(0,1);}
    public static boolean legacy(int mode,int variant,String display){
        return mode==ExtraGames.FIRST_LAST&&variant<=1&&(display==null||display.indexOf('_')<0);
    }
    public static String replacement(String prompt,String answer){
        if(MATCH_FIRST.equals(prompt)&&answer!=null&&!answer.isEmpty())return edge(answer,false);
        if(MATCH_LAST.equals(prompt)&&answer!=null&&!answer.isEmpty())return edge(answer,true);
        return answer;
    }
    public static String word(String display,String letter){
        if(display==null||letter==null||letter.length()!=1)return "";
        int nl=display.indexOf('\n');
        if(nl<0)return "";
        return display.substring(nl+1).replace("_",letter);
    }
    public static LearningGame.Round next(boolean last,int level,Random random){
        if(level<0||level>2)throw new IllegalArgumentException("level");
        boolean match=level==2&&random.nextBoolean();
        int bound=level==0?10:level==1?25:WORDS.length;
        String[] item=WORDS[random.nextInt(bound)];
        String word=item[1],letter=edge(word,last);
        String blank=last?word.substring(0,word.length()-1)+"_":"_"+word.substring(1);
        ArrayList<String> options=new ArrayList<>();
        String right=letter,prompt=last?FILL_LAST:FILL_FIRST;
        int size=level==0?3:level==1?4:6;
        if(match){
            ArrayList<String> same=new ArrayList<>(),other=new ArrayList<>();
            for(String candidate:OPTIONS){
                if(candidate.equals(word))continue;
                if(edge(candidate,last).equals(letter))same.add(candidate);else other.add(candidate);
            }
            if(!same.isEmpty()){
                right=same.get(random.nextInt(same.size()));prompt=last?MATCH_LAST:MATCH_FIRST;
                options.add(right);Collections.shuffle(other,random);
                // A single matching word; other options never have the target edge.
                for(int i=0;i<size-1;i++)options.add(other.get(i));
            }
        }
        if(options.isEmpty()){
            options.add(letter);
            String similar="abcdefghijklmnopqrstuvwxyz";
            ArrayList<String> letters=new ArrayList<>();
            for(int i=0;i<similar.length();i++)if(!similar.substring(i,i+1).equals(letter))letters.add(similar.substring(i,i+1));
            Collections.shuffle(letters,random);
            for(int i=0;i<size-1;i++)options.add(letters.get(i));
        }
        Collections.shuffle(options,random);
        List<Integer> colors=new ArrayList<>();
        for(int i=0;i<size;i++)colors.add(0xff385b86);
        return new LearningGame.Round(prompt,item[0]+"\n"+blank,0xff385b86,options,colors,options.indexOf(right));
    }
}
