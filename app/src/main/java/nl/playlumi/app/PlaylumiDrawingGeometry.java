package nl.playlumi.app;

/** Aspect-preserving paper geometry, independent from Android View and bitmap APIs. */
public final class PlaylumiDrawingGeometry {
    public static final int NEW_WIDTH=1024,NEW_HEIGHT=768;
    public static final int MAX_PIXELS=2_000_000,MAX_EDGE=4096;
    public static final float PAPER_INSET_DP=8;
    private PlaylumiDrawingGeometry(){}

    public static final class Paper {
        public final float left,top,right,bottom,scale;
        private Paper(float left,float top,float right,float bottom,float scale){
            this.left=left;this.top=top;this.right=right;this.bottom=bottom;this.scale=scale;
        }
        public boolean valid(){return scale>0;}
        public boolean contains(float x,float y){return valid()&&x>=left&&x<=right&&y>=top&&y<=bottom;}
        public float xFraction(float viewX){return valid()?unit((viewX-left)/(right-left)):0;}
        public float yFraction(float viewY){return valid()?unit((viewY-top)/(bottom-top)):0;}
        public float viewX(float fraction){return left+unit(fraction)*(right-left);}
        public float viewY(float fraction){return top+unit(fraction)*(bottom-top);}
    }
    public static Paper fit(float width,float height,int documentWidth,int documentHeight,float inset){
        if(!finite(width)||!finite(height)||width<=0||height<=0||documentWidth<=0||documentHeight<=0)
            return new Paper(0,0,0,0,0);
        float margin=finite(inset)?Math.max(0,inset):0;
        float availableWidth=Math.max(0,width-2*margin),availableHeight=Math.max(0,height-2*margin);
        if(availableWidth==0||availableHeight==0)return new Paper(0,0,0,0,0);
        float scale=Math.min(availableWidth/documentWidth,availableHeight/documentHeight);
        float paperWidth=documentWidth*scale,paperHeight=documentHeight*scale;
        float left=(width-paperWidth)/2,top=(height-paperHeight)/2;
        return new Paper(left,top,left+paperWidth,top+paperHeight,scale);
    }
    /** Decode legacy private PNGs within a fixed memory budget, preserving their proportions. */
    public static int sampleSize(int width,int height){
        if(width<=0||height<=0)return 1;
        int sample=1;
        while((decodedEdge(width,sample)*decodedEdge(height,sample)>MAX_PIXELS||
                decodedEdge(width,sample)>MAX_EDGE||decodedEdge(height,sample)>MAX_EDGE)&&sample<(1<<30))sample*=2;
        return sample;
    }
    private static long decodedEdge(int edge,int sample){return ((long)edge+sample-1)/sample;}
    public static int safeBrushSize(int size){return size>=0&&size<=2?size:1;}
    public static int brushWidthDp(int size){return safeBrushSize(size)==0?4:safeBrushSize(size)==2?16:8;}
    private static boolean finite(float value){return !Float.isNaN(value)&&!Float.isInfinite(value);}
    private static float unit(float value){
        if(Float.isNaN(value))return 0;
        return Math.max(0,Math.min(1,value));
    }
}
