package nl.playlumi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.LinearInterpolator;

/** An animated, native garden scene for the selected plant. */
public final class PlaylumiGardenView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PlaylumiGarden garden;
    private final int plant;
    private ValueAnimator animator;
    private float phase;

    public PlaylumiGardenView(Context context,PlaylumiGarden garden,int plant) {
        super(context);
        this.garden=garden;this.plant=plant;
        setContentDescription("Magische tuin: "+PlaylumiGarden.NAMES[plant]+
                ", groeifase "+garden.stage(plant)+" van vier");
    }
    private void paint(int color) {
        p.reset();p.setAntiAlias(true);p.setColor(color);p.setStyle(Paint.Style.FILL);
    }
    private void oval(Canvas canvas,float a,float b,float c,float d,int color) {
        paint(color);canvas.drawOval(new RectF(a,b,c,d),p);
    }
    private void rectangle(Canvas c,float a,float b,float d,float e,float radius,int color) {
        paint(color);c.drawRoundRect(new RectF(a,b,d,e),radius,radius,p);
    }
    private void emoji(Canvas c,String text,float x,float y,int size) {
        paint(Color.WHITE);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        p.setTextSize(size);
        c.drawText(text,x,y,p);
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if(getWidth()<1 || getHeight()<1)return;
        c.save();
        c.scale(getWidth()/340f,getHeight()/256f);
        rectangle(c,0,0,340,256,26,0xffdef5ff);
        rectangle(c,0,185,340,256,20,0xffc6e5ac);
        rectangle(c,0,215,340,256,21,0xff9fba7b);
        oval(c,258,26,310,78,0xffffda6c);
        oval(c,265,34,303,70,0xffffedb5);
        float floatX=PlaylumiMotion.isReduced()?0:(float)Math.sin(phase)*6f;
        oval(c,23+floatX,30,102+floatX,61,0xffffffff);
        oval(c,40+floatX,19,86+floatX,58,0xffffffff);
        oval(c,188-floatX,55,250-floatX,80,0xddffffff);
        oval(c,200-floatX,46,241-floatX,75,0xeeffffff);
        oval(c,99,222,240,249,0x66837060);
        int stage=garden.stage(plant);
        if(stage==0){
            oval(c,145,206,178,222,0xffb77a45);
            oval(c,152,208,165,213,0xffe6b17b);
        } else {
            paint(0xff3f9871);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(stage>=3?12:8);
            c.drawLine(166,216,166,stage==1?172:stage==2?137:102,p);
            Path left=new Path();
            left.moveTo(165,189);
            left.quadTo(109,158,110,198);
            left.quadTo(135,211,165,189);
            c.drawPath(left,p);
            Path right=new Path();
            right.moveTo(165,179);
            right.quadTo(225,144,222,184);
            right.quadTo(198,201,165,179);
            c.drawPath(right,p);
            if(stage==1)emoji(c,"🌱",167,203,70);
            if(stage==2)emoji(c,"🌿",167,169,93);
            if(stage==3) {
                emoji(c,"🌾",167,149,100);
                emoji(c,"✨",216,112,24);
            }
            if(stage==4){
                float sway=PlaylumiMotion.isReduced()?0:(float)Math.sin(phase)*2.5f;
                c.save();
                c.rotate(sway,166,143);
                emoji(c,PlaylumiGarden.ICONS[plant],168,166,110);
                c.restore();
                emoji(c,"🦋",245+floatX,111,30);
                emoji(c,"✨",90-floatX,107,28);
            }
        }
        c.restore();
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(PlaylumiMotion.isReduced())return;
        animator=ValueAnimator.ofFloat(0f,(float)(Math.PI*2));
        animator.setDuration(2600);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(v->{phase=(Float)v.getAnimatedValue();invalidate();});
        animator.start();
    }
    @Override protected void onDetachedFromWindow() {
        if(animator!=null){animator.cancel();animator=null;}
        super.onDetachedFromWindow();
    }
}
