package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Version-three content for the real mosaic board and animal habitat game. */
public final class PlaylumiHandsOn {
    private PlaylumiHandsOn(){}
    private static final String[] HOMES={"🌊 Zee","🌾 Boerderij","🌳 Bos","🏜️ Woestijn","❄️ Poolgebied"};
    private static final String[][] ANIMALS={
        {"🐠","Tropische vis","0"},{"🐬","Dolfijn","0"},{"🐙","Octopus","0"},
        {"🐄","Koe","1"},{"🐑","Schaap","1"},{"🐔","Kip","1"},
        {"🐿️","Eekhoorn","2"},{"🦉","Bosuil","2"},{"🦌","Edelhert","2"},
        {"🐪","Dromedaris","3"},{"🦂","Woestijnschorpioen","3"},{"🦊","Woestijnvos","3"},
        {"🐧","Keizerspinguïn","4"},{"🐻‍❄️","IJsbeer","4"}
    };
    private static final String[] COLORS={"🔴 Rood","🔵 Blauw","🟡 Geel","🟢 Groen","🟣 Paars","🟠 Oranje"};
    private static final int[] PAINTS={0xffdb3b4c,0xff2768cf,0xfff5d02c,0xff269a54,0xff9143c2,0xfff07b24};
    public static int habitatCount(int level){return 3+level;}
    public static int habitatSteps(int level){return 3+2*level;}
    public static int mosaicSteps(int level){return level==0?4:level==1?6:9;}
    public static int mosaicColumns(int level){return level==0?2:3;}
    public static int mosaicColors(int level){return level==0?2:level==1?3:5;}
    public static String animalName(String symbol){
        for(String[] a:ANIMALS)if(a[0].equals(symbol))return a[1];
        return "";
    }
    public static String homeFor(String symbol){
        for(String[] a:ANIMALS)if(a[0].equals(symbol))return HOMES[Integer.parseInt(a[2])];
        return "";
    }
    public static String name(String value){
        int space=value.indexOf(' ');return space<0?value:value.substring(space+1);
    }
    public static int color(String option){
        for(int i=0;i<COLORS.length;i++)if(COLORS[i].equals(option))return PAINTS[i];
        return 0xffe6d5f8;
    }
    public static int habitatColor(String option){
        for(int i=0;i<HOMES.length;i++)if(HOMES[i].equals(option))
            return new int[]{0xffd0e7ff,0xffffedc5,0xffd3f0d7,0xffffd6a5,0xffe1e9fa}[i];
        return 0xffe6d5f8;
    }
    /** Each active home appears at least once; extra animals never repeat. */
    public static void habitats(int level,Random r,String[] targets,String[] answers,List<String> opts){
        int homes=habitatCount(level);
        ArrayList<Integer> selected=new ArrayList<>(),rest=new ArrayList<>();
        for(int h=0;h<homes;h++){
            opts.add(HOMES[h]);ArrayList<Integer> group=new ArrayList<>();
            for(int i=0;i<ANIMALS.length;i++)if(Integer.parseInt(ANIMALS[i][2])==h)group.add(i);
            Collections.shuffle(group,r);selected.add(group.remove(0));rest.addAll(group);
        }
        Collections.shuffle(rest,r);
        while(selected.size()<targets.length)selected.add(rest.remove(0));
        Collections.shuffle(selected,r);
        for(int i=0;i<targets.length;i++){targets[i]=ANIMALS[selected.get(i)][0];answers[i]=homeFor(targets[i]);}
    }
    /** Draw a complete age-specific grid using a palette that is actually represented. */
    public static void mosaic(int level,Random r,String[] targets,String[] answers,List<String> opts){
        ArrayList<String> palette=new ArrayList<>();
        Collections.addAll(palette,COLORS);Collections.shuffle(palette,r);
        int colorCount=mosaicColors(level);
        ArrayList<String> tiles=new ArrayList<>();
        for(int i=0;i<colorCount;i++){opts.add(palette.get(i));tiles.add(palette.get(i));}
        while(tiles.size()<targets.length)tiles.add(opts.get(r.nextInt(colorCount)));
        Collections.shuffle(tiles,r);
        for(int i=0;i<targets.length;i++)targets[i]=answers[i]=tiles.get(i);
        // Keep palette order independent of the target grid.
        Collections.shuffle(opts,r);
    }
}
