package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The permanent, offline expedition. Published v1 stages are deliberately pinned to
 * catalog identifiers, not list indices or titles; additions to the library must
 * never change the meaning of an already saved milestone.
 */
public final class PlaylumiExpedition {
    public static final int CHAPTERS=6,STAGES_PER_CHAPTER=5,TOTAL=30;
    public static final int CHAPTER_STARS=15,FINALE_STARS=30;
    public static final String[] TITLES={
        "De sterrenkaart","De letterpoort","Het kristallenkasteel",
        "Het maanconcert","Het dierenfestival","De regenboogschat"
    };
    public static final String[] ICONS={"🚀","📖","🏰","♫","🐾","🌈"};
    public static final String[] STAMPS={"Sterrenzegel","Letterzegel","Kristalzegel","Maanzegel","Dierenzegel","Regenboogzegel"};
    public static final String[] STORIES={
        "De sterren zijn verstopt. Help Lumi de weg naar de letterpoort te vinden.",
        "Achter de poort ligt een geheim woord. Verzamel letters en maak de deur open.",
        "Een kristalpad leidt door het kasteel. Ontdek vormen en bereik de gouden ster.",
        "De maan wacht op een nieuw lied. Oefen ritmes en maak het concert compleet.",
        "De dierenvriendjes bereiden een feest voor. Help iedereen en verzamel de juiste spullen.",
        "Alle kleuren komen samen. Vind de laatste aanwijzingen en ontdek de regenboogschat."
    };
    public static final String[] FINISH={
        "De sterrenkaart wijst de weg!","De letterpoort is geopend!","Het kristal straalt weer!",
        "Het maanconcert klinkt!","Alle dierenvriendjes zijn klaar voor het feest!","De regenboogschat is gevonden!"
    };
    private PlaylumiExpedition(){}
    private static String q(int world,int mode,int variant){return id(world,0,mode,variant);}
    private static String g(int world,int kind,int mode){return id(world,kind,mode,0);}
    private static String id(int world,int kind,int mode,int variant){return world+":"+kind+":"+mode+":"+variant;}
    private static String puzzle(int kind){return g(PlaylumiPuzzle.world(kind),PlaylumiGameCatalog.INTERACTIVE,kind);}
    private static String[] pinned(int age){
        return new String[]{
            // Chapter 1: the first stage is a real board, so even new readers can start.
            puzzle(PlaylumiPuzzle.NUMBER_HUNT),
            q(0,PlaylumiFocusedCounting.MODE,PlaylumiFocusedCounting.VARIANT),
            q(0,ExtraGames.COMPARE,PlaylumiThinkingPractice.EQUAL_GROUPS),
            age==0?q(0,LearningGame.COUNT,PlaylumiSkillPractice.VARIANT):
                age==1?q(0,LearningGame.NUMBERS,PlaylumiSkillPractice.VARIANT):puzzle(PlaylumiPuzzle.SUMS),
            g(0,PlaylumiGameCatalog.INTERACTIVE,PlaylumiInteractive.NUMBERS),
            // Chapter 2: literacy, with a different task for each age.
            q(1,LearningGame.LETTERS,0),q(1,LearningGame.LETTERS,1),
            age==0?g(4,PlaylumiGameCatalog.MINI,PlaylumiMiniChallenge.MEMORY):
                age==1?q(1,LearningGame.WORDS,0):q(1,PlaylumiReadingPractice.MODE,PlaylumiReadingPractice.VARIANT),
            g(1,PlaylumiGameCatalog.INTERACTIVE,PlaylumiInteractive.LETTERS),
            puzzle(PlaylumiPuzzle.WORD_PATH),
            // Chapter 3: spatial reasoning.
            q(2,LearningGame.SHAPES,0),
            age==0?q(2,LearningGame.SHAPES,1):puzzle(PlaylumiPuzzle.SYMMETRY),
            q(2,LearningGame.SHAPES,PlaylumiSkillPractice.VARIANT),
            g(2,PlaylumiGameCatalog.MINI,PlaylumiMiniChallenge.DRAG),
            puzzle(PlaylumiPuzzle.MAZE),
            // Chapter 4: pattern recognition and a real audio/visual finale.
            q(2,LearningGame.PATTERNS,0),
            g(3,PlaylumiGameCatalog.MUSIC,0),
            q(3,LearningGame.PATTERNS,PlaylumiSkillPractice.VARIANT),
            age==0?g(4,PlaylumiGameCatalog.MINI,PlaylumiMiniChallenge.BALLOONS):id(3,PlaylumiGameCatalog.MUSIC,0,age==2?2:1),
            puzzle(PlaylumiPuzzle.RHYTHM),
            // Chapter 5: understanding animals and categories.
            q(4,21,1),q(4,ExtraGames.CATEGORIES,0),
            q(4,PlaylumiOddOneOut.MODE,PlaylumiOddOneOut.VARIANT),
            g(4,PlaylumiGameCatalog.INTERACTIVE,PlaylumiInteractive.HABITATS),
            puzzle(PlaylumiPuzzle.SORT),
            // Chapter 6: the final task is age-appropriate.
            q(5,LearningGame.COLORS,0),
            age==0?q(5,LearningGame.COLORS,1):q(5,LearningGame.COLORS,PlaylumiSkillPractice.VARIANT),
            g(5,PlaylumiGameCatalog.INTERACTIVE,PlaylumiInteractive.MOSAIC),
            puzzle(PlaylumiPuzzle.PIXEL),
            age==0?g(4,PlaylumiGameCatalog.STARS,0):
                age==1?q(5,LearningGame.PATTERNS,4):puzzle(PlaylumiPuzzle.MAP)
        };
    }
    public static String key(int age){return "expedition_v1_progress_"+PlaylumiRocketCurriculum.level(age);}
    public static int safe(int done){return Math.max(0,Math.min(TOTAL,done));}
    public static int currentChapter(int done){return Math.min(CHAPTERS-1,safe(done)/STAGES_PER_CHAPTER);}
    public static int chapterProgress(int done,int chapter){
        if(chapter<0||chapter>=CHAPTERS)throw new IllegalArgumentException("chapter");
        return Math.max(0,Math.min(STAGES_PER_CHAPTER,safe(done)-chapter*STAGES_PER_CHAPTER));
    }
    public static int stamps(int done){return safe(done)/STAGES_PER_CHAPTER;}
    public static int starsEarned(int done){return stamps(done)*CHAPTER_STARS+(safe(done)==TOTAL?FINALE_STARS:0);}
    public static int totalStars(){return CHAPTERS*CHAPTER_STARS+FINALE_STARS;}
    public static boolean available(int done,int chapter){return chapter>=0&&chapter<CHAPTERS&&chapter<=currentChapter(done);}
    public static boolean finished(int done){return safe(done)==TOTAL;}
    public static String routeId(int age,int index){
        if(index<0||index>=TOTAL)return "";
        return pinned(PlaylumiRocketCurriculum.level(age))[index];
    }
    public static PlaylumiGameCatalog.Entry route(int age,int index){
        if(index<0||index>=TOTAL)return null;
        return PlaylumiGameCatalog.find(age,routeId(age,index));
    }
    public static List<PlaylumiGameCatalog.Entry> chapter(int age,int index){
        if(index<0||index>=CHAPTERS)throw new IllegalArgumentException("chapter");
        ArrayList<PlaylumiGameCatalog.Entry> result=new ArrayList<>();
        for(int i=index*STAGES_PER_CHAPTER;i<(index+1)*STAGES_PER_CHAPTER;i++){
            PlaylumiGameCatalog.Entry entry=route(age,i);
            if(entry==null||!PlaylumiCompletion.eligible(entry))throw new IllegalStateException("Missing expedition route "+routeId(age,i));
            result.add(entry);
        }
        return Collections.unmodifiableList(result);
    }
    public static final class Advance{
        public final int before,after,bonus;
        private Advance(int before,int after){this.before=before;this.after=after;bonus=starsEarned(after)-starsEarned(before);}
        public boolean changed(){return before!=after;}
    }
    /**
     * Only a completed catalog round started from the expedition can advance it.
     * The caller commits this result and any wallet bonus in its existing completion transaction.
     * Old completions, repeated missions, arbitrary routes and partial rounds do not count.
     */
    public static Advance complete(int age,int stored,String route,boolean expedition,int completed,int total){
        int done=safe(stored);
        if(!expedition||done==TOTAL||total<=0||completed!=total||route==null||!route.equals(routeId(age,done)))return new Advance(done,done);
        if(!PlaylumiCompletion.eligible(PlaylumiGameCatalog.find(age,route)))return new Advance(done,done);
        return new Advance(done,done+1);
    }
}
