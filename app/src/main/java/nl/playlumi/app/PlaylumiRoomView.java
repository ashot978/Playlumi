package nl.playlumi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Native illustrated personal room with the child's own chosen companion. */
public final class PlaylumiRoomView extends FrameLayout {
    private final PlaylumiRoom room;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    public PlaylumiRoomView(Context context,PlaylumiRoom room,int avatar) {
        this(context,room,avatar,null);
    }
    public PlaylumiRoomView(Context context,PlaylumiRoom room,int avatar,PlaylumiWardrobe wardrobe) {
        super(context);
        this.room=room;
        setWillNotDraw(false);
        setContentDescription("De eigen kamer van Lumi, met gekozen inrichting");
        addEmoji(PlaylumiRoom.ICONS[PlaylumiRoom.WINDOW][room.selected(PlaylumiRoom.WINDOW)],
                54,Gravity.TOP|Gravity.LEFT,32,30);
        addEmoji(PlaylumiRoom.ICONS[PlaylumiRoom.FURNITURE][room.selected(PlaylumiRoom.FURNITURE)],
                60,Gravity.BOTTOM|Gravity.LEFT,12,37);
        addEmoji(PlaylumiRoom.ICONS[PlaylumiRoom.DECOR][room.selected(PlaylumiRoom.DECOR)],
                54,Gravity.BOTTOM|Gravity.RIGHT,16,45);
        PlaylumiAvatar friend=new PlaylumiAvatar(context,avatar,wardrobe);
        friend.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        friend.setContentDescription("Lumi in de kamer");
        LayoutParams starParams=new LayoutParams(dp(202),dp(225),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        starParams.bottomMargin=dp(11);
        addView(friend,starParams);
        PlaylumiMotion.floatMascot(friend);
        friend.setOnClickListener(v->{
            friend.wave();
            PlaylumiMotion.pop(friend);
            PlaylumiDelight.sprinkle(this,friend);
        });
    }
    private int dp(int v) {return (int)(getResources().getDisplayMetrics().density*v+.5f);}
    private void addEmoji(String value,int size,int gravity,int horizontal,int vertical) {
        TextView item=new TextView(getContext());
        item.setText(value);item.setTextSize(size);
        item.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        item.setGravity(Gravity.CENTER);
        item.setContentDescription("Tik om het kamervoorwerp te laten bewegen: "+value);
        item.setClickable(true);item.setFocusable(true);
        item.setOnClickListener(v->{
            PlaylumiDelight.wiggle(item);
            PlaylumiDelight.sprinkle(this,item);
        });
        if((gravity & Gravity.RIGHT)==Gravity.RIGHT)
            PlaylumiDelight.invite(item);
        LayoutParams lp=new LayoutParams(dp(86),dp(86),gravity);
        if((gravity&Gravity.RIGHT)==Gravity.RIGHT)lp.rightMargin=dp(horizontal);
        else lp.leftMargin=dp(horizontal);
        if((gravity&Gravity.BOTTOM)==Gravity.BOTTOM)lp.bottomMargin=dp(vertical);
        else lp.topMargin=dp(vertical);
        addView(item,lp);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getWidth()<=0 || getHeight()<=0)return;
        canvas.save();
        float sx=getWidth()/340f,sy=getHeight()/330f;
        canvas.scale(sx,sy);
        paint.setColor(PlaylumiRoom.WALL_COLORS[room.selected(PlaylumiRoom.WALL)]);
        canvas.drawRoundRect(new RectF(0,0,340,330),27,27,paint);
        paint.setColor(PlaylumiRoom.FLOOR_COLORS[room.selected(PlaylumiRoom.FLOOR)]);
        canvas.drawRoundRect(new RectF(0,235,340,330),19,19,paint);
        paint.setColor(0x55ffffff);
        canvas.drawRoundRect(new RectF(7,8,333,38),18,18,paint);
        paint.setColor(0xffe8b6bc);
        canvas.drawRoundRect(new RectF(0,229,340,239),6,6,paint);
        paint.setColor(0x88ffffff);
        canvas.drawOval(new RectF(65,271,277,320),paint);
        paint.setColor(0x449d70bc);
        canvas.drawOval(new RectF(91,281,250,310),paint);
        paint.setColor(0xffc0aedc);
        canvas.drawRoundRect(new RectF(26,52,129,157),14,14,paint);
        paint.setColor(0xffd1f1ff);
        canvas.drawRoundRect(new RectF(32,58,123,151),11,11,paint);
        paint.setColor(0xffffffff);
        canvas.drawRoundRect(new RectF(76,58,81,151),3,3,paint);
        canvas.drawRoundRect(new RectF(32,103,123,108),3,3,paint);
        paint.setColor(0x77ffffff);
        canvas.drawCircle(283,54,14,paint);
        canvas.drawCircle(298,83,6,paint);
        canvas.restore();
    }
}
