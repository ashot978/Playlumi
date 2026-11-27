package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small shelves of related games. Original routes, order and saved state stay intact. */
public final class PlaylumiGameShelf {
    public static final class Group {
        public final int world;
        public final String id,title,description,icon;
        public final List<PlaylumiGameCatalog.Entry> entries;

        private Group(int world,String[] label,List<PlaylumiGameCatalog.Entry> routes){
            this.world=world;id=label[0];title=label[1];description=label[2];icon=label[3];
            entries=Collections.unmodifiableList(new ArrayList<>(routes));
        }

        /** The first age-appropriate route; all other variants remain available in entries. */
        public PlaylumiGameCatalog.Entry defaultEntry(){return entries.get(0);}

        public boolean contains(String entryId){
            for(PlaylumiGameCatalog.Entry entry:entries)if(entry.id.equals(entryId))return true;
            return false;
        }
    }

    // Stable family ids are presentation state only, never replacements for game ids.
    private static final String[][][] LABELS={
        {
            {"tellen","Tellen","Ontdek getallen en hoeveelheden","123"},
            {"rekenen","Rekenen","Speel met sommen","+"},
            {"tijd-meten","Tijd en meten","Ontdek de klok, dagen en lengte","◷"}
        },
        {
            {"letters","Letters","Herken letters en hun volgorde","Aa"},
            {"woorden","Woorden en spelling","Ontdek woorden, voorwerpen en zinnen","Ab"},
            {"lezen-rijmen","Lezen en rijmen","Lees een verhaal of zoek een rijm","📖"}
        },
        {
            {"vormen","Vormen","Zoek, tel en verplaats vormen","▲"},
            {"patronen","Patronen","Ontdek wat er volgt","◆"},
            {"puzzelen","Puzzelen","Vind de weg of de ontbrekende plek","🧩"}
        },
        {
            {"melodieen","Melodieën","Luister en speel de reeks na","♫"},
            {"notenpatronen","Notenpatronen","Maak de rij muzieknoten af","♪"}
        },
        {
            {"dieren","Dieren","Ontdek dieren en zoek gelijke plaatjes","🐾"},
            {"denkspelletjes","Denkspelletjes","Kies wat bij elkaar hoort","💡"},
            {"actief-spelen","Actief spelen","Vang sterren, tel en tik","★"}
        },
        {
            {"kleuren","Kleuren","Herken kleuren en kleurpatronen","●"},
            {"maken","Tekenen en mozaïek","Teken vrij of maak een kleurplaat","🎨"},
            {"weer-seizoenen","Weer en seizoenen","Ontdek zon, regen en de seizoenen","☀"}
        }
    };

    private PlaylumiGameShelf(){}

    /** At most three families per world. Families without routes for this age are omitted. */
    public static List<Group> groups(int age,int world){
        if(world<0||world>=LABELS.length)return Collections.emptyList();
        ArrayList<List<PlaylumiGameCatalog.Entry>> buckets=new ArrayList<>();
        for(int i=0;i<LABELS[world].length;i++)buckets.add(new ArrayList<>());
        for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age)){
            if(entry.world==world)buckets.get(family(entry)).add(entry);
        }
        ArrayList<Group> groups=new ArrayList<>();
        for(int i=0;i<buckets.size();i++)if(!buckets.get(i).isEmpty())
            groups.add(new Group(world,LABELS[world][i],buckets.get(i)));
        return Collections.unmodifiableList(groups);
    }

    public static Group findGroup(int age,int world,String id){
        if(id==null)return null;
        for(Group group:groups(age,world))if(group.id.equals(id))return group;
        return null;
    }

    /** Locate a restored game using its existing route id, without rewriting that id. */
    public static Group groupForEntry(int age,String entryId){
        PlaylumiGameCatalog.Entry entry=PlaylumiGameCatalog.find(age,entryId);
        if(entry==null)return null;
        return findGroup(age,entry.world,LABELS[entry.world][family(entry)][0]);
    }

    // Every route in a world enters exactly one bucket, including later compatible variants.
    private static int family(PlaylumiGameCatalog.Entry entry){
        boolean quiz=entry.kind==PlaylumiGameCatalog.QUIZ;
        boolean interactive=entry.kind==PlaylumiGameCatalog.INTERACTIVE;
        switch(entry.world){
            case 0:
                if(quiz&&(entry.mode==16||entry.mode==22))return 2;
                if(quiz&&entry.mode==ExtraGames.MATH||interactive&&entry.mode==PlaylumiPuzzle.SUMS)return 1;
                return 0;
            case 1:
                if(quiz&&(entry.mode==LearningGame.LETTERS||entry.mode==ExtraGames.FIRST_LAST))return 0;
                if(quiz&&(entry.mode==ExtraGames.RHYMES||
                        entry.mode==PlaylumiReadingPractice.MODE&&entry.variant==PlaylumiReadingPractice.VARIANT))return 2;
                return 1;
            case 2:
                if(quiz&&entry.mode==LearningGame.SHAPES||
                        entry.kind==PlaylumiGameCatalog.MINI&&entry.mode==PlaylumiMiniChallenge.DRAG)return 0;
                if(quiz&&(entry.mode==LearningGame.PATTERNS||entry.mode==23)||
                        interactive&&entry.mode==PlaylumiPuzzle.SYMMETRY)return 1;
                return 2;
            case 3:
                return quiz&&entry.mode==LearningGame.PATTERNS?1:0;
            case 4:
                if(quiz&&entry.mode==21||interactive&&entry.mode==PlaylumiInteractive.HABITATS||
                        entry.kind==PlaylumiGameCatalog.MINI&&entry.mode==PlaylumiMiniChallenge.MEMORY)return 0;
                if(entry.kind==PlaylumiGameCatalog.BUBBLES||entry.kind==PlaylumiGameCatalog.STARS||
                        entry.kind==PlaylumiGameCatalog.MINI&&entry.mode==PlaylumiMiniChallenge.BALLOONS)return 2;
                return 1;
            case 5:
                if(quiz&&entry.mode==17)return 2;
                if(entry.kind==PlaylumiGameCatalog.DRAW||interactive)return 1;
                return 0;
            default:throw new IllegalArgumentException("Unknown game world");
        }
    }
}
