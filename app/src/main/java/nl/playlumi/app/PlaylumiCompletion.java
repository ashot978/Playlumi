package nl.playlumi.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Permanent, age-specific catalog completion marks. Not a wallet or reward engine. */
public final class PlaylumiCompletion {
    private PlaylumiCompletion(){}
    public static String key(int age){return "library_completed_v1_"+PlaylumiRocketCurriculum.level(age);}
    public static boolean eligible(PlaylumiGameCatalog.Entry entry){
        return entry!=null&&entry.kind!=PlaylumiGameCatalog.DRAW;
    }
    public static boolean record(Set<String> completed,int age,PlaylumiGameCatalog.Entry entry,int done,int total){
        if(completed==null||!eligible(entry)||total<=0||done!=total)return false;
        PlaylumiGameCatalog.Entry canonical=PlaylumiGameCatalog.find(age,entry.id);
        return eligible(canonical)&&completed.add(canonical.id);
    }
    public static List<PlaylumiGameCatalog.Entry> completed(int age,Set<String> marks){
        ArrayList<PlaylumiGameCatalog.Entry> entries=new ArrayList<>();
        if(marks!=null)for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age))
            if(eligible(e)&&marks.contains(e.id))entries.add(e);
        return entries;
    }
    public static int baseStars(int credited){return Math.max(0,Math.min(40,credited))*PlaylumiWardrobe.STARS_FOR_ANSWER;}
    public static int medalSize(int height){return height<500?68:height<720?88:108;}
    public static boolean sideBySide(int width,int height){return width>=540||height<540&&width>=360;}
}
