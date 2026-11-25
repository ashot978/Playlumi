package nl.playlumi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** Eight little stars make a learning round easier to understand than a long bar. */
public final class PlaylumiStarTrail extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int total;
    private int done,lastAdded=-1;
    private float bounce;
    private ValueAnimator finishAnimation;

    public PlaylumiStarTrail(Context context,int total,int progress) {
        super(context);
        if(total<=0 || total>40)throw new IllegalArgumentException("total");
        this.total=total;
        this.done=Math.max(0,Math.min(total,progress));
        setContentDescription(this.done+" van "+total+" sterren verzameld");
    }
    private void star(Canvas c,float x,float y,float radius,int fill) {
        Path path=new Path();
        for(int k=0;k<10;k++) {
            double a=Math.PI*k/5-Math.PI/2;
            float r=k%2==0?radius:radius*.44f;
            float px=x+(float)Math.cos(a)*r;
            float py=y+(float)Math.sin(a)*r;
            if(k==0)path.moveTo(px,py);else path.lineTo(px,py);
        }
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0,y-radius,0,y+radius,
                fill==0xfff2b942?0xfffff7a2:fill,fill==0xfff2b942?0xffffa323:fill,Shader.TileMode.CLAMP));
        c.drawPath(path,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f,radius*.085f));paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(fill==0xfff2b942?0xffd88624:0xffbb91d7);c.drawPath(path,paint);
        paint.setStyle(Paint.Style.FILL);
    }
    public void showProgress(int value) {
        int newValue=Math.max(0,Math.min(total,value));
        if(newValue==done)return;
        if(finishAnimation!=null) {
            finishAnimation.cancel();finishAnimation=null;
        }
        done=newValue;
        lastAdded=Math.max(0,done-1);
        setContentDescription(done+" van "+total+" sterren verzameld");
        if(PlaylumiMotion.isReduced() || !isAttachedToWindow()) {
            bounce=0;invalidate();return;
        }
        finishAnimation=ValueAnimator.ofFloat(0f,1f,0f);
        finishAnimation.setDuration(560);
        finishAnimation.setInterpolator(new DecelerateInterpolator());
        finishAnimation.addUpdateListener(animation->{
            bounce=(Float)animation.getAnimatedValue();
            invalidate();
        });
        finishAnimation.start();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getWidth()==0 || getHeight()==0)return;
        float step=(float)getWidth()/total;
        float centerY=getHeight()/2f;
        float radius=Math.min(step*.41f,getHeight()*.43f);
        for(int i=0;i<total;i++) {
            float x=(i+.5f)*step;
            float scale=i==lastAdded?1f+bounce*.28f:1f;
            if(i<done) {
                star(canvas,x,centerY,radius*scale,0xfff2b942);
            } else {
                star(canvas,x,centerY,radius*.94f,0xfff1dcfa);
            }
        }
    }
    @Override protected void onDetachedFromWindow() {
        if(finishAnimation!=null)finishAnimation.cancel();
        finishAnimation=null;
        super.onDetachedFromWindow();
    }
}
