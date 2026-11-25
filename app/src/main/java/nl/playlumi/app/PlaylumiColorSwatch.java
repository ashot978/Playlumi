package nl.playlumi.app;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** The approved paint splash, dynamically tinted to the current real question. */
public final class PlaylumiColorSwatch extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Bitmap art;
    public PlaylumiColorSwatch(Context context,int color){this(context,color,true);}
    public PlaylumiColorSwatch(Context context,int color,boolean scenic){
        super(context);art=PlaylumiMagicArt.colorQuestion(context,color,scenic);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription("Groot kleurvlak");
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(),widthSpec),
                resolveSize(getSuggestedMinimumHeight(),heightSpec));
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        float scale=Math.min(w/art.getWidth(),h/art.getHeight());
        float dw=art.getWidth()*scale,dh=art.getHeight()*scale;
        canvas.drawBitmap(art,null,new RectF((w-dw)/2,(h-dh)/2,(w+dw)/2,(h+dh)/2),paint);
    }
}
