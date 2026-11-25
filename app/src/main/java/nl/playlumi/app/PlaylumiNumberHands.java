package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.View;

/** Countable raised fingers, drawn at the real available size; no printed solution. */
public final class PlaylumiNumberHands extends View {
    private final int count;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public PlaylumiNumberHands(Context context,int count){
        super(context);
        if(count<1||count>10)throw new IllegalArgumentException("finger count");
        this.count=count;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription("Aantal opgestoken vingers: "+count);
    }
    @Override protected void onMeasure(int w,int h){
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(),w),resolveSize(getSuggestedMinimumHeight(),h));
    }
    private void shape(Canvas c,RectF r,float rad,boolean folded){
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(r.left,r.top,r.right,r.bottom,
                new int[]{0xffffe4b2,0xffffc687,0xffe99a65},null,Shader.TileMode.CLAMP));
        c.drawRoundRect(r,rad,rad,p);p.setShader(null);
        p.setColor(folded?0xffc97c59:0xffb66b4a);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);
        c.drawRoundRect(r,rad,rad,p);p.setStyle(Paint.Style.FILL);
    }
    private void hand(Canvas c,float x,float y,float size,int n,boolean mirror){
        c.save();c.translate(x,y);c.scale(size/110f,size/125f);
        if(mirror){c.translate(110,0);c.scale(-1,1);}
        // Four separated vertical fingers; curled fingers stay visibly on the palm.
        float[] tops={12,4,13,24};
        for(int i=0;i<4;i++)if(n>i){
            float fx=32+i*17;
            shape(c,new RectF(fx,tops[i],fx+14,76),7,false);
        }
        // Thumb is the fifth raised finger; when curled it is across the palm.
        if(n==5){
            c.save();c.rotate(-34,29,63);
            shape(c,new RectF(17,31,32,85),7.5f,false);c.restore();
        }
        shape(c,new RectF(26,56,100,108),21,false);
        shape(c,new RectF(43,100,82,121),10,false);
        for(int i=n;i<4;i++){
            float fx=32+i*17;
            shape(c,new RectF(fx,57,fx+14,81),7,true);
        }
        if(n<5){
            c.save();c.rotate(-40,40,86);
            shape(c,new RectF(30,74,58,89),7,true);c.restore();
        }
        p.setColor(0x66ffffff);c.drawOval(52,88,78,94,p);
        c.restore();
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        int hands=count>5?2:1;
        float availableW=getWidth()*.86f,availableH=getHeight()*.80f;
        float size=Math.min(availableH,availableW/(hands==2?2.06f:1f));
        if(size<=0)return;
        float total=size*(hands==2?2.06f:1);
        float left=(getWidth()-total)/2,top=(getHeight()-size)/2;
        hand(c,left,top,size,Math.min(5,count),false);
        if(hands==2)hand(c,left+size*1.06f,top,size,count-5,true);
    }
}
