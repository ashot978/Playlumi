package nl.playlumi.app;

/** Density-independent geometry for the growing city. The seven sites are real
 * saved achievement rewards; geometry and drawing never grant progress. */
public final class PlaylumiCityLayout {
    public static final int SITES=7;
    public static final float MIN_TARGET=48f;
    public final float width,height;
    public final int columns,rows;
    private final Plot[] plots=new Plot[SITES];

    public static final class Plot {
        public final float left,top,width,artHeight,labelHeight,height;
        private Plot(float l,float t,float w,float art,float label){
            left=l;top=t;width=w;artHeight=art;labelHeight=label;height=art+label;
        }
        public float centerX(){return left+width/2f;}
        public float groundY(){return top+artHeight-8f;}
        public float right(){return left+width;}
        public float bottom(){return top+height;}
    }

    private PlaylumiCityLayout(float availableWidth,float fontScale){
        width=Math.max(96f,finite(availableWidth)?availableWidth:360f);
        float textScale=finite(fontScale)?Math.max(1f,fontScale):1f;
        // Keep the longest native status label readable as system text grows.
        // Wide windows can retain two columns instead of jumping straight to one.
        int desired=width>=620f?3:width>=300f?2:1;
        float minimumCell=Math.max(136f,112f*textScale);
        while(desired>1&&(width-24f-10f*(desired-1))/desired<minimumCell)desired--;
        columns=desired;
        rows=(SITES+columns-1)/columns;
        float edge=12f,gap=10f;
        float cell=(width-edge*2f-gap*(columns-1))/columns;
        float art=Math.max(92f,Math.min(176f,cell*.90f));
        float label=Math.max(58f,(textScale<=1.2f?36f:50f)*textScale+16f);
        float row=art+label+16f;
        for(int i=0;i<SITES;i++){
            int r=i/columns,c=i%columns;
            int inRow=Math.min(columns,SITES-r*columns);
            float used=inRow*cell+(inRow-1)*gap;
            float x=(width-used)/2f+c*(cell+gap);
            plots[i]=new Plot(x,28f+r*row,cell,art,label);
        }
        height=28f+rows*row+14f;
    }
    public static PlaylumiCityLayout forWidth(float widthDp,float fontScale){
        return new PlaylumiCityLayout(widthDp,fontScale);
    }
    private static boolean finite(float value){return !Float.isNaN(value)&&!Float.isInfinite(value);}
    public Plot plot(int index){
        if(index<0||index>=SITES)throw new IllegalArgumentException("Unknown city site");
        return plots[index];
    }
}
