package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

/** Draw actual, consistent colors instead of platform-dependent colored emoji. */
public final class PlaylumiPatternView extends View {
    private final String[] tokens;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    public PlaylumiPatternView(Context context,String display){
        super(context);tokens=PlaylumiPatterns.tokens(display);
        StringBuilder description=new StringBuilder("Patroon: ");
        for(String token:tokens){
            int index=PlaylumiPatterns.colorIndex(token);
            description.append(index>=0?LearningGame.COLOR_NAMES[index]:"?".equals(token)?"ontbreekt":token).append(", ");
        }
        setContentDescription(description.toString());
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private int columns(int width){return Math.min(tokens.length,Math.max(3,(width-dp(28))/dp(40)));}
    @Override protected void onMeasure(int ws,int hs){
        int width=MeasureSpec.getSize(ws),cols=columns(width),rows=(tokens.length+cols-1)/cols;
        setMeasuredDimension(resolveSize(width,ws),resolveSize(Math.max(getSuggestedMinimumHeight(),dp(25+rows*43)),hs));
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);int cols=columns(getWidth()),rows=(tokens.length+cols-1)/cols;
        float pitch=Math.min(dp(53),(getWidth()-dp(24))/(float)cols);
        float vertical=Math.min(dp(48),(getHeight()-dp(16))/(float)rows);
        float radius=Math.max(3,Math.min(pitch,vertical)*.37f);
        for(int i=0;i<tokens.length;i++){
            int row=i/cols,count=Math.min(cols,tokens.length-row*cols);
            float x=(getWidth()-count*pitch)/2f+(i%cols+.5f)*pitch;
            float y=(getHeight()-rows*vertical)/2f+(row+.5f)*vertical;
            String token=tokens[i];int index=PlaylumiPatterns.colorIndex(token);
            paint.setStyle(Paint.Style.FILL);
            if(index>=0){
                paint.setColor(PlaylumiPatterns.color(index));c.drawCircle(x,y,radius,paint);
                paint.setColor(0xff614775);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1));c.drawCircle(x,y,radius,paint);
            }else{
                if("?".equals(token)){
                    paint.setColor(0xffe8d3ff);
                    c.drawRoundRect(new RectF(x-radius,y-radius,x+radius,y+radius),dp(8),dp(8),paint);
                    paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(2));paint.setColor(0xff7444a8);
                    c.drawRoundRect(new RectF(x-radius,y-radius,x+radius,y+radius),dp(8),dp(8),paint);
                    paint.setStyle(Paint.Style.FILL);
                }
                paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setColor(0xff432467);
                paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(radius*1.65f);
                c.drawText(token,x,y-(paint.ascent()+paint.descent())/2f,paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
    }
}
