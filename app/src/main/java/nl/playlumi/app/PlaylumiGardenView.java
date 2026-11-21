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
    private ValueAnimator growthAnimator;
    private int previousStage=-1;
    private int growthAction=PlaylumiGarden.WATER;
    private long growthElapsed=PlaylumiGardenMotion.DURATION_MILLIS;

    /**
     * Called after the garden's stage has been saved. UI-only: progress,
     * stars and the chosen action have already been decided by the model.
     */
    public void animateGrowth(int previous,int action) {
        if(PlaylumiMotion.isReduced() || previous<0 || previous>=4)return;
        if(growthAnimator!=null) {
            growthAnimator.cancel();
            growthAnimator=null;
        }
        previousStage=previous;
        growthAction=action;
        growthElapsed=0;
        growthAnimator=ValueAnimator.ofInt(0,PlaylumiGardenMotion.DURATION_MILLIS);
        growthAnimator.setDuration(PlaylumiGardenMotion.DURATION_MILLIS);
        growthAnimator.setInterpolator(new LinearInterpolator());
        growthAnimator.addUpdateListener(a->{
            growthElapsed=(Integer)a.getAnimatedValue();
            invalidate();
        });
        growthAnimator.start();
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if(getWidth()<1 || getHeight()<1)return;
        c.save();
        c.scale(getWidth()/340f,getHeight()/256f);

        // Clear, layered garden sky and ground. These are drawn without images
        // or external resources, and do not affect the user's saved plant.
        rectangle(c,0,0,340,256,26,0xffdff5ff);
        rectangle(c,0,183,340,256,20,0xffd5efbd);
        rectangle(c,0,217,340,256,20,0xffa8c58a);
        oval(c,258,24,310,76,0xffffd366);
        oval(c,264,31,304,69,0xffffedb3);
        float drift=PlaylumiMotion.isReduced()?0:(float)Math.sin(phase)*5f;
        oval(c,20+drift,33,106+drift,65,0xeeffffff);
        oval(c,41+drift,20,84+drift,57,0xffffffff);
        oval(c,191-drift,55,249-drift,82,0xeaffffff);
        oval(c,203-drift,45,239-drift,77,0xfaffffff);
        // Small flowers make the garden less empty without cluttering the plant.
        oval(c,24,212,34,224,0xffffe8a1);
        oval(c,41,224,56,238,0xfff3b9db);
        oval(c,294,206,310,222,0xffe9c0ff);
        oval(c,318,227,330,237,0xffffec9a);
        oval(c,101,223,243,246,0x5d668755);

        int stage=garden.stage(plant);
        PlaylumiGardenMotion.Frame frame=PlaylumiGardenMotion.sample(
                growthElapsed,PlaylumiMotion.isReduced());
        if(previousStage>=0 && !frame.finished) {
            drawPlantLayer(c,previousStage,frame.oldAlpha,1f,drift);
            drawPlantLayer(c,stage,frame.newAlpha,(float)frame.newScale,drift);
            if(frame.waterOrSunshine>0)
                drawCareGlow(c,growthAction,(float)frame.waterOrSunshine);
            if(frame.sparkle>0)
                drawSparkles(c,(float)frame.sparkle);
        } else {
            drawPlantLayer(c,stage,1,1f,drift);
        }
        c.restore();
    }

    private void drawPlantLayer(Canvas c,int stage,double opacity,float scale,float drift) {
        if(opacity<=0)return;
        c.save();
        c.saveLayerAlpha(0,70,340,250,(int)Math.round(255*opacity));
        c.scale(scale,scale,166,220);
        if(stage==0) {
            oval(c,145,205,179,223,0xffb87846);
            oval(c,152,207,166,213,0xffeab984);
        } else {
            paint(0xff398a69);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(stage>=3?12:8);
            c.drawLine(166,218,166,stage==1?173:stage==2?139:107,p);
            p.setStyle(Paint.Style.FILL);
            Path left=new Path();
            left.moveTo(166,192);left.quadTo(108,153,110,198);
            left.quadTo(135,211,166,192);c.drawPath(left,p);
            Path right=new Path();
            right.moveTo(166,180);right.quadTo(224,144,221,183);
            right.quadTo(195,199,166,180);c.drawPath(right,p);
            if(stage==1)emoji(c,"🌱",166,204,71);
            if(stage==2)emoji(c,"🌿",166,172,92);
            if(stage==3) {
                emoji(c,"🌾",166,152,101);
                emoji(c,"✨",215,112,24);
            }
            if(stage==4) {
                float sway=PlaylumiMotion.isReduced()?0:
                        (float)Math.sin(phase)*2.4f;
                c.save();c.rotate(sway,166,143);
                emoji(c,PlaylumiGarden.ICONS[plant],167,168,110);
                c.restore();
                emoji(c,"🦋",243+drift,111,29);
                emoji(c,"✨",89-drift,108,27);
            }
        }
        c.restore();
        c.restore();
    }

    private void drawCareGlow(Canvas c,int action,float alpha) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(action==PlaylumiGarden.WATER?0xff8bcaf4:0xffffcf78);
        p.setAlpha((int)(190*alpha));
        for(int i=0;i<4;i++) {
            float x=116+i*28;
            float y=87+(i%2)*19;
            c.drawCircle(x,y,4+i*.4f,p);
        }
    }

    private void drawSparkles(Canvas c,float alpha) {
        p.setColor(0xffffd96c);
        p.setAlpha((int)(220*alpha));
        for(int i=0;i<7;i++) {
            double angle=i*Math.PI*2/7;
            float x=166+(float)Math.cos(angle)*72;
            float y=149+(float)Math.sin(angle)*60;
            c.drawCircle(x,y,3.4f,p);
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(PlaylumiMotion.isReduced())return;
        animator=ValueAnimator.ofFloat(0f,(float)(Math.PI*2));
        animator.setDuration(2800);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(v->{phase=(Float)v.getAnimatedValue();invalidate();});
        animator.start();
    }

    @Override protected void onDetachedFromWindow() {
        if(animator!=null){animator.cancel();animator=null;}
        if(growthAnimator!=null){growthAnimator.cancel();growthAnimator=null;}
        super.onDetachedFromWindow();
    }
}
