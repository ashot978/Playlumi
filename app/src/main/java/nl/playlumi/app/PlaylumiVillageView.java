package nl.playlumi.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;

/**
 * Playlumi's approved illustrated village, stored offline inside the APK.
 * Every building and bottom-tab is a real hit target, not a static menu picture.
 * The artwork is reconstructed and SHA-256 checked by Gradle before compilation.
 */
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
    private static final String[] NAMES={
            "Rekenraket","Letterhuis","Vormkasteel","Muziekstudio",
            "Speelplein","Kleurentuin","Stervriend","Ouderzone",
            "Missies","Beloningen","Home","Collectie","Profiel","Instellingen",
            "Sterren","Ontdekte gebouwen","Speler"
    };
    private static final float[][] DISCOVERED_AT={
            {384,459},{851,464},{291,775},{856,778},{327,1119},{838,1124}
    };

    private final Paint pen=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Listener listener;
    private final SharedPreferences discoveryPrefs;
    private final Bitmap village;
    private final int profileNumber;
    private final int starBalance;
    private int discovered;
    private int pressed=-1;
    private float downX,downY;
    private boolean tracking;
    private boolean animated;
    private int celebrated=-1;
    private long celebratedAt;
    private final long created=SystemClock.uptimeMillis();

    public PlaylumiVillageView(Context context,Listener listener,int profile,int stars) {
        super(context);
        this.listener=listener;
        this.profileNumber=Math.max(0,profile);
        this.starBalance=Math.max(0,stars);
        this.discoveryPrefs=context.getSharedPreferences("playlumi-village-"+profileNumber,
                Context.MODE_PRIVATE);
        this.discovered=PlaylumiVillageProgress.sanitize(discoveryPrefs.getInt("visited",0));
        try (InputStream in=context.getAssets().open("playlumi_village.webp")) {
            this.village=BitmapFactory.decodeStream(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Playlumi 0.73 village artwork absent from APK",ex);
        }
        if(village==null || village.getWidth()!=941 || village.getHeight()!=1672) {
            throw new IllegalStateException("Invalid village artwork; expected 941x1672");
        }
        setFocusable(true);
        setClickable(true);
        updateDescription();
    }
    private void updateDescription() {
        setContentDescription("Magisch Playlumi-dorp met zes speelgebouwen. "+
                PlaylumiVillageProgress.visitedCount(discovered)+
                " gebouwen bezocht. Tik op een gebouw om het spel te starten.");
    }
    private void paintColor(int c) {
        pen.reset();
        pen.setAntiAlias(true);
        pen.setColor(c);
    }
    private void text(Canvas c,String value,float x,float y,float size,int color) {
        paintColor(color);
        pen.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",
                android.graphics.Typeface.BOLD));
        pen.setTextSize(size);
        pen.setTextAlign(Paint.Align.CENTER);
        pen.setShadowLayer(2,0,2,0x8825124c);
        c.drawText(value,x,y,pen);
        pen.clearShadowLayer();
    }
    private void rounded(Canvas c,float l,float t,float r,float b,int color) {
        paintColor(color);
        c.drawRoundRect(l,t,r,b,24,24,pen);
    }
    private void drawRealCounters(Canvas c) {
        // The original concept contains decorative placeholder numbers.
        // Cover only those values with live game state, never fake game currency.
        rounded(c,346,24,490,79,0xff552d96);
        rounded(c,539,24,681,79,0xff552d96);
        rounded(c,729,24,822,79,0xff552d96);
        rounded(c,120,158,298,211,0xff552d96);
        text(c,"★ "+Math.min(99999,starBalance),417,62,37,0xffffe8a4);
        text(c,"★ "+PlaylumiVillageProgress.visitedCount(discovered)+"/6",610,61,33,0xffffe8a4);
        text(c,"P"+(profileNumber+1),772,62,31,Color.WHITE);
        text(c,"Speler "+(profileNumber+1),209,194,28,Color.WHITE);
    }
    private void drawDiscovered(Canvas c) {
        for(int i=0;i<PlaylumiVillageProgress.BUILDINGS;i++) {
            if(!PlaylumiVillageProgress.isVisited(discovered,i))continue;
            float x=DISCOVERED_AT[i][0],y=DISCOVERED_AT[i][1];
            paintColor(0xff6732a7);c.drawCircle(x,y,23,pen);
            paintColor(0xffffdb5f);c.drawCircle(x,y,17,pen);
            text(c,"✓",x,y+11,31,0xff4e2485);
        }
    }
    private void drawSparkles(Canvas c) {
        if(!animated || PlaylumiMotion.isReduced())return;
        float t=(SystemClock.uptimeMillis()-created)/1000f;
        for(int i=0;i<9;i++) {
            float x=100+(i*143)%730;
            float y=280+(i*193)%1150+(float)Math.sin(t*.92f+i*1.65f)*9f;
            float r=3.2f+(float)Math.cos(t*1.2f+i)*1.6f;
            paintColor(i%2==0?0xdffff2a4:0xbbe9c5ff);
            c.drawCircle(x,y,Math.max(1,r),pen);
        }
        if(celebrated>=0) {
            long elapsed=SystemClock.uptimeMillis()-celebratedAt;
            if(elapsed<1100) {
                float x=DISCOVERED_AT[celebrated][0];
                float y=DISCOVERED_AT[celebrated][1];
                paintColor((Math.max(0,Math.round(225f*(1-elapsed/1100f)))<<24)|0x00ffeaa1);
                pen.setStyle(Paint.Style.STROKE);
                pen.setStrokeWidth(8f);
                c.drawCircle(x,y,30+elapsed/10f,pen);
                pen.setStyle(Paint.Style.FILL);
            } else celebrated=-1;
        }
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w=getWidth(),h=getHeight();
        if(w<=0 || h<=0)return;
        canvas.drawColor(0xff6a53a2);
        float[] dst=PlaylumiVillageHit.destination(w,h);
        canvas.save();
        canvas.translate(dst[0],dst[1]);
        canvas.scale(dst[2]/PlaylumiVillageHit.ART_W,dst[3]/PlaylumiVillageHit.ART_H);
        pen.reset();pen.setFilterBitmap(true);pen.setAntiAlias(true);
        canvas.drawBitmap(village,null,
                new RectF(0,0,PlaylumiVillageHit.ART_W,PlaylumiVillageHit.ART_H),pen);
        drawRealCounters(canvas);
        drawDiscovered(canvas);
        if(pressed>=0) {
            float[] box=PlaylumiVillageHit.bounds(pressed);
            paintColor(0xd5ffdf68);
            pen.setStyle(Paint.Style.STROKE);
            pen.setStrokeWidth(7f);
            canvas.drawRoundRect(box[0]+3,box[1]+3,box[2]-3,box[3]-3,26,26,pen);
            pen.setStyle(Paint.Style.FILL);
        }
        drawSparkles(canvas);
        canvas.restore();
        if(animated && getWindowVisibility()==VISIBLE && !PlaylumiMotion.isReduced()) {
            postInvalidateDelayed(90);
        }
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animated=true;
        postInvalidate();
    }
    @Override protected void onDetachedFromWindow() {
        animated=false;
        super.onDetachedFromWindow();
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        int chosen=PlaylumiVillageHit.at(getWidth(),getHeight(),event.getX(),event.getY());
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pressed=chosen;downX=event.getX();downY=event.getY();
                tracking=true;invalidate();return true;
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(event.getX()-downX)>28f ||
                        Math.abs(event.getY()-downY)>28f) {
                    tracking=false;pressed=-1;invalidate();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                pressed=-1;tracking=false;invalidate();return true;
            case MotionEvent.ACTION_UP:
                int before=pressed;
                pressed=-1;invalidate();
                if(tracking && chosen>=0 && before==chosen) {
                    if(chosen<PlaylumiVillageProgress.BUILDINGS &&
                            !PlaylumiVillageProgress.isVisited(discovered,chosen)) {
                        discovered=PlaylumiVillageProgress.markVisited(discovered,chosen);
                        discoveryPrefs.edit().putInt("visited",discovered).apply();
                        celebrated=chosen;celebratedAt=SystemClock.uptimeMillis();
                        updateDescription();
                    }
                    performClick();
                    listener.visit(chosen);
                }
                tracking=false;return true;
            default:return true;
        }
    }
    @Override public boolean performClick() { super.performClick();return true; }
}
