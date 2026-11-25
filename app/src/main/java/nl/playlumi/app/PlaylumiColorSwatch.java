package nl.playlumi.app;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** Large accurate color target rather than a tiny typographic bullet. */
public final class PlaylumiColorSwatch extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int color;
    public PlaylumiColorSwatch(Context context,int color){
        super(context);this.color=color;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription("Groot kleurvlak");
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(),widthSpec),
                resolveSize(getSuggestedMinimumHeight(),heightSpec));
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float w=getWidth(),h=getHeight();
        float radius=PlaylumiWorldStyle.swatchDiameter(w,h)/2f;
        if(radius<=0)return;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x183d2b58);
        canvas.drawCircle(w/2,h/2+4*getResources().getDisplayMetrics().density,radius,paint);
        paint.setColor(color);
        canvas.drawCircle(w/2,h/2,radius,paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f*getResources().getDisplayMetrics().density);
        paint.setColor(0x5035224f);
        canvas.drawCircle(w/2,h/2,radius,paint);
        paint.setStyle(Paint.Style.FILL);
    }
}
