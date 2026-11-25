package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

/** A single-building illustration with a separate readable title plate. */
public final class PlaylumiPortalView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Bitmap art;
    private final int world;
    private final String heading;
    private final float density;
    public PlaylumiPortalView(Context context,int world,String heading) {
        super(context);this.world=PlaylumiWorldStyle.valid(world)?world:0;
        this.heading=heading;density=getResources().getDisplayMetrics().density;
        art=PlaylumiWorldBackdrop.art(context);
        setContentDescription("Illustratie van "+heading);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w=getWidth(),h=getHeight(),m=3*density;
        if(w<=2*m||h<=2*m)return;
        RectF box=new RectF(m,m,w-m,h-m);
        float radius=19*density;
        canvas.save();
        Path path=new Path();path.addRoundRect(box,radius,radius,Path.Direction.CW);
        canvas.clipPath(path);
        p.setShader(new LinearGradient(0,0,0,h,PlaylumiWorldStyle.accent(world),
                PlaylumiWorldStyle.deep(world),Shader.TileMode.CLAMP));
        canvas.drawRect(box,p);p.setShader(null);
        Rect src=PlaylumiWorldBackdrop.crop(world);
        float titleH=Math.min(34*density,h*.25f);
        float areaH=box.height()-titleH;
        // Preserve the original illustration proportions, never stretch it.
        float scale=Math.min(box.width()/src.width(),areaH/src.height());
        float dw=src.width()*scale,dh=src.height()*scale;
        RectF dst=new RectF((w-dw)/2,m+(areaH-dh)/2,(w+dw)/2,m+(areaH+dh)/2);
        p.setColor(0xfffce2a7);
        canvas.drawRoundRect(dst.left-2*density,dst.top,dst.right+2*density,dst.bottom,8*density,8*density,p);
        canvas.drawBitmap(art,src,dst,p);
        p.setColor(0xff26164b);canvas.drawRect(box.left,box.bottom-titleH,box.right,box.bottom,p);
        p.setColor(0xffffe8af);p.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.min(22*density,w/(heading.length()*.62f+3)));
        canvas.drawText("✦ "+heading+" ✦",w/2,box.bottom-titleH*.5f-(p.ascent()+p.descent())/2,p);
        canvas.restore();
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f*density);
        p.setColor(0xffffd98a);canvas.drawRoundRect(box,radius,radius,p);p.setStyle(Paint.Style.FILL);
    }
}
