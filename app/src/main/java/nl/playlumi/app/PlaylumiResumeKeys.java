package nl.playlumi.app;

/** Profile separation is supplied by SharedPreferences; each age and route also has its own namespace. */
public final class PlaylumiResumeKeys {
    private PlaylumiResumeKeys(){}
    public static String last(int age){return "library_last_v2_"+PlaylumiRocketCurriculum.level(age);}
    public static String session(int age,String route){
        if(route==null||route.isEmpty())throw new IllegalArgumentException("route");
        return "library_resume_v2_"+PlaylumiRocketCurriculum.level(age)+"_"+route;
    }
}
