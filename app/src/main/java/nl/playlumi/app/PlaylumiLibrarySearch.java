package nl.playlumi.app;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Local, age-filtered search. Never change a child's selected age to satisfy a query. */
public final class PlaylumiLibrarySearch {
    private PlaylumiLibrarySearch(){}
    static String normalize(String text){
        return Normalizer.normalize(text==null?"":text,Normalizer.Form.NFD).replaceAll("\\p{M}+","")
                .toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").trim();
    }
    private static final String[] WORLD_WORDS={"rekenen wiskunde getallen cijfers","taal letters lezen woorden","vormen patronen ruimtelijk",
            "muziek luisteren tonen ritme","dieren spelen tellen memory","kleuren tekenen weer seizoenen"};
    private static String tags(PlaylumiGameCatalog.Entry e){
        if(e.kind==PlaylumiGameCatalog.QUIZ&&e.mode==16)return "klok tijd uren minuten";
        if(e.kind==PlaylumiGameCatalog.QUIZ&&e.mode==ExtraGames.MATH)return "sommen rekenen";
        if(e.kind==PlaylumiGameCatalog.MINI&&e.mode==PlaylumiMiniChallenge.MEMORY)return "geheugen kaartjes paren";
        if(e.kind==PlaylumiGameCatalog.QUIZ&&e.mode==ExtraGames.FIRST_LAST)return "letters ontbrekende letter";
        return "";
    }
    public static List<PlaylumiGameCatalog.Entry> find(int age,String query){
        String clean=normalize(query);
        ArrayList<PlaylumiGameCatalog.Entry> exact=new ArrayList<>(),other=new ArrayList<>();
        for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age)){
            String title=normalize(e.title),all=normalize(e.title+" "+e.description+" "+PlaylumiWorldStyle.NAMES[e.world]+" "+WORLD_WORDS[e.world]+" "+tags(e));
            boolean matches=true;
            for(String token:clean.split(" "))if(!all.contains(token)){matches=false;break;}
            if(matches){if(!clean.isEmpty()&&title.contains(clean))exact.add(e);else other.add(e);}
        }
        exact.addAll(other);return exact;
    }
}
