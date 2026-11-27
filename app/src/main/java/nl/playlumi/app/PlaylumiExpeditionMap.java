package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

/** A six-island expedition map with real chapter progress. Accessible chapter
 * controls live immediately below this illustration. No animation or timer. */
public final class PlaylumiExpeditionMap extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int progress;
    private static final int[] LAND={0xffa4c9ff,0xffffb9d1,0xffc7b1f5,0xffc7b7f5,0xffa2e3d9,0xffb7e5a5};
    public PlaylumiExpeditionMap(Context context,int progress){
        super(context);this.progress=PlaylumiExpedition.safe(progress);
        StringBuilder desc=new StringBuilder("Expeditiekaart: "+this.progress+" van 30 opdrachten. ");
        for(int i=0;i<6;i++)desc.append(PlaylumiExpedition.TITLES[i]).append(": ").append(PlaylumiExpedition.chapterProgress(this.progress,i)).append(" van 5. ");
        setContentDescription(desc.toString());setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    private float dp(float v){return v*getResources().getDisplayMetrics().density;}
    @Override protected void onMeasure(int ws,int hs){
        int w=MeasureSpec.getSize(ws);if(w<=0)w=Math.round(dp(320));
        setMeasuredDimension(resolveSize(w,ws),resolveSize(Math.round(dp(w/dp(1)>=540?186:222)),hs));
    }
    private void fill(int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);}
    private void line(int color,float width){p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setColor(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
    private void text(Canvas c,String s,float x,float y,float size,int color){
        fill(color);p.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);c.drawText(s,x,y-(p.ascent()+p.descent())*.5f,p);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        float pad=dp(3),corner=dp(21);
        fill(0xff35205a);p.setShader(new LinearGradient(0,0,w,h,new int[]{0xff39265f,0xff24456c,0xff4b326c},null,Shader.TileMode.CLAMP));
        c.drawRoundRect(new RectF(pad,pad,w-pad,h-pad),corner,corner,p);p.setShader(null);
        for(int i=0;i<21;i++){
            float x=dp(12)+(w-dp(24))*((i*37%101)/100f),y=dp(8)+(h-dp(16))*((i*43%97)/96f);
            fill(i%4==0?0x88ffe9af:0x55ffffff);c.drawCircle(x,y,dp(i%4==0?1.8f:1),p);
        }
        boolean wide=w/dp(1)>=540;
        int cols=wide?6:3;float span=(w-dp(20))/cols;
        float r=Math.min(dp(wide?35:30),Math.min(span*.36f,wide?h*.25f:h*.15f));
        float[] xs=new float[6],ys=new float[6];
        for(int i=0;i<6;i++){
            int row=i/cols,col=row==0?i%cols:cols-1-i%cols;
            xs[i]=dp(10)+(col+.5f)*span;
            ys[i]=wide?h*.48f:(row==0?h*.27f:h*.70f);
        }
        for(int i=1;i<6;i++){
            Path path=new Path();path.moveTo(xs[i-1],ys[i-1]);
            float mid=(xs[i-1]+xs[i])*.5f;
            path.cubicTo(mid,ys[i-1],mid,ys[i],xs[i],ys[i]);
            line(i<=PlaylumiExpedition.stamps(progress)?0xffffd477:0xff836faa,dp(5));c.drawPath(path,p);
        }
        for(int i=0;i<6;i++){
            int done=PlaylumiExpedition.chapterProgress(progress,i);
            boolean unlocked=PlaylumiExpedition.available(progress,i),current=i==PlaylumiExpedition.currentChapter(progress)&&!PlaylumiExpedition.finished(progress);
            float x=xs[i],y=ys[i];
            fill(0xff24153f);c.drawOval(new RectF(x-r*1.12f,y+r*.25f,x+r*1.12f,y+r*1.08f),p);
            if(current){fill(0x66ffde88);c.drawCircle(x,y,r+dp(6),p);}
            fill(unlocked?LAND[i]:0xff6e6389);c.drawCircle(x,y,r,p);
            line(done==5||current?0xffffdc8b:0xffa69ac5,dp(current?3:2));c.drawCircle(x,y,r,p);
            text(c,done==5?"✓":unlocked?PlaylumiExpedition.ICONS[i]:""+(i+1),x,y,Math.min(dp(28),r*.92f),0xff39275c);
            float dots=Math.min(dp(10),r*.32f),dy=y+r+dp(10);
            for(int j=0;j<5;j++){fill(j<done?0xffffcf68:0xff82709e);c.drawCircle(x+(j-2)*dots,dy,dp(3),p);}
            text(c,done==5?"Zegel":current?"Nu":""+(i+1),x,dy+dp(12),dp(10),0xffffedc5);
        }
        p.setShader(null);p.setStyle(Paint.Style.FILL);
    }
}
