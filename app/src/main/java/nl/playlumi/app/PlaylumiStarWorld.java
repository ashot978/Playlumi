package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/**
 * Interactive painted game hub. No web views, extra permissions or external assets.
 * All hit targets remain large and launch existing Playlumi activities.
 */
public final class PlaylumiStarWorld extends View {
    public interface Listener { void onVisit(int destination); }
    public static final int ADVENTURE=0, EXPLORE=1, MINI_GAMES=2,
            MUSIC=3, GARDEN=4, FRIEND=5;
    private static final int[] PALETTE={
            0xffedbaff,0xffa8eaf8,0xffffd88c,0xffffabd2,0xffb0edb8,0xfffae9a4
    };
    private static final String[] SYMBOLS={"🚀","🌈","🎪","🎵","🌱","⭐"};
    private static final String[] LABELS={
            "REKENRAKET","ONTDEK","SPEELPLEIN","MUZIEKMAAN","STERRENTUIN","STERVRIEND"
    };
    private static final String[] SPEECH={
            "Vlieg op avontuur","Ontdek de werelden","Speel met je handen",
            "Maak muziek","Laat je tuin groeien","Versier je stervriend"
    };
    private static final float[] CX={88,272,88,272,88,272};
    private static final float[] CY={186,186,343,343,502,502};
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Listener listener;
    private final long started=SystemClock.uptimeMillis();
    private final float density;
    private int active=-1;
    private boolean animating;

