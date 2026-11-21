package nl.playlumi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.view.animation.LinearInterpolator;

/** Subtle, moving pastel stars in the background, never over game answers. */
public final class PlaylumiSkyView extends View {
    private static final int[] PALETTE={
            0x409bc7ec,0x449bdcc9,0x48edc3e5,0x42f2d499,0x3fbdacf0
    };
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;
    private float phase;

    public PlaylumiSkyView(Context context) {
        super(context);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setClickable(false);
        setFocusable(false);
    }
    private static float fractional(float number) {
        return number-(float)Math.floor(number);
    }
    private void star(Canvas canvas,float x,float y,float radius,int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        Path path=new Path();
        for(int n=0;n<8;n++) {
            float r=n%2==0?radius:radius*.40f;
            float angle=(float)(Math.PI*n/4-Math.PI/2);
            float px=x+(float)Math.cos(angle)*r;
            float py=y+(float)Math.sin(angle)*r;
            if(n==0)path.moveTo(px,py); else path.lineTo(px,py);
        }
        path.close();
        canvas.drawPath(path,paint);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width=getWidth(),height=getHeight();
        if(width<=0 || height<=0)return;
        for(int i=0;i<13;i++) {
            float fx=.06f+.88f*fractional(i*.618034f+.15f);
            float fy=.11f+.78f*fractional(i*.414213f+.09f);
            float drift=PlaylumiMotion.isReduced()?0f:
                    (float)Math.sin(phase+i*1.13f)*5f;
            float bob=PlaylumiMotion.isReduced()?0f:
                    (float)Math.cos(phase*.75f+i*.91f)*9f;
            float x=fx*width+drift;
            float y=fy*height+bob;
            int color=PALETTE[i%PALETTE.length];
            float size=getResources().getDisplayMetrics().density*(i%3==0?8:5);
            if(i%3==0)star(canvas,x,y,size,color);
            else {
                paint.setColor(color);
                canvas.drawCircle(x,y,size,paint);
            }
        }
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(PlaylumiMotion.isReduced() ||
                (android.os.Build.VERSION.SDK_INT>=26 && !ValueAnimator.areAnimatorsEnabled()))
            return;
        animator=ValueAnimator.ofFloat(0f,(float)(Math.PI*2));
        animator.setDuration(9200);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(value->{
            phase=(Float)value.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }
    @Override protected void onDetachedFromWindow() {
        if(animator!=null) {
            animator.cancel();
            animator=null;
        }
        super.onDetachedFromWindow();
    }
}
