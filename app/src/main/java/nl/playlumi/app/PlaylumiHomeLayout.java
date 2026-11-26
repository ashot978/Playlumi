package nl.playlumi.app;

/** The home layout follows the current usable window, not the device model. */
public final class PlaylumiHomeLayout {
    public static final int PHONE=0,FOLD=1,EXPANDED=2;
    private PlaylumiHomeLayout(){}
    public static int mode(int width,int height){
        if(width>=1100)return EXPANDED;
        if(width>=600||(width>=520&&width>height))return FOLD;
        return PHONE;
    }
    public static int profileWidth(int width){return Math.max(180,Math.min(250,width/6));}
    public static int actionWidth(int width){return Math.max(150,Math.min(245,width/3));}
    public static int worldColumns(int width,int height){
        int mode=mode(width,height);
        if(mode==EXPANDED)return 3;
        return width>=900?3:2;
    }
    public static int artHeight(int width,int height){
        int columns=worldColumns(width,height),rows=6/columns;
        int contentWidth=width-32-actionWidth(width)-(mode(width,height)==EXPANDED?profileWidth(width)+8:0);
        int fitWidth=(contentWidth/columns)*2/3;
        int fitHeight=(height-180)/rows-65;
        return Math.max(84,Math.min(175,Math.min(fitWidth,fitHeight)));
    }
}
