package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

/** Larger real shapes, counting objects and token cards; no new game state. */
public final class PlaylumiSkillBoard extends View {
    private static final int INK=0xff432467,PURPLE=0xff9675d1,GOLD=0xffffd477,GREEN=0xff25764c;
    private final int mode;
    private final LearningGame.Round round;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    public PlaylumiSkillBoard(Context context,int mode,int variant,LearningGame.Round round){
        super(context);
        if(!PlaylumiSkillVisual.supported(mode,variant,round))throw new IllegalArgumentException("Skill board");
        this.mode=mode;this.round=round;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);refresh();
    }
    private float dp(float n){return n*getResources().getDisplayMetrics().density;}
    public void refresh(){setContentDescription(PlaylumiSkillVisual.description(mode,round));invalidate();}
    @Override protected void onMeasure(int ws,int hs){
        int width=MeasureSpec.getSize(ws);if(width<=0)width=Math.round(dp(280));
        int height=Math.round(dp(104));
        if(mode!=LearningGame.SHAPES&&mode!=LearningGame.COUNT){
            int rows=PlaylumiSkillVisual.rows((int)(width/dp(1)),PlaylumiSkillVisual.tokens(round).length);
            height=Math.round(dp(16+rows*58));
        }
        setMeasuredDimension(resolveSize(width,ws),resolveSize(Math.max(height,getSuggestedMinimumHeight()),hs));
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);if(getWidth()<=0||getHeight()<=0)return;
        if(mode==LearningGame.SHAPES)shape(c);
        else if(mode==LearningGame.COUNT)dots(c);
        else tokens(c);
        paint.setStyle(Paint.Style.FILL);
    }
    private void fill(int color){paint.setStyle(Paint.Style.FILL);paint.setColor(color);}
    private void stroke(int color,float width){paint.setStyle(Paint.Style.STROKE);paint.setColor(color);paint.setStrokeWidth(width);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStrokeCap(Paint.Cap.ROUND);}
    private void shape(Canvas c){
        float x=getWidth()/2f,y=getHeight()/2f,r=Math.min(dp(55),Math.min(getWidth()*.30f,getHeight()*.36f));
        float[] vx,vy;String s=round.display;
        if("●".equals(s)){
            fill(PURPLE);c.drawCircle(x,y,r,paint);stroke(INK,dp(2));c.drawCircle(x,y,r,paint);return;
        }else if("▲".equals(s)){
            vx=new float[]{x,x+r,x-r};vy=new float[]{y-r,y+r*.82f,y+r*.82f};
        }else if("◆".equals(s)){
            vx=new float[]{x,x+r,x,x-r};vy=new float[]{y-r,y,y+r,y};
        }else{
            float ry="▭".equals(s)?r*.59f:r*.82f,rx="▭".equals(s)?r*1.25f:r*.82f;
            vx=new float[]{x-rx,x+rx,x+rx,x-rx};vy=new float[]{y-ry,y-ry,y+ry,y+ry};
        }
        Path path=new Path();path.moveTo(vx[0],vy[0]);
        for(int i=1;i<vx.length;i++)path.lineTo(vx[i],vy[i]);path.close();
        fill(PURPLE);c.drawPath(path,paint);stroke(INK,dp(2));c.drawPath(path,paint);
        if(round.isSolved())for(int i=0;i<vx.length;i++){
            fill(GOLD);c.drawCircle(vx[i],vy[i],dp(7),paint);
            stroke(INK,dp(1.7f));c.drawCircle(vx[i],vy[i],dp(7),paint);
        }
    }
    private void dots(Canvas c){
        int count=PlaylumiSkillVisual.dots(round.display),delta=PlaylumiSkillVisual.dotDelta(round);
        int slots=count+(delta==1?1:0);
        float pitch=Math.min(dp(64),(getWidth()-dp(20))/slots);
        float radius=Math.min(dp(22),Math.min(pitch*.32f,getHeight()*.31f)),y=getHeight()/2f;
        for(int i=0;i<slots;i++){
            float x=getWidth()/2f+(i-(slots-1)/2f)*pitch;
            boolean removed=delta==-1&&i==slots-1,added=delta==1&&i==slots-1;
            fill(removed?0xffeadff2:added?GOLD:PURPLE);c.drawCircle(x,y,radius,paint);
            stroke(added?GREEN:INK,dp(2));c.drawCircle(x,y,radius,paint);
            if(removed){
                stroke(0xff9d4360,dp(3));
                c.drawLine(x-radius*.48f,y-radius*.48f,x+radius*.48f,y+radius*.48f,paint);
                c.drawLine(x-radius*.48f,y+radius*.48f,x+radius*.48f,y-radius*.48f,paint);
            }
        }
    }
    private void tokens(Canvas c){
        String[] values=PlaylumiSkillVisual.tokens(round);int filled=PlaylumiSkillVisual.filledIndex(round);
        int cols=PlaylumiSkillVisual.columns((int)(getWidth()/dp(1)),values.length),rows=(values.length+cols-1)/cols;
        float pitch=Math.min(dp(82),(getWidth()-dp(18))/cols),line=Math.min(dp(60),(getHeight()-dp(10))/rows);
        float boxW=pitch*.88f,boxH=line*.86f;
        paint.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));paint.setTextAlign(Paint.Align.CENTER);
        for(int i=0;i<values.length;i++){
            int row=i/cols,n=Math.min(cols,values.length-row*cols);
            float x=(getWidth()-n*pitch)/2f+(i%cols+.5f)*pitch,y=(getHeight()-rows*line)/2f+(row+.5f)*line;
            String value=values[i];boolean blank="?".equals(value),done=i==filled;
            RectF box=new RectF(x-boxW/2,y-boxH/2,x+boxW/2,y+boxH/2);
            if(!"→".equals(value)){
                fill(done?0xffd0f0d8:blank?0xffe6d5f8:0xfffff7df);c.drawRoundRect(box,dp(9),dp(9),paint);
                stroke(done?GREEN:blank?0xff9262c2:0xffe0bd79,dp(done?2.5f:1.2f));c.drawRoundRect(box,dp(9),dp(9),paint);
            }
            fill(done?GREEN:INK);paint.setTextSize(Math.min(boxH*.63f,dp(31)));
            float measured=paint.measureText(value);
            if(measured>boxW*.79f&&measured>0)paint.setTextSize(paint.getTextSize()*boxW*.79f/measured);
            c.drawText(value,x,y-(paint.ascent()+paint.descent())/2f,paint);
        }
    }
}
