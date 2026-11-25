package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import java.io.InputStream;
import java.io.IOException;

/**
 * Premium illustrated game portal. Uses the exact same actual village artwork
 * as the Playlumi home screen; no placeholder emojis or flat game icons.
 */
public final class PlaylumiPortalView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Bitmap artwork;
    private final int building;
    private final String heading;
    private final float density;

    public PlaylumiPortalView(Context context,int building,String heading) {
        super(context);
        this.building=Math.max(0,Math.min(PlaylumiVillageHit.COLORS,building));
        this.heading=heading;
        this.density=getResources().getDisplayMetrics().density;
        try(InputStream input=context.getAssets().open("playlumi_village.webp")) {
            artwork=BitmapFactory.decodeStream(input);
        }catch(IOException e) {
            throw new IllegalStateException("The required Playlumi village image is missing",e);
        }
        if(artwork==null || artwork.getWidth()!=941 || artwork.getHeight()!=1672)
            throw new IllegalStateException("Invalid illustrated game artwork");
        setContentDescription("Illustratie van "+heading+" in het magische Playlumi-dorp");
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
    }
    private Rect cropFor(float targetRatio) {
        float[] bounds=PlaylumiVillageHit.bounds(building);
        float cx=(bounds[0]+bounds[2])*.5f;
        float cy=(bounds[1]+bounds[3])*.5f;
        // Include surrounding paths, flowers and sky to make the artwork feel
        // like a real scene, not a close-up of a single emoji.
        float bw=bounds[2]-bounds[0]+55;
        float bh=bounds[3]-bounds[1]+40;
        float cropW=Math.max(bw,bh*targetRatio);
        float cropH=Math.max(bh,cropW/targetRatio);
        cropW=Math.min(941f,cropW);
        cropH=Math.min(1672f,cropH);
        // Keep the building slightly above center, leaving room for the title.
        cy+=18f;
        float left=Math.max(0,Math.min(941f-cropW,cx-cropW*.5f));
        float top=Math.max(0,Math.min(1672f-cropH,cy-cropH*.5f));
        return new Rect(Math.round(left),Math.round(top),
                Math.round(left+cropW),Math.round(top+cropH));
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width=getWidth(),height=getHeight();
        if(width<=0 || height<=0)return;
        float margin=4*density;
        float r=22*density;
        RectF dst=new RectF(margin,margin,width-margin,height-margin);
        canvas.save();
        Path rounded=new Path();
        rounded.addRoundRect(dst,r,r,Path.Direction.CW);
        canvas.clipPath(rounded);
        paint.reset();paint.setAntiAlias(true);paint.setFilterBitmap(true);
        Rect src=cropFor((dst.width()/dst.height()));
        canvas.drawBitmap(artwork,src,dst,paint);
        float fadeStart=dst.top+dst.height()*.62f;
        paint.reset();paint.setAntiAlias(true);
        paint.setShader(new LinearGradient(0,fadeStart,0,dst.bottom,
                new int[]{0x00000000,0x995b2e91,0xe62f1b68},null,
                Shader.TileMode.CLAMP));
        canvas.drawRect(dst.left,fadeStart,dst.right,dst.bottom,paint);
        paint.setShader(null);
        // Bold title plate, using the game's purple/gold palette.
        paint.setColor(0xffffeab7);
        paint.setTextSize(Math.min(29*density,width/10f));
        paint.setTypeface(android.graphics.Typeface.create(
                "sans-serif-rounded",android.graphics.Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(2*density,0,2*density,0xff371959);
        canvas.drawText("✦ "+heading+" ✦",width*.5f,dst.bottom-23*density,paint);
        paint.clearShadowLayer();
        canvas.restore();
        // Clear visible gold outline: same visual language as the village tabs.
        paint.reset();paint.setAntiAlias(true);paint.setColor(0xffffdfa1);
        paint.setStrokeWidth(2.5f*density);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(dst,r,r,paint);
        paint.setStyle(Paint.Style.FILL);
    }
}
