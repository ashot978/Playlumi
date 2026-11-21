package nl.playlumi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * The home and picture-select screens live in a gently moving illustrated
 * playground: clouds, sun, rolling hills, butterflies and flowers.
 * Decorative only and removed automatically with the hosting screen.
 */
public final class PlaylumiWonderlandView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private ValueAnimator idle;
    private float phase;

    public PlaylumiWonderlandView(Context context){
        super(context);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setClickable(false);setFocusable(false);
    }

    private void oval(Canvas c,float l,float t,float r,float b,int color){
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        c.drawOval(new RectF(l,t,r,b),paint);
    }
    private void circle(Canvas c,float x,float y,float r,int color){
        paint.setColor(color);paint.setStyle(Paint.Style.FILL);
        c.drawCircle(x,y,r,paint);
    }
    private void cloud(Canvas c,float x,float y,float size){
        oval(c,x-size,y,x+size,y+size*.39f,0xcaffffff);
        oval(c,x-size*.63f,y-size*.28f,x+size*.29f,y+size*.38f,0xe8ffffff);
        oval(c,x-size*.02f,y-size*.20f,x+size*.83f,y+size*.37f,0xedffffff);
    }
    private void flower(Canvas c,float x,float y,float size,int tint){
        paint.setColor(0xa0529c76);paint.setStrokeWidth(Math.max(1,size*.13f));
        c.drawLine(x,y,x,y+size*1.5f,paint);
        for(int petal=0;petal<5;petal++){
            double a=petal*Math.PI*2/5+phase*.03;
            circle(c,x+(float)Math.cos(a)*size*.75f,
                    y+(float)Math.sin(a)*size*.75f,size*.53f,tint);
        }
        circle(c,x,y,size*.47f,0xfff9d37b);
    }

    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float width=getWidth(),height=getHeight();
        if(width<1 || height<1)return;
        canvas.save();
        // All motifs are semi-transparent; dark text and controls stay readable.
        float breathe=PlaylumiMotion.isReduced()?0:(float)Math.sin(phase);
        float drift=PlaylumiMotion.isReduced()?0:(float)Math.sin(phase*.8f)*width*.025f;
        float sunX=width*.83f,sunY=height*.12f;
        circle(canvas,sunX,sunY,width*.10f,0x49ffe6a0);
        circle(canvas,sunX,sunY,width*.073f,0x88ffe6a0);
        circle(canvas,sunX-width*.021f,sunY-width*.005f,width*.005f,0xffc38f70);
        circle(canvas,sunX+width*.021f,sunY-width*.005f,width*.005f,0xffc38f70);

        cloud(canvas,width*.19f+drift,height*.17f,width*.14f);
        cloud(canvas,width*.71f-drift,height*.34f,width*.11f);
        cloud(canvas,width*.25f-drift*.45f,height*.58f,width*.09f);

        path.reset();path.moveTo(0,height*.80f);
        path.cubicTo(width*.24f,height*.70f,width*.43f,height*.83f,
                width*.63f,height*.76f);
        path.cubicTo(width*.83f,height*.70f,width,height*.79f,width,height*.78f);
        path.lineTo(width,height);path.lineTo(0,height);path.close();
        paint.setColor(0x66c8edcf);paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path,paint);
        path.reset();path.moveTo(0,height*.91f);
        path.cubicTo(width*.29f,height*.80f,width*.58f,height*.94f,
                width,height*.85f);
        path.lineTo(width,height);path.lineTo(0,height);path.close();
        paint.setColor(0x67b9e4cb);canvas.drawPath(path,paint);

        // Two drifting butterflies and several tiny meadow flowers.
        float bx=width*.17f+drift*1.5f,by=height*.46f+breathe*10f;
        oval(canvas,bx-10,by-7,bx-1,by+8,0x9dda99ee);
        oval(canvas,bx+1,by-7,bx+10,by+8,0x9dda99ee);
        oval(canvas,bx-7,by+1,bx-1,by+12,0x99f9c6ee);
        oval(canvas,bx+1,by+1,bx+7,by+12,0x99f9c6ee);
        circle(canvas,bx,by,2,0xff8666b1);
        float b2x=width*.87f-drift, b2y=height*.67f-breathe*6;
        oval(canvas,b2x-7,b2y-5,b2x,b2y+8,0x84f9bc77);
        oval(canvas,b2x,b2y-5,b2x+7,b2y+8,0x84f9bc77);

        flower(canvas,width*.10f,height*.89f,6,0xa9ee9dbf);
        flower(canvas,width*.27f,height*.95f,5,0xb7b9b5f9);
        flower(canvas,width*.78f,height*.92f,6,0xb9f5c19b);
        flower(canvas,width*.92f,height*.84f,5,0xc1dcabf9);
        for(int i=0;i<13;i++){
            float x=width*(.04f+(i*0.07f)%0.92f);
            float y=height*(.22f+(i*0.073f)%0.49f);
            float radius=i%3==0?3.4f:2.1f;
            circle(canvas,x+drift*(i%2==0?1f:-1f)*.18f,
                    y+breathe*(i%3-1)*2f,radius,0x7be5b7e9);
        }
        canvas.restore();
    }

    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        if(PlaylumiMotion.isReduced() ||
                (Build.VERSION.SDK_INT>=26 && !ValueAnimator.areAnimatorsEnabled()))return;
        idle=ValueAnimator.ofFloat(0,(float)(Math.PI*2));
        idle.setDuration(12000);
        idle.setRepeatCount(ValueAnimator.INFINITE);
        idle.setInterpolator(new LinearInterpolator());
        idle.addUpdateListener(a->{
            phase=(Float)a.getAnimatedValue();
            invalidate();
        });
        idle.start();
    }
    @Override protected void onDetachedFromWindow(){
        if(idle!=null){idle.cancel();idle=null;}
        super.onDetachedFromWindow();
    }
}
