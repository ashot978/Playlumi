package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;

/** Actual analog clock: hands encode the time, never print the correct answer. */
public final class PlaylumiClockFace extends View {
    private final int hour,minute;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public PlaylumiClockFace(Context c,int hour,int minute){
        super(c);
        this.hour=hour;this.minute=minute;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        int shortHand=hour%12==0?12:hour%12;
        int longHand=minute/5==0?12:minute/5;
        setContentDescription("Wijzerklok. Korte wijzer "+(minute==0?"op ":"na ")+shortHand+", lange wijzer op "+longHand);
    }
    @Override protected void onMeasure(int w,int h){
        // A normal View consumes the entire AT_MOST height inside a ScrollView.
        // Respect the explicitly requested question-card height instead.
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(),w),
                resolveSize(getSuggestedMinimumHeight(),h));
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float x=getWidth()/2f,y=getHeight()/2f,r=Math.min(getWidth(),getHeight())*.43f;
        if(r<2)return;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xffc08439);c.drawCircle(x,y,r,p);
        p.setColor(0xffffd777);c.drawCircle(x,y,r*.95f,p);
        p.setColor(0xfffff8dc);c.drawCircle(x,y,r*.87f,p);
        for(int i=0;i<60;i++){
            double a=i*Math.PI/30-Math.PI/2;
            float inner=i%5==0?.81f:.85f;
            p.setColor(i%5==0?0xff694184:0xffcdb996);p.setStrokeWidth(Math.max(1,r*(i%5==0?.016f:.007f)));
            c.drawLine(x+(float)Math.cos(a)*r*inner,y+(float)Math.sin(a)*r*inner,
                    x+(float)Math.cos(a)*r*.90f,y+(float)Math.sin(a)*r*.90f,p);
        }
        p.setColor(0xff694184);p.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        p.setTextAlign(Paint.Align.CENTER);p.setTextSize(r*.19f);
        for(int i=1;i<=12;i++){
            double a=i*Math.PI/6-Math.PI/2;
            c.drawText(""+i,x+(float)Math.cos(a)*r*.68f,y+(float)Math.sin(a)*r*.68f-(p.ascent()+p.descent())/2,p);
        }
        hand(c,x,y,r*.43f,((hour%12)+minute/60f)*30,0xff553078,r*.085f);
        hand(c,x,y,r*.69f,minute*6,0xff238b96,r*.052f);
        p.setColor(0xffdf9f3f);c.drawCircle(x,y,r*.075f,p);
    }
    private void hand(Canvas c,float x,float y,float len,float angle,int color,float width){
        double a=Math.toRadians(angle-90);
        p.setColor(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x,y,x+(float)Math.cos(a)*len,y+(float)Math.sin(a)*len,p);
    }
}
