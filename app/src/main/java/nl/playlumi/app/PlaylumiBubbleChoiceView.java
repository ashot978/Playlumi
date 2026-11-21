package nl.playlumi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * An illustrated, irregular floating toy, instead of a square Android button.
 * Uses native Canvas; clicks/accessibility are handled by a real focusable View.
 */
public final class PlaylumiBubbleChoiceView extends View {
    private final String picture;
    private final String title;
    private final int seed;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path outline=new Path();
    private final RadialGradient fill;
    private ValueAnimator idle;
    private long elapsed;

    public PlaylumiBubbleChoiceView(Context context,String picture,String title,
                                    int tint,int index){
        super(context);
        this.picture=picture;
        this.title=title;
        this.seed=index;
        int light=PlaylumiVisualStyle.lightened(tint,570);
        int dark=PlaylumiVisualStyle.lightened(tint,110);
        fill=new RadialGradient(63,35,101,new int[]{0xffffffff,light,dark},
                new float[]{0f,.35f,1f},Shader.TileMode.CLAMP);
        setClickable(true);
        setFocusable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription("Speel "+title);
        setMinimumHeight(dp(177));
    }

    private int dp(int n){
        return (int)(n*getResources().getDisplayMetrics().density+.5f);
    }

    @Override protected void onMeasure(int widthSpec,int heightSpec){
        setMeasuredDimension(resolveSize(dp(140),widthSpec),
                resolveSize(dp(178),heightSpec));
    }

    private void organicBubble(float x,float y,float radius,float shimmer){
        outline.reset();
        final int petals=12;
        for(int i=0;i<petals;i++){
            double angle=(Math.PI*2*i/petals)-Math.PI/2;
            double next=(Math.PI*2*(i+1)/petals)-Math.PI/2;
            float r=radius*(1f+.043f*(float)Math.sin(i*1.7+seed+shimmer));
            float nr=radius*(1f+.043f*(float)Math.sin((i+1)*1.7+seed+shimmer));
            float px=x+(float)Math.cos(angle)*r;
            float py=y+(float)Math.sin(angle)*r;
            float nx=x+(float)Math.cos(next)*nr;
            float ny=y+(float)Math.sin(next)*nr;
            if(i==0)outline.moveTo(px,py);
            outline.quadTo(px,py,(px+nx)/2,(py+ny)/2);
        }
        outline.close();
    }

    private void circle(Canvas c,float x,float y,float radius,int color){
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        c.drawCircle(x,y,radius,paint);
    }

    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(getWidth()<=0 || getHeight()<=0)return;
        float density=getResources().getDisplayMetrics().density;
        float scale=Math.min(getWidth()/(190f*density),
                getHeight()/(184f*density));
        // Exact floating-point sizing keeps the illustrated bubble visible
        // even in narrow phone columns (<190dp), without stretching.
        canvas.save();
        canvas.translate((getWidth()-190f*density*scale)/2f,
                (getHeight()-184f*density*scale)/2f);
        canvas.scale(density*scale,density*scale);
        PlaylumiBubbleMotion.Frame frame=PlaylumiBubbleMotion.sample(
                elapsed,seed,PlaylumiMotion.isReduced());
        float shine=frame.sparkle;
        circle(canvas,26,39+frame.lift*.6f,3.5f,0xfff6c875);
        circle(canvas,164,46-frame.lift*.6f,4,0xffa6d8f2);
        circle(canvas,31,124,2.5f,0xffe9a5d9);
        circle(canvas,159,117,3,0xffa8dfb2);

        canvas.save();
        canvas.translate(0f,frame.lift);
        canvas.rotate(frame.tilt,95,82);
        canvas.scale(frame.scale,frame.scale,95,82);
        circle(canvas,96,94,68,0x200d4569); // soft grounded shadow
        organicBubble(95,79,70,frame.tilt*.09f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xffffffff);
        paint.setShader(fill);
        canvas.drawPath(outline,paint);
        paint.setShader(null);
        paint.setColor(0x77ffffff);
        canvas.drawOval(new RectF(53,34,87,48),paint);
        circle(canvas,139,39,3.1f,0x99ffffff);
        paint.setColor(0xff3c3155);
        paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(55);
        Paint.FontMetrics fm=paint.getFontMetrics();
        canvas.drawText(picture,95,80-(fm.ascent+fm.descent)/2f,paint);
        canvas.restore();

        // A floating pill-shaped name tag, not a square card.
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        paint.setColor(0xf7ffffff);
        canvas.drawRoundRect(new RectF(24,146,166,181),18,18,paint);
        paint.setColor(PlaylumiVisualStyle.INK);
        paint.setTextSize(19);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        canvas.drawText(title,95,169,paint);
        if(shine>0.32f){
            circle(canvas,146,21,2.8f,0xffffd773);
            circle(canvas,152,27,1.9f,0xffd59cf3);
        }
        canvas.restore();
    }

    @Override public boolean onTouchEvent(MotionEvent event){
        if(!PlaylumiMotion.isReduced()){
            int action=event.getActionMasked();
            if(action==MotionEvent.ACTION_DOWN){
                animate().cancel();
                animate().scaleX(.93f).scaleY(.93f).setDuration(95).start();
            } else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL){
                animate().cancel();
                animate().scaleX(1f).scaleY(1f).setDuration(220).start();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        if(PlaylumiMotion.isReduced() ||
                (Build.VERSION.SDK_INT>=26 && !ValueAnimator.areAnimatorsEnabled()))
            return;
        idle=ValueAnimator.ofInt(0,(int)PlaylumiBubbleMotion.LOOP_MILLIS-1);
        idle.setDuration(PlaylumiBubbleMotion.LOOP_MILLIS);
        idle.setRepeatCount(ValueAnimator.INFINITE);
        idle.setStartDelay((seed%4)*115L);
        idle.setInterpolator(new LinearInterpolator());
        idle.addUpdateListener(a->{
            elapsed=(Integer)a.getAnimatedValue();
            invalidate();
        });
        idle.start();
    }

    @Override protected void onDetachedFromWindow(){
        if(idle!=null){idle.cancel();idle=null;}
        animate().cancel();
        super.onDetachedFromWindow();
    }
}
