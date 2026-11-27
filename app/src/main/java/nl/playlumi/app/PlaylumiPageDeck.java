package nl.playlumi.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed viewport with a pinned header and discrete horizontal pages.
 * The original view instances and their callbacks are retained: a page change
 * never regenerates questions, recreates an editor or changes stored progress.
 * There is no ScrollView, scroll offset, fling or vertical scroll action.
 */
public final class PlaylumiPageDeck extends ViewGroup {
    private final LinearLayout content;
    private final LinearLayout controls;
    private final Button previous,next;
    private final TextView counter;
    private final boolean allowSwipe,game;
    private final IdentityHashMap<View,Integer> hidden=new IdentityHashMap<>();
    private List<PlaylumiPagePlan.Page> pages=java.util.Collections.emptyList();
    private View header,pendingReveal,pinned,pageAnchor;
    private final IdentityHashMap<View,Integer> blockStarts=new IdentityHashMap<>();
    private int pinnedExtent,pinnedWidth,pinnedHeight,bodyLeft;
    private boolean pinnedSide,blankGesture,centerContent,singlePage;
    private int contentTop;
    private final java.util.Set<View> atomic=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<View,Boolean>());
    private final java.util.Set<View> split=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<View,Boolean>());
    private final java.util.WeakHashMap<View,String> pageNames=new java.util.WeakHashMap<>();
    private int maxBlocks;
    private int headerHeight,selected,requested=-1,bodyTop,bodyHeight,contentWidth,naturalHeight;
    private int contentBottom,contentLeft,contentRight;
    private boolean laidOut,bodyGesture;
    private int gestureAxis=PlaylumiPagePlan.AXIS_UNDECIDED;
    private float downX,downY;
    public PlaylumiPageDeck(Context context,boolean allowSwipe,boolean game){
        super(context);this.allowSwipe=allowSwipe;this.game=game;
        maxBlocks=game?Integer.MAX_VALUE:4;
        setClipChildren(true);setClipToPadding(true);
        content=new LinearLayout(context){
            @Override public boolean dispatchTouchEvent(MotionEvent event){
                // clipBounds limits drawing, but Android hit testing still sees
                // the full natural layout behind the pinned header and footer.
                // Only a visible page may begin a new child interaction.
                if(event.getActionMasked()==MotionEvent.ACTION_DOWN&&
                        !pages.isEmpty()&&!PlaylumiPagePlan.containsPoint(
                                pages.get(selected),getWidth(),event.getX(),event.getY()))return false;
                return super.dispatchTouchEvent(event);
            }
        };content.setOrientation(LinearLayout.VERTICAL);
        content.setClipChildren(true);content.setClipToPadding(true);addView(content,new LayoutParams(-1,-2));
        controls=new LinearLayout(context);controls.setGravity(Gravity.CENTER_VERTICAL);controls.setPadding(dp(4),0,dp(4),0);
        GradientDrawable bg=new GradientDrawable();bg.setColor(0xe639215c);bg.setCornerRadius(dp(14));controls.setBackground(bg);
        previous=arrow("‹","Vorige inhoudspagina");next=arrow("›","Volgende inhoudspagina");
        counter=new TextView(context);counter.setGravity(Gravity.CENTER);counter.setTextColor(0xfffff1cf);
        counter.setTextSize(13);counter.setMaxLines(2);counter.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);counter.setIncludeFontPadding(false);
        counter.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        controls.addView(previous,new LinearLayout.LayoutParams(dp(54),-1));
        controls.addView(counter,new LinearLayout.LayoutParams(0,-1,1f));
        controls.addView(next,new LinearLayout.LayoutParams(dp(54),-1));
        addView(controls,new LayoutParams(-1,dp(48)));
        previous.setOnClickListener(v->turn(-1));next.setOnClickListener(v->turn(1));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private Button arrow(String glyph,String description){
        Button b=new Button(getContext());b.setAllCaps(false);b.setText(glyph);b.setTextSize(26);b.setTextColor(0xffffefc5);
        b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(0,0,0,0);
        GradientDrawable surface=new GradientDrawable();surface.setColor(0x00000000);surface.setCornerRadius(dp(12));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x26ffe3a2),surface,null));b.setStateListAnimator(null);b.setContentDescription(description);
        return b;
    }
    public void keepTogether(View view){if(view!=null)atomic.add(view);requestLayout();}
    public void splitChildren(View view){if(view!=null)split.add(view);requestLayout();}
    public void namePage(View view,String name){if(view!=null&&name!=null)pageNames.put(view,name);requestLayout();}
    public void setCentered(boolean value){centerContent=value;requestLayout();}
    public void setSinglePage(boolean value){singlePage=value;requestLayout();}
    public void setMaxBlocks(int limit){maxBlocks=Math.max(1,limit);requestLayout();}
    public LinearLayout content(){return content;}
    public int currentPage(){return selected;}
    public int pageCount(){return Math.max(1,pages.size());}
    public void requestPage(int page){requested=Math.max(0,page);if(laidOut)requestLayout();}
    public void setHeader(View view,int height){
        if(header!=null)removeView(header);
        header=view;headerHeight=Math.max(0,height);
        if(view!=null)addView(view,new LayoutParams(-1,headerHeight));
        requestLayout();
    }
    public void setPinnedView(View view,int extent,boolean side){
        if(pinned!=null)removeView(pinned);
        pinned=view;pinnedExtent=Math.max(1,extent);pinnedSide=side;
        if(view!=null)addView(view,new LayoutParams(-1,-2));
        requestLayout();
    }
    public void reveal(View view){
        if(view==null)return;
        pendingReveal=view;requestLayout();
    }
    public boolean isVerticalScrollEnabled(){return false;}
    @Override public boolean canScrollVertically(int direction){return false;}
    @Override public boolean canScrollHorizontally(int direction){return direction<0?selected>0:selected+1<pages.size();}
    @Override public boolean requestChildRectangleOnScreen(View child,Rect rectangle,boolean immediate){return false;}
    private int footerHeight(){return singlePage?0:dp(48);}
    @Override protected void onMeasure(int ws,int hs){
        int width=MeasureSpec.getSize(ws),height=MeasureSpec.getSize(hs);
        int outerW=Math.max(1,width-getPaddingLeft()-getPaddingRight());
        int outerH=Math.max(1,height-getPaddingTop()-getPaddingBottom());
        int head=header==null?0:Math.min(headerHeight,Math.max(0,outerH-footerHeight()-1));
        int footer=Math.min(footerHeight(),Math.max(0,outerH-head-1));
        bodyHeight=Math.max(1,outerH-head-footer-dp(4));
        contentWidth=outerW;pinnedWidth=0;pinnedHeight=0;
        if(pinned!=null){
            if(pinnedSide){
                pinnedWidth=Math.min(pinnedExtent,Math.max(1,outerW*52/100));pinnedHeight=bodyHeight;
                contentWidth=Math.max(1,outerW-pinnedWidth-dp(6));
            }else{
                pinnedWidth=outerW;pinnedHeight=Math.min(pinnedExtent,Math.max(1,bodyHeight*60/100));
                bodyHeight=Math.max(1,bodyHeight-pinnedHeight-dp(5));
            }
            pinned.measure(MeasureSpec.makeMeasureSpec(pinnedWidth,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(pinnedHeight,MeasureSpec.EXACTLY));
        }
        if(header!=null)header.measure(MeasureSpec.makeMeasureSpec(outerW,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(head,MeasureSpec.EXACTLY));
        controls.measure(MeasureSpec.makeMeasureSpec(outerW,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(footer,MeasureSpec.EXACTLY));
        // Natural layout is measured once, then packed into fixed pages.
        content.measure(MeasureSpec.makeMeasureSpec(contentWidth,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));
        naturalHeight=content.getMeasuredHeight();
        setMeasuredDimension(resolveSize(width,ws),resolveSize(height,hs));
    }
    private void restoreHidden(){
        for(Map.Entry<View,Integer> entry:hidden.entrySet())entry.getKey().setImportantForAccessibility(entry.getValue());
        hidden.clear();
    }
    private void blocks(View view,int y,List<PlaylumiPagePlan.Block> output){
        if(view.getVisibility()!=VISIBLE||view.getHeight()<=0)return;
        int bottom=y+view.getHeight();
        // Keep cards, answer rows and custom canvases intact. Only oversized
        // structural containers may be divided between their real child views.
        if(view instanceof LinearLayout&&!view.isClickable()&&!atomic.contains(view)&&(split.contains(view)||view.getHeight()>bodyHeight)){
            LinearLayout container=(LinearLayout)view;int before=output.size();
            for(int i=0;i<container.getChildCount();i++){
                View child=container.getChildAt(i);
                blocks(child,y+child.getTop(),output);
            }
            if(output.size()>before)return;
        }
        int start=Math.max(0,y);output.add(new PlaylumiPagePlan.Block(start,Math.max(start+1,bottom)));blockStarts.put(view,start);
    }
    private int topOf(View view){
        if(view==content)return 0;
        int y=0;View child=view;
        while(child!=content&&child.getParent() instanceof View){
            if(child.getVisibility()!=VISIBLE||child.getHeight()<=0)return -1;
            y+=child.getTop();child=(View)child.getParent();
        }
        return child==content?y:-1;
    }
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
        restoreHidden();
        int left=getPaddingLeft(),top=getPaddingTop(),right=getWidth()-getPaddingRight(),bottom=getHeight()-getPaddingBottom();
        int head=header==null?0:header.getMeasuredHeight();
        if(header!=null)header.layout(left,top,right,top+head);
        bodyTop=top+head+dp(2);bodyLeft=left;
        if(pinned!=null){
            pinned.layout(left,bodyTop,left+pinnedWidth,bodyTop+pinnedHeight);
            if(pinnedSide)bodyLeft+=pinnedWidth+dp(6);
            else bodyTop+=pinnedHeight+dp(5);
        }
        controls.layout(left,bottom-controls.getMeasuredHeight(),right,bottom);
        // Layout the natural content in neutral coordinates before inspecting bounds.
        content.setScaleX(1f);content.setScaleY(1f);content.setClipBounds(null);
        content.layout(0,0,contentWidth,naturalHeight);
        ArrayList<PlaylumiPagePlan.Block> measured=new ArrayList<>();blockStarts.clear();
        for(int i=0;i<content.getChildCount();i++){
            View child=content.getChildAt(i);
            blocks(child,child.getTop(),measured);
        }
        if(singlePage&&!measured.isEmpty()){
            int start=Integer.MAX_VALUE,end=0;
            for(PlaylumiPagePlan.Block block:measured){start=Math.min(start,block.start);end=Math.max(end,block.end);}
            measured.clear();measured.add(new PlaylumiPagePlan.Block(start,end));
        }
        pages=PlaylumiPagePlan.pack(measured,bodyHeight,maxBlocks);
        int anchor=pageAnchor==null?-1:topOf(pageAnchor),reveal=-1;
        if(pendingReveal!=null){
            int y=topOf(pendingReveal);
            if(y>=0)reveal=y+Math.min(pendingReveal.getHeight()/2,dp(28));
            pendingReveal=null;
        }
        selected=PlaylumiPagePlan.resolvePage(pages,selected,requested,anchor,reveal);
        requested=-1;laidOut=true;
        displayPage();
    }
    private void hideOffPage(View view,int y,int start,int end){
        if(view.getVisibility()!=VISIBLE)return;
        int bottom=y+view.getHeight();
        if(bottom<=start||y>=end){
            hidden.put(view,view.getImportantForAccessibility());
            view.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }else if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                View child=group.getChildAt(i);hideOffPage(child,y+child.getTop(),start,end);
            }
        }
    }
    private void displayPage(){
        if(pages.isEmpty())return;
        restoreHidden();
        PlaylumiPagePlan.Page page=pages.get(selected);
        pageAnchor=null;int first=Integer.MAX_VALUE;
        for(Map.Entry<View,Integer> block:blockStarts.entrySet()){
            int start=block.getValue();
            if(start>=page.start&&start<page.end&&start<first){pageAnchor=block.getKey();first=start;}
        }
        float scale=page.scale;
        int x=bodyLeft+Math.round((contentWidth-contentWidth*scale)/2f);
        int displayedHeight=Math.round((page.end-page.start)*scale);
        contentTop=bodyTop+(centerContent?Math.max(0,(bodyHeight-displayedHeight)/2):0);
        int y=contentTop-Math.round(page.start*scale);
        content.setPivotX(0);content.setPivotY(0);content.setScaleX(scale);content.setScaleY(scale);
        content.offsetLeftAndRight(x-content.getLeft());content.offsetTopAndBottom(y-content.getTop());
        content.setClipBounds(new Rect(0,page.start,contentWidth,page.end));
        contentLeft=x;contentRight=x+Math.round(contentWidth*scale);
        contentBottom=contentTop+displayedHeight;
        for(int i=0;i<content.getChildCount();i++){
            View child=content.getChildAt(i);hideOffPage(child,child.getTop(),page.start,page.end);
        }
        String name="";int best=Integer.MAX_VALUE;
        for(Map.Entry<View,String> entry:pageNames.entrySet()){
            View anchor=entry.getKey();if(anchor==null||anchor.getVisibility()!=VISIBLE)continue;
            int anchorY=topOf(anchor);if(anchorY>=page.start&&anchorY<page.end&&anchorY<best){best=anchorY;name=entry.getValue();}
        }
        counter.setText(PlaylumiPagePlan.caption(name,selected,pages.size()));
        counter.setContentDescription("Inhoudspagina "+(selected+1)+" van "+pages.size());
        previous.setEnabled(selected>0);next.setEnabled(selected+1<pages.size());
        previous.setAlpha(previous.isEnabled()?1f:.28f);next.setAlpha(next.isEnabled()?1f:.28f);
        controls.setVisibility(pages.size()>1?VISIBLE:INVISIBLE);
        // Debug acceptance can independently prove every measured block fits.
        if((getContext().getApplicationInfo().flags&android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0)
            android.util.Log.i("PlaylumiQA","deckPage="+(selected+1)+" deckTotal="+pages.size()+" deckScale="+scale+" deckBody="+bodyHeight);
        invalidate();
    }
    private void turn(int direction){
        int target=PlaylumiPagePlan.safePage(selected+direction,pages.size());
        if(target==selected)return;
        selected=target;displayPage();
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }
    private void updateGestureAxis(MotionEvent event){
        gestureAxis=PlaylumiPagePlan.lockAxis(gestureAxis,event.getX()-downX,event.getY()-downY,dp(14));
    }
    private int gestureDirection(MotionEvent event){
        if(!bodyGesture||gestureAxis==PlaylumiPagePlan.AXIS_VERTICAL)return 0;
        return PlaylumiPagePlan.swipe(event.getX()-downX,event.getY()-downY,dp(48),allowSwipe&&pages.size()>1);
    }
    @Override public boolean onInterceptTouchEvent(MotionEvent event){
        return event.getActionMasked()==MotionEvent.ACTION_MOVE&&gestureDirection(event)!=0;
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        if(event.getActionMasked()==MotionEvent.ACTION_UP){
            updateGestureAxis(event);int direction=gestureDirection(event);if(direction!=0)turn(direction);
        }
        return true;
    }
    @Override public boolean dispatchTouchEvent(MotionEvent event){
        final int action=event.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){
            downX=event.getX();downY=event.getY();gestureAxis=PlaylumiPagePlan.AXIS_UNDECIDED;
            bodyGesture=downX>=bodyLeft&&downX<getWidth()-getPaddingRight()&&downY>=bodyTop&&downY<controls.getTop();
            blankGesture=bodyGesture&&(downY<contentTop||downY>=contentBottom||downX<contentLeft||downX>contentRight);
            if(blankGesture)return true;
        }else if(action==MotionEvent.ACTION_MOVE)updateGestureAxis(event);
        if(blankGesture){
            if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){blankGesture=false;boolean handled=onTouchEvent(event);bodyGesture=false;return handled;}
            return true;
        }
        boolean handled=super.dispatchTouchEvent(event);
        if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL)bodyGesture=false;
        return handled;
    }
    @Override protected boolean drawChild(Canvas canvas,View child,long drawingTime){
        if(child!=content)return super.drawChild(canvas,child,drawingTime);
        int save=canvas.save();
        canvas.clipRect(bodyLeft,contentTop,getWidth()-getPaddingRight(),Math.min(contentBottom,controls.getTop()));
        boolean result=super.drawChild(canvas,child,drawingTime);canvas.restoreToCount(save);return result;
    }
}
