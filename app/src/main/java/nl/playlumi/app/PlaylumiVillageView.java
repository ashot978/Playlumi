package nl.playlumi.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/**
 * Offline interactive game village. Entire scene is drawn natively: no external
 * image required. Each building opens a real mini-game in MainActivity.
 */
public final class PlaylumiVillageView extends View {
    public interface Listener { void visit(int place); }
    public static final int ROCKET=0, BOOKS=1, SHAPES=2, MUSIC=3,
            PLAYGROUND=4, COLORS=5, FRIEND=6, PARENTS=7,
            MISSIONS=8, REWARDS=9, HOME=10, COLLECTION=11, PROFILE=12;
    private static final float W=941f,H=1672f;
    private static final String[] NAMES={
            "Rekenraket","Letterhuis","Vormkasteel","Muziekstudio",
            "Speelplein","Kleurentuin"
    };
    private static final String[] SYMBOLS={"1+2","Aa","△ ○","♫","★","🎨"};
    private static final int[] PALETTE={
            0xffe8caff,0xffffe5ae,0xffc9eaff,0xfffbc8e8,0xffffd7b5,0xffc8f3d7
    };
    private static final float[][] HIT={
            {40,340,447,720},{491,340,903,720},
            {40,746,447,1135},{491,746,903,1135},
            {40,1155,447,1480},{491,1155,903,1480},
            {380,185,560,326},{706,78,925,188},
            {0,1506,180,1672},{182,1506,367,1672},
            {368,1506,575,1672},{576,1506,760,1672},
            {761,1506,941,1672}
    };
    private final Paint pen=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Listener listener;
    private final SharedPreferences discoveryPrefs;
    private int discovered;
    private int pressed=-1;
    private boolean tracking;
    private float downX,downY;
    private long celebrationStarted;
    private int lastDiscovered=-1;
    private final long started=SystemClock.uptimeMillis();

