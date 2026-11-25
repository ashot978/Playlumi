package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import java.io.InputStream;
import java.io.IOException;

/** Full-screen, world-specific backdrop from actual bundled game illustrations. */
public final class PlaylumiWorldBackdrop extends View {
    private static Bitmap cached;
    private final Bitmap artwork;
    private final int world;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    public static synchronized Bitmap art(Context context) {
        if(cached==null) {
            try(InputStream in=context.getAssets().open("playlumi_village.webp")) {
                cached=BitmapFactory.decodeStream(in);
            }catch(IOException e){throw new IllegalStateException("Missing village artwork",e);}
            if(cached==null || cached.getWidth()!=941 || cached.getHeight()!=1672)
                throw new IllegalStateException("Invalid village artwork");
        }
        return cached;
    }
    public static Rect crop(int world) {
        float[] b=PlaylumiVillageHit.bounds(world);
        // Isolate the individual building rather than the adjacent world.
        return new Rect(Math.max(0,(int)b[0]+8),Math.max(0,(int)b[1]-8),
                Math.min(941,(int)b[2]-8),Math.min(1672,(int)b[3]-44));
    }
    public PlaylumiWorldBackdrop(Context context,int world) {
        super(context);this.world=world;artwork=art(context);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setClickable(false);setFocusable(false);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        Rect src=crop(world);
        float scale=Math.max(w/src.width(),h/src.height());
        float dw=src.width()*scale,dh=src.height()*scale;
        canvas.drawBitmap(artwork,src,new RectF((w-dw)/2,(h-dh)/2,(w+dw)/2,(h+dh)/2),paint);
        int pale=PlaylumiWorldStyle.pale(world);
        paint.setShader(new LinearGradient(0,0,0,h,
                new int[]{0xef000000|(pale&0xffffff),0xf8fffff4,0xe9000000|(pale&0xffffff)},null,Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,w,h,paint);paint.setShader(null);
        paint.setColor(0x24000000|(PlaylumiWorldStyle.accent(world)&0xffffff));
        float edge=12*getResources().getDisplayMetrics().density;
        canvas.drawRect(0,0,edge,h,paint);canvas.drawRect(w-edge,0,w,h,paint);
        for(int i=0;i<14;i++){
            float x=((i*37+19)%100)/100f*w,y=((i*61+13)%100)/100f*h;
            canvas.drawCircle(x,y,edge*(i%3==0?.65f:.30f),paint);
        }
    }
}
