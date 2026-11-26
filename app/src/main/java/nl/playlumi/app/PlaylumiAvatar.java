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
        draw(canvas,(getWidth()-size)/2,(getHeight()-size)/2,size,avatar,wardrobe==null?null:wardrobe.equippedItems());
    }
    private static void oval(Canvas c,Paint p,float l,float t,float r,float b,int color) {
        p.setColor(color);c.drawOval(l,t,r,b,p);
    }
    private static void polygon(Canvas c,Paint p,int color,float... xy) {
        Path path=new Path();path.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);
        path.close();p.setColor(color);c.drawPath(path,p);
    }
    public static void draw(Canvas c,float x,float y,float size,int choice){draw(c,x,y,size,choice,null);}
    private static int item(int[] outfit,int slot){
        return outfit!=null&&outfit.length>slot?Math.max(0,Math.min(PlaylumiWardrobe.OPTIONS-1,outfit[slot])):0;
    }
    private static void accessory(Canvas c,Paint p,String icon,float x,float y,float size){
        p.setShader(null);p.setColor(0xffffffff);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);
        p.setTypeface(android.graphics.Typeface.DEFAULT);c.drawText(icon,x,y,p);
    }
    public static void draw(Canvas c,float x,float y,float size,int choice,int[] outfit) {
        choice=PlaylumiWorldStyle.safeAvatar(choice);
        c.save();c.translate(x,y);c.scale(size/100f,size/100f);
        Path circle=new Path();circle.addCircle(50,50,49,Path.Direction.CW);c.clipPath(circle);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        oval(c,p,1,1,99,99,0xff432270);
        int color=item(outfit,PlaylumiWardrobe.COLORS);
        oval(c,p,3,3,97,97,color==0?0xffffdf94:PlaylumiWardrobe.BODY_COLORS[color]);
        p.setShader(new LinearGradient(0,6,0,94,0xfff8dcff,0xff8a5fd5,Shader.TileMode.CLAMP));
        c.drawCircle(50,50,44,p);p.setShader(null);
        for(int i=0;i<6;i++) {
            float sx=15+(i*23)%72,sy=16+(i*29)%67;
            polygon(c,p,0x99fff1c5,sx,sy-4,sx+1,sy-1,sx+4,sy,sx+1,sy+1,sx,sy+4,sx-1,sy+1,sx-4,sy,sx-1,sy-1);
        }
        int aura=item(outfit,PlaylumiWardrobe.AURAS);
        if(aura>0){
            int glow=PlaylumiWardrobe.BODY_COLORS[aura];
            p.setColor(glow);p.setStrokeWidth(3f);p.setStyle(Paint.Style.STROKE);c.drawCircle(50,50,41,p);p.setStyle(Paint.Style.FILL);
            for(int n=0;n<8;n++){
                double angle=n*Math.PI/4;float sx=50+(float)Math.cos(angle)*37,sy=50+(float)Math.sin(angle)*37;
                polygon(c,p,glow,sx,sy-4,sx+3,sy,sx,sy+4,sx-3,sy);
            }
        }
        switch(choice) {
            case 1:
                polygon(c,p,0xffa44a37,22,45,19,16,40,32);
                polygon(c,p,0xffa44a37,78,45,81,16,60,32);
                polygon(c,p,0xffffd8ba,24,22,35,37,25,40);
                polygon(c,p,0xffffd8ba,76,22,65,37,75,40);
                oval(c,p,19,30,81,83,0xfff29a4b);
                oval(c,p,27,52,73,81,0xffffecd2);
                break;
            case 2:
                oval(c,p,24,27,76,82,0xffffd3a6);
                oval(c,p,20,24,80,51,0xff653b98);
                polygon(c,p,0xff66399d,21,37,53,9,79,39);
                oval(c,p,15,34,85,44,0xff3e245e);
                polygon(c,p,0xffffd987,53,18,56,24,63,25,58,30,59,36,53,33,47,36,48,30,43,25,50,24);
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
        oval(c,p,28,61,36,66,0x88ee7899);oval(c,p,64,61,72,66,0x88ee7899);
        p.setColor(0xff583153);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.8f);p.setStrokeCap(Paint.Cap.ROUND);
        c.drawArc(new RectF(43,57,57,71),8,164,false,p);p.setStyle(Paint.Style.FILL);
        int hat=item(outfit,PlaylumiWardrobe.HATS),glasses=item(outfit,PlaylumiWardrobe.GLASSES),friend=item(outfit,PlaylumiWardrobe.FRIENDS),clothes=item(outfit,PlaylumiWardrobe.OUTFITS);
        if(clothes>0)accessory(c,p,PlaylumiWardrobe.ITEM_ICONS[PlaylumiWardrobe.OUTFITS][clothes],50,90,23);
        if(glasses>0){
            int ink=glasses==1?0xff372a55:PlaylumiWardrobe.BODY_COLORS[glasses];
            p.setColor(ink);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);
            c.drawOval(31,46,47,61,p);c.drawOval(53,46,69,61,p);c.drawLine(47,52,53,52,p);p.setStyle(Paint.Style.FILL);
            if(glasses==1){oval(c,p,33,48,45,59,0xc0382851);oval(c,p,55,48,67,59,0xc0382851);}
            if(glasses>=3)accessory(c,p,PlaylumiWardrobe.ITEM_ICONS[PlaylumiWardrobe.GLASSES][glasses],50,56,13);
        }
        if(hat>0)accessory(c,p,PlaylumiWardrobe.ITEM_ICONS[PlaylumiWardrobe.HATS][hat],50,34,32);
        if(friend>0)accessory(c,p,PlaylumiWardrobe.ITEM_ICONS[PlaylumiWardrobe.FRIENDS][friend],79,86,24);
        c.restore();
    }
}
