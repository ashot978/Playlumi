package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/** Touchable illustrated map: existing app destinations, no game state changes. */
public final class PlaylumiWorldMapView extends View {
    public interface Listener { void onDestination(int destination); }
    public static final int LEARN=0, MINI=1, FRIEND=2, HOME=3;
    private static final float[] XS={102,285,104,285};
    private static final float[] YS={106,106,277,277};
    private static final String[] NAMES={"LEERWERELDEN","SPEELPLEIN","STERVRIEND","MIJN HUIS"};
    private static final String[] SYMBOLS={"🌈","🎮","⭐","🏡"};
    private static final int[] PALETTE={0xff87ccfb,0xffffc17e,0xffbba2f0,0xff8fe7b6};
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Listener listener;
    private long tick;
    private final boolean animate;

    public PlaylumiWorldMapView(Context context, Listener listener) {
        super(context);
        this.listener=listener;
        this.animate=!PlaylumiMotion.isReduced();
        setContentDescription("Interactiekaart. Linksboven leerwerelden, rechtsboven minigames, linksonder stervriend, rechtsonder mijn huis.");
        setFocusable(true);
        setClickable(true);
    }

    private void fill(int color) {p.reset();p.setAntiAlias(true);p.setStyle(Paint.Style.FILL);p.setColor(color);}
    private void label(Canvas c,String text,float x,float y,int size,int color) {
        fill(color);p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD));
        p.setTextSize(size); c.drawText(text,x,y,p);
    }
    private void circle(Canvas c,float x,float y,float r,int color) {
        fill(color);c.drawCircle(x,y,r,p);
    }
    private void path(Canvas c) {
        fill(0xfffff8cd);p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeWidth(11);
        p.setPathEffect(new android.graphics.DashPathEffect(new float[]{3,17},0));
        Path trail=new Path();
        trail.moveTo(102,106);trail.cubicTo(154,147,230,69,285,106);
        trail.cubicTo(345,143,230,228,285,277);
        trail.cubicTo(224,317,146,239,104,277);
        c.drawPath(trail,p);p.setPathEffect(null);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getWidth()<=0||getHeight()<=0)return;
        canvas.save();
        canvas.scale(getWidth()/390f,getHeight()/365f);
        fill(0xff2b5c91);
        canvas.drawRoundRect(new RectF(3,5,387,360),34,34,p);
        fill(0xffbdeef6);
        p.setShader(new LinearGradient(0,0,0,365,new int[]{0xff87cafa,0xffc5f7ef,0xffe1f6c8},
                null,Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(6,5,384,354),32,32,p);p.setShader(null);
        fill(0x70ffffff);
        canvas.drawOval(2,11,174,63,p);
        canvas.drawOval(243,16,385,70,p);
        fill(0xff7ecbaa);
        canvas.drawOval(-20,285,183,404,p);
        canvas.drawOval(158,285,430,422,p);
        label(canvas,"KIES JOUW AVONTUUR",195,34,21,0xff234e73);
        path(canvas);
        for(int i=0;i<4;i++) {
            float wave=animate?(float)Math.sin(tick*.024+i*1.7)*3f:0;
            float x=XS[i],y=YS[i]+wave;
            circle(canvas,x,y+9,67,0x44406b6e);
            circle(canvas,x,y,67,0xfffcfaf0);
            circle(canvas,x,y,61,0xff295a80);
            circle(canvas,x,y-5,58,PALETTE[i]);
            circle(canvas,x-18,y-25,15,0x77ffffff);
            label(canvas,SYMBOLS[i],x,y+12,43,0xff172e53);
            fill(0xfffffff3);canvas.drawRoundRect(new RectF(x-68,y+43,x+68,y+65),12,12,p);
            label(canvas,NAMES[i],x,y+59,14,0xff244b6c);
        }
        canvas.restore();
        if(animate && isAttachedToWindow() && getVisibility()==VISIBLE) {
            tick++;postInvalidateDelayed(80);
        }
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN)return true;
        if(event.getActionMasked()==MotionEvent.ACTION_UP) {
            float x=event.getX()*390/Math.max(1,getWidth());
            float y=event.getY()*365/Math.max(1,getHeight());
            int chosen=-1;
            for(int i=0;i<4;i++) {
                float dx=x-XS[i],dy=y-YS[i];
                if(dx*dx+dy*dy<70*70){chosen=i;break;}
            }
            if(chosen>=0 && listener!=null) {
                performClick();
                listener.onDestination(chosen);
                return true;
            }
        }
        return true;
    }
    @Override public boolean performClick() {super.performClick();return true;}
}
