package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

/** Scalable original medal artwork. Gold for earned achievements, violet for discovery. */
public final class PlaylumiMedal extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String glyph;private final boolean earned;private final int theme;
    public PlaylumiMedal(Context context,String glyph,boolean earned,int theme){
        super(context);this.glyph=glyph;this.earned=earned;this.theme=Math.max(0,theme)%3;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    private void color(int c){paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setColor(c);}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float size=Math.min(getWidth(),getHeight());if(size<=0)return;
        c.save();c.translate((getWidth()-size)/2f,(getHeight()-size)/2f);c.scale(size/100f,size/100f);
        int ribbon=new int[]{0xff7350ba,0xff398c89,0xffb65a91}[theme];
        if(!earned)ribbon=0xffa69ab9;
        color(ribbon);Path tails=new Path();
        tails.moveTo(27,54);tails.lineTo(19,96);tails.lineTo(35,86);tails.lineTo(47,98);tails.lineTo(55,61);
        tails.moveTo(50,61);tails.lineTo(57,98);tails.lineTo(69,86);tails.lineTo(84,96);tails.lineTo(73,53);tails.close();c.drawPath(tails,paint);
        color(0x39261148);c.drawCircle(50,45,39,paint);
        paint.setShader(new LinearGradient(15,6,84,84,earned?new int[]{0xfffff1ae,0xffd99a3e,0xffffe9a2}:new int[]{0xfff2ebfa,0xffa79ac1,0xffe4d8f5},null,Shader.TileMode.CLAMP));
        c.drawCircle(50,42,38,paint);
        color(earned?0xffa66b27:0xff8778a8);c.drawCircle(50,42,32,paint);
        paint.setShader(new LinearGradient(20,14,75,74,earned?0xfffff4d6:0xfff3edf9,earned?0xffffd477:0xffd3c6eb,Shader.TileMode.CLAMP));
        c.drawCircle(50,42,29,paint);
        color(earned?0xfffffff0:0xffeee7f6);c.drawCircle(34,23,2.7f,paint);c.drawCircle(66,61,2.2f,paint);
        color(earned?0xff583178:0xff706482);paint.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(30);
        float measured=paint.measureText(glyph);if(measured>48)paint.setTextSize(30*48/measured);
        Paint.FontMetrics fm=paint.getFontMetrics();c.drawText(glyph,50,42-(fm.ascent+fm.descent)/2,paint);
        c.restore();
    }
}
