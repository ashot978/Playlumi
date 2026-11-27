package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Arrays;

/** Bounded, document-relative drawing operations. No Android, storage or reward state. */
public final class PlaylumiDrawingHistory {
    public static final int MAX_ACTIONS=32,MAX_POINTS=4096;
    public static final int PAINT=0,ERASE=1,CLEAR=2;

    /** The View must apply this operation to its base image before it leaves Undo history. */
    public interface Compactor { void flatten(Action action); }

    public static final class Action {
        public final int kind,color;
        /** Width as a fraction of the shorter document edge, captured when the stroke starts. */
        public final float width;
        public final boolean simplified;
        private final float[] points;

        private Action(int kind,int color,float width,float[] points,boolean simplified){
            this.kind=kind;this.color=color;this.width=width;this.points=points;this.simplified=simplified;
        }
        public int pointCount(){return points.length/2;}
        public float x(int point){return points[point*2];}
        public float y(int point){return points[point*2+1];}
        public static Action clear(){return new Action(CLEAR,0,0,new float[0],false);}
    }

    /** One whole pointer gesture; long gestures keep both ends and report simplification. */
    public static final class StrokeBuilder {
        private final int kind,color;
        private final float width;
        private float[] points=new float[64];
        private int count;
        private boolean simplified;

        public StrokeBuilder(int color,float width,boolean eraser,float x,float y){
            if(!finite(width)||width<=0)throw new IllegalArgumentException("positive finite stroke width");
            this.kind=eraser?ERASE:PAINT;this.color=color;this.width=Math.min(1f,width);
            if(!append(x,y))throw new IllegalArgumentException("finite first point");
        }
        public int kind(){return kind;}
        public int color(){return color;}
        public float width(){return width;}
        public int pointCount(){return count;}
        public float x(int point){return points[point*2];}
        public float y(int point){return points[point*2+1];}
        public boolean simplified(){return simplified;}

        public boolean append(float x,float y){
            if(!finite(x)||!finite(y))return false;
            x=unit(x);y=unit(y);
            if(count>0&&points[(count-1)*2]==x&&points[(count-1)*2+1]==y)return false;
            if(count==MAX_POINTS){
                // Retain the start, every second intermediate point, and the previous end.
                // The new endpoint is then appended, so an arbitrarily long gesture continues.
                int keep=1;
                for(int i=2;i<count-1;i+=2){
                    points[keep*2]=points[i*2];points[keep*2+1]=points[i*2+1];keep++;
                }
                points[keep*2]=points[(count-1)*2];points[keep*2+1]=points[(count-1)*2+1];
                count=keep+1;simplified=true;
            }
            if((count+1)*2>points.length)
                points=Arrays.copyOf(points,Math.min(MAX_POINTS*2,points.length*2));
            points[count*2]=x;points[count*2+1]=y;count++;return true;
        }
        public Action build(){
            return new Action(kind,color,width,Arrays.copyOf(points,count*2),simplified);
        }
    }

    private final ArrayList<Action> actions=new ArrayList<>();
    private int cursor;
    private boolean baseHasArtwork;

    public PlaylumiDrawingHistory(boolean baseHasArtwork){this.baseHasArtwork=baseHasArtwork;}
    public boolean canUndo(){return cursor>0;}
    public boolean canRedo(){return cursor<actions.size();}
    public int appliedCount(){return cursor;}
    public int retainedCount(){return actions.size();}
    public Action applied(int index){
        if(index<0||index>=cursor)throw new IndexOutOfBoundsException("unapplied action");
        return actions.get(index);
    }
    public boolean contains(Action action){return actions.contains(action);}

    public void append(Action action,Compactor compactor){
        if(action==null)throw new IllegalArgumentException("drawing action");
        if(cursor==MAX_ACTIONS){
            if(compactor==null)throw new IllegalArgumentException("base-image compactor");
            Action first=actions.get(0);
            // Do not discard an operation if composing it into the base fails.
            compactor.flatten(first);
            baseHasArtwork=hasArtworkAfter(baseHasArtwork,first);
            actions.remove(0);cursor--;
        }
        while(actions.size()>cursor)actions.remove(actions.size()-1);
        actions.add(action);cursor++;
    }
    public boolean undo(){if(!canUndo())return false;cursor--;return true;}
    public boolean redo(){if(!canRedo())return false;cursor++;return true;}

    /** Conservative: an eraser can remove pixels, but never invents drawn content. */
    public boolean hasArtwork(){
        boolean present=baseHasArtwork;
        for(int i=0;i<cursor;i++)present=hasArtworkAfter(present,actions.get(i));
        return present;
    }
    private static boolean hasArtworkAfter(boolean before,Action action){
        return action.kind==CLEAR?false:action.kind==PAINT||before;
    }
    private static boolean finite(float value){return !Float.isNaN(value)&&!Float.isInfinite(value);}
    private static float unit(float value){return Math.max(0,Math.min(1,value));}
}
