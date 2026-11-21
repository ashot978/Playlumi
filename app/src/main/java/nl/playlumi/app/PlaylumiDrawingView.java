package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.os.SystemClock;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/** Finger painting canvas storing one picture privately and completely offline. */
public final class PlaylumiDrawingView extends View {
    public static final int[] PALETTE={0xffef6096,0xff8157dd,0xff2996d9,0xff4faf74,
            0xffffb837,0xff43416b};
    private static final class Stroke {
        final int ink;
        final Path path=new Path();
        Stroke(int ink){this.ink=ink;}
    }
    private final ArrayList<Stroke> lines=new ArrayList<>();
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final File savedFile;
    private Bitmap background;
    private Stroke current;
    private int ink=PALETTE[0];
    private float sparkleX,sparkleY;
    private long sparkleAt;
    public PlaylumiDrawingView(Context context,File savedFile) {
        super(context);
        this.savedFile=savedFile;
        setBackgroundColor(Color.WHITE);
        setContentDescription("Teken met je vinger op dit witte vel");
        if(savedFile.isFile()) background=BitmapFactory.decodeFile(savedFile.getAbsolutePath());
    }
    private float brushWidth(){return 8*getResources().getDisplayMetrics().density;}
    public void color(int color) { ink=color; }
    public void undo() {
        if(!lines.isEmpty()) {lines.remove(lines.size()-1);invalidate();}
    }
    public void clear() {
        lines.clear();
        if(background!=null){background.recycle();background=null;}
        invalidate();
    }
    public boolean save() {
        if(getWidth()<1||getHeight()<1)return false;
        Bitmap bmp=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bmp);
        drawArtwork(canvas);
        File staging=new File(savedFile.getParentFile(),savedFile.getName()+".tmp");
        try(FileOutputStream out=new FileOutputStream(staging)) {
            if(!bmp.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("PNG encoder");
            out.flush();
        } catch(IOException ex) {
            bmp.recycle();staging.delete();return false;
        }
        if(savedFile.exists() && !savedFile.delete()){bmp.recycle();staging.delete();return false;}
        if(!staging.renameTo(savedFile)){bmp.recycle();return false;}
        if(background!=null)background.recycle();
        background=bmp;
        lines.clear();
        invalidate();
        return true;
    }
    private void drawArtwork(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        if(background!=null && !background.isRecycled()) {
            paint.reset();paint.setAntiAlias(true);
            canvas.drawBitmap(background,null,new RectF(0,0,getWidth(),getHeight()),paint);
        }
        for(Stroke stroke:lines) {
            paint.reset();paint.setAntiAlias(true);
            paint.setColor(stroke.ink);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(brushWidth());
            canvas.drawPath(stroke.path,paint);
        }
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        drawArtwork(c);
        if(!PlaylumiMotion.isReduced() && sparkleAt>0) {
            long since=SystemClock.uptimeMillis()-sparkleAt;
            if(since>=0 && since<280) {
                float fade=1f-since/280f;
                paint.reset();paint.setAntiAlias(true);
                paint.setColor((Math.round(175*fade)<<24)|0x00f7bb46);
                float radius=3f*getResources().getDisplayMetrics().density;
                c.drawCircle(sparkleX+radius*4,sparkleY-radius*5,radius*fade+1,paint);
                c.drawCircle(sparkleX-radius*4,sparkleY-radius*2,radius*fade+1,paint);
                paint.setColor((Math.round(190*fade)<<24)|0x009f85ee);
                c.drawCircle(sparkleX+radius*2,sparkleY+radius*3,radius*fade+1,paint);
                postInvalidateDelayed(45);
            }
        }
    }
    private void trackSparkle(float x,float y) {
        if(PlaylumiMotion.isReduced())return;
        sparkleX=x;sparkleY=y;sparkleAt=SystemClock.uptimeMillis();
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        float x=Math.max(0,Math.min(getWidth(),event.getX()));
        float y=Math.max(0,Math.min(getHeight(),event.getY()));
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(true);
                current=new Stroke(ink);current.path.moveTo(x,y);
                current.path.lineTo(x+.01f,y+.01f);
                lines.add(current);trackSparkle(x,y);invalidate();return true;
            case MotionEvent.ACTION_MOVE:
                if(current!=null){
                    current.path.lineTo(x,y);
                    trackSparkle(x,y);invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(current!=null){
                    current.path.lineTo(x,y);current=null;
                    trackSparkle(x,y);invalidate();
                }
                if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(false);
                performClick();return true;
            case MotionEvent.ACTION_CANCEL:
                current=null;
                if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            default:return false;
        }
    }
    @Override public boolean performClick() {
        super.performClick();
        return true;
    }
    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Keep the private image file; only release the decoded bitmap.
        if(background!=null){background.recycle();background=null;}
    }
}
