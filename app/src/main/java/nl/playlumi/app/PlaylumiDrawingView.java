package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AtomicFile;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

/** Private offline paper with aspect-preserving drawing, real erasing and bounded Undo/Redo. */
public final class PlaylumiDrawingView extends View {
    public static final int[] PALETTE={0xffef6096,0xff8157dd,0xff2996d9,0xff4faf74,
            0xffffb837,0xff43416b};
    private static final int FRAME_COLOR=0xffded9e9,PAPER_EDGE=0xff9f94b5;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PorterDuffXfermode eraserMode=new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private final AtomicFile savedFile;
    private final float density,sparkleRadius;
    private final Path currentPath=new Path();
    private final IdentityHashMap<PlaylumiDrawingHistory.Action,Path> paths=new IdentityHashMap<>();
    private final PlaylumiDrawingHistory history;
    private final PlaylumiDrawingHistory.Compactor compactor=this::flatten;
    private Bitmap background;
    private PlaylumiDrawingHistory.StrokeBuilder current;
    private PlaylumiDrawingGeometry.Paper paper=PlaylumiDrawingGeometry.fit(0,0,1,1,0);
    private int documentWidth=PlaylumiDrawingGeometry.NEW_WIDTH,documentHeight=PlaylumiDrawingGeometry.NEW_HEIGHT;
    private boolean dirty=true,persisted,eraser,lastStrokeSimplified;
    private int ink=PALETTE[0],brushSize=1,activePointer=-1;
    private float sparkleX,sparkleY;
    private long sparkleAt;
    private Runnable stateChanged;

    public PlaylumiDrawingView(Context context,File savedFile){
        super(context);this.savedFile=new AtomicFile(savedFile);
        density=getResources().getDisplayMetrics().density;sparkleRadius=3*density;
        setBackgroundColor(FRAME_COLOR);
        setContentDescription("Teken met je vinger op dit witte vel");
        // AtomicFile.openRead restores the last committed backup after an interrupted write.
        background=readBackground();
        if(background!=null){documentWidth=background.getWidth();documentHeight=background.getHeight();}
        persisted=background!=null;dirty=!persisted;
        history=new PlaylumiDrawingHistory(persisted);
    }
    private Bitmap readBackground(){
        try{
            BitmapFactory.Options size=new BitmapFactory.Options();size.inJustDecodeBounds=true;
            try(FileInputStream in=savedFile.openRead()){BitmapFactory.decodeStream(in,null,size);}
            if(size.outWidth<=0||size.outHeight<=0)return null;
            BitmapFactory.Options decode=new BitmapFactory.Options();
            decode.inSampleSize=PlaylumiDrawingGeometry.sampleSize(size.outWidth,size.outHeight);
            decode.inPreferredConfig=Bitmap.Config.ARGB_8888;decode.inMutable=true;
            try(FileInputStream in=savedFile.openRead()){
                Bitmap loaded=BitmapFactory.decodeStream(in,null,decode);
                // PNGs saved on white paper may be decoded as opaque. The mutable base
                // must retain alpha when an old eraser operation is compacted into it.
                if(loaded!=null){loaded.setHasAlpha(true);loaded.setDensity(Bitmap.DENSITY_NONE);}
                return loaded;
            }
        }catch(IOException|RuntimeException ignored){
            // A new or unreadable file still needs a first successful save.
            return null;
        }
    }

    public void setOnStateChangedListener(Runnable listener){stateChanged=listener;notifyState();}
    private void notifyState(){if(stateChanged!=null)stateChanged.run();}
    public int color(){return ink;}
    /** Choosing ink returns to the pencil without changing any pixels or Undo history. */
    public void color(int color){
        finishCurrentStroke();
        if(ink==color&&!eraser)return;
        ink=color;eraser=false;lastStrokeSimplified=false;notifyState();
    }
    public int brushSize(){return brushSize;}
    public void setBrushSize(int size){
        finishCurrentStroke();size=PlaylumiDrawingGeometry.safeBrushSize(size);
        if(brushSize==size)return;
        brushSize=size;lastStrokeSimplified=false;notifyState();
    }
    public boolean isEraser(){return eraser;}
    public void setEraser(boolean enabled){
        finishCurrentStroke();
        if(eraser==enabled)return;
        eraser=enabled;lastStrokeSimplified=false;notifyState();
    }
    public boolean canUndo(){return current!=null||history.canUndo();}
    public boolean canRedo(){return current==null&&history.canRedo();}
    public boolean wasLastStrokeSimplified(){return lastStrokeSimplified;}
    public void getPaperBounds(RectF out){out.set(paper.left,paper.top,paper.right,paper.bottom);}

    public boolean undo(){
        finishCurrentStroke();
        if(!history.undo())return false;
        dirty=true;lastStrokeSimplified=false;sparkleAt=0;invalidate();notifyState();return true;
    }
    public boolean redo(){
        finishCurrentStroke();
        if(!history.redo())return false;
        dirty=true;lastStrokeSimplified=false;sparkleAt=0;invalidate();notifyState();return true;
    }
    /** Clearing the paper is one reversible operation, including a previously loaded PNG. */
    public void clear(){
        finishCurrentStroke();
        if(!history.hasArtwork())return;
        history.append(PlaylumiDrawingHistory.Action.clear(),compactor);trimPaths();
        dirty=true;lastStrokeSimplified=false;sparkleAt=0;invalidate();notifyState();
    }

