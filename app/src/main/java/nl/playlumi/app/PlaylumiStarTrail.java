package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** A quiet, stable row of stars for the current round's progress. */
public final class PlaylumiStarTrail extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int total;
    private int done;

    public PlaylumiStarTrail(Context context,int total,int progress) {
        super(context);
        if(total<=0 || total>40)throw new IllegalArgumentException("total");
        this.total=total;done=Math.max(0,Math.min(total,progress));
        setContentDescription(done+" van "+total+" klaar");
    }
    private void star(Canvas canvas,float x,float y,float radius,boolean complete) {
        Path path=new Path();
        for(int k=0;k<10;k++) {
            double angle=Math.PI*k/5-Math.PI/2;
            float r=k%2==0?radius:radius*.44f;
            float px=x+(float)Math.cos(angle)*r,py=y+(float)Math.sin(angle)*r;
            if(k==0)path.moveTo(px,py);else path.lineTo(px,py);
        }
        path.close();
        paint.setStyle(complete?Paint.Style.FILL:Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f,radius*.12f));paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(complete?0xffd7a63d:0xffa58fb7);canvas.drawPath(path,paint);
        paint.setStyle(Paint.Style.FILL);
    }
    public void showProgress(int value) {
        int next=Math.max(0,Math.min(total,value));
        if(next==done)return;
        done=next;setContentDescription(done+" van "+total+" klaar");invalidate();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getWidth()==0 || getHeight()==0)return;
        float density=getResources().getDisplayMetrics().density;
        float step=Math.min((float)getWidth()/total,Math.min(26*density,getHeight()*1.6f));
        float left=(getWidth()-step*total)/2f,centerY=getHeight()/2f;
        float radius=Math.min(step*.34f,getHeight()*.39f);
        for(int i=0;i<total;i++)star(canvas,left+(i+.5f)*step,centerY,radius,i<done);
    }
}
