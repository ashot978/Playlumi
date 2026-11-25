package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

/** A responsive, illustrated castle gate with one large, countable shape. */
public final class PlaylumiShapeGate extends FrameLayout {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean completed;
    public PlaylumiShapeGate(Context context,String symbol){
        super(context);
        setWillNotDraw(false);
        setBackground(PlaylumiMagicArt.question(context,false));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription("Vormpoort: "+symbol);
        TextView shape=new TextView(context);
        shape.setText(symbol);
        shape.setTextSize(52);
        shape.setTextColor(PlaylumiMagicArt.INK);
        shape.setGravity(Gravity.CENTER);
        shape.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        shape.setIncludeFontPadding(false);
        shape.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        if(Build.VERSION.SDK_INT>=26)shape.setAutoSizeTextTypeUniformWithConfiguration(30,76,2,android.util.TypedValue.COMPLEX_UNIT_SP);
        FrameLayout.LayoutParams child=new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER);
        addView(shape,child);
    }
    public void setCompleted(boolean value){completed=value;invalidate();}
    private void star(Canvas canvas,float x,float y,float r){
        Path path=new Path();
        for(int i=0;i<10;i++){
            double a=-Math.PI/2+i*Math.PI/5;
            float rr=i%2==0?r:r*.44f;
            float xx=x+(float)Math.cos(a)*rr,yy=y+(float)Math.sin(a)*rr;
            if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);
        }
        path.close();p.setColor(0xffffce62);canvas.drawPath(path,p);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float w=getWidth(),h=getHeight();
        if(w<=0||h<=0)return;
        float gh=h*.83f,gw=Math.min(w*.74f,gh*1.45f);
        float cx=w/2,top=(h-gh)/2,bottom=top+gh;
        float l=cx-gw/2,r=cx+gw/2;
        Path gate=new Path();
        gate.moveTo(l,bottom);gate.lineTo(l,top+gh*.42f);
        gate.quadTo(l,top+gh*.10f,cx,top);
        gate.quadTo(r,top+gh*.10f,r,top+gh*.42f);
        gate.lineTo(r,bottom);gate.close();
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,top,0,bottom,
                completed?0xffe2f7ca:0xfffcf1ff,completed?0xffb6e8c9:0xffc8b0ec,Shader.TileMode.CLAMP));
        canvas.drawPath(gate,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeWidth(Math.max(2,gh*.035f));p.setColor(completed?0xff438b61:0xff8151bc);
        canvas.drawPath(gate,p);p.setStyle(Paint.Style.FILL);
        float star=gh*.058f;
        star(canvas,l-gw*.09f,top+gh*.45f,star);
        star(canvas,r+gw*.09f,top+gh*.45f,star);
        if(completed)star(canvas,cx,top+gh*.13f,star);
    }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
        super.onSizeChanged(w,h,oldw,oldh);
        TextView shape=(TextView)getChildAt(0);
        // Size to the real gate, including small portrait and large unfolded screens.
        shape.setPadding(Math.round(w*.24f),Math.round(h*.27f),Math.round(w*.24f),Math.round(h*.12f));
    }
}
