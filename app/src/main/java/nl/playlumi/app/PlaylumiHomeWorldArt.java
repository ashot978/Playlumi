package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Path;
import android.view.View;

/** One full-bleed image, no fitted copy, grey padding or repeated border artwork. */
public final class PlaylumiHomeWorldArt extends View {
    private final Bitmap bitmap;private final Rect src;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    public PlaylumiHomeWorldArt(Context context,int world){
        super(context);bitmap=PlaylumiWorldBackdrop.art(context);
        src=PlaylumiWorldBackdrop.crop(world);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        float radius=18*getResources().getDisplayMetrics().density;
        RectF area=new RectF(0,0,w,h);Path path=new Path();
        path.addRoundRect(area,new float[]{radius,radius,radius,radius,0,0,0,0},Path.Direction.CW);
        c.save();c.clipPath(path);
        // Crop the source at the target aspect ratio. The real image covers every pixel.
        float target=w/h,source=src.width()/(float)src.height();
        Rect crop=new Rect(src);
        if(source>target){
            int needed=Math.max(1,Math.min(src.width(),Math.round(src.height()*target)));
            crop.left=src.left+(src.width()-needed)/2;crop.right=crop.left+needed;
        }else{
            int needed=Math.max(1,Math.min(src.height(),Math.round(src.width()/target)));
            crop.top=src.top+(src.height()-needed)/2;crop.bottom=crop.top+needed;
        }
        c.drawBitmap(bitmap,crop,area,paint);c.restore();
    }
}
