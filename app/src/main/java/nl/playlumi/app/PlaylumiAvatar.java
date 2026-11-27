package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/** Original illustrated fantasy avatar medallions. Character choice is cosmetic. */
public final class PlaylumiAvatar extends View {
    private final int avatar;
    private final PlaylumiWardrobe wardrobe;
    private final int[] drawnOutfit=new int[PlaylumiWardrobe.SLOTS];
    // Geometry is immutable after construction. The paint is per drawing thread,
    // so the static village/room renderer can reuse it without sharing mutation.
    private static final ThreadLocal<Paint> PAINT=new ThreadLocal<Paint>(){
        @Override protected Paint initialValue(){return new Paint(Paint.ANTI_ALIAS_FLAG);}
    };
    private static final Path CIRCLE=circle(),LUMI=lumiSilhouette(),CAPE=cape(),
            CAPE_LIGHT_FOLDS=capeFolds(false),CAPE_DARK_FOLDS=capeFolds(true),
            NECK=neck(),GARMENT=garment(),SCARF=scarf();
    private static final Path BOW_LEFT=shape(39,69,47,72,47,77,38,79),
            BOW_RIGHT=shape(61,69,53,72,53,77,62,79),SCARF_TAIL=shape(34,70,42,73,38,87,29,84),
            FOX_EAR_LEFT=shape(22,45,19,16,40,32),FOX_EAR_RIGHT=shape(78,45,81,16,60,32),
            FOX_INNER_LEFT=shape(24,22,35,37,25,40),FOX_INNER_RIGHT=shape(76,22,65,37,75,40),
            WIZARD_HAT=shape(21,37,53,9,79,39),
            WIZARD_STAR=shape(53,18,56,24,63,25,58,30,59,36,53,33,47,36,48,30,43,25,50,24);
    private static final Path[] SPARKLES=sparkles(),AURA_MARKS=auraMarks(),EMBROIDERY={
            smallStar(24,42,2.6f),smallStar(77,43,2.7f),smallStar(35,71,2.5f),smallStar(65,72,2.7f)};
    private static final int[] RAINBOW_STRIPES={0xffeda1bc,0xfff3bd9b,0xfff9dda1,
            0xffb7d6af,0xffa5cfdf,0xffb7b4df,0xffd6b0dc};
    private static final RectF SMILE=new RectF(43,57,57,71),LUMI_SMILE=new RectF(43,55,57,69),
            FACE_OPENING=new RectF(26,41,74,76),
            VISOR=new RectF(23,8,77,77),VISOR_GLINT_LEFT=new RectF(27,12,73,74),
            VISOR_GLINT_RIGHT=new RectF(28,13,72,73);
    private static final LinearGradient MEDALLION=new LinearGradient(0,6,0,94,0xfff8dcff,0xff8a5fd5,Shader.TileMode.CLAMP),
            GOLDEN_BODY=new LinearGradient(0,15,0,88,0xfffff4a0,0xffffb52c,Shader.TileMode.CLAMP),
            CAPE_FABRIC=cloth(0xfff77d9b,0xffaa3267),SCARF_FABRIC=cloth(0xffabd9ea,0xff638cae),
            STAR_FABRIC=cloth(0xff7b79c8,0xff424879),RAINBOW_FABRIC=cloth(0xfff1b6d4,0xffa78dc6),
            SPACE_FABRIC=cloth(0xffffffff,0xffb9d2e5);
    public PlaylumiAvatar(Context context,int avatar){this(context,avatar,null);}
    public PlaylumiAvatar(Context context,int avatar,PlaylumiWardrobe wardrobe) {
        super(context);this.avatar=PlaylumiWorldStyle.safeAvatar(avatar);this.wardrobe=wardrobe;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    public void react(int kind) {
        if(PlaylumiMotion.isReduced())return;
        switch(kind){case 1:PlaylumiDelight.hop(this);break;case 2:PlaylumiDelight.wiggle(this);break;
            case 3:PlaylumiDelight.wiggle(this);break;case 4:PlaylumiMotion.pop(this);break;
            case 5:PlaylumiDelight.spin(this);break;default:PlaylumiMotion.pop(this);}
    }
    public void dance(){react(1);}
    public void wave(){react(3);}
    public void surprised(){react(4);}
    public void twirl(){react(5);}
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);float size=Math.min(getWidth(),getHeight());
        if(wardrobe!=null)for(int i=0;i<drawnOutfit.length;i++)drawnOutfit[i]=wardrobe.equipped(i);
        draw(canvas,(getWidth()-size)/2,(getHeight()-size)/2,size,avatar,wardrobe==null?null:drawnOutfit);
    }
    private static void oval(Canvas c,Paint p,float l,float t,float r,float b,int color) {
        p.setColor(color);c.drawOval(l,t,r,b,p);
    }
    private static Path shape(float... xy) {
        Path path=new Path();path.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);
        path.close();return path;
    }
    private static void painted(Canvas c,Paint p,Path path,int color){
        p.setColor(color);c.drawPath(path,p);
    }
    private static Path circle(){Path result=new Path();result.addCircle(50,50,49,Path.Direction.CW);return result;}
    private static LinearGradient cloth(int light,int dark){
        return new LinearGradient(15,37,83,86,light,dark,Shader.TileMode.CLAMP);
    }
    private static Path[] sparkles(){
        Path[] result=new Path[6];
        for(int i=0;i<result.length;i++){
            float x=15+(i*23)%72,y=16+(i*29)%67;
            result[i]=shape(x,y-4,x+1,y-1,x+4,y,x+1,y+1,x,y+4,x-1,y+1,x-4,y,x-1,y-1);
        }
        return result;
    }
    private static Path[] auraMarks(){
        Path[] result=new Path[8];
        for(int i=0;i<result.length;i++){
            double a=i*Math.PI/4;float x=50+(float)Math.cos(a)*37,y=50+(float)Math.sin(a)*37;
            result[i]=shape(x,y-4,x+3,y,x,y+4,x-3,y);
        }
        return result;
    }
    private static Path cape(){
        Path result=new Path();result.moveTo(31,42);result.cubicTo(36,47,64,47,69,42);
        result.cubicTo(72,57,81,73,88,88);result.quadTo(77,87,70,82);
        result.quadTo(61,88,50,86);result.quadTo(39,88,30,82);
        result.quadTo(22,88,12,88);result.cubicTo(20,72,28,55,31,42);result.close();return result;
    }
    private static Path capeFolds(boolean dark){
        Path result=new Path();
        if(dark){result.moveTo(41,52);result.quadTo(40,74,39,83);result.moveTo(59,52);result.quadTo(60,74,61,83);}
        else{result.moveTo(33,47);result.quadTo(27,70,23,84);result.moveTo(67,47);result.quadTo(73,69,77,84);}
        return result;
    }
    private static Path neck(){
        Path result=new Path();result.moveTo(12,37);result.lineTo(31,40);
        result.cubicTo(34,48,35,61,50,69);result.cubicTo(65,61,66,48,69,40);result.lineTo(88,37);return result;
    }
    private static Path garment(){
        Path result=new Path(NECK);result.lineTo(92,89);result.lineTo(8,89);result.close();return result;
    }
    private static Path scarf(){
        Path result=new Path();result.moveTo(33,66);result.quadTo(49,74,66,66);
        result.lineTo(66,72);result.quadTo(51,81,32,73);result.close();return result;
    }
    public static void draw(Canvas c,float x,float y,float size,int choice){draw(c,x,y,size,choice,null);}
    private static int item(int[] outfit,int slot){
        return outfit!=null&&outfit.length>slot?Math.max(0,Math.min(PlaylumiWardrobe.OPTIONS-1,outfit[slot])):0;
    }
    private static void accessory(Canvas c,Paint p,String icon,float x,float y,float size){
        p.setShader(null);p.setColor(0xffffffff);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);
        p.setTypeface(android.graphics.Typeface.DEFAULT);c.drawText(icon,x,y,p);
    }
    private static Path lumiSilhouette(){
        Path star=new Path();
        for(int n=0;n<10;n++){
            double angle=-Math.PI/2+n*Math.PI/5;
            float r=n%2==0?39:21;
            float sx=50+(float)Math.cos(angle)*r,sy=50+(float)Math.sin(angle)*r;
            if(n==0)star.moveTo(sx,sy);else star.lineTo(sx,sy);
        }
        star.close();return star;
    }
    private static void outline(Canvas c,Paint p,Path path,int color,float width){
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);
        c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
    }
    private static void fabric(Canvas c,Paint p,Path shape,LinearGradient gradient){
        p.setStyle(Paint.Style.FILL);p.setShader(gradient);
        c.drawPath(shape,p);p.setShader(null);
    }
    /** Back pieces are behind Lumi, so neither cape nor backpack can cover the face. */
    private static void costumeBack(Canvas c,Paint p,int clothes){
        if(clothes==2){
            fabric(c,p,CAPE,CAPE_FABRIC);outline(c,p,CAPE,0xff812752,1.5f);
            outline(c,p,CAPE_LIGHT_FOLDS,0xffdc9db3,1.5f);outline(c,p,CAPE_DARK_FOLDS,0xff922e5d,1.3f);
        }else if(clothes==6){
            p.setColor(0xff7185aa);c.drawRoundRect(21,44,79,77,8,8,p);
            p.setColor(0xffd0ddeb);c.drawRoundRect(24,46,76,76,6,6,p);
            p.setColor(0xff536e97);c.drawRoundRect(22,52,28,69,3,3,p);c.drawRoundRect(72,52,78,69,3,3,p);
            p.setColor(0xffffd187);c.drawRoundRect(24,55,26,61,1,1,p);c.drawRoundRect(74,55,76,61,1,1,p);
        }
    }
    /** Tailored arms and lower points keep Lumi's head and smiling face golden. */
    private static void costumeOnStar(Canvas c,Paint p,Path star,int clothes){
        if(clothes==0)return;
        if(clothes==2){
            // Two shoulder clasps attach the cape; no second hero or emoji badge.
            oval(c,p,29,40,36,47,0xffffedb6);oval(c,p,64,40,71,47,0xffffedb6);
            oval(c,p,31,42,34,45,0xffb88349);oval(c,p,66,42,69,45,0xffb88349);
            return;
        }
        if(clothes==1){
            painted(c,p,BOW_LEFT,0xffb3477d);painted(c,p,BOW_RIGHT,0xffdf83b0);
            p.setColor(0xffefb0cb);c.drawRoundRect(47,71,53,77,2,2,p);
            return;
        }
        if(clothes==3){
            // Keep the full scarf below the raised smile instead of painting gold over it.
            c.save();c.translate(0,2);
            fabric(c,p,SCARF,SCARF_FABRIC);painted(c,p,SCARF_TAIL,0xff87b8d2);
            p.setColor(0xffd1e6ed);p.setStrokeWidth(1.5f);c.drawLine(31,81,38,83,p);
            c.restore();
            return;
        }
        if(clothes<4)return;
        c.save();c.clipPath(star);
        if(clothes==4){
            fabric(c,p,GARMENT,STAR_FABRIC);outline(c,p,NECK,0xffffdda0,1.8f);
            // Gold cuffs and seams make a suit, while the tiny motifs stay decorative.
            p.setColor(0xffffdc94);p.setStrokeWidth(3);c.drawLine(21,38,26,48,p);c.drawLine(79,38,74,48,p);
            c.drawLine(28,77,36,79,p);c.drawLine(64,79,72,77,p);
            p.setColor(0xffadb5e4);p.setStrokeWidth(1);c.drawLine(34,67,31,76,p);c.drawLine(66,67,69,76,p);
            for(Path motif:EMBROIDERY)painted(c,p,motif,0xffffe7a9);
        }else if(clothes==5){
            fabric(c,p,GARMENT,RAINBOW_FABRIC);c.save();c.clipPath(GARMENT);
            for(int i=0;i<RAINBOW_STRIPES.length;i++){
                p.setColor(RAINBOW_STRIPES[i]);c.drawRect(10,37+i*7.5f,90,44.5f+i*7.5f,p);
            }
            p.setColor(0x50ffffff);p.setStrokeWidth(.7f);
            for(int row=0;row<7;row++)for(int col=0;col<13;col++){
                float sx=13+col*6,sy=40+row*7.5f;
                c.drawLine(sx-1,sy-1,sx,sy+1,p);c.drawLine(sx,sy+1,sx+1,sy-1,p);
            }
            c.restore();outline(c,p,NECK,0xffb97fae,2.3f);
            p.setColor(0xffc18ab7);p.setStrokeWidth(3);c.drawLine(21,38,26,48,p);c.drawLine(79,38,74,48,p);
            c.drawLine(28,78,36,80,p);c.drawLine(64,80,72,78,p);
        }else{
            fabric(c,p,GARMENT,SPACE_FABRIC);outline(c,p,NECK,0xff809fbe,2);
            p.setColor(0xff617aa3);p.setStrokeWidth(4);c.drawLine(20,38,25,47,p);c.drawLine(80,38,75,47,p);
            c.drawLine(27,77,36,80,p);c.drawLine(64,80,73,77,p);
            p.setColor(0xff8fabc5);p.setStrokeWidth(1.2f);c.drawLine(34,65,31,76,p);c.drawLine(66,65,69,76,p);
            p.setColor(0xffd3a56a);c.drawRoundRect(24,43,29,48,1.3f,1.3f,p);
            p.setColor(0xff7daed2);c.drawRoundRect(71,43,76,48,1.3f,1.3f,p);
            oval(c,p,25,44,27,46,0xffffe3aa);oval(c,p,73,44,75,46,0xffe0f3ff);
        }
        c.restore();p.setShader(null);p.setStyle(Paint.Style.FILL);
    }
    private static Path smallStar(float x,float y,float r){
        Path s=new Path();
        for(int i=0;i<10;i++){
            double a=-Math.PI/2+i*Math.PI/5;float radius=i%2==0?r:r*.45f;
            float px=x+(float)Math.cos(a)*radius,py=y+(float)Math.sin(a)*radius;
            if(i==0)s.moveTo(px,py);else s.lineTo(px,py);
        }
        s.close();return s;
    }
    private static void goldenFace(Canvas c,Paint p,int clothes){
        // Bow, scarf and cape already leave the golden face open. Repainting it
        // would hide their upper edges; only the three full suits need an opening.
        if(clothes<4)return;
        // Keep the same golden body gradient under the cheeks and entire smile.
        // The silhouette clip protects the two lower star points and leaves the
        // fabric, cuffs and accessories visibly around the rounded face opening.
        c.save();c.clipPath(LUMI);p.setStyle(Paint.Style.FILL);p.setColor(0xffffffff);p.setShader(GOLDEN_BODY);
        c.drawOval(FACE_OPENING,p);p.setShader(null);c.restore();
    }
    /** Transparent visor with a rim and off-face glints; the face is never covered. */
    private static void spaceHelmet(Canvas c,Paint p){
        p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4.8f);p.setColor(0xff7895b7);c.drawOval(VISOR,p);
        p.setStrokeWidth(2.8f);p.setColor(0xffedf7ff);c.drawOval(VISOR,p);
        p.setStrokeWidth(1.2f);p.setColor(0x99eaffff);c.drawArc(VISOR_GLINT_LEFT,192,45,false,p);
        c.drawArc(VISOR_GLINT_RIGHT,-58,21,false,p);
        p.setStyle(Paint.Style.FILL);p.setColor(0xffb9d2e4);
        c.drawRoundRect(20,38,27,54,3,3,p);c.drawRoundRect(73,38,80,54,3,3,p);
        p.setColor(0xff6983a8);c.drawRoundRect(21,42,25,50,2,2,p);c.drawRoundRect(75,42,79,50,2,2,p);
        p.setColor(0xfff1d5a5);c.drawRoundRect(43,73,57,78,2,2,p);
    }
    public static void draw(Canvas c,float x,float y,float size,int choice,int[] outfit) {
        choice=PlaylumiWorldStyle.safeAvatar(choice);
        c.save();c.translate(x,y);c.scale(size/100f,size/100f);
        c.clipPath(CIRCLE);
        Paint p=PAINT.get();p.reset();p.setAntiAlias(true);
        oval(c,p,1,1,99,99,0xff432270);
        int color=item(outfit,PlaylumiWardrobe.COLORS);
        oval(c,p,3,3,97,97,color==0?0xffffdf94:PlaylumiWardrobe.BODY_COLORS[color]);
        p.setShader(MEDALLION);
        c.drawCircle(50,50,44,p);p.setShader(null);
        for(Path sparkle:SPARKLES)painted(c,p,sparkle,0x99fff1c5);
        int aura=item(outfit,PlaylumiWardrobe.AURAS);
        if(aura>0){
            int glow=PlaylumiWardrobe.BODY_COLORS[aura];
            p.setColor(glow);p.setStrokeWidth(3f);p.setStyle(Paint.Style.STROKE);c.drawCircle(50,50,41,p);p.setStyle(Paint.Style.FILL);
            for(Path mark:AURA_MARKS)painted(c,p,mark,glow);
        }
        int clothes=item(outfit,PlaylumiWardrobe.OUTFITS);
        int hat=item(outfit,PlaylumiWardrobe.HATS);
        switch(choice) {
            case 0:
                costumeBack(c,p,clothes);
                p.setColor(0xffa96c20);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setStrokeJoin(Paint.Join.ROUND);
                c.drawPath(LUMI,p);p.setStyle(Paint.Style.FILL);p.setShader(GOLDEN_BODY);
                c.drawPath(LUMI,p);p.setShader(null);
                oval(c,p,43,31,51,39,0x99ffffff);
                costumeOnStar(c,p,LUMI,clothes);goldenFace(c,p,clothes);
                break;
            case 1:
                painted(c,p,FOX_EAR_LEFT,0xffa44a37);painted(c,p,FOX_EAR_RIGHT,0xffa44a37);
                painted(c,p,FOX_INNER_LEFT,0xffffd8ba);painted(c,p,FOX_INNER_RIGHT,0xffffd8ba);
                oval(c,p,19,30,81,83,0xfff29a4b);
                oval(c,p,27,52,73,81,0xffffecd2);
                break;
            case 2:
                oval(c,p,24,27,76,82,0xffffd3a6);
                oval(c,p,20,24,80,51,0xff653b98);
                painted(c,p,WIZARD_HAT,0xff66399d);
                oval(c,p,15,34,85,44,0xff3e245e);
                painted(c,p,WIZARD_STAR,0xffffd987);
                oval(c,p,24,44,76,82,0xffffd3a6);
                break;
            default:
                oval(c,p,24,10,42,53,0xfff9f1ff);
                oval(c,p,58,10,76,53,0xfff9f1ff);
                oval(c,p,29,15,37,43,0xffe4a6cd);
                oval(c,p,63,15,71,43,0xffe4a6cd);
                oval(c,p,18,32,82,83,0xfff8f1ff);
                break;
        }
        oval(c,p,36,49,43,60,0xff3d2558);oval(c,p,57,49,64,60,0xff3d2558);
        oval(c,p,37,50,40,53,0xffffffff);oval(c,p,58,50,61,53,0xffffffff);
        if(choice==0){oval(c,p,30,61,37,66,0x88ee7899);oval(c,p,63,61,70,66,0x88ee7899);}
        else{oval(c,p,28,61,36,66,0x88ee7899);oval(c,p,64,61,72,66,0x88ee7899);}
        p.setColor(0xff583153);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.8f);p.setStrokeCap(Paint.Cap.ROUND);
        c.drawArc(choice==0?LUMI_SMILE:SMILE,8,164,false,p);p.setStyle(Paint.Style.FILL);
        int glasses=item(outfit,PlaylumiWardrobe.GLASSES),friend=item(outfit,PlaylumiWardrobe.FRIENDS);
        if(clothes>0&&choice!=0)accessory(c,p,PlaylumiWardrobe.ITEM_ICONS[PlaylumiWardrobe.OUTFITS][clothes],50,90,23);
        if(choice==0&&(clothes==6||hat==6))spaceHelmet(c,p);
        if(glasses>0){
            int ink=glasses==1?0xff372a55:PlaylumiWardrobe.BODY_COLORS[glasses];
            p.setColor(ink);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);
            c.drawOval(31,46,47,61,p);c.drawOval(53,46,69,61,p);c.drawLine(47,52,53,52,p);p.setStyle(Paint.Style.FILL);
            if(glasses==1){oval(c,p,33,48,45,59,0xc0382851);oval(c,p,55,48,67,59,0xc0382851);}
            if(glasses>=3)accessory(c,p,PlaylumiWardrobe.ITEM_ICONS[PlaylumiWardrobe.GLASSES][glasses],50,56,13);
        }
        if(hat>0&&!(choice==0&&hat==6))
            accessory(c,p,PlaylumiWardrobe.ITEM_ICONS[PlaylumiWardrobe.HATS][hat],50,choice==0?28:34,choice==0?27:32);
        if(friend>0)accessory(c,p,PlaylumiWardrobe.ITEM_ICONS[PlaylumiWardrobe.FRIENDS][friend],79,86,24);
        c.restore();
    }
}