    public boolean save(){
        finishCurrentStroke();
        // Tool changes and a different View size must not re-encode an unchanged legacy PNG.
        if(!dirty&&persisted)return true;
        // Fixed document dimensions keep saving independent of a transient zero-size View.
        Bitmap composed=null;FileOutputStream out=null;
        try{
            composed=Bitmap.createBitmap(documentWidth,documentHeight,Bitmap.Config.ARGB_8888);
            composed.setDensity(Bitmap.DENSITY_NONE);
            drawDocument(new Canvas(composed));
            out=savedFile.startWrite();
            CRC32 checksum=new CRC32();
            CheckedOutputStream encoded=new CheckedOutputStream(out,checksum);
            if(!composed.compress(Bitmap.CompressFormat.PNG,100,encoded))throw new IOException("PNG encoder");
            encoded.flush();
            long bytes=out.getChannel().position();
            // Surface sync failures before committing; finishWrite itself logs some errors.
            out.getFD().sync();
            savedFile.finishWrite(out);out=null;
            if(!matchesSavedBytes(bytes,checksum.getValue()))return false;
            persisted=true;dirty=false;
            // Saving does not flatten the live history: both Undo and Redo remain available.
            return true;
        }catch(IOException|RuntimeException ex){
            return false;
        }finally{
            try{if(out!=null)savedFile.failWrite(out);}
            finally{if(composed!=null)composed.recycle();}
        }
    }
    private boolean matchesSavedBytes(long expectedSize,long expectedChecksum)throws IOException{
        CRC32 checksum=new CRC32();long size=0;byte[] buffer=new byte[8192];
        try(FileInputStream in=savedFile.openRead()){
            for(int read;(read=in.read(buffer))!=-1;){
                checksum.update(buffer,0,read);size+=read;
                if(size>expectedSize)return false;
            }
        }
        return size==expectedSize&&checksum.getValue()==expectedChecksum;
    }