    public PlaylumiVillageView(Context context,Listener listener,int profile) {
        super(context);
        this.listener=listener;
        discoveryPrefs=context.getSharedPreferences("playlumi-village-"+Math.max(0,profile),
                Context.MODE_PRIVATE);
        discovered=PlaylumiVillageProgress.sanitize(discoveryPrefs.getInt("visited",0));
        setFocusable(true);
        setClickable(true);
        updateDescription();
    }
    private void updateDescription() {
        setContentDescription("Playlumi magisch dorp, "+
                PlaylumiVillageProgress.visitedCount(discovered)+
                " van zes speelplekken bezocht. Tik op een gebouw om te spelen.");
    }
    @Override protected void onMeasure(int ws,int hs) {
        int width=MeasureSpec.getSize(ws);
        setMeasuredDimension(width,Math.max(1,Math.round(width*H/W)));
    }
    private void fill(int color) { pen.reset(); pen.setAntiAlias(true); pen.setColor(color); }
    private void rect(Canvas c,float l,float t,float r,float b,float rad,int color) {
        fill(color);c.drawRoundRect(l,t,r,b,rad,rad,pen);
    }
    private void circle(Canvas c,float x,float y,float rad,int color) {
        fill(color);c.drawCircle(x,y,rad,pen);
    }
    private void text(Canvas c,String s,float x,float y,int size,int color) {
        fill(color);
        pen.setTextSize(size);pen.setTypeface(android.graphics.Typeface.create(
                "sans-serif-rounded",android.graphics.Typeface.BOLD));
        pen.setTextAlign(Paint.Align.CENTER);
        c.drawText(s,x,y,pen);
    }
    private void ellipse(Canvas c,float x,float y,float rx,float ry,int color) {
        fill(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,pen);
    }
    private void hill(Canvas c,float x,float y,float rx,float ry,int color) {
        ellipse(c,x,y,rx,ry,color);
        ellipse(c,x-50,y-19,rx*.5f,ry*.42f,0x4487e9a4);
    }
    private void tree(Canvas c,float x,float y,float scale) {
        rect(c,x-9*scale,y-11*scale,x+9*scale,y+47*scale,6*scale,0xff9b6548);
        circle(c,x,y-28*scale,43*scale,0xff3cae72);
        circle(c,x-24*scale,y-43*scale,31*scale,0xff5ed58b);
        circle(c,x+17*scale,y-51*scale,29*scale,0xff7be6a2);
        circle(c,x+7*scale,y-20*scale,5*scale,0xffffdc80);
    }
    private void roof(Canvas c,float x,float y,float halfWidth,int color) {
        fill(color);
        Path roof=new Path();
        roof.moveTo(x-halfWidth,y+28);
        roof.lineTo(x,y-76);
        roof.lineTo(x+halfWidth,y+28);
        roof.close();
        c.drawPath(roof,pen);
    }
    private void cloud(Canvas c,float x,float y) {
        circle(c,x-55,y+8,37,0xcaffffff);
        circle(c,x,y-15,53,0xe5ffffff);
        circle(c,x+52,y+10,36,0xd6ffffff);
    }
    private void sparkle(Canvas c,float x,float y,float r,int color) {
        fill(color);
        Path p=new Path();
        p.moveTo(x,y-r);p.lineTo(x+r*.27f,y-r*.27f);
        p.lineTo(x+r,y);p.lineTo(x+r*.27f,y+r*.27f);
        p.lineTo(x,y+r);p.lineTo(x-r*.27f,y+r*.27f);
        p.lineTo(x-r,y);p.lineTo(x-r*.27f,y-r*.27f);p.close();
        c.drawPath(p,pen);
    }
    private void building(Canvas c,int i) {
        float[] hit=HIT[i];
        float x=(hit[0]+hit[2])*.5f;
        float top=hit[1]+27;
        int base=PALETTE[i];
        hill(c,x,top+220,188,78,0xff78ce94);
        ellipse(c,x,top+206,140,29,0x337b4b69);
        switch(i) {
            case ROCKET: {
                rect(c,x-114,top+87,x+119,top+208,28,0xff967bd2);
                rect(c,x-101,top+101,x+101,top+202,20,0xffd7c8ff);
                rect(c,x-49,top-62,x+49,top+174,24,0xfff8f8ff);
                roof(c,x,top-10,49,0xfff08da7);
                ellipse(c,x,top+38,28,31,0xff61a9e7);
                circle(c,x,top+38,15,0xffb8f4ff);
                rect(c,x-67,top+119,x-48,top+193,9,0xffe58b95);
                rect(c,x+48,top+119,x+67,top+193,9,0xffe58b95);
                sparkle(c,x,top+211,29,0xffffd151);break;
            }
            case BOOKS: {
                rect(c,x-124,top+60,x+124,top+207,21,0xffffe2ac);
                roof(c,x,top+58,141,0xffd977ba);
                rect(c,x-30,top+131,x+30,top+207,12,0xff9b5c9e);
                rect(c,x-92,top+105,x-52,top+149,9,0xfffff0bb);
                rect(c,x+52,top+105,x+92,top+149,9,0xfffff0bb);
                text(c,"A",x,top+66,58,0xff784c9b);break;
            }
            case SHAPES: {
                rect(c,x-115,top+42,x+115,top+213,18,0xffd1d5fc);
                for(int n=-1;n<=1;n++) {
                    float xx=x+n*100;
                    rect(c,xx-29,top+3,xx+29,top+124,10,0xffb7a1e8);
                    roof(c,xx,top+11,41,0xffaf72d7);
                }
                rect(c,x-28,top+131,x+28,top+211,15,0xff8469b9);
                text(c,"△ ○ □",x,top+117,37,0xff6f3eb1);break;
            }
            case MUSIC: {
                rect(c,x-127,top+51,x+127,top+215,38,0xffb27ae3);
                ellipse(c,x,top+53,128,76,0xffda9af3);
                rect(c,x-97,top+137,x+97,top+198,14,Color.WHITE);
                for(int n=0;n<5;n++)
                    rect(c,x-86+n*36,top+138,x-68+n*36,top+172,3,0xff4d3274);
                text(c,"♫",x,top+89,111,0xffffd86d);break;
            }
            case PLAYGROUND: {
                rect(c,x-125,top+102,x+125,top+222,21,0xffffefcf);
                roof(c,x,top+95,157,0xffff778d);
                rect(c,x-46,top+145,x+46,top+223,15,0xffecbce4);
                circle(c,x-65,top+188,20,0xff55c9f2);
                circle(c,x+72,top+194,21,0xffffb94f);
                text(c,"★",x,top+85,72,0xffffe565);break;
            }
            case COLORS: {
                rect(c,x-119,top+83,x+118,top+228,25,0xffb4e8bf);
                roof(c,x,top+88,138,0xffecb0d2);
                rect(c,x-33,top+155,x+37,top+226,12,0xfff9eac2);
                for(int n=0;n<5;n++)
                    circle(c,x-91+n*43,top+105+(n%2)*10,13,
                        new int[]{0xfff47d94,0xfff6ba50,0xff71bcea,0xffa68fe0,0xff8dd0a5}[n]);
                text(c,"✿",x,top+84,75,0xffed77a3);break;
            }
            default: break;
        }
        // A large readable banner is part of each clickable building.
        rect(c,x-174,hit[3]-101,x+174,hit[3]-31,32,0xfff9e9c7);
        rect(c,x-167,hit[3]-95,x+167,hit[3]-37,26,0xff633eab);
        text(c,NAMES[i],x,hit[3]-53,35,Color.WHITE);
        if(PlaylumiVillageProgress.isVisited(discovered,i)) {
            circle(c,x+135,top+55,24,0xfff6d352);
            text(c,"✓",x+135,top+66,31,0xff6944a6);
        }
    }
    private void nav(Canvas c,int place,String icon,String name,float x) {
        boolean selected=place==HOME;
        if(selected)circle(c,x,1554,61,0xffffd66e);
        text(c,icon,x,1567,53,selected?0xff7339a2:0xffffde93);
        text(c,name,x,1620,27,Color.WHITE);
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        c.save();
        c.scale(getWidth()/W,getHeight()/H);
        fill(Color.WHITE);
        pen.setShader(new LinearGradient(0,0,0,H,
                new int[]{0xff93bdff,0xffc8e9f9,0xffa3e3b9,0xff69bfa4},
                null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,W,H,pen);pen.setShader(null);
        cloud(c,130,220);cloud(c,790,275);
        circle(c,771,259,105,0x50fff0a9);
        for(int i=0;i<12;i++) {
            float xx=37+(i*163)%890,yy=245+(i*307)%1185;
            tree(c,xx,yy,.50f+(i%3)*.15f);
        }
        rect(c,22,28,925,170,48,0xff61389d);
        text(c,"★ Playlumi",261,114,70,0xffffe19f);
        rect(c,578,67,708,132,29,0xff8763b9);
        text(c,"★  "+PlaylumiVillageProgress.visitedCount(discovered),643,111,40,0xffffffff);
        rect(c,725,66,912,134,28,0xffffeed3);
        text(c,"Ouders",818,112,34,0xff603d91);
        sparkle(c,470,216,48,0xffffe27b);
        text(c,"Mijn magische dorp",W*.5f,292,59,0xff5e3492);
        text(c,"Tik op een gebouw om te spelen",W*.5f,339,32,0xff6a5492);
        // Two-column isometric-inspired level layout.
        for(int i=0;i<6;i++)building(c,i);
        rect(c,273,1412,668,1470,25,0xff6343a3);
        text(c,"Ontdekt: "+PlaylumiVillageProgress.visitedCount(discovered)+"/6",470,1454,37,0xffffe9aa);
        // Friendly star mascot in the center.
        circle(c,470,215,58,0xffffd352);
        text(c,"★",470,244,97,0xfffff1a9);
        rect(c,0,1494,941,1672,44,0xff523390);
        nav(c,MISSIONS,"☷","Missies",93);
        nav(c,REWARDS,"★","Beloningen",267);
        nav(c,HOME,"⌂","Home",470);
        nav(c,COLLECTION,"▣","Collectie",666);
        nav(c,PROFILE,"☺","Profiel",850);
        if(pressed>=0) {
            float[] r=HIT[pressed];
            fill(0xaaffec98);pen.setStyle(Paint.Style.STROKE);pen.setStrokeWidth(10);
            c.drawRoundRect(r[0]+5,r[1]+5,r[2]-5,r[3]-5,30,30,pen);
            pen.setStyle(Paint.Style.FILL);
        }
        if(lastDiscovered>=0&&!PlaylumiMotion.isReduced()) {
            long elapsed=SystemClock.uptimeMillis()-celebrationStarted;
            if(elapsed<1100) {
                float[] r=HIT[lastDiscovered];
                float x=(r[0]+r[2])*.5f;
                float y=(r[1]+r[3])*.5f;
                sparkle(c,x,y-110,36+elapsed*.09f,0xffffe29b);
                postInvalidateDelayed(55);
            } else lastDiscovered=-1;
        }
        c.restore();
    }
    private int hit(float x,float y) {
        for(int i=PARENTS;i<=PROFILE;i++)if(within(i,x,y))return i;
        for(int i=ROCKET;i<=COLORS;i++)if(within(i,x,y))return i;
        if(within(FRIEND,x,y))return FRIEND;
        return -1;
    }
    private boolean within(int i,float x,float y) {
        float[] r=HIT[i];
        return x>=r[0] && x<=r[2] && y>=r[1] && y<=r[3];
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        float x=e.getX()*W/Math.max(1,getWidth());
        float y=e.getY()*H/Math.max(1,getHeight());
        switch(e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX=e.getX();downY=e.getY();
                pressed=hit(x,y);tracking=true;invalidate();return true;
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(e.getX()-downX)>25 || Math.abs(e.getY()-downY)>25) {
                    pressed=-1;tracking=false;invalidate();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                pressed=-1;tracking=false;invalidate();return true;
            case MotionEvent.ACTION_UP:
                int chosen=hit(x,y),first=pressed;
                pressed=-1;invalidate();
                if(tracking && chosen>=0 && chosen==first) {
                    if(chosen<PlaylumiVillageProgress.BUILDINGS &&
                            !PlaylumiVillageProgress.isVisited(discovered,chosen)) {
                        discovered=PlaylumiVillageProgress.markVisited(discovered,chosen);
                        discoveryPrefs.edit().putInt("visited",discovered).apply();
                        lastDiscovered=chosen;
                        celebrationStarted=SystemClock.uptimeMillis();
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
