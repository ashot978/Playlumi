package nl.playlumi.app;

/** Twelve original offline instrument-only tunes, no speech or user data. */
public final class PlaylumiMusicBox {
    public static final int COUNT=12, PAGE_SIZE=6, PAGES=2;
    private static final String[] NAMES={
        "Sterrenfeest","Tuingeluk","Verhaalmuziek","Kaartenpaar",
        "Ontdekpret","Groot feest","Welkom thuis","Bubbeldans",
        "Sterrenregen","Hiep hiep","Vrolijke tik","Level omhoog"
    };
    private static final String[] ICONS={
        "🎉","🌱","📖","🧩","🦋","🎈","🌈","🫧","✨","👏","🥁","🚀"
    };
    private PlaylumiMusicBox(){}

    private static void check(int index){
        if(index<0||index>=COUNT)throw new IllegalArgumentException("track");
    }
    public static int effect(int index){check(index);return PlaylumiTunes.BONUS+index;}
    public static String name(int index){check(index);return NAMES[index];}
    public static String title(int index){check(index);return ICONS[index]+" "+NAMES[index];}
    public static int safePage(int page){return page<0||page>=PAGES?0:page;}
    public static int safeSelected(int index){return index<0||index>=COUNT?-1:index;}
    public static int firstOnPage(int page){return safePage(page)*PAGE_SIZE;}
    /** For a uniform draw, select a different tune than the previous one. */
    public static int surprise(int previous,int draw){
        int old=safeSelected(previous);
        int choices=old<0?COUNT:COUNT-1;
        if(draw<0||draw>=choices)throw new IllegalArgumentException("draw");
        return old<0?draw:draw>=old?draw+1:draw;
    }
}
