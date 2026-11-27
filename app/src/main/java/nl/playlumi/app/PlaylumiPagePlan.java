package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pure layout policy. Pages are disjoint, whole measured blocks, never arbitrary scroll offsets. */
public final class PlaylumiPagePlan {
    private PlaylumiPagePlan(){}
    public static final class Block {
        public final int start,end;
        public Block(int start,int end){
            if(start<0||end<=start)throw new IllegalArgumentException("page block");
            this.start=start;this.end=end;
        }
    }
    public static final class Page {
        public final int start,end,blocks;
        public final float scale;
        private Page(int start,int end,int blocks,int viewport){
            this.start=start;this.end=end;this.blocks=blocks;
            this.scale=Math.min(1f,(float)viewport/Math.max(1,end-start));
        }
    }
    public static List<Page> pack(List<Block> input,int viewport,int maxBlocks){
        int height=Math.max(1,viewport),limit=Math.max(1,maxBlocks);
        ArrayList<Block> sorted=new ArrayList<>(input);
        // Android 23 supports Collections.sort, but not Comparator.comparingInt/thenComparingInt.
        Collections.sort(sorted,(a,b)->a.start!=b.start?Integer.compare(a.start,b.start):Integer.compare(a.end,b.end));
        ArrayList<Block> merged=new ArrayList<>();
        for(Block b:sorted){
            if(merged.isEmpty()||b.start>=merged.get(merged.size()-1).end)merged.add(b);
            else{
                Block old=merged.remove(merged.size()-1);
                merged.add(new Block(old.start,Math.max(old.end,b.end)));
            }
        }
        ArrayList<Page> pages=new ArrayList<>();
        int start=-1,end=-1,count=0;
        for(Block b:merged){
            if(count>0&&(b.end-start>height||count>=limit)){
                pages.add(new Page(start,end,count,height));start=-1;count=0;
            }
            if(count==0)start=b.start;
            end=b.end;count++;
            // If an indivisible custom surface is taller than the viewport, fit
            // that surface on its own page; never crop a button, canvas or board.
            if(end-start>=height){pages.add(new Page(start,end,count,height));start=-1;count=0;}
        }
        if(count>0)pages.add(new Page(start,end,count,height));
        if(pages.isEmpty())pages.add(new Page(0,0,0,height));
        return Collections.unmodifiableList(pages);
    }
    /** Calm menus: at most four game tiles, with bigger labels on narrow or large-text screens. */
    public static int libraryColumns(int width,float fontScale){
        return width<300||(fontScale>=1.35f&&width<480)?1:2;
    }
    public static int libraryRows(int height,float fontScale){
        return height<490||height<600&&fontScale>=1.35f?1:2;
    }
    public static int libraryTileHeight(int height,int rows){
        int available=Math.max(88,height-204);
        return Math.max(88,Math.min(218,available/Math.max(1,rows)-14));
    }
    public static String caption(String name,int index,int count){
        int pages=Math.max(1,count),current=safePage(index,pages)+1;
        String suffix="Pagina "+current+" van "+pages;
        return name==null||name.trim().isEmpty()?suffix:name.trim()+" · "+current+"/"+pages;
    }
    /** One row on compact/landscape windows; two on sufficiently tall ones. */
    public static int shelfRows(int height,float fontScale){
        return height<620||fontScale>=1.35f&&height<760?1:2;
    }
    public static int safePage(int index,int count){return Math.max(0,Math.min(index,Math.max(0,count-1)));}
    public static int pageFor(List<Page> pages,int coordinate){
        if(pages.isEmpty())return 0;
        for(int i=0;i<pages.size();i++)if(coordinate<pages.get(i).end)return i;
        return pages.size()-1;
    }
    public static int resolvePage(List<Page> pages,int previous,int request,int anchor,int reveal){
        if(reveal>=0)return pageFor(pages,reveal);
        if(request>=0)return safePage(request,pages.size());
        return anchor>=0?pageFor(pages,anchor):safePage(previous,pages.size());
    }
    public static final int AXIS_UNDECIDED=0,AXIS_HORIZONTAL=1,AXIS_VERTICAL=2;
    public static int lockAxis(int current,float dx,float dy,float slop){
        if(current!=AXIS_UNDECIDED)return current;
        float x=Math.abs(dx),y=Math.abs(dy);
        if(x>=slop&&x>=1.55f*y)return AXIS_HORIZONTAL;
        if(y>=slop&&y>=1.35f*x)return AXIS_VERTICAL;
        return AXIS_UNDECIDED;
    }
    public static int swipe(float dx,float dy,float threshold,boolean permitted){
        if(!permitted||Math.abs(dx)<threshold||Math.abs(dx)<1.55f*Math.abs(dy))return 0;
        return dx<0?1:-1;
    }
}