    @Override protected void onSizeChanged(int width,int height,int oldWidth,int oldHeight){
        super.onSizeChanged(width,height,oldWidth,oldHeight);
        // End a finger gesture in its original coordinate system before the paper moves.
        if(current!=null)finishCurrentStroke();
        paper=PlaylumiDrawingGeometry.fit(width,height,documentWidth,documentHeight,
                PlaylumiDrawingGeometry.PAPER_INSET_DP*density);
    }
    private void flatten(PlaylumiDrawingHistory.Action action){
        if(background==null){
            background=Bitmap.createBitmap(documentWidth,documentHeight,Bitmap.Config.ARGB_8888);
            background.setDensity(Bitmap.DENSITY_NONE);
        }
        drawAction(new Canvas(background),action);
    }
    private void trimPaths(){
        for(Iterator<PlaylumiDrawingHistory.Action> it=paths.keySet().iterator();it.hasNext();)
            if(!history.contains(it.next()))it.remove();
    }
    private Path pathFor(PlaylumiDrawingHistory.Action action){
        Path path=paths.get(action);
        if(path==null){
            path=new Path();
            for(int i=0;i<action.pointCount();i++){
                float x=action.x(i)*documentWidth,y=action.y(i)*documentHeight;
                if(i==0)path.moveTo(x,y);else path.lineTo(x,y);
            }
            paths.put(action,path);
        }
        return path;
    }
    private void rebuildCurrentPath(){
        currentPath.rewind();
        if(current==null)return;
        for(int i=0;i<current.pointCount();i++){
            float x=current.x(i)*documentWidth,y=current.y(i)*documentHeight;
            if(i==0)currentPath.moveTo(x,y);else currentPath.lineTo(x,y);
        }
    }
    private void strokePaint(int kind,int color,float width){
        paint.reset();paint.setAntiAlias(true);paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(width*Math.min(documentWidth,documentHeight));
        if(kind==PlaylumiDrawingHistory.ERASE)paint.setXfermode(eraserMode);
    }
    private void drawAction(Canvas canvas,PlaylumiDrawingHistory.Action action){
        if(action.kind==PlaylumiDrawingHistory.CLEAR){
            canvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);return;
        }
        strokePaint(action.kind,action.color,action.width);
        if(action.pointCount()==1){
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(action.x(0)*documentWidth,action.y(0)*documentHeight,paint.getStrokeWidth()/2,paint);
        }else canvas.drawPath(pathFor(action),paint);
    }
    /** An isolated transparent layer makes CLEAR erase old PNG pixels and new ink alike. */
    private void drawDocument(Canvas canvas){
        canvas.drawColor(Color.WHITE);
        int layer=canvas.saveLayer(0,0,documentWidth,documentHeight,null);
        if(background!=null){
            paint.reset();paint.setAntiAlias(true);paint.setFilterBitmap(true);
            canvas.drawBitmap(background,0,0,paint);
        }
        for(int i=0;i<history.appliedCount();i++)drawAction(canvas,history.applied(i));
        if(current!=null){
            strokePaint(current.kind(),current.color(),current.width());
            if(current.pointCount()==1){
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(current.x(0)*documentWidth,current.y(0)*documentHeight,paint.getStrokeWidth()/2,paint);
            }else canvas.drawPath(currentPath,paint);
        }
        canvas.restoreToCount(layer);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawColor(FRAME_COLOR);
        if(!paper.valid())return;
        int saved=canvas.save();
        canvas.clipRect(paper.left,paper.top,paper.right,paper.bottom);
        canvas.translate(paper.left,paper.top);canvas.scale(paper.scale,paper.scale);
        drawDocument(canvas);canvas.restoreToCount(saved);
        paint.reset();paint.setAntiAlias(true);paint.setColor(PAPER_EDGE);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(density);
        canvas.drawRect(paper.left,paper.top,paper.right,paper.bottom,paint);
        if(!PlaylumiMotion.isReduced()&&sparkleAt>0&&!eraser){
            long since=SystemClock.uptimeMillis()-sparkleAt;
            if(since>=0&&since<280){
                float fade=1f-since/280f;
                paint.reset();paint.setAntiAlias(true);
                paint.setColor((Math.round(175*fade)<<24)|0x00f7bb46);
                canvas.drawCircle(sparkleX+sparkleRadius*4,sparkleY-sparkleRadius*5,sparkleRadius*fade+1,paint);
                canvas.drawCircle(sparkleX-sparkleRadius*4,sparkleY-sparkleRadius*2,sparkleRadius*fade+1,paint);
                paint.setColor((Math.round(190*fade)<<24)|0x009f85ee);
                canvas.drawCircle(sparkleX+sparkleRadius*2,sparkleY+sparkleRadius*3,sparkleRadius*fade+1,paint);
                postInvalidateDelayed(45);
            }
        }
    }
    private void trackSparkle(float x,float y){
        if(eraser||PlaylumiMotion.isReduced())return;
        sparkleX=x;sparkleY=y;sparkleAt=SystemClock.uptimeMillis();
    }
    private void releasePointer(){
        activePointer=-1;
        if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(false);
    }
    private void finishCurrentStroke(){
        if(current==null){releasePointer();return;}
        PlaylumiDrawingHistory.Action action=current.build();
        rebuildCurrentPath();
        history.append(action,compactor);
        paths.put(action,new Path(currentPath));trimPaths();
        current=null;releasePointer();dirty=true;lastStrokeSimplified=action.simplified;
        invalidate();notifyState();
    }
    private void appendPoint(float x,float y){
        if(current!=null)current.append(paper.xFraction(x),paper.yFraction(y));
    }
    private void appendEvent(MotionEvent event,int pointer){
        for(int i=0;i<event.getHistorySize();i++)
            appendPoint(event.getHistoricalX(pointer,i),event.getHistoricalY(pointer,i));
        appendPoint(event.getX(pointer),event.getY(pointer));
        rebuildCurrentPath();
        trackSparkle(paper.viewX(paper.xFraction(event.getX(pointer))),
                paper.viewY(paper.yFraction(event.getY(pointer))));
        invalidate();
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                if(!paper.contains(event.getX(),event.getY()))return false;
                finishCurrentStroke();activePointer=event.getPointerId(0);
                if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(true);
                float width=PlaylumiDrawingGeometry.brushWidthDp(brushSize)*density/
                        (paper.scale*Math.min(documentWidth,documentHeight));
                current=new PlaylumiDrawingHistory.StrokeBuilder(ink,width,eraser,
                        paper.xFraction(event.getX()),paper.yFraction(event.getY()));
                dirty=true;lastStrokeSimplified=false;rebuildCurrentPath();
                trackSparkle(event.getX(),event.getY());invalidate();return true;
            case MotionEvent.ACTION_MOVE:
                if(current!=null){
                    int pointer=event.findPointerIndex(activePointer);
                    if(pointer>=0)appendEvent(event,pointer);else finishCurrentStroke();
                }
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerId(event.getActionIndex())==activePointer){
                    appendEvent(event,event.getActionIndex());finishCurrentStroke();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(current!=null){
                    int pointer=event.findPointerIndex(activePointer);
                    if(pointer>=0)appendEvent(event,pointer);
                    finishCurrentStroke();
                }else releasePointer();
                performClick();return true;
            case MotionEvent.ACTION_CANCEL:
                // The partial line has already been shown, so keep it as one undoable gesture.
                finishCurrentStroke();return true;
            default:return false;
        }
    }
    @Override public boolean performClick(){super.performClick();return true;}
    @Override protected void onDetachedFromWindow(){
        finishCurrentStroke();sparkleAt=0;super.onDetachedFromWindow();
        // Keep the bounded document/history if the same View is reattached. The bitmap is
        // collected with this View, rather than silently removing its loaded artwork here.
    }
}
