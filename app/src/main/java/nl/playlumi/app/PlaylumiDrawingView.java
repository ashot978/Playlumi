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
import android.util.AtomicFile;
import android.view.View;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

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
    private final AtomicFile savedFile;
    private final RectF artworkBounds=new RectF();
    private final float brushWidth,sparkleRadius;
    private Bitmap background;
    private Stroke current;
    private boolean dirty=true,persisted;
    private int ink=PALETTE[0];
    private float sparkleX,sparkleY;
    private long sparkleAt;
    public PlaylumiDrawingView(Context context,File savedFile) {
        super(context);
        this.savedFile=new AtomicFile(savedFile);
        float density=getResources().getDisplayMetrics().density;
        brushWidth=8*density;sparkleRadius=3*density;
        setBackgroundColor(Color.WHITE);
        setContentDescription("Teken met je vinger op dit witte vel");
        // openRead also restores the last committed backup after an interrupted write.
        try(FileInputStream in=this.savedFile.openRead()) {
            background=BitmapFactory.decodeStream(in);
        } catch(IOException ignored) {
            // A new or unreadable file still needs a first successful save.
        }
        persisted=background!=null;dirty=!persisted;
    }
    public void color(int color) { ink=color; }
    public void undo() {
        if(!lines.isEmpty()) {
            Stroke removed=lines.remove(lines.size()-1);
            if(current==removed)current=null;
            dirty=true;invalidate();
        }
    }
    public void clear() {
        boolean changed=!lines.isEmpty()||background!=null;
        current=null;lines.clear();
        if(background!=null){background.recycle();background=null;}
        if(changed)dirty=true;
        invalidate();
    }
    public boolean save() {
        if(!dirty&&persisted)return true;
        if(getWidth()<1||getHeight()<1)return false;
        Bitmap composed=null;
        FileOutputStream out=null;
        try {
            composed=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
            drawArtwork(new Canvas(composed));
            out=savedFile.startWrite();
            CRC32 checksum=new CRC32();
            CheckedOutputStream encoded=new CheckedOutputStream(out,checksum);
            if(!composed.compress(Bitmap.CompressFormat.PNG,100,encoded))throw new IOException("PNG encoder");
            encoded.flush();
            long bytes=out.getChannel().position();
            // Surface sync failures before committing; AtomicFile.finishWrite logs some errors.
            out.getFD().sync();
            savedFile.finishWrite(out);out=null;
            if(!matchesSavedBytes(bytes,checksum.getValue()))return false;
            persisted=true;dirty=false;
            // Keep the original background and strokes so Undo survives explicit and automatic saves.
            return true;
        } catch(IOException|RuntimeException ex) {
            return false;
        } finally {
            // AtomicFile owns closing this stream on both success and failure.
            try {
                if(out!=null)savedFile.failWrite(out);
            } finally {
                if(composed!=null)composed.recycle();
            }
        }
    }
    private boolean matchesSavedBytes(long expectedSize,long expectedChecksum) throws IOException {
        CRC32 checksum=new CRC32();long size=0;
        byte[] buffer=new byte[8192];
        try(FileInputStream in=savedFile.openRead()) {
            for(int read;(read=in.read(buffer))!=-1;) {
                checksum.update(buffer,0,read);size+=read;
                if(size>expectedSize)return false;
            }
        }
        return size==expectedSize&&checksum.getValue()==expectedChecksum;
    }
    private void drawArtwork(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        if(background!=null && !background.isRecycled()) {
            paint.reset();paint.setAntiAlias(true);
            artworkBounds.set(0,0,getWidth(),getHeight());
            canvas.drawBitmap(background,null,artworkBounds,paint);
        }
        for(int i=0;i<lines.size();i++) {
            Stroke stroke=lines.get(i);
            paint.reset();paint.setAntiAlias(true);
            paint.setColor(stroke.ink);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(brushWidth);
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
                float radius=sparkleRadius;
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
                lines.add(current);dirty=true;trackSparkle(x,y);invalidate();return true;
            case MotionEvent.ACTION_MOVE:
                if(current!=null){
                    current.path.lineTo(x,y);
                    dirty=true;trackSparkle(x,y);invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(current!=null){
                    current.path.lineTo(x,y);current=null;
                    dirty=true;trackSparkle(x,y);invalidate();
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
