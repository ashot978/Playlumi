package nl.playlumi.app;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/** Tiny landscape tile artwork, scalable from gallery thumbnails to full game boards. */
public final class PlaylumiBuilderTile extends Drawable {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final char piece;
    private int alpha=255;
    private ColorFilter filter;
    public PlaylumiBuilderTile(char piece){if(!PlaylumiBuilder.validPiece(piece))throw new IllegalArgumentException("tile");this.piece=piece;}
    private void fill(int color){p.setColor(color);p.setAlpha(alpha);p.setStyle(Paint.Style.FILL);p.setColorFilter(filter);}
    private void stroke(int color,float width){fill(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
    @Override public void draw(Canvas canvas){
        RectF r=new RectF(getBounds());float w=r.width(),h=r.height(),u=Math.min(w,h);
        if(u<=0)return;float x=r.centerX(),y=r.centerY(),radius=u*.09f;
        fill(PlaylumiBuilder.color(piece));canvas.drawRoundRect(r,radius,radius,p);
        stroke(0xff9bb18e,Math.max(.7f,u*.018f));canvas.drawRoundRect(new RectF(r.left+.5f,r.top+.5f,r.right-.5f,r.bottom-.5f),radius,radius,p);
        boolean bridge=piece==PlaylumiBuilder.BRIDGE_H||piece==PlaylumiBuilder.BRIDGE_V;
        if(piece==PlaylumiBuilder.WATER||bridge){
            stroke(0xff5d9dcc,Math.max(1,u*.025f));
            for(int j=0;j<3;j++){
                float yy=r.top+u*(.24f+.23f*j);Path wave=new Path();wave.moveTo(r.left+u*.13f,yy);
                wave.quadTo(r.left+u*.29f,yy-u*.09f,r.left+u*.45f,yy);
                wave.quadTo(r.left+u*.61f,yy+u*.09f,r.left+u*.83f,yy);canvas.drawPath(wave,p);
            }
        }
        if(PlaylumiBuilder.rotatable(piece)){
            int ports=PlaylumiBuilder.ports(piece);
            Path route=new Path();
            float[][] endpoints={{x,r.top},{r.right,y},{x,r.bottom},{r.left,y}};
            boolean started=false;
            for(int d=0;d<4;d++)if((ports&(1<<d))!=0){
                if(!started){route.moveTo(endpoints[d][0],endpoints[d][1]);route.lineTo(x,y);started=true;}
                else route.lineTo(endpoints[d][0],endpoints[d][1]);
            }
            stroke(bridge?0xff795538:0xff9a8baf,u*.46f);p.setStrokeCap(Paint.Cap.BUTT);canvas.drawPath(route,p);
            stroke(bridge?0xffe6bd84:0xff514767,u*.35f);p.setStrokeCap(Paint.Cap.BUTT);canvas.drawPath(route,p);
            if(bridge){
                stroke(0xffa77748,Math.max(1,u*.028f));
                for(int j=0;j<5;j++){
                    float offset=(j-2)*u*.17f;
                    if(piece==PlaylumiBuilder.BRIDGE_H)canvas.drawLine(x+offset,y-u*.15f,x+offset,y+u*.15f,p);
                    else canvas.drawLine(x-u*.15f,y+offset,x+u*.15f,y+offset,p);
                }
            }else{
                stroke(0xffffebbd,Math.max(1,u*.032f));p.setPathEffect(new android.graphics.DashPathEffect(new float[]{u*.12f,u*.11f},0));
                canvas.drawPath(route,p);p.setPathEffect(null);
            }
        }else if(piece==PlaylumiBuilder.EMPTY){
            stroke(0xffb5cda0,Math.max(1,u*.028f));canvas.drawLine(x-u*.09f,y+u*.10f,x-u*.14f,y-u*.04f,p);
            canvas.drawLine(x-u*.09f,y+u*.10f,x,y-u*.09f,p);canvas.drawLine(x-u*.09f,y+u*.10f,x+u*.08f,y-u*.01f,p);
        }
    }
    @Override public void setAlpha(int alpha){this.alpha=alpha;invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){this.filter=filter;invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
