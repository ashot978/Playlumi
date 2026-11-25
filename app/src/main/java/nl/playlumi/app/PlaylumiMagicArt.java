package nl.playlumi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Actual approved Playlumi reference illustrations. The images are texture atlases,
 * never a screenshot with invisible hotspots. Each button, score and question is
 * a separate native Android view. Baked example answers are removed from skins.
 */
public final class PlaylumiMagicArt {
    public static final int INK=0xff3d195f, CREAM=0xfffff6dc, GOLD=0xffffdc88;
    private static final int[] PASTELS={0xffd9f3ff,0xffffe4bd,0xffe8d9ff,
            0xffd5f5df,0xffffdded,0xfffff1b6};
    private static final Map<String,Bitmap> SOURCES=new HashMap<>();
    private static final Map<String,Bitmap> SPRITES=new HashMap<>();
    private PlaylumiMagicArt() {}
    public static String assetName(int world) {
        switch(world) {
            case PlaylumiVillageHit.BOOKS:return "letters";
            case PlaylumiVillageHit.COLORS:return "colors";
            case PlaylumiVillageHit.MUSIC:return "music";
            case PlaylumiVillageHit.PLAYGROUND:return "memory";
            default:return "letters";
        }
    }
    private static synchronized Bitmap source(Context context,String name) {
        Bitmap found=SOURCES.get(name);
        if(found!=null)return found;
        try(InputStream in=context.getAssets().open("magic/"+name+".webp")) {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inPreferredConfig=Bitmap.Config.ARGB_8888;
            options.inScaled=false;
            found=BitmapFactory.decodeStream(in,null,options);
            if(found==null || found.getWidth()!=941 || found.getHeight()!=1672)
                throw new IllegalStateException("Invalid approved reference: "+name);
            SOURCES.put(name,found);return found;
        }catch(IOException e){throw new IllegalStateException("Missing approved reference: "+name,e);}
    }
    private static Bitmap crop(Bitmap b,int x1,int y1,int x2,int y2) {
        return Bitmap.createBitmap(b,x1,y1,x2-x1,y2-y1);
    }
    private static int blend(int a,int b,float t) {
        t=Math.max(0f,Math.min(1f,t));
        return Color.rgb(Math.round(Color.red(a)*(1-t)+Color.red(b)*t),
                Math.round(Color.green(a)*(1-t)+Color.green(b)*t),
                Math.round(Color.blue(a)*(1-t)+Color.blue(b)*t));
    }
    // Clear only the sample text on a gradient/parchment face, keeping the
    // original gloss, bevel, golden edge, stars, and illustrated surroundings.
    private static void clearExample(Bitmap b,int l,int t,int r,int bot) {
        l=Math.max(9,l);r=Math.min(b.getWidth()-9,r);
        t=Math.max(0,t);bot=Math.min(b.getHeight(),bot);
        int fade=8;
        for(int y=t;y<bot;y++) {
            int a=b.getPixel(l-3,y),z=b.getPixel(r+3,y);
            for(int x=l;x<r;x++){
                float mix=(float)(x-l)/Math.max(1,r-l-1);
                float f=Math.min(1f,Math.min(Math.min(x-l,r-1-x),Math.min(y-t,bot-1-y))/(float)fade);
                b.setPixel(x,y,blend(b.getPixel(x,y),blend(a,z,mix),f));
            }
        }
    }
    private static synchronized Bitmap cleaned(Context c,String id,String src,int x1,int y1,int x2,int y2,
                                                int l,int t,int r,int bot) {
        Bitmap b=SPRITES.get(id);
        if(b!=null)return b;
        b=crop(source(c,src),x1,y1,x2,y2).copy(Bitmap.Config.ARGB_8888,true);
        clearExample(b,l-x1,t-y1,r-x1,bot-y1);
        SPRITES.put(id,b);
        return b;
    }
    public static int nearestPastel(int color) {
        int best=0,dist=Integer.MAX_VALUE;
        for(int i=0;i<PASTELS.length;i++){
            int dr=Color.red(color)-Color.red(PASTELS[i]),
                    dg=Color.green(color)-Color.green(PASTELS[i]),
                    db=Color.blue(color)-Color.blue(PASTELS[i]);
            int d=dr*dr+dg*dg+db*db;
            if(d<dist){dist=d;best=i;}
        }
        return best;
    }
    public static Drawable pastel(Context c,int index) {
        // Blue, peach, purple, mint, pink, and gold faces from Letterhuis.
        int[][] rects={{103,1305,465,1434},{475,1305,835,1434},
                {103,1030,465,1158},{475,1030,835,1158},
                {103,1166,465,1293},{475,1166,835,1293}};
        int i=((index%rects.length)+rects.length)%rects.length;int[] q=rects[i];
        Bitmap b=cleaned(c,"pill"+i,"letters",q[0],q[1],q[2],q[3],
                q[0]+92,q[1]+20,q[2]-92,q[3]-16);
        return new Skin(b,true,.41f);
    }
    public static Drawable primary(Context c) {
        Bitmap b=cleaned(c,"primary","colors",196,1441,753,1586,
                264,1471,707,1556);
        return new Skin(b,true,.39f);
    }
    public static Drawable nav(Context c,boolean home) {
        int[] r=home?new int[]{792,15,929,151}:new int[]{19,16,150,151};
        Bitmap b=crop(source(c,"letters"),r[0],r[1],r[2],r[3]);
        return new Skin(b,false,.44f);
    }
    public static Drawable ribbon(Context c) {
        Bitmap b=cleaned(c,"ribbon","letters",47,535,899,718,
                183,578,769,660);
        return new Skin(b,false,.08f);
    }
    private static GradientDrawable sculpted(Context c,int[] colors,float radius,int stroke,int border){
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
        float density=c.getResources().getDisplayMetrics().density;
        d.setCornerRadius(radius*density);
        d.setStroke(Math.max(1,Math.round(stroke*density)),border);
        return d;
    }
    /** Scalable real rounded surfaces, not stretched rectangular textures. */
    public static Drawable softPanel(Context c){
        return sculpted(c,new int[]{0xe638235c,0xec4b326f,0xe62f1e50},30,1,0x90c7a1ef);
    }
    public static Drawable silkRibbon(Context c){
        return sculpted(c,new int[]{0xff8854c0,0xff62308d,0xff442162},24,2,0xffffd382);
    }
    public static Drawable parchment(Context c){
        return sculpted(c,new int[]{0xfffff8dc,0xffffe7b4,0xfffff2d3},30,3,0xffeeb45b);
    }
    public static Drawable gemButton(Context c,int index){
        int base=PASTELS[((index%PASTELS.length)+PASTELS.length)%PASTELS.length];
        int edge=blend(base,0xff9672bd,.42f);
        GradientDrawable outer=sculpted(c,new int[]{0xfffffff9,base,blend(base,0xff8d65b0,.24f)},29,2,edge);
        GradientDrawable inner=sculpted(c,new int[]{blend(base,Color.WHITE,.75f),base,blend(base,0xffd5bce8,.12f)},25,1,0xcaffffff);
        android.graphics.drawable.LayerDrawable layers=new android.graphics.drawable.LayerDrawable(new Drawable[]{outer,inner});
        int inset=Math.max(2,Math.round(c.getResources().getDisplayMetrics().density*3));
        layers.setLayerInset(1,inset,inset,inset,inset);
        return layers;
    }
    public static Drawable creamPlate(Context c) {
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xfffffff3,0xffffedcf,0xffffdfb6});
        d.setCornerRadius(c.getResources().getDisplayMetrics().density*24);
        d.setStroke(Math.max(2,(int)(2.5f*c.getResources().getDisplayMetrics().density)),0xffffc45c);
        return d;
    }
    public static Drawable question(Context c,boolean scene) {
        Bitmap b=scene?
                cleaned(c,"questionScene","letters",0,705,941,1014,383,756,563,948):
                cleaned(c,"parchment","letters",271,710,663,985,382,756,563,948);
        return new Skin(b,false,scene?.025f:.13f);
    }
    /** Keep the real shiny paint-splash from the approved artwork, tint only paint pixels.
     * Neither the sample yellow nor the sample question appears as fixed game data. */
    public static Bitmap colorQuestion(Context c,int color,boolean scenic) {
        Bitmap original=source(c,"colors");
        int x0=scenic?0:255,y0=scenic?680:681,x1=scenic?941:705,y1=scenic?1011:1000;
        Bitmap b=crop(original,x0,y0,x1,y1).copy(Bitmap.Config.ARGB_8888,true);
        float[] hsv=new float[3],target=new float[3];
        Color.colorToHSV(color,target);
        for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++){
            int ax=x+x0,ay=y+y0;
            boolean splash=ax>=324 && ax<=658 && ay>=720 && ay<=955;
            boolean jar=scenic && ax>=766 && ax<=899 && ay>=826 && ay<=980;
            if(!splash && !jar)continue;
            int old=b.getPixel(x,y);
            Color.colorToHSV(old,hsv);
            if(hsv[0]>=34f && hsv[0]<=70f && hsv[1]>.56f){
                float strength=Math.min(1f,(hsv[1]-.56f)/.20f);
                float shade=Math.max(.65f,hsv[2]);
                float sat=Math.max(.50f,target[1]*.92f);
                int tone=Color.HSVToColor(new float[]{target[0],sat,
                        Math.min(1f,Math.max(.32f,target[2]*shade+.13f))});
                b.setPixel(x,y,blend(old,tone,strength));
            }
        }
        return b;
    }
    public static Drawable animal(Context c,int index) {
        int[][] rr={{51,930,464,1235},{478,930,895,1235},
                {51,1244,464,1551},{478,1244,895,1551}};
        int i=((index%4)+4)%4;int[] q=rr[i];
        // Remove the fixed label, retain the painted animal and its golden nameplate.
        Bitmap b=cleaned(c,"animal"+i,"music",q[0],q[1],q[2],q[3],
                q[0]+74,q[1]+207,q[2]-72,q[1]+268);
        return new Skin(b,false,.13f);
    }
    public static Drawable memory(Context c,int index,boolean face,boolean matched) {
        int[][] rr={{39,760,319,949},{325,760,606,949},{612,760,896,949},
                {39,958,319,1148},{325,958,606,1148},{612,958,896,1148}};
        int i=((index%6)+6)%6;
        if(matched)i=5;
        int[] q=rr[i];
        Bitmap b;
        if(face) {
            b=cleaned(c,"memoryface"+i,"memory",q[0],q[1],q[2],q[3],
                    q[0]+76,q[1]+40,q[2]-76,q[3]-32);
        } else {
            b=crop(source(c,"memory"),q[0],q[1],q[2],q[3]);
        }
        return new Skin(b,false,.17f);
    }
    public static View hero(Context context,int world) {return new Hero(context,world);}
    public static View backdrop(Context context,int world) {return new Backdrop(context,world);}
    private static Rect heroRect(int world) {
        switch(world){
            case PlaylumiVillageHit.BOOKS:return new Rect(0,137,941,431);
            case PlaylumiVillageHit.COLORS:return new Rect(0,135,941,417);
            case PlaylumiVillageHit.MUSIC:return new Rect(0,127,941,437);
            case PlaylumiVillageHit.PLAYGROUND:return new Rect(0,135,941,478);
            case PlaylumiVillageHit.ROCKET:return new Rect(0,222,514,481);
            case PlaylumiVillageHit.SHAPES:return new Rect(0,505,350,786);
            default:return PlaylumiWorldBackdrop.crop(world);
        }
    }
    private static boolean hasApprovedHero(int world) {
        return world==PlaylumiVillageHit.BOOKS || world==PlaylumiVillageHit.COLORS ||
                world==PlaylumiVillageHit.MUSIC || world==PlaylumiVillageHit.PLAYGROUND;
    }
    private static final class Hero extends View {
        private final Bitmap art;private final Rect src;private final int world;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        Hero(Context c,int w){
            super(c);world=w;art=hasApprovedHero(w)?source(c,assetName(w)):PlaylumiWorldBackdrop.art(c);
            src=heroRect(w);
            setContentDescription("Geillustreerde spelwereld "+PlaylumiWorldStyle.NAMES[w]);
        }
        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
            RectF box=new RectF(0,0,w,h);
            float radius=Math.min(h*.22f,26*getResources().getDisplayMetrics().density);
            Path clip=new Path();clip.addRoundRect(box,radius,radius,Path.Direction.CW);
            c.save();c.clipPath(clip);
            p.setColor(0xff392360);c.drawRect(box,p);
            if(!hasApprovedHero(world)&&w/h>3.25f){
                // Show the entire building in a dedicated rounded illustrated
                // hero rather than zooming into a tiny crop across a tablet.
                p.setShader(new LinearGradient(0,0,w,h,new int[]{0xff314969,0xff442764,0xff744a9c},null,Shader.TileMode.CLAMP));
                c.drawRect(box,p);p.setShader(null);
                float imageW=h*src.width()/src.height(),imageH=h;
                float x=Math.max(0,(w*.48f-imageW)/2);
                c.drawBitmap(art,src,new RectF(x,0,x+imageW,imageH),p);
                p.setShader(new LinearGradient(x+imageW*.82f,0,x+imageW,0,0x00442764,0xff442764,Shader.TileMode.CLAMP));
                c.drawRect(x+imageW*.82f,0,x+imageW,h,p);p.setShader(null);
                float tx=w*.73f;
                p.setColor(0xffffdd89);p.setTypeface(android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD));
                p.setTextAlign(Paint.Align.CENTER);p.setTextSize(Math.min(h*.22f,w*.035f));
                c.drawText(PlaylumiWorldStyle.NAMES[world],tx,h*.50f,p);
                p.setTextSize(h*.18f);p.setColor(0xfff4d5ff);
                c.drawText("✦  Jouw sterrenavontuur  ✦",tx,h*.74f,p);
                c.restore();
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(0xffffd88b);
                c.drawRoundRect(1,1,w-1,h-1,radius,radius,p);p.setStyle(Paint.Style.FILL);
                return;
            }
            // On a folded/unfolded screen, cover the full banner with scenery,
            // preserving proportions rather than stretching the building.
            float scale=Math.max(w/src.width(),h/src.height());
            float dw=src.width()*scale,dh=src.height()*scale;
            c.drawBitmap(art,src,new RectF((w-dw)/2,(h-dh)/2,(w+dw)/2,(h+dh)/2),p);
            c.restore();
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(0xffffd88b);
            c.drawRoundRect(1,1,w-1,h-1,radius,radius,p);p.setStyle(Paint.Style.FILL);
        }
    }
    private static final class Backdrop extends View {
        final Bitmap art;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        final int floorStart;
        Backdrop(Context c,int w){
            super(c);art=source(c,assetName(w));
            floorStart=w==PlaylumiVillageHit.COLORS?1592:1575;
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            setClickable(false);setFocusable(false);
        }
        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
            p.setShader(new LinearGradient(0,0,0,h,new int[]{0xff251748,0xff503080,0xff63468e,0xff3b225f},null,Shader.TileMode.CLAMP));
            c.drawRect(0,0,w,h,p);p.setShader(null);
            if(getResources().getConfiguration().screenWidthDp>=600){
                // A coherent starry sky with a soft illustrated garden footer.
                // No stretched side strips or hard rectangular transitions.
                p.setShader(new android.graphics.RadialGradient(w*.5f,h*.42f,
                        Math.max(w,h)*.74f,new int[]{0xff6b4b9b,0xff493170,0xff281943},null,Shader.TileMode.CLAMP));
                c.drawRect(0,0,w,h,p);p.setShader(null);
                Bitmap garden=PlaylumiWorldBackdrop.art(getContext());
                Rect gardenSrc=new Rect(0,1240,941,1454);
                float footer=Math.min(h*.20f,w*.22f);
                c.drawBitmap(garden,gardenSrc,new RectF(0,h-footer,w,h),p);
                p.setShader(new LinearGradient(0,h-footer,0,h,
                        new int[]{0xff32204f,0xe632204f,0x9032204f,0x1232204f},
                        new float[]{0,.22f,.58f,1f},Shader.TileMode.CLAMP));
                c.drawRect(0,h-footer,w,h,p);p.setShader(null);
                float density=getResources().getDisplayMetrics().density;
                for(int i=0;i<35;i++){
                    float x=((i*97+17)%101)/100f*w;
                    float y=((i*61+29)%101)/100f*(h-footer*.4f);
                    float starRadius=(i%7==0?3.8f:i%3==0?1.8f:1.1f)*density;
                    p.setColor(i%7==0?0x8fffe2a1:0x4cfff3d0);
                    if(i%7==0){
                        Path star=new Path();
                        star.moveTo(x,y-starRadius*1.6f);star.lineTo(x+starRadius*.35f,y-starRadius*.35f);
                        star.lineTo(x+starRadius*1.6f,y);star.lineTo(x+starRadius*.35f,y+starRadius*.35f);
                        star.lineTo(x,y+starRadius*1.6f);star.lineTo(x-starRadius*.35f,y+starRadius*.35f);
                        star.lineTo(x-starRadius*1.6f,y);star.lineTo(x-starRadius*.35f,y-starRadius*.35f);
                        star.close();c.drawPath(star,p);
                    }else c.drawCircle(x,y,starRadius,p);
                }
                return;
            }
            float edge=Math.min(w*.053f,55*getResources().getDisplayMetrics().density);
            // Only foliage and the stone-path footer are sampled: no baked
            // buttons, letters, scores, or prompts in the background.
            c.drawBitmap(art,new Rect(0,485,43,1672),new RectF(0,0,edge,h),p);
            c.drawBitmap(art,new Rect(898,485,941,1672),new RectF(w-edge,0,w,h),p);
            float floorH=Math.min(h*.105f,w*.11f);
            c.drawBitmap(art,new Rect(0,floorStart,941,1672),new RectF(0,h-floorH,w,h),p);
            p.setShader(new LinearGradient(0,h-floorH,0,h-floorH*.55f,0xff503080,0x00503080,Shader.TileMode.CLAMP));
            c.drawRect(edge,h-floorH,w-edge,h-floorH*.55f,p);p.setShader(null);
            p.setColor(0x66fff0b2);
            for(int i=0;i<12;i++){
                float x=edge+((i*97+39)%100)/100f*Math.max(1,w-2*edge);
                float y=((i*61+21)%100)/100f*h;
                c.drawCircle(x,y,(i%3==0?2.4f:1.3f)*getResources().getDisplayMetrics().density,p);
            }
        }
    }
    /** Texture skin with horizontal stretch caps, so glossy button ends do not flatten on a tablet. */
    private static final class Skin extends Drawable {
        final Bitmap b;final boolean stretchCaps;final float radiusFraction;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        Skin(Bitmap b,boolean stretch,float radius){this.b=b;stretchCaps=stretch;radiusFraction=radius;}
        @Override public void draw(Canvas c) {
            Rect q=getBounds();float w=q.width(),h=q.height();if(w<=0||h<=0)return;
            RectF dest=new RectF(q);float rad=Math.min(w*.25f,h*radiusFraction);
            Path mask=new Path();mask.addRoundRect(dest,rad,rad,Path.Direction.CW);
            c.save();c.clipPath(mask);
            if(stretchCaps && w>h*1.6f){
                int cap=Math.min(b.getWidth()/3,Math.round(b.getHeight()*.46f));
                float end=Math.min(w/3,h*.49f);
                c.drawBitmap(b,new Rect(0,0,cap,b.getHeight()),new RectF(q.left,q.top,q.left+end,q.bottom),p);
                c.drawBitmap(b,new Rect(cap,0,b.getWidth()-cap,b.getHeight()),new RectF(q.left+end,q.top,q.right-end,q.bottom),p);
                c.drawBitmap(b,new Rect(b.getWidth()-cap,0,b.getWidth(),b.getHeight()),new RectF(q.right-end,q.top,q.right,q.bottom),p);
            }else c.drawBitmap(b,null,dest,p);
            c.restore();
        }
        @Override public void setAlpha(int value){p.setAlpha(value);invalidateSelf();}
        @Override public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);invalidateSelf();}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
}
