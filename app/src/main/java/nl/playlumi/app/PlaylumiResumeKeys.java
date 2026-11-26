package nl.playlumi.app;

/** Profile separation is supplied by SharedPreferences; each age and route also has its own namespace. */
public final class PlaylumiResumeKeys {
    private PlaylumiResumeKeys(){}
    public static String last(int age){return "library_last_v2_"+PlaylumiRocketCurriculum.level(age);}
    /** A started but unanswered quiz is resumable; a completed or inconsistent one is not. */
    public static boolean validQuizProgress(int count,int attempts,boolean solved,int goal){
        return goal>0&&count>=0&&count<goal&&attempts>=count&&(!solved||count>0);
    }
    public static String session(int age,String route){
        if(route==null||route.isEmpty())throw new IllegalArgumentException("route");
        return "library_resume_v2_"+PlaylumiRocketCurriculum.level(age)+"_"+route;
    }
}
