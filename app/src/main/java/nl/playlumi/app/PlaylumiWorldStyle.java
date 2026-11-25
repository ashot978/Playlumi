package nl.playlumi.app;

/** Shared world identity and avatar choices, independent of Android. */
public final class PlaylumiWorldStyle {
    public static final String[] NAMES={"Rekenraket","Letterhuis","Vormkasteel","Muziekstudio","Speelplein","Kleurentuin"};
    public static final String[] SUBTITLES={"Op missie tussen de sterren!","Ontdek de magie van letters.","Open de poorten van het vormenrijk.","Maak muziek met je dierenvriendjes.","Kies je volgende speelavontuur.","Maak de wereld kleurrijk!"};
    // Preserve saved IDs 1..3. The retired Lumi ID 0 maps to Fenna.
    public static final int FIRST_AVATAR=1;
    public static final String[] AVATAR_NAMES={"","Fenna","Milo","Nova"};
    public static final int PRIMARY_ON_DARK=0xfffff5dc,SECONDARY_ON_DARK=0xffffebbf;
    private static final int[] ACCENT={0xff3657b5,0xff9b4775,0xff7151b4,0xff7041ac,0xff14807e,0xff2b8458};
    private static final int[] DEEP={0xff21316d,0xff59274f,0xff432371,0xff3e236f,0xff174f61,0xff194f42};
    private static final int[] PALE={0xffe1efff,0xffffe6ed,0xffede1ff,0xfff1e2ff,0xffd8f4f2,0xffe3f4df};
    private PlaylumiWorldStyle(){}
    public static boolean valid(int world){return world>=0 && world<NAMES.length;}
    public static int safeAvatar(int id){return id>=FIRST_AVATAR && id<AVATAR_NAMES.length?id:FIRST_AVATAR;}
    public static int accent(int world){return ACCENT[valid(world)?world:0];}
    public static int deep(int world){return DEEP[valid(world)?world:0];}
    public static int pale(int world){return PALE[valid(world)?world:0];}
    private static int blend(int a,int b,float t){
        int r=Math.round(((a>>16)&255)*(1-t)+((b>>16)&255)*t);
        int g=Math.round(((a>>8)&255)*(1-t)+((b>>8)&255)*t);
        int bl=Math.round((a&255)*(1-t)+(b&255)*t);
        return 0xff000000|(r<<16)|(g<<8)|bl;
    }
    public static int plateTop(int world){return blend(deep(world),accent(world),.55f);}
    public static int plateMiddle(int world){return blend(deep(world),accent(world),.32f);}
    public static float swatchDiameter(float width,float height){return Math.max(0,Math.min(width,height)*.78f);}
}
