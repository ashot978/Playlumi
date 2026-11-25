package nl.playlumi.app;

/** Age-specific, explicit curriculum. The village does not reuse stale adaptive levels. */
public final class PlaylumiRocketCurriculum {
    private PlaylumiRocketCurriculum(){}
    public static final class Mission {
        public final String title,description;
        public final int mode,variant;
        Mission(String title,String description,int mode,int variant){
            this.title=title;this.description=description;this.mode=mode;this.variant=variant;
        }
    }
    private static Mission m(String t,String d,int mode,int v){return new Mission(t,d,mode,v);}
    private static final Mission[][] OFFER={
        {
            m("Voorwerpen tellen","Tel tot drie",LearningGame.COUNT,0),
            m("Vingers tellen","Hoeveel vingers zie je?",LearningGame.NUMBERS,0),
            m("Meer sterren tellen","Tel andere voorwerpen",LearningGame.COUNT,1)
        },
        {
            m("Voorwerpen tellen","Tel tot vijf",LearningGame.COUNT,0),
            m("Vingers tellen","Herken hoeveelheden",LearningGame.NUMBERS,0),
            m("Cijfer naar woord","Koppel bijvoorbeeld 5 aan vijf",LearningGame.NUMBERS,1),
            m("Woord naar cijfer","Koppel bijvoorbeeld vijf aan 5",LearningGame.NUMBERS,2),
            m("Meer of minder","Vergelijk getallen tot tien",ExtraGames.COMPARE,0)
        },
        {
            m("Optellen","Reken sommen uit tot twintig",ExtraGames.MATH,0),
            m("Aftrekken","Reken uit hoeveel er overblijft",ExtraGames.MATH,1),
            m("Getal aanvullen","Welk getal ontbreekt?",ExtraGames.MATH,2),
            m("Meer of minder","Vergelijk getallen tot twintig",ExtraGames.COMPARE,0),
            m("Verhaalsommen","Los kleine sterrenavonturen op",ExtraGames.MATH,4)
        }
    };
    public static Mission[] missions(int age){return OFFER[Math.max(0,Math.min(2,age))].clone();}
    public static boolean isMathMode(int mode){
        return mode==LearningGame.COUNT||mode==LearningGame.NUMBERS||mode==ExtraGames.MATH||
                mode==ExtraGames.COMPARE||mode==ExtraGames.SEQUENCES;
    }
    public static int level(int age){return Math.max(0,Math.min(2,age));}
}
