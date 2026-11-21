package nl.playlumi.app;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Puffy handmade cloud shapes for child-facing actions, not rigid cards. */
public final class PlaylumiCloudDrawable extends Drawable {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path outline=new Path();
    private final int color;
    private int alpha=255;

    public PlaylumiCloudDrawable(int background){color=background;}

    @Override public void draw(Canvas canvas){
        Rect bounds=getBounds();
        float left=bounds.left,top=bounds.top;
        float w=bounds.width(),h=bounds.height();
        if(w<=0 || h<=0)return;
        outline.reset();
        if(w<90 || h<52){
            outline.addRoundRect(new RectF(left+2,top+2,left+w-2,top+h-2),
                    Math.min(h/2f,22),Math.min(h/2f,22),Path.Direction.CW);
        }else{
            float x=left,y=top;
            outline.moveTo(x+26,y+7);
            outline.cubicTo(x+w*.28f,y+1,x+w*.45f,y+7,x+w*.60f,y+5);
            outline.cubicTo(x+w*.82f,y+2,x+w-11,y+8,x+w-8,y+24);
            outline.cubicTo(x+w,y+h*.43f,x+w-5,y+h*.70f,x+w-16,y+h-9);
            outline.cubicTo(x+w*.83f,y+h-2,x+w*.61f,y+h-7,x+w*.43f,y+h-4);
            outline.cubicTo(x+w*.19f,y+h-1,x+7,y+h+1,x+7,y+h-21);
            outline.cubicTo(x+1,y+h*.65f,x+4,y+11,x+26,y+7);
            outline.close();
        }
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x210f3664);
        paint.setAlpha(alpha);
        canvas.save();
        canvas.translate(0,2);
        canvas.drawPath(outline,paint);
        canvas.restore();

        paint.setColor(0xffffffff);
        paint.setShader(new LinearGradient(left,top,left+w,top+h,
                new int[]{PlaylumiVisualStyle.lightened(color,680),
                        PlaylumiVisualStyle.lightened(color,160)},
                null,Shader.TileMode.CLAMP));
        paint.setAlpha(alpha);
        canvas.drawPath(outline,paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(0x9cffffff);
        paint.setAlpha(alpha);
        canvas.drawPath(outline,paint);
        paint.setStyle(Paint.Style.FILL);
    }
    @Override public void setAlpha(int next){alpha=Math.max(0,Math.min(255,next));invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    @Override public void getOutline(Outline result){
        Rect b=getBounds();
        result.setRoundRect(b,Math.max(1f,Math.min(b.height()/2f,32f)));
    }
}
