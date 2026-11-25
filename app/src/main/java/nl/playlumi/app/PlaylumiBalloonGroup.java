package nl.playlumi.app;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Draw the exact quantity of discrete balloons; the answer is never printed. */
public final class PlaylumiBalloonGroup extends Drawable {
    private final int quantity;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    public PlaylumiBalloonGroup(int quantity){
        if(quantity<1||quantity>8)throw new IllegalArgumentException("balloon quantity");
        this.quantity=quantity;
    }
    @Override public void draw(Canvas c){
        Rect b=getBounds();
        float width=b.width()*.78f,height=b.height()*.76f;
        int columns=quantity<=2?quantity:quantity<=4?2:3;
        int rows=(quantity+columns-1)/columns;
        float size=Math.min(width/columns,height/rows)*.86f;
        float stepX=width/columns,stepY=height/rows;
        for(int i=0;i<quantity;i++){
            int row=i/columns,col=i%columns;
            int inRow=Math.min(columns,quantity-row*columns);
            float x=b.exactCenterX()+(col-(inRow-1)*.5f)*stepX;
            float y=b.exactCenterY()+(row-(rows-1)*.5f)*stepY-size*.08f;
            float r=size*.32f;
            paint.setShader(null);paint.setColor(0xff8c5274);paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1,size*.035f));paint.setStrokeCap(Paint.Cap.ROUND);
            Path string=new Path();string.moveTo(x,y+r*1.12f);
            string.cubicTo(x-r*.5f,y+r*1.4f,x+r*.45f,y+r*1.7f,x,y+r*1.94f);
            c.drawPath(string,paint);
            paint.setStyle(Paint.Style.FILL);paint.setColor(0xffb72d70);
            Path knot=new Path();knot.moveTo(x,y+r*.95f);knot.lineTo(x-r*.16f,y+r*1.25f);knot.lineTo(x+r*.16f,y+r*1.25f);knot.close();c.drawPath(knot,paint);
            paint.setShader(new RadialGradient(x-r*.3f,y-r*.4f,r*1.55f,
                    new int[]{0xffffc5df,0xffef488d,0xffa82d69},null,Shader.TileMode.CLAMP));
            c.drawOval(x-r,y-r*1.12f,x+r,y+r*1.05f,paint);paint.setShader(null);
            paint.setColor(0x8cffffff);c.drawOval(x-r*.56f,y-r*.76f,x-r*.17f,y-r*.33f,paint);
        }
    }
    @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
