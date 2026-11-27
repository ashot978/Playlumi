package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/** Offline history/geometry gate. Android PNG/alpha rendering is covered by emulator acceptance. */
public final class DrawingHistoryCheck {
    private static final int WIDTH=40,HEIGHT=30;
    private static int operations,papers;
    private static void require(boolean okay,String message){if(!okay)throw new AssertionError(message);}

    /** Independent small pixel oracle: imported image + round-capped paint, erase and clear. */
    private static void apply(int[] pixels,PlaylumiDrawingHistory.Action action){
        if(action.kind==PlaylumiDrawingHistory.CLEAR){Arrays.fill(pixels,0);return;}
        double radius=action.width*Math.min(WIDTH,HEIGHT)/2;
        for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){
            double distance=Double.MAX_VALUE;
            for(int i=0;i<Math.max(1,action.pointCount()-1);i++){
                int end=Math.min(i+1,action.pointCount()-1);
                double ax=action.x(i)*WIDTH,ay=action.y(i)*HEIGHT;
                double dx=action.x(end)*WIDTH-ax,dy=action.y(end)*HEIGHT-ay;
                double length=dx*dx+dy*dy;
                double t=length==0?0:Math.max(0,Math.min(1,((x+.5-ax)*dx+(y+.5-ay)*dy)/length));
                double ex=x+.5-ax-t*dx,ey=y+.5-ay-t*dy;
                distance=Math.min(distance,ex*ex+ey*ey);
            }
            if(distance<=radius*radius)pixels[y*WIDTH+x]=action.kind==PlaylumiDrawingHistory.ERASE?0:action.color;
        }
    }
    private static int[] imported(boolean populated){
        int[] image=new int[WIDTH*HEIGHT];
        if(populated)for(int y=5;y<HEIGHT-5;y++)for(int x=5;x<WIDTH-5;x++)image[y*WIDTH+x]=7;
        return image;
    }
    private static int[] actual(int[] base,PlaylumiDrawingHistory history){
        int[] image=base.clone();
        for(int i=0;i<history.appliedCount();i++)apply(image,history.applied(i));
        return image;
    }
    private static int[] expected(int[] imported,ArrayList<PlaylumiDrawingHistory.Action> reference,int cursor){
        int start=0;
        for(int i=cursor-1;i>=0;i--)if(reference.get(i).kind==PlaylumiDrawingHistory.CLEAR){start=i;break;}
        int[] image=start>0?new int[WIDTH*HEIGHT]:imported.clone();
        for(int i=start;i<cursor;i++)apply(image,reference.get(i));
        return image;
    }
    private static PlaylumiDrawingHistory.Action generated(Random random){
        if(random.nextInt(12)==0)return PlaylumiDrawingHistory.Action.clear();
        PlaylumiDrawingHistory.StrokeBuilder stroke=new PlaylumiDrawingHistory.StrokeBuilder(
                1+random.nextInt(6),.03f+random.nextFloat()*.18f,random.nextInt(4)==0,
                random.nextFloat(),random.nextFloat());
        for(int i=0;i<random.nextInt(4);i++)stroke.append(random.nextFloat(),random.nextFloat());
        return stroke.build();
    }
    private static void historySweep(boolean loaded,long seed){
        Random random=new Random(seed);
        int[] initial=imported(loaded),base=initial.clone();
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(loaded);
        ArrayList<PlaylumiDrawingHistory.Action> reference=new ArrayList<>();
        int cursor=0,floor=0;
        for(int step=0;step<1200;step++){
            int operation=random.nextInt(10);
            if(operation<2){
                boolean possible=cursor>floor;require(history.undo()==possible,"wrong Undo boundary");
                if(possible)cursor--;
            }else if(operation==2){
                boolean possible=cursor<reference.size();require(history.redo()==possible,"wrong Redo boundary");
                if(possible)cursor++;
            }else{
                PlaylumiDrawingHistory.Action action=generated(random);
                while(reference.size()>cursor)reference.remove(reference.size()-1);
                reference.add(action);cursor++;
                if(cursor-floor>PlaylumiDrawingHistory.MAX_ACTIONS)floor++;
                history.append(action,oldest->apply(base,oldest));
            }
            require(history.retainedCount()<=32,"unbounded retained history");
            require(history.appliedCount()==cursor-floor,"applied history cursor differs");
            require(history.canRedo()==(cursor<reference.size()),"incorrect Redo availability");
            require(Arrays.equals(expected(initial,reference,cursor),actual(base,history)),
                    "composed image changed at history boundary or branch, step "+step+" seed "+seed);
            operations++;
        }
    }
    private static void explicitEraserAndClearOverflow(){
        int[] base=imported(true);
        PlaylumiDrawingHistory history=new PlaylumiDrawingHistory(true);
        PlaylumiDrawingHistory.Action eraser=new PlaylumiDrawingHistory.StrokeBuilder(1,.2f,true,.5f,.5f).build();
        history.append(eraser,action->apply(base,action));
        for(int i=0;i<32;i++)history.append(new PlaylumiDrawingHistory.StrokeBuilder(2,.04f,false,.05f,.05f).build(),
                action->apply(base,action));
        require(base[(HEIGHT/2)*WIDTH+WIDTH/2]==0,"eraser was not preserved in the base");
        require(base[8*WIDTH+8]==7,"compacting eraser lost unrelated imported pixels");
        int[] beforeClear=actual(base,history);
        history.append(PlaylumiDrawingHistory.Action.clear(),action->apply(base,action));
        require(history.undo(),"clear cannot be undone");
        require(Arrays.equals(beforeClear,actual(base,history)),"Undo Clear lost the old composition");
        history.redo();
        for(int i=0;i<32;i++)history.append(new PlaylumiDrawingHistory.StrokeBuilder(3,.04f,false,.95f,.95f).build(),
                action->apply(base,action));
        while(history.undo()){}
        for(int pixel:actual(base,history))require(pixel==0,"old artwork returned after Clear was compacted");
    }
    private static void geometrySweep(){
        for(int width:new int[]{280,360,390,430,800,1200})for(int height:new int[]{210,300,320,800})
            for(int[] document:new int[][]{{1024,768},{1520,430},{600,1400}}){
                PlaylumiDrawingGeometry.Paper paper=PlaylumiDrawingGeometry.fit(width,height,document[0],document[1],8);
                require(paper.valid(),"missing usable paper");
                require(Math.abs((paper.right-paper.left)/document[0]-(paper.bottom-paper.top)/document[1])<.00001,
                        "paper is stretched");
                require(paper.left>=7.999&&paper.top>=7.999&&paper.right<=width-7.999&&paper.bottom<=height-7.999,
                        "paper leaves its inset");
                for(int i=0;i<=100;i++){
                    float value=i/100f;
                    require(Math.abs(paper.xFraction(paper.viewX(value))-value)<.00001,"x drift after resize");
                    require(Math.abs(paper.yFraction(paper.viewY(value))-value)<.00001,"y drift after resize");
                }
                papers++;
            }
    }
    private static void longGesture(){
        PlaylumiDrawingHistory.StrokeBuilder stroke=new PlaylumiDrawingHistory.StrokeBuilder(1,.02f,false,.1f,.2f);
        for(int i=0;i<40000;i++){
            stroke.append((i%501)/500f,(i%301)/300f);
            require(stroke.pointCount()<=PlaylumiDrawingHistory.MAX_POINTS,"unbounded gesture points");
        }
        stroke.append(.987f,.654f);
        PlaylumiDrawingHistory.Action action=stroke.build();
        require(action.simplified&&action.pointCount()>1,"long gesture discarded or silently simplified");
        require(action.x(0)==.1f&&action.y(0)==.2f,"long gesture start lost");
        require(action.x(action.pointCount()-1)==.987f&&action.y(action.pointCount()-1)==.654f,"long gesture end lost");
    }
    public static void main(String[] args){
        for(long seed:new long[]{110L,82913L}){historySweep(false,seed);historySweep(true,seed);}
        explicitEraserAndClearOverflow();geometrySweep();longGesture();
        System.out.println("PASS: "+operations+" history/independent pixel comparisons, "+papers+
                " aspect-preserving paper fits, imported-image eraser/Clear overflow, and a 40000-point gesture.");
    }
}
