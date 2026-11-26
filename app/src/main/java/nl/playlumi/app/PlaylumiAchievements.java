package nl.playlumi.app;

/** Stable milestone indices. Earned achievements are permanent; progress does not affect spendable stars. */
public final class PlaylumiAchievements {
    public static final int METRICS=18;
    public static final String[] GROUPS={"Spelen en leren","Ontdekken en verzamelen","Speeldagen"};
    public static final class Milestone {
        public final int group,metric,goal;public final String title,description,icon;
        Milestone(int group,int metric,int goal,String title,String description,String icon){
            this.group=group;this.metric=metric;this.goal=goal;this.title=title;this.description=description;this.icon=icon;
        }
    }
    private static Milestone m(int group,int metric,int goal,String title,String description,String icon){
        return new Milestone(group,metric,goal,title,description,icon);
    }
    public static final Milestone[] ALL={
        m(0,0,1,"Eerste stap","1 goed antwoord","🌱"),
        m(0,0,25,"Nieuwsgierige leerling","25 goede antwoorden","📚"),
        m(0,0,100,"Sterrenleerling","100 goede antwoorden","⭐"),
        m(0,0,250,"Superdenker","250 goede antwoorden","🧠"),
        m(0,0,500,"Magische meester","500 goede antwoorden","👑"),
        m(0,1,1,"Eerste quizronde","1 quizronde voltooid","🏅"),
        m(0,1,10,"Tien avonturen","10 quizrondes voltooid","🎖️"),
        m(0,1,25,"Oefenkampioen","25 quizrondes voltooid","🏆"),
        m(0,2,5,"Vijf op rij","5 goede antwoorden achter elkaar in één categorie","✨"),
        m(0,2,10,"Tien op rij","10 goede antwoorden achter elkaar in één categorie","💫"),
        m(0,3,100,"Sterrenvanger","100 speelsterren verdiend","🌟"),
        m(0,3,500,"Sterrenverzamelaar","500 speelsterren verdiend","🌠"),
        m(0,3,1000,"Sterrenkoning","1.000 speelsterren verdiend","👑"),
        m(1,4,10,"Spelontdekker","10 verschillende spellen geprobeerd","🎮"),
        m(1,4,25,"Avonturier","25 verschillende spellen geprobeerd","🗺️"),
        m(1,4,40,"Wereldreiziger","40 verschillende spellen geprobeerd","🚀"),
        m(1,5,6,"Dorp ontdekt","Alle zes speelwerelden bezocht","🏰"),
        m(1,6,1,"Eerste schat","1 schat verzameld","💎"),
        m(1,6,9,"Schattenzoeker","9 schatten verzameld","🧭"),
        m(1,6,18,"Magische schatkamer","Alle 18 schatten verzameld","🏆"),
        m(1,7,5,"Eigen stijl","5 accessoires vrijgespeeld","🎀"),
        m(1,7,15,"Kledingkast vol magie","15 accessoires vrijgespeeld","🪄"),
        m(1,8,1,"Kunstenaar","Een tekening bewaard","🎨"),
        m(1,9,1,"Lievelingsspel","Een favoriet bewaard","💜"),
        m(1,12,3,"Raketverkenner","Alle schatten van Rekenraket","🚀"),
        m(1,13,3,"Letterheld","Alle schatten van Letterhuis","📖"),
        m(1,14,3,"Vormenkampioen","Alle schatten van Vormkasteel","🏰"),
        m(1,15,3,"Muziekster","Alle schatten van Muziekstudio","🎵"),
        m(1,16,3,"Speelheld","Alle schatten van Speelplein","🎪"),
        m(1,17,3,"Kleurkunstenaar","Alle schatten van Kleurentuin","🌈"),
        m(2,10,1,"Eerste speeldag","Op 1 dag gespeeld","☀️"),
        m(2,10,5,"Vijf speeldagen","Op 5 verschillende dagen gespeeld","🌼"),
        m(2,10,10,"Tien speeldagen","Op 10 verschillende dagen gespeeld","🌻"),
        m(2,10,30,"Dertig speeldagen","Op 30 verschillende dagen gespeeld","🌳"),
        m(2,11,3,"Drie dagen op rij","Langste speelreeks van 3 dagen","🔥"),
        m(2,11,7,"Een week op rij","Langste speelreeks van 7 dagen","✨"),
        m(2,11,14,"Twee weken op rij","Langste speelreeks van 14 dagen","🌟"),
        m(2,11,30,"Dertig dagen op rij","Langste speelreeks van 30 dagen","🏅")
    };
    private PlaylumiAchievements(){}
    public static long unlock(long previous,int[] stats){
        long known=previous&((1L<<ALL.length)-1);
        for(int i=0;i<ALL.length;i++)if(stats[ALL[i].metric]>=ALL[i].goal)known|=1L<<i;
        return known;
    }
}
