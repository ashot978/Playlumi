package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Path;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.View;

/** Each world has its own original village building, cropped without geometric stretching. */
public final class PlaylumiHomeWorldArt extends View {
    private final Bitmap bitmap;private final Rect src;private final int world;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    public PlaylumiHomeWorldArt(Context context,int world){
        super(context);this.world=world;bitmap=PlaylumiWorldBackdrop.art(context);
        src=PlaylumiWorldBackdrop.crop(world);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        float radius=12*getResources().getDisplayMetrics().density;
        RectF area=new RectF(0,0,w,h);Path path=new Path();path.addRoundRect(area,radius,radius,Path.Direction.CW);
        c.save();c.clipPath(path);
        // A soft matching backdrop fills the edges while the entire building stays visible.
        float cover=Math.max(w/src.width(),h/src.height());
        float cw=src.width()*cover,ch=src.height()*cover;
        paint.setShader(null);paint.setAlpha(255);
        c.drawBitmap(bitmap,src,new RectF((w-cw)/2,(h-ch)/2,(w+cw)/2,(h+ch)/2),paint);
        paint.setColor(0xb033204f);c.drawRect(area,paint);
        float fit=Math.min(w/src.width(),h/src.height());
        float dw=src.width()*fit,dh=src.height()*fit;
        paint.setAlpha(255);c.drawBitmap(bitmap,src,new RectF((w-dw)/2,(h-dh)/2,(w+dw)/2,(h+dh)/2),paint);
        paint.setShader(new LinearGradient(0,h*.78f,0,h,new int[]{0x00301c50,0x77301c50},null,Shader.TileMode.CLAMP));
        c.drawRect(area,paint);paint.setShader(null);
        c.restore();
    }
}
