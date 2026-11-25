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
    public PlaylumiAvatar(Context context,int avatar) {
        super(context);this.avatar=PlaylumiWorldStyle.safeAvatar(avatar);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);float size=Math.min(getWidth(),getHeight());
        draw(canvas,(getWidth()-size)/2,(getHeight()-size)/2,size,avatar);
    }
    private static void oval(Canvas c,Paint p,float l,float t,float r,float b,int color) {
        p.setColor(color);c.drawOval(l,t,r,b,p);
    }
    private static void polygon(Canvas c,Paint p,int color,float... xy) {
        Path path=new Path();path.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);
        path.close();p.setColor(color);c.drawPath(path,p);
    }
    public static void draw(Canvas c,float x,float y,float size,int choice) {
        choice=PlaylumiWorldStyle.safeAvatar(choice);
        c.save();c.translate(x,y);c.scale(size/100f,size/100f);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        oval(c,p,1,1,99,99,0xff432270);
        oval(c,p,3,3,97,97,0xffffdf94);
        p.setShader(new LinearGradient(0,6,0,94,0xfff8dcff,0xff8a5fd5,Shader.TileMode.CLAMP));
        c.drawCircle(50,50,44,p);p.setShader(null);
        for(int i=0;i<6;i++) {
            float sx=15+(i*23)%72,sy=16+(i*29)%67;
            polygon(c,p,0x99fff1c5,sx,sy-4,sx+1,sy-1,sx+4,sy,sx+1,sy+1,sx,sy+4,sx-1,sy+1,sx-4,sy,sx-1,sy-1);
        }
        switch(choice) {
            case 0:
                Path star=new Path();
                for(int i=0;i<10;i++){
                    double a=-Math.PI/2+i*Math.PI/5;float r=i%2==0?36:18;
                    float px=50+(float)Math.cos(a)*r,py=50+(float)Math.sin(a)*r;
                    if(i==0)star.moveTo(px,py);else star.lineTo(px,py);
                }
                star.close();p.setColor(0xffe69b35);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setStrokeJoin(Paint.Join.ROUND);c.drawPath(star,p);p.setStyle(Paint.Style.FILL);
                p.setShader(new LinearGradient(0,15,0,88,0xfffff3a5,0xffffb532,Shader.TileMode.CLAMP));
                c.drawPath(star,p);p.setShader(null);
                break;
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
        c.restore();
    }
}
