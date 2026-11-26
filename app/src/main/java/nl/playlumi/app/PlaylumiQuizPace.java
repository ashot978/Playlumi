package nl.playlumi.app;

/** Per-profile quiz pace; other minigames retain their existing timing. */
public final class PlaylumiQuizPace {
    public static final int AUTO=0,CALM=1,MANUAL=2;
    public static final String[] NAMES={"Automatisch","Rustig tempo","Zelf verder gaan"};
    public static final String[] DETAILS={"De volgende vraag verschijnt vanzelf.","Extra leestijd na elk goed antwoord.","Tik op Verder wanneer je klaar bent."};
    private PlaylumiQuizPace(){}
    public static int safe(int value){return value>=AUTO&&value<=MANUAL?value:AUTO;}
    public static int delay(int value,boolean retried,String feedback){
        switch(safe(value)){
            case MANUAL:return -1;
            case CALM:return Math.max(2200,Math.min(5000,1000+(feedback==null?0:feedback.length()*55)));
            default:return PlaylumiQuizCoach.delay(retried,feedback);
        }
    }
}
