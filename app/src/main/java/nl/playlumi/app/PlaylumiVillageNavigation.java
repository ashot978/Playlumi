package nl.playlumi.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

/** Native focusable controls aligned with the approved illustration, also usable by TalkBack. */
public final class PlaylumiVillageNavigation extends FrameLayout {
    private final View artwork;
    private final Button[] targets=new Button[18];
    private final int[] places={0,1,2,3,4,5,6,7,8,9,10,11,12,16,14,15,16,13};
    private final float[][] artBounds=new float[18][];
    private final int[][] targetBounds=new int[18][4];
    private final float density;
    private int boundsWidth=-1,boundsHeight=-1;
    public PlaylumiVillageNavigation(Context context,PlaylumiVillageView art,
            PlaylumiVillageView.Listener listener,int stars,int daily){
        super(context);density=getResources().getDisplayMetrics().density;artwork=art;
        art.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        art.setFocusable(false);
        addView(art,new LayoutParams(-1,-1));
        for(int i=0;i<targets.length;i++){
            final int place=places[i];
            // VillageHit returns a defensive copy. Keep it for this view's
            // lifetime instead of cloning all 18 hit regions every layout.
            artBounds[i]=i==16?new float[]{686,0,850,101}:PlaylumiVillageHit.bounds(place);
            Button button=new Button(context);button.setId(View.generateViewId());
            button.setText("");button.setPadding(0,0,0,0);
            button.setMinWidth(0);button.setMinHeight(0);button.setMinimumWidth(0);button.setMinimumHeight(0);
            button.setFocusable(true);button.setContentDescription(description(place,stars,daily));
            if(i==16)button.setContentDescription(art.profileLabel()+", open Lumi");
            GradientDrawable focus=new GradientDrawable();focus.setColor(0x184c2a7f);
            focus.setCornerRadius(18*density);focus.setStroke(Math.max(2,Math.round(3*density)),0xffffdf85);
            StateListDrawable surface=new StateListDrawable();
            surface.addState(new int[]{android.R.attr.state_focused},focus);
            GradientDrawable idle=new GradientDrawable();idle.setColor(Color.TRANSPARENT);idle.setCornerRadius(18*density);
            surface.addState(new int[0],idle);
            button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x30ffe3a2),surface,null));
            button.setStateListAnimator(null);
            button.setOnClickListener(v->listener.visit(place));
            targets[i]=button;addView(button,new LayoutParams(1,1));
        }
    }
    private String description(int place,int stars,int daily){
        if(PlaylumiWorldStyle.valid(place))return PlaylumiWorldStyle.NAMES[place]+", open speelwereld";
        switch(place){
            case PlaylumiVillageHit.HOME:return "Open Lumi's stad";
            case PlaylumiVillageHit.DISCOVERY:return "Dagelijkse uitdagingen, "+daily+" van 3 behaald";
            case PlaylumiVillageHit.STARS:return stars+" speelsterren, open beloningen";
            case PlaylumiVillageHit.FRIEND:case PlaylumiVillageHit.PLAYER:case PlaylumiVillageHit.PROFILE:return "Open Lumi";
            case PlaylumiVillageHit.PARENTS:return "Ouderzone";
            case PlaylumiVillageHit.MISSIONS:return "Alle speelwerelden";
            case PlaylumiVillageHit.REWARDS:return "Beloningen";
            case PlaylumiVillageHit.COLLECTION:return "Mijn schatten";
            case PlaylumiVillageHit.SETTINGS:return "Instellingen";
            default:return "Playlumi";
        }
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        super.onMeasure(widthSpec,heightSpec);
        int width=getMeasuredWidth(),height=getMeasuredHeight();
        artwork.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
        if(width!=boundsWidth||height!=boundsHeight)mapBounds(width,height);
        for(int i=0;i<targets.length;i++){
            int[] area=targetBounds[i];
            targets[i].measure(MeasureSpec.makeMeasureSpec(area[2],MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(area[3],MeasureSpec.EXACTLY));
        }
    }
    private void mapBounds(int width,int height){
        boundsWidth=width;boundsHeight=height;
        float[] dst=PlaylumiVillageHit.destination(width,height);
        for(int i=0;i<targets.length;i++){
            float[] bounds=artBounds[i];
            float x1=dst[0]+bounds[0]*dst[2]/PlaylumiVillageHit.ART_W;
            float y1=dst[1]+bounds[1]*dst[3]/PlaylumiVillageHit.ART_H;
            float x2=dst[0]+bounds[2]*dst[2]/PlaylumiVillageHit.ART_W;
            float y2=dst[1]+bounds[3]*dst[3]/PlaylumiVillageHit.ART_H;
            float w=Math.min(width,Math.max(48*density,x2-x1)),h=Math.min(height,Math.max(48*density,y2-y1));
            int x=Math.round(Math.max(0,Math.min(width-w,(x1+x2-w)/2)));
            int y=Math.round(Math.max(0,Math.min(height-h,(y1+y2-h)/2)));
            int[] area=targetBounds[i];area[0]=x;area[1]=y;area[2]=Math.round(w);area[3]=Math.round(h);
        }
    }
    @Override protected void onLayout(boolean changed,int left,int top,int right,int bottom){
        artwork.layout(0,0,right-left,bottom-top);
        for(int i=0;i<targets.length;i++){
            int[] area=targetBounds[i];
            targets[i].layout(area[0],area[1],area[0]+area[2],area[1]+area[3]);
        }
    }
}