    public PlaylumiStarWorld(Context context, Listener listener) {
        super(context);
        this.listener=listener;
        density=getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        setFocusable(true);
        setContentDescription("Sterrenwereld. Tik op een van de zes speelplekken: "
                +"Rekenraket, Ontdek, Speelplein, Muziekmaan, Sterrentuin of Stervriend.");
        setMinimumHeight((int)(610*density));
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec) {
        int width=MeasureSpec.getSize(widthSpec);
        int wanted=(int)(width*610f/360f);
        wanted=Math.max((int)(480*density),Math.min(wanted,(int)(710*density)));
        setMeasuredDimension(width,resolveSize(wanted,heightSpec));
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();animating=true;postInvalidateOnAnimation();
    }
    @Override protected void onDetachedFromWindow() {
        animating=false;super.onDetachedFromWindow();
    }
    private void fill(int color) {p.reset();p.setAntiAlias(true);p.setColor(color);}
    private void circle(Canvas c,float x,float y,float radius,int color) {
        fill(color);c.drawCircle(x,y,radius,p);
    }
    private void label(Canvas c,String text,float x,float y,float size,int color,boolean bold) {
        fill(color);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);
        p.setTypeface(android.graphics.Typeface.create(
                bold?"sans-serif-rounded":"sans-serif",bold?1:0));
        c.drawText(text,x,y,p);
    }
    private void rounded(Canvas c,RectF r,float radius,int color) {
        fill(color);c.drawRoundRect(r,radius,radius,p);
    }
    private float shimmer(float offset) {
        return (float)Math.sin((SystemClock.uptimeMillis()-started)/530.0+offset);
    }
    private void star(Canvas c,float x,float y,float r,int color) {
        Path path=new Path();
        for(int i=0;i<10;i++) {
            double a=Math.PI*i/5.0-Math.PI/2;
            float d=i%2==0?r:r*.45f;
            float sx=x+(float)Math.cos(a)*d,sy=y+(float)Math.sin(a)*d;
            if(i==0)path.moveTo(sx,sy);else path.lineTo(sx,sy);
        }
        path.close();fill(color);c.drawPath(path,p);
    }
    private void island(Canvas c,int index) {
        float x=CX[index],y=CY[index];
        float jump=2.3f*shimmer(index*1.3f);
        // A soft floating island with an illuminated portal, not a rectangular card.
        fill(0x44031234);c.drawOval(new RectF(x-73,y+66,x+73,y+83),p);
        fill(0xff6556a5);c.drawOval(new RectF(x-72,y+45,x+72,y+70),p);
        fill(0xffa69bd5);c.drawOval(new RectF(x-74,y+33,x+74,y+59),p);
        fill(PALETTE[index]);c.drawCircle(x,y+jump,58,p);
        fill(0x66ffffff);c.drawCircle(x-19,y-18+jump,23,p);
        circle(c,x,y+jump,49,0x35ffffff);
        label(c,SYMBOLS[index],x,y+21+jump,57,Color.WHITE,true);
        star(c,x+51,y-39+jump,8,0xfffff0a2);
        rounded(c,new RectF(x-70,y+62,x+70,y+99),18,0xff302957);
        label(c,LABELS[index],x,y+86,15,Color.WHITE,true);
        label(c,SPEECH[index],x,y+113,11,0xffe9e3ff,false);
        if(active==index) {
            fill(0xfffff1af);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);
            c.drawCircle(x,y+jump,62,p);p.setStyle(Paint.Style.FILL);
        }
    }
    @Override protected void onDraw(Canvas real) {
        super.onDraw(real);
        final float sx=getWidth()/360f,sy=getHeight()/610f;
        real.save();real.scale(sx,sy);
        fill(Color.WHITE);
        p.setShader(new LinearGradient(0,0,0,610,
                new int[]{0xff19174b,0xff463a83,0xff8164b0},null,Shader.TileMode.CLAMP));
        real.drawRect(0,0,360,610,p);p.setShader(null);
        // Nebulae and a glowing moon make the whole screen a navigable game world.
        for(int i=0;i<3;i++) {
            float x=i==0?12:i==1?346:165;
            float y=i==0?180:i==1?375:590;
            fill(0x334cd6f6);
            p.setShader(new RadialGradient(x,y,130,0x6651c7e4,0x0040a4e0,Shader.TileMode.CLAMP));
            real.drawCircle(x,y,130,p);p.setShader(null);
        }
        for(int i=0;i<45;i++) {
            float x=(i*101+17)%351+4;
            float y=(i*173+13)%596+4;
            float twinkle=.45f+.55f*Math.abs(shimmer(i*.85f));
            circle(real,x,y,(i%4==0?2.3f:1.2f)*twinkle,0xfff9ebd4);
        }
        circle(real,180,34,31,0x44ffffff);
        star(real,180,35,23,0xffffe788);
        label(real,"DE STERRENWERELD",180,91,23,0xfffff0b7,true);
        label(real,"Tik op een magische plek!",180,118,14,0xffe8defb,false);
        // The dotted orbit connects destinations, suggesting a shared world.
        fill(0x77f8eecb);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);
        p.setPathEffect(new android.graphics.DashPathEffect(new float[]{5,13},0));
        Path route=new Path();route.moveTo(88,186);
        route.quadTo(180,130,272,186);
        route.quadTo(328,260,272,343);
        route.quadTo(180,408,88,343);
        route.quadTo(34,424,88,502);
        route.quadTo(180,548,272,502);
        real.drawPath(route,p);p.setPathEffect(null);p.setStyle(Paint.Style.FILL);
        for(int i=0;i<6;i++)island(real,i);
        real.restore();
        if(animating && getWindowVisibility()==VISIBLE)postInvalidateDelayed(45);
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        float x=event.getX()*360f/Math.max(1,getWidth());
        float y=event.getY()*610f/Math.max(1,getHeight());
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
            active=find(x,y);invalidate();return true;
        }
        if(event.getActionMasked()==MotionEvent.ACTION_CANCEL) {
            active=-1;invalidate();return true;
        }
        if(event.getActionMasked()==MotionEvent.ACTION_UP) {
            int chosen=find(x,y);
            int previous=active;active=-1;invalidate();
            if(chosen>=0 && chosen==previous) {
                performClick();listener.onVisit(chosen);
            }
            return true;
        }
        return true;
    }
    private int find(float x,float y) {
        for(int i=0;i<6;i++) {
            float dx=x-CX[i],dy=y-CY[i]-10;
            if(Math.abs(dx)<=73 && Math.abs(dy)<=83)return i;
        }
        return -1;
    }
    @Override public boolean performClick() {
        super.performClick();return true;
    }
}
