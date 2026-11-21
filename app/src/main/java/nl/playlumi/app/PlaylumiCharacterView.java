package nl.playlumi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.view.animation.LinearInterpolator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

/** Native Canvas mascot; each owned/equipped item visibly changes its appearance. */
public final class PlaylumiCharacterView extends View {
    private static final int INK=0xff4f386e;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PlaylumiWardrobe wardrobe;
    private ValueAnimator idleMotion;
    private float cycle;
    private int reaction;
    private long reactionUntil;

    /** 1 = happy dance, 2 = encouragement, 3 = wave. */
    public void react(int kind) {
        reaction=kind;
        reactionUntil=SystemClock.uptimeMillis()+1400;
        invalidate();
    }
    public void dance() { react(1); }
    public void wave() { react(3); }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT>=26 && !ValueAnimator.areAnimatorsEnabled()) return;
        idleMotion=ValueAnimator.ofFloat(0f,(float)(Math.PI*2));
        idleMotion.setDuration(2400);
        idleMotion.setRepeatCount(ValueAnimator.INFINITE);
        idleMotion.setInterpolator(new LinearInterpolator());
        idleMotion.addUpdateListener(anim -> {
            cycle=(Float)anim.getAnimatedValue();
            invalidate();
        });
        idleMotion.start();
    }
    @Override protected void onDetachedFromWindow() {
        if (idleMotion!=null) {idleMotion.cancel();idleMotion=null;}
        super.onDetachedFromWindow();
    }

    public PlaylumiCharacterView(Context context,PlaylumiWardrobe wardrobe) {
        super(context);
        this.wardrobe=wardrobe;
        setContentDescription("Jouw Playlumi-ster met zelfgekozen kleding en accessoires");
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w=getWidth(),h=getHeight();
        if(w==0||h==0)return;
        canvas.save();
        float scale=Math.min(w/300f,h/330f);
        canvas.translate((w-300f*scale)/2f,(h-330f*scale)/2f);
        canvas.scale(scale,scale);
        boolean reacting=SystemClock.uptimeMillis()<reactionUntil;
        canvas.rotate((float)Math.sin(cycle)*1.7f+(reacting && reaction==1?(float)Math.sin(cycle*3)*5f:0f),150f,164f);
        drawSparkles(canvas);
        drawShoes(canvas);
        if(wardrobe.equipped(PlaylumiWardrobe.OUTFITS)==2)drawCape(canvas);
        drawArms(canvas);
        drawStar(canvas);
        drawFace(canvas);
        drawClothes(canvas);
        drawGlasses(canvas);
        drawHat(canvas);
        drawFriend(canvas);
        canvas.restore();
    }
    private Paint color(int col) {
        p.reset();p.setAntiAlias(true);p.setColor(col);p.setStyle(Paint.Style.FILL);
        return p;
    }
    private void line(Canvas c,float x,float y,float x2,float y2,int col,float width) {
        Paint t=color(col);t.setStrokeWidth(width);t.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x,y,x2,y2,t);
    }
    private void ellipse(Canvas c,float a,float b,float d,float e,int col) {
        c.drawOval(new RectF(a,b,d,e),color(col));
    }
    private void round(Canvas c,float a,float b,float d,float e,int radius,int col) {
        c.drawRoundRect(new RectF(a,b,d,e),radius,radius,color(col));
    }
    private void polygon(Canvas c,float[] xy,int col) {
        Path path=new Path();path.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);
        path.close();c.drawPath(path,color(col));
    }
    private void emoji(Canvas c,String value,float x,float y,int size) {
        Paint paint=color(0xff5c4081);
        paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        paint.setTextSize(size);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(value,x,y,paint);
    }
    private void drawSparkles(Canvas c) {
        emoji(c,"✦",43,83,25);
        emoji(c,"✧",251,69,22);
        emoji(c,"✧",51,235,17);
        emoji(c,"✦",265,220,18);
        ellipse(c,50,110,57,117,0xffffcfea);
        ellipse(c,241,250,251,260,0xffa0e6f9);
    }
    private void drawShoes(Canvas c) {
        line(c,122,248,116,276,0xff7350a0,12);
        line(c,180,248,186,276,0xff7350a0,12);
        round(c,83,266,125,290,11,0xffe47cb8);
        round(c,177,266,218,290,11,0xff7b9ce7);
        ellipse(c,92,271,105,276,0xffffffff);
        ellipse(c,189,271,202,276,0xffffffff);
    }
    private void drawArms(Canvas c) {
        boolean excited=SystemClock.uptimeMillis()<reactionUntil && (reaction==1 || reaction==3);
        float wave=excited?(float)Math.sin(cycle*4)*23f:(float)Math.sin(cycle*2)*5f;
        float lift=excited?42f:0f;
        line(c,78,171,37,200-lift-wave,0xff8b68bd,13);
        line(c,222,171,264,194-lift+wave,0xff8b68bd,13);
        ellipse(c,25,191-lift-wave,45,211-lift-wave,0xffffddb6);
        ellipse(c,258,184-lift+wave,276,202-lift+wave,0xffffddb6);
    }
    private void drawStar(Canvas c) {
        Path star=new Path();
        double angle=-Math.PI/2;
        for(int i=0;i<10;i++) {
            float radius=(i%2==0)?111f:64f;
            float x=150+(float)Math.cos(angle+i*Math.PI/5)*radius;
            float y=164+(float)Math.sin(angle+i*Math.PI/5)*radius;
            if(i==0)star.moveTo(x,y);else star.lineTo(x,y);
        }
        star.close();
        Paint shadow=color(0xffbda3d2);shadow.setShadowLayer(10,0,8,0x448a64a9);
        c.drawPath(star,shadow);
        Paint body=color(PlaylumiWardrobe.BODY_COLORS[wardrobe.equipped(PlaylumiWardrobe.COLORS)]);
        body.setStrokeJoin(Paint.Join.ROUND);
        c.drawPath(star,body);
        Paint border=color(0xffe49a78);border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(5);border.setStrokeJoin(Paint.Join.ROUND);
        c.drawPath(star,border);
        ellipse(c,101,100,150,120,0x5affffff);
    }
    private void drawFace(Canvas c) {
        boolean blink=Math.sin(cycle)>0.993;
        if (blink) {
            line(c,106,159,120,159,INK,5);
            line(c,180,159,194,159,INK,5);
        } else {
            ellipse(c,104,148,119,172,INK);
            ellipse(c,179,148,194,172,INK);
            ellipse(c,109,151,115,158,0xffffffff);
            ellipse(c,183,151,189,158,0xffffffff);
        }
        ellipse(c,84,178,108,192,0x77f16aa3);
        ellipse(c,192,178,216,192,0x77f16aa3);
        Paint mouth=color(INK);mouth.setStyle(Paint.Style.STROKE);
        mouth.setStrokeWidth(6);mouth.setStrokeCap(Paint.Cap.ROUND);
        boolean happy=SystemClock.uptimeMillis()<reactionUntil && reaction==1;
        c.drawArc(new RectF(happy?118:125,154,happy?183:176,happy?223:211),13,152,false,mouth);
        ellipse(c,145,174,155,184,0xffed9a83);
    }
    private void drawCape(Canvas c) {
        polygon(c,new float[]{96,156,204,156,234,279,66,279},0xff8555da);
        polygon(c,new float[]{98,166,199,166,211,265,90,265},0xffbfa2fa);
    }
    private void drawClothes(Canvas c) {
        int i=wardrobe.equipped(PlaylumiWardrobe.OUTFITS);
        switch(i) {
            case 1: {
                polygon(c,new float[]{139,219,106,200,106,238,139,224},0xffea6ca2);
                polygon(c,new float[]{162,219,195,200,195,238,162,224},0xffea6ca2);
                ellipse(c,139,213,163,235,0xfffff7fb);
                break;
            }
            case 3: {
                Paint scarf=color(0xff8ed4fa);
                scarf.setStyle(Paint.Style.STROKE);scarf.setStrokeWidth(13);
                c.drawArc(new RectF(114,184,191,232),15,155,false,scarf);
                round(c,173,216,189,266,7,0xff8ed4fa);
                line(c,177,242,183,246,0xff6584bc,2);
                break;
            }
            case 4: {
                round(c,116,211,184,252,15,0xffb0d8ff);
                ellipse(c,139,220,161,243,0xff7448a8);
                emoji(c,"✦",150,240,19);
                break;
            }
            default:break;
        }
    }
    private void drawGlasses(Canvas c) {
        int i=wardrobe.equipped(PlaylumiWardrobe.GLASSES);
        if(i==0)return;
        switch(i) {
            case 1:
                round(c,91,147,129,176,10,0xff332f58);
                round(c,170,147,208,176,10,0xff332f58);
                line(c,129,155,170,155,0xff332f58,7);
                line(c,92,150,79,147,0xff332f58,5);
                line(c,208,150,222,147,0xff332f58,5);
                break;
            case 2: case 4: {
                int border=i==4?0xffee6db5:0xff7555a8;
                Paint lens=color(border);lens.setStyle(Paint.Style.STROKE);lens.setStrokeWidth(6);
                c.drawCircle(113,161,22,lens);
                c.drawCircle(188,161,22,lens);
                line(c,136,159,166,159,border,5);
                if(i==4) {
                    line(c,89,144,96,140,0xff4eb5f4,6);
                    line(c,203,144,210,140,0xff7dcf7f,6);
                }
                break;
            }
            case 3:
                emoji(c,"💗",112,179,37);
                emoji(c,"💗",189,179,37);
                line(c,136,157,165,157,0xffe076b6,5);
                break;
            default:break;
        }
    }
    private void drawHat(Canvas c) {
        int i=wardrobe.equipped(PlaylumiWardrobe.HATS);
        switch(i) {
            case 1:
                polygon(c,new float[]{101,102,95,43,124,73,148,30,175,73,205,43,199,102},0xffffc94f);
                round(c,98,95,202,110,7,0xffffa3bd);
                ellipse(c,140,80,157,97,0xff8d57c7);
                break;
            case 2:
                polygon(c,new float[]{115,111,152,34,189,111},0xffad76f0);
                ellipse(c,143,27,162,45,0xfff5d86e);
                round(c,112,104,193,114,6,0xffe89cbf);
                emoji(c,"✦",153,89,23);
                break;
            case 3:
                polygon(c,new float[]{108,111,147,17,189,111},0xff775bc5);
                ellipse(c,84,103,217,120,0xff775bc5);
                ellipse(c,143,24,155,37,0xffffd66b);
                emoji(c,"✦",150,90,24);
                break;
            case 4:
                ellipse(c,102,99,198,114,0xff71c694);
                emoji(c,"🌼",115,108,34);
                emoji(c,"🌸",149,96,33);
                emoji(c,"🌼",184,109,31);
                break;
            default:break;
        }
    }
    private void drawFriend(Canvas c) {
        int i=wardrobe.equipped(PlaylumiWardrobe.FRIENDS);
        if(i==0)return;
        String[] icons={"","🦋","🌙","🐰","☁️"};
        ellipse(c,212,227,281,300,0xcfffffff);
        emoji(c,icons[i],247,282,43);
    }
}
