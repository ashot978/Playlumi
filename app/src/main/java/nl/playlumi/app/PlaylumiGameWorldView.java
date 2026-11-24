package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/**
 * Decorative game-world scenery. Never intercepts touch or stores game progress.
 * Draws without any external assets or permissions.
 */
public final class PlaylumiGameWorldView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean animated;
    private final boolean night;
    private long frame;

    public PlaylumiGameWorldView(Context context, boolean night) {
        super(context);
        this.night = night;
        animated = !PlaylumiMotion.isReduced();
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setClickable(false);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        final float u = w / 390f;
        canvas.save();
        canvas.scale(u, u);
        float worldHeight = h / u;
        paint.setShader(new LinearGradient(0,0,0,worldHeight,
                night ? new int[]{0xff151f59,0xff425ba0,0xff9dcddd} :
                        new int[]{0xffa9eaff,0xffe0faff,0xfff8f4e0},
                null, Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,390,worldHeight,paint);
        paint.setShader(null);

        // Calm landscape, always behind the scrollable interactive content.
        float horizon = worldHeight * .78f;
        fill(0x55ffffff);
        canvas.drawCircle(316, worldHeight*.13f, 79, paint);
        fill(night ? 0xffe1eea5 : 0xffffe9a1);
        canvas.drawCircle(311, worldHeight*.12f, 35, paint);
        for (int i=0;i<8;i++) {
            float x = (i*73+19)%385;
            float y = 95+(i*107)%Math.max(130,(int)(worldHeight*.53f));
            float shimmer = animated ? (float)Math.sin(frame*.025+i)*1.8f : 0;
            fill(night ? 0xfffff8ce : 0xffffffff);
            star(canvas,x,y,4.6f+shimmer,5);
        }

        drawCloud(canvas,40,worldHeight*.17f,1f);
        drawCloud(canvas,285,worldHeight*.31f,.73f);
        drawCloud(canvas,155,worldHeight*.47f,.59f);

        fill(night ? 0xff668c9d : 0xffb9e7b0);
        canvas.drawOval(-130,horizon-35,240,horizon+105,paint);
        fill(night ? 0xff427c8b : 0xff8ad7ac);
        canvas.drawOval(145,horizon-14,550,horizon+125,paint);
        fill(night ? 0xff2b777b : 0xff65c597);
        canvas.drawRect(0,horizon+49,390,worldHeight,paint);

        // A tiny distant treehouse, more like a game map than a form.
        drawTree(canvas,43,horizon+40,1.1f);
        drawTree(canvas,339,horizon+39,.85f);

        fill(0xffe9c078);
        canvas.drawRoundRect(247,horizon-49,311,horizon+29,12,12,paint);
        fill(0xffc86080);
        Path roof = new Path();
        roof.moveTo(239,horizon-46); roof.lineTo(279,horizon-92);
        roof.lineTo(319,horizon-46); roof.close();
        canvas.drawPath(roof,paint);
        fill(0xff65466c);
        canvas.drawRoundRect(274,horizon-11,291,horizon+29,8,8,paint);
        fill(0xfffff4ad);
        canvas.drawCircle(262,horizon-25,5,paint);
        canvas.drawCircle(302,horizon-25,5,paint);

        // Decorative stepping stones lead towards the next adventure.
        for (int i=0;i<5;i++) {
            float x=155+(float)Math.sin(i*.8f)*48;
            float y=horizon+33+i*23;
            fill(i%2==0 ? 0xffffe5a2 : 0xffedf3df);
            canvas.drawOval(x-18,y-8,x+18,y+8,paint);
        }
        canvas.restore();
        if (animated && getVisibility()==VISIBLE && isAttachedToWindow()) {
            frame++;
            postInvalidateDelayed(80);
        }
    }
    private void fill(int color) { paint.setColor(color);paint.setStyle(Paint.Style.FILL); }
    private void drawCloud(Canvas c,float x,float y,float s) {
        fill(0xb8ffffff);
        c.save();c.translate(x,y);c.scale(s,s);
        c.drawCircle(-19,0,21,paint);
        c.drawCircle(3,-9,27,paint);
        c.drawCircle(29,0,22,paint);
        c.drawOval(-34,-4,45,18,paint);
        c.restore();
    }
    private void drawTree(Canvas c,float x,float y,float s) {
        c.save();c.translate(x,y);c.scale(s,s);
        fill(0xff946745);c.drawRoundRect(-6,-48,6,19,5,5,paint);
        fill(0xff248e74);c.drawCircle(0,-52,28,paint);
        fill(0xff42b28d);c.drawCircle(-12,-68,20,paint);
        fill(0xff6ac7a1);c.drawCircle(13,-65,20,paint);
        fill(0xffffd46f);c.drawCircle(10,-43,5,paint);
        c.restore();
    }
    private void star(Canvas c,float x,float y,float radius,int points) {
        Path p=new Path();
        for (int i=0;i<points*2;i++) {
            double a=Math.PI*i/points-Math.PI/2;
            float r=i%2==0 ? radius : radius*.45f;
            float px=x+(float)Math.cos(a)*r,py=y+(float)Math.sin(a)*r;
            if(i==0)p.moveTo(px,py);else p.lineTo(px,py);
        }
        p.close();c.drawPath(p,paint);
    }
}
