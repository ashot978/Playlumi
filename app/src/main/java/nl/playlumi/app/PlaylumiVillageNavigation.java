package nl.playlumi.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Six building targets, one parent entry and two clear destinations. */
public final class PlaylumiVillageNavigation extends FrameLayout {
    private final View artwork;
    private final int[] places={PlaylumiVillageHit.PARENTS,0,1,2,3,4,5};
    private final Button[] targets=new Button[places.length];
    private final float[][] artBounds=new float[places.length][];
    private final int[][] targetBounds=new int[places.length][4];
    private final TextView title,instruction;
    private final int[] titleBounds=new int[4],instructionBounds=new int[4];
    private final float density;
    private int boundsWidth=-1,boundsHeight=-1;
    public PlaylumiVillageNavigation(Context context,PlaylumiVillageView art,
            PlaylumiVillageView.Listener listener,int stars,int daily){
        super(context);density=getResources().getDisplayMetrics().density;artwork=art;
        art.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);art.setFocusable(false);
        addView(art,new LayoutParams(-1,-1));
        title=label(context,"PlayLumi",25,0xffffefc5,true);
        instruction=label(context,"Kies een speelwereld",16,0xffe7dbee,false);
        addView(title,new LayoutParams(1,1));addView(instruction,new LayoutParams(1,1));
        for(int i=0;i<targets.length;i++){
            final int place=places[i];artBounds[i]=PlaylumiVillageView.navigationBounds(place);
            boolean building=PlaylumiWorldStyle.valid(place),selected=place==PlaylumiVillageHit.HOME;
            Button button=new Button(context);button.setId(View.generateViewId());button.setAllCaps(false);
            button.setText(building?"":place==PlaylumiVillageHit.PARENTS?"Ouders":selected?"Dorp":"Werelden");
            button.setTextColor(selected?0xff39215c:0xffffefc5);button.setTextSize(place==PlaylumiVillageHit.PARENTS?14:17);
            button.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));button.setMaxLines(1);
            button.setPadding(0,0,0,0);button.setMinWidth(0);button.setMinHeight(0);
            button.setMinimumWidth(0);button.setMinimumHeight(0);button.setFocusable(true);
            button.setContentDescription(description(place));
            if(android.os.Build.VERSION.SDK_INT>=26&&!building)
                button.setAutoSizeTextTypeUniformWithConfiguration(11,place==PlaylumiVillageHit.PARENTS?14:17,1,android.util.TypedValue.COMPLEX_UNIT_SP);
            GradientDrawable focus=surface(building?0x184c2a7f:selected?0xffffe5ac:0xff56377c);
            focus.setStroke(Math.max(2,Math.round(2*density)),0xffffdf85);
            StateListDrawable state=new StateListDrawable();state.addState(new int[]{android.R.attr.state_focused},focus);
            GradientDrawable idle=surface(building?Color.TRANSPARENT:selected?0xffffe5ac:0xff4b306d);
            state.addState(new int[0],idle);
            button.setBackground(new RippleDrawable(ColorStateList.valueOf(selected?0x22563282:0x30ffe3a2),state,null));
            button.setStateListAnimator(null);button.setSelected(selected);
            button.setOnClickListener(v->listener.visit(place));targets[i]=button;addView(button,new LayoutParams(1,1));
        }
    }
    private TextView label(Context context,String text,int size,int color,boolean bold){
        TextView label=new TextView(context);label.setText(text);label.setTextSize(size);label.setTextColor(color);
        label.setTypeface(Typeface.create("sans-serif-rounded",bold?Typeface.BOLD:Typeface.NORMAL));
        label.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);label.setIncludeFontPadding(false);label.setMaxLines(1);
        if(android.os.Build.VERSION.SDK_INT>=26)
            label.setAutoSizeTextTypeUniformWithConfiguration(12,size,1,android.util.TypedValue.COMPLEX_UNIT_SP);
        return label;
    }
    private GradientDrawable surface(int color){
        GradientDrawable surface=new GradientDrawable();surface.setColor(color);surface.setCornerRadius(16*density);return surface;
    }
    private String description(int place){
        if(PlaylumiWorldStyle.valid(place))return PlaylumiWorldStyle.NAMES[place]+", open speelwereld";
        if(place==PlaylumiVillageHit.HOME)return "Dorp";
        if(place==PlaylumiVillageHit.MISSIONS)return "Alle speelwerelden";
        return "Voor ouders";
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        super.onMeasure(widthSpec,heightSpec);
        int width=getMeasuredWidth(),height=getMeasuredHeight();
        artwork.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
        if(width!=boundsWidth||height!=boundsHeight)mapBounds(width,height);
        measure(title,titleBounds);measure(instruction,instructionBounds);
        for(int i=0;i<targets.length;i++)measure(targets[i],targetBounds[i]);
    }
    private void measure(View view,int[] area){
        view.measure(MeasureSpec.makeMeasureSpec(area[2],MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(area[3],MeasureSpec.EXACTLY));
    }
    private void mapBounds(int width,int height){
        boundsWidth=width;boundsHeight=height;float[] dst=PlaylumiVillageHit.destination(width,height);
        map(new float[]{28,20,654,119},titleBounds,dst,width,height,false);
        map(new float[]{28,127,913,220},instructionBounds,dst,width,height,false);
        for(int i=0;i<targets.length;i++)map(artBounds[i],targetBounds[i],dst,width,height,true);
    }
    private void map(float[] bounds,int[] area,float[] dst,int width,int height,boolean touch){
        float x1=dst[0]+bounds[0]*dst[2]/PlaylumiVillageHit.ART_W,y1=dst[1]+bounds[1]*dst[3]/PlaylumiVillageHit.ART_H;
        float x2=dst[0]+bounds[2]*dst[2]/PlaylumiVillageHit.ART_W,y2=dst[1]+bounds[3]*dst[3]/PlaylumiVillageHit.ART_H;
        float minimum=touch?48*density:1;
        float w=Math.min(width,Math.max(minimum,x2-x1)),h=Math.min(height,Math.max(minimum,y2-y1));
        area[0]=Math.round(Math.max(0,Math.min(width-w,(x1+x2-w)/2)));
        area[1]=Math.round(Math.max(0,Math.min(height-h,(y1+y2-h)/2)));
        area[2]=Math.round(w);area[3]=Math.round(h);
    }
    @Override protected void onLayout(boolean changed,int left,int top,int right,int bottom){
        artwork.layout(0,0,right-left,bottom-top);layout(title,titleBounds);layout(instruction,instructionBounds);
        for(int i=0;i<targets.length;i++)layout(targets[i],targetBounds[i]);
    }
    private void layout(View view,int[] area){view.layout(area[0],area[1],area[0]+area[2],area[1]+area[3]);}
}
