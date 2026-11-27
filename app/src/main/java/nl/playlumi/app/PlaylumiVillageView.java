package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;

/** The original village illustration, with calm native navigation over its old chrome. */
public final class PlaylumiVillageView extends View {
    public interface Listener { void visit(int place); }

    public static final int ROCKET=PlaylumiVillageHit.ROCKET,
            BOOKS=PlaylumiVillageHit.BOOKS,
            SHAPES=PlaylumiVillageHit.SHAPES,
            MUSIC=PlaylumiVillageHit.MUSIC,
            PLAYGROUND=PlaylumiVillageHit.PLAYGROUND,
            COLORS=PlaylumiVillageHit.COLORS,
            FRIEND=PlaylumiVillageHit.FRIEND,
            PARENTS=PlaylumiVillageHit.PARENTS,
            MISSIONS=PlaylumiVillageHit.MISSIONS,
            REWARDS=PlaylumiVillageHit.REWARDS,
            HOME=PlaylumiVillageHit.HOME,
            COLLECTION=PlaylumiVillageHit.COLLECTION,
            PROFILE=PlaylumiVillageHit.PROFILE,
            SETTINGS=PlaylumiVillageHit.SETTINGS,
            STARS=PlaylumiVillageHit.STARS,
            DISCOVERY=PlaylumiVillageHit.DISCOVERY,
            PLAYER=PlaylumiVillageHit.PLAYER;
    static final int CHROME_COLOR=0xff39215c;
    static final float HEADER_BOTTOM=238f,FOOTER_TOP=1434f;

    private final Paint pen=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Listener listener;
    private final Bitmap village;
    private int pressed=-1;
    private float downX,downY;
    private boolean tracking;

    public PlaylumiVillageView(Context context,Listener listener,int profile,int stars,int avatar){this(context,listener,profile,stars,avatar,null);}
    public PlaylumiVillageView(Context context,Listener listener,int profile,int stars,int avatar,int[] outfit){this(context,listener,profile,stars,avatar,outfit,0);}
    public PlaylumiVillageView(Context context,Listener listener,int profile,int stars,int avatar,int[] outfit,int playStreak) {
        this(context,listener,profile,stars,avatar,outfit,playStreak,0);
    }
    public PlaylumiVillageView(Context context,Listener listener,int profile,int stars,int avatar,int[] outfit,int playStreak,int dailyCompleted) {
        super(context);this.listener=listener;
        try (InputStream in=context.getAssets().open("playlumi_village.webp")) {
            village=BitmapFactory.decodeStream(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Playlumi village artwork absent from APK",ex);
        }
        if(village==null || village.getWidth()!=941 || village.getHeight()!=1672) {
            throw new IllegalStateException("Invalid village artwork; expected 941x1672");
        }
        setFocusable(true);setClickable(true);
        setContentDescription("PlayLumi. Kies een van de zes speelwerelden in het dorp.");
    }
    public String profileLabel(){return "Lumi";}

    /** Shared visible bounds keep the painted chrome and native targets in agreement. */
    static float[] navigationBounds(int place){
        if(PlaylumiWorldStyle.valid(place))return PlaylumiVillageHit.bounds(place);
        if(place==PARENTS)return new float[]{686,34,913,139};
        if(place==HOME)return new float[]{24,1484,458,1628};
        if(place==MISSIONS)return new float[]{482,1484,917,1628};
        throw new IllegalArgumentException("village destination");
    }
    private int destinationAt(float x,float y){
        float[] destination=PlaylumiVillageHit.destination(getWidth(),getHeight());
        if(x<destination[0]||y<destination[1]||x>destination[0]+destination[2]||y>destination[1]+destination[3])return -1;
        float[] point=PlaylumiVillageHit.artCoordinate(getWidth(),getHeight(),x,y);
        int[] places={PARENTS,HOME,MISSIONS,ROCKET,BOOKS,SHAPES,MUSIC,PLAYGROUND,COLORS};
        for(int place:places){
            float[] bounds=navigationBounds(place);
            if(point[0]>=bounds[0]&&point[0]<=bounds[2]&&point[1]>=bounds[1]&&point[1]<=bounds[3])return place;
        }
        return -1;
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w=getWidth(),h=getHeight();
        if(w<=0 || h<=0)return;
        canvas.drawColor(CHROME_COLOR);
        float[] dst=PlaylumiVillageHit.destination(w,h);
        canvas.save();canvas.translate(dst[0],dst[1]);
        canvas.scale(dst[2]/PlaylumiVillageHit.ART_W,dst[3]/PlaylumiVillageHit.ART_H);
        pen.reset();pen.setFilterBitmap(true);pen.setAntiAlias(true);
        canvas.drawBitmap(village,null,new RectF(0,0,PlaylumiVillageHit.ART_W,PlaylumiVillageHit.ART_H),pen);
        // Cover every baked counter, avatar card and old tab, including their icons.
        // The six buildings and Lumi in the middle remain the original artwork.
        pen.setColor(CHROME_COLOR);
        canvas.drawRect(0,0,PlaylumiVillageHit.ART_W,HEADER_BOTTOM,pen);
        canvas.drawRect(0,FOOTER_TOP,PlaylumiVillageHit.ART_W,PlaylumiVillageHit.ART_H,pen);
        pen.setColor(0xff76578f);pen.setStrokeWidth(2);
        canvas.drawLine(0,HEADER_BOTTOM-1,PlaylumiVillageHit.ART_W,HEADER_BOTTOM-1,pen);
        canvas.drawLine(0,FOOTER_TOP+1,PlaylumiVillageHit.ART_W,FOOTER_TOP+1,pen);
        canvas.restore();
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        int chosen=destinationAt(event.getX(),event.getY());
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pressed=chosen;downX=event.getX();downY=event.getY();tracking=chosen>=0;return true;
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(event.getX()-downX)>28f || Math.abs(event.getY()-downY)>28f){tracking=false;pressed=-1;}
                return true;
            case MotionEvent.ACTION_CANCEL:
                pressed=-1;tracking=false;return true;
            case MotionEvent.ACTION_UP:
                int before=pressed;pressed=-1;
                if(tracking&&chosen>=0&&before==chosen){performClick();listener.visit(chosen);}
                tracking=false;return true;
            default:return true;
        }
    }
    @Override public boolean performClick(){super.performClick();return true;}
}
