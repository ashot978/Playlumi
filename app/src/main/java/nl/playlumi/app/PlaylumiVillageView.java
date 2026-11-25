package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;

/**
 * Illustrated, scrollable game village. Game locations are buildings, not menu cards.
 * The complete town illustration is packaged offline in assets/playlumi_village.webp.
 * It is intentionally loaded from assets so the art can be updated without rewriting
 * the screen or any children's progress / game-state code.
 */
public final class PlaylumiVillageView extends View {
    public interface Listener { void visit(int place); }
    public static final int ROCKET=0, BOOKS=1, SHAPES=2, MUSIC=3,
            PLAYGROUND=4, COLORS=5, FRIEND=6, PARENTS=7,
            MISSIONS=8, REWARDS=9, HOME=10, COLLECTION=11, PROFILE=12;
    private static final float ART_W=941f, ART_H=1672f;
    // Touch areas are broad enough to work on narrow phones, in picture coordinates.
    private static final float[][] HIT={
            {115,253,467,530},    // Rocket + its game banner
            {535,262,927,548},    // Letter house
            {25,535,360,880},     // Shape castle
            {610,542,924,887},    // Music studio
            {5,889,385,1266},    // Playground
            {570,891,936,1250},  // Color garden
            {351,614,613,921},   // Playlumi friend in square
            {730,83,940,190},    // Parent zone
            {0,1467,177,1668},   // Missions
            {179,1467,365,1668}, // Rewards
            {366,1434,574,1668}, // Home
            {575,1467,759,1668}, // Collection
            {761,1467,941,1668}  // Profile
    };
    private static final String[] NAMES={
            "Rekenraket","Letterhuis","Vormkasteel","Muziekstudio",
            "Speelplein","Kleurentuin","Stervriend","Ouderzone",
            "Missies","Beloningen","Home","Collectie","Profiel"
    };
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Listener callback;
    private Bitmap village;
    private int pressed=-1;
    private float downX,downY;
    private boolean tracking;
    private boolean animated;
    private final long started=SystemClock.uptimeMillis();

    public PlaylumiVillageView(Context context,Listener callback) {
        super(context);
        this.callback=callback;
        try(InputStream in=context.getAssets().open("playlumi_village.webp")) {
            village=BitmapFactory.decodeStream(in);
        } catch(IOException ignored) {
            // Missing asset is a packaging error; make it visible rather than crashing.
            village=null;
        }
        setFocusable(true);
        setContentDescription("Magisch Playlumi-leerdorp. Tik op een gebouw om een spel te openen.");
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
    }
    @Override protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height=Math.max(1,Math.round(width*ART_H/ART_W));
        setMeasuredDimension(width,height);
    }
    private float imageX(float screenX) { return screenX*ART_W/Math.max(1,getWidth()); }
    private float imageY(float screenY) { return screenY*ART_H/Math.max(1,getHeight()); }
    private int hit(float x,float y) {
        // The central star friend takes precedence where it overlaps the buildings.
        if(within(FRIEND,x,y))return FRIEND;
        // Navigation and parent area have priority over world scenery.
        for(int i=PARENTS;i<=PROFILE;i++)if(within(i,x,y))return i;
        for(int i=ROCKET;i<=COLORS;i++)if(within(i,x,y))return i;
        return -1;
    }
    private boolean within(int i,float x,float y) {
        float[] r=HIT[i];
        return x>=r[0]&&x<=r[2]&&y>=r[1]&&y<=r[3];
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float sx=getWidth()/ART_W,sy=getHeight()/ART_H;
        canvas.save();
        canvas.scale(sx,sy);
        if(village!=null&&!village.isRecycled()) {
            p.reset();p.setFilterBitmap(true);p.setAntiAlias(true);
            canvas.drawBitmap(village,null,new RectF(0,0,ART_W,ART_H),p);
        } else {
            p.setColor(0xff472c89);
            canvas.drawColor(0xff472c89);
            p.setColor(Color.WHITE);p.setTextSize(42);
            canvas.drawText("Playlumi dorp ontbreekt",100,780,p);
        }
        if(pressed>=0) {
            float[] r=HIT[pressed];
            float cx=(r[0]+r[2])*.5f,cy=(r[1]+r[3])*.5f;
            float radius=Math.max(r[2]-r[0],r[3]-r[1])*.65f;
            p.reset();p.setAntiAlias(true);p.setStyle(Paint.Style.STROKE);
            p.setColor(0xccfff1a0);p.setStrokeWidth(8);
            canvas.drawCircle(cx,cy,radius,p);
            p.setStyle(Paint.Style.FILL);
        }
        // A few subtle animated particles give the static town a living-game feel.
        if(village!=null && animated && !PlaylumiMotion.isReduced()) {
            long time=SystemClock.uptimeMillis()-started;
            for(int i=0;i<8;i++) {
                float phase=(float)(time/950.0+i*1.17);
                float x=85+(i*137)%800;
                float y=240+(i*241)%1120+(float)Math.sin(phase)*13f;
                float rad=3+(float)Math.sin(phase*1.5f)*1.7f;
                p.reset();p.setAntiAlias(true);
                p.setColor(0xbafff2af);
                canvas.drawCircle(x,y,Math.max(1f,rad),p);
            }
        }
        canvas.restore();
        if(animated && getWindowVisibility()==VISIBLE && !PlaylumiMotion.isReduced())
            postInvalidateDelayed(65);
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();animated=true;postInvalidate();
    }
    @Override protected void onDetachedFromWindow() {
        animated=false;super.onDetachedFromWindow();
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        float x=imageX(e.getX()),y=imageY(e.getY());
        switch(e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX=e.getX();downY=e.getY();
                pressed=hit(x,y);tracking=true;invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(e.getX()-downX)>25||Math.abs(e.getY()-downY)>25) {
                    pressed=-1;tracking=false;invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                int chosen=hit(x,y);
                int first=pressed;
                pressed=-1;invalidate();
                if(tracking && chosen>=0 && chosen==first) {
                    performClick();
                    callback.visit(chosen);
                }
                tracking=false;
                return true;
            case MotionEvent.ACTION_CANCEL:
                pressed=-1;tracking=false;invalidate();return true;
            default:return true;
        }
    }
    @Override public boolean performClick() {
        super.performClick();
        return true;
    }
}
