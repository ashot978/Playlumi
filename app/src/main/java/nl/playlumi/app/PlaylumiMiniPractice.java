package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Presentation and new memory content. All previously released animal cards remain valid. */
public final class PlaylumiMiniPractice {
    private PlaylumiMiniPractice(){}
    private static final String[] THEMES={"Boerderijdieren","Bos en tuin","Waterdieren","Wilde dieren"};
    private static final String[][][] ANIMALS={
        {{"🐶","Hond"},{"🐱","Kat"},{"🐮","Koe"},{"🐰","Konijn"},{"🐔","Kip"},{"🐴","Paard"},{"🐑","Schaap"},{"🐷","Varken"}},
        {{"🐸","Kikker"},{"🐻","Beer"},{"🦊","Vos"},{"🐝","Bij"},{"🦋","Vlinder"},{"🐿️","Eekhoorn"},{"🦉","Uil"},{"🦌","Hert"}},
        {{"🐢","Schildpad"},{"🐧","Pinguïn"},{"🐟","Vis"},{"🐙","Octopus"},{"🐬","Dolfijn"},{"🐳","Walvis"},{"🦀","Krab"},{"🐠","Tropische vis"}},
        {{"🐵","Aap"},{"🐘","Olifant"},{"🦁","Leeuw"},{"🐼","Panda"},{"🐯","Tijger"},{"🦒","Giraf"},{"🦓","Zebra"},{"🦏","Neushoorn"}}
    };
    public static String animalName(String symbol){
        for(String[][] group:ANIMALS)for(String[] animal:group)
            if(animal[0].equals(symbol))return animal[1];
        return "";
    }
    private static int group(String symbol){
        for(int i=0;i<ANIMALS.length;i++)for(String[] animal:ANIMALS[i])if(animal[0].equals(symbol))return i;
        return -1;
    }
    public static String theme(List<String> cards){
        if(cards==null||cards.isEmpty())return "Dieren";
        int first=group(cards.get(0));
        if(first<0)return "Dieren";
        for(String card:cards)if(group(card)!=first)return "Dieren";
        return THEMES[first];
    }
    public static List<String> memory(int level,Random r){
        if(level<0||level>2||r==null)throw new IllegalArgumentException("memory");
        ArrayList<String> pool=new ArrayList<>(),cards=new ArrayList<>();
        for(String[] animal:ANIMALS[r.nextInt(ANIMALS.length)])pool.add(animal[0]);
        Collections.shuffle(pool,r);
        int pairs=level==0?3:level==1?4:6;
        for(int i=0;i<pairs;i++){cards.add(pool.get(i));cards.add(pool.get(i));}
        Collections.shuffle(cards,r);
        return cards;
    }
    public static String cardLabel(int index,boolean visible,boolean matched,String symbol){
        if(matched)return "Gevonden paar: "+animalName(symbol);
        if(visible)return "Kaart "+(index+1)+": "+animalName(symbol);
        return "Gesloten kaart "+(index+1);
    }
    /** Regular rectangles: 3x2, 4x2 or 4x3, with a narrow fallback. */
    public static int memoryColumns(int contentWidth,int count){
        if(count==6)return contentWidth<216?2:3;
        if(contentWidth>=296)return Math.min(4,count);
        return contentWidth>=216?3:2;
    }
    public static int memoryCardHeight(int contentWidth,int height,int count){
        int columns=memoryColumns(contentWidth,count),rows=(count+columns-1)/columns;
        int cell=Math.max(1,(contentWidth-columns*6)/columns);
        return Math.max(54,Math.min(104,Math.min((int)(cell*.84f),(height-205-rows*8)/rows)));
    }
    public static int bubbleColumns(int contentWidth,int count){
        return Math.max(1,Math.min(count,contentWidth>=470?4:contentWidth>=225?3:2));
    }
    public static int bubbleSize(int contentWidth,int height,int count){
        int columns=bubbleColumns(contentWidth,count),rows=(count+columns-1)/columns;
        return Math.max(48,Math.min(94,Math.min((contentWidth-columns*8)/columns,(height-210-rows*10)/rows)));
    }
    /** Visual progress represents only actions already taken, not the next answer. */
    public static String trail(int done,int total,String filled,String blank){
        StringBuilder text=new StringBuilder();int safe=Math.max(0,Math.min(total,done));
        for(int i=0;i<total;i++){if(i>0)text.append("  ");text.append(i<safe?filled:blank);}
        return text.toString();
    }
}
