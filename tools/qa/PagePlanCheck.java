package nl.playlumi.app;
import java.util.*;

public final class PagePlanCheck {
    private static void require(boolean good,String message){if(!good)throw new AssertionError(message);}
    public static void main(String[] args){
        Random r=new Random(1232026);int cases=0,blocks=0,swipes=0;
        for(int i=0;i<12000;i++){
            int height=100+r.nextInt(1000),limit=1+r.nextInt(6),count=1+r.nextInt(60),position=0;
            ArrayList<PlaylumiPagePlan.Block> items=new ArrayList<>();
            for(int j=0;j<count;j++){
                position+=r.nextInt(30);
                int end=position+1+r.nextInt(j%8==0?height*2:Math.max(1,height/3));
                items.add(new PlaylumiPagePlan.Block(position,end));
                position=end;
                if(j%7==0&&position>10)position-=r.nextInt(Math.min(position,30));
            }
            Collections.shuffle(items,r);
            List<PlaylumiPagePlan.Page> pages=PlaylumiPagePlan.pack(items,height,limit);
            require(!pages.isEmpty(),"Empty deck");
            int prev=-1;
            for(PlaylumiPagePlan.Page page:pages){
                require(page.start>=prev&&page.end>page.start,"Overlapping or empty pages");
                require(page.blocks<=limit,"Too many blocks");
                require(page.scale>0&&page.scale<=1&&(page.end-page.start)*page.scale<=height+.01f,"Page exceeds viewport");
                prev=page.end;
            }
            for(PlaylumiPagePlan.Block item:items){
                int owners=0;
                for(PlaylumiPagePlan.Page page:pages)if(page.start<=item.start&&page.end>=item.end)owners++;
                require(owners==1,"Control cut, lost or duplicated");
                int selected=PlaylumiPagePlan.pageFor(pages,item.start);
                require(pages.get(selected).start<=item.start&&pages.get(selected).end>=item.end,"Wrong anchor");
                blocks++;
            }
            cases++;
            float y=60+r.nextInt(1000),x=r.nextInt(30)-15;
            require(PlaylumiPagePlan.swipe(x,y,48,true)==0&&PlaylumiPagePlan.swipe(x,-y,48,true)==0,"Vertical swipe changed page");
            require(PlaylumiPagePlan.swipe(-200,20,48,true)==1&&PlaylumiPagePlan.swipe(200,20,48,true)==-1,"Horizontal swipe lost");
            require(PlaylumiPagePlan.swipe(-200,20,48,false)==0,"Game gesture intercepted");swipes+=5;
        }
        for(int w:new int[]{240,320,360,600,900,1200})for(float font:new float[]{1f,1.3f,1.5f}){
            int count=PlaylumiCityLayout.forWidth(w,font).columns;
            for(int n=1;n<=count;n++){
                PlaylumiCityLayout map=PlaylumiCityLayout.forCount(w,font,n);
                require(map.rows==1&&map.columns==n,"City page has too many rows");
                for(int index=0;index<n;index++){
                    PlaylumiCityLayout.Plot plot=map.plot(index);
                    require(plot.left>=0&&plot.right()<=map.width+.1&&plot.bottom()<=map.height+.1,"City plot cropped");
                }
            }
        }
        System.out.println("PASS: "+cases+" randomized page decks, "+blocks+" intact measured controls, "+swipes+" swipe-direction checks and compact city pages.");
    }
}
