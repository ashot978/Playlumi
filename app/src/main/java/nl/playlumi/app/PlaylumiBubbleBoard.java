package nl.playlumi.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

/** One focused touch-based game: pop exactly the requested number of bubbles. */
public final class PlaylumiBubbleBoard extends LinearLayout {
    public interface Listener {
        void onPop();
        void onSolved();
    }
    private final PlaylumiBubbles game;
    private final Listener listener;
    private final ArrayList<ObjectAnimator> floating=new ArrayList<>();
    private final TextView count;
    private boolean awarded;
    private static final int[][] GRADIENTS={
            {0xffc9e9ff,0xff7dbcea},
            {0xffffd1f1,0xfff59dc2},
            {0xfffff2bf,0xffffd06e},
            {0xffd7f6d3,0xff8cdac2},
            {0xffe3d6ff,0xffb4a0ee},
            {0xffffe6cd,0xffeda6ae}
    };
    public PlaylumiBubbleBoard(Context context,PlaylumiBubbles state,Listener events) {
        super(context);
        game=state;listener=events;awarded=state.solved();
        setOrientation(VERTICAL);
        setPadding(dp(5),dp(4),dp(5),dp(6));
        setGravity(Gravity.CENTER_HORIZONTAL);
        TextView intro=text("🫧 Prik "+state.target+" bellen!",21,0xff624698);
        addView(intro,new LayoutParams(-1,dp(43)));
        count=text("✨ "+state.popped()+" / "+state.target,19,0xff34876a);
        boolean wide=PlaylumiLayout.useWideLayout(
                getResources().getConfiguration().screenWidthDp,
                getResources().getConfiguration().orientation==
                        android.content.res.Configuration.ORIENTATION_LANDSCAPE);
        int perRow=wide?4:3;
        int bubbleHeight=getResources().getConfiguration().screenHeightDp<700?72:80;
        for(int start=0;start<state.total;start+=perRow) {
            LinearLayout row=new LinearLayout(context);
            row.setGravity(Gravity.CENTER);
            for(int i=start;i<Math.min(start+perRow,state.total);i++) {
                final int index=i;
                Button b=new Button(context);
                b.setAllCaps(false);
                b.setText("🫧");
                b.setTextSize(wide?31:35);
                b.setGravity(Gravity.CENTER);
                b.setMinHeight(dp(60));
                b.setPadding(dp(3),dp(3),dp(3),dp(3));
                b.setContentDescription("Zeepbel "+(index+1)+" van "+state.total+", tik om te prikken");
                GradientDrawable shape=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                        GRADIENTS[i%GRADIENTS.length]);
                shape.setShape(GradientDrawable.OVAL);
                shape.setStroke(dp(2),0xeaffffff);
                b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x66ffffff),shape,null));
                b.setElevation(dp(4));
                LayoutParams bp=new LayoutParams(0,dp(bubbleHeight),1);
                bp.setMargins(dp(4),dp(4),dp(4),dp(5));
                row.addView(b,bp);
                if(state.isPopped(i))b.setVisibility(INVISIBLE);
                else {
                    b.setOnClickListener(v->pop(b,index));
                    if(!PlaylumiMotion.isReduced()) {
                        ObjectAnimator floaty=ObjectAnimator.ofFloat(b,View.TRANSLATION_Y,
                                0,-dp(7+(i%3)*3),0);
                        floaty.setDuration(1300+i*180);
                        floaty.setStartDelay(i*105L);
                        floaty.setRepeatCount(ValueAnimator.INFINITE);
                        floaty.setInterpolator(new AccelerateDecelerateInterpolator());
                        floating.add(floaty);
                    }
                }
            }
            addView(row,new LayoutParams(-1,-2));
        }
        updateStatus();
        addView(count,new LayoutParams(-1,dp(44)));
    }
    private TextView text(String s,int size,int color) {
        TextView t=new TextView(getContext());
        t.setText(s);t.setTextSize(size);t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
        t.setGravity(Gravity.CENTER);return t;
    }
    private int dp(int value) {
        return (int)(getResources().getDisplayMetrics().density*value+.5f);
    }
    private void pop(Button button,int index) {
        if(!game.pop(index))return;
        button.setEnabled(false);
        listener.onPop();
        button.animate().cancel();
        button.setTranslationY(0);
        if(PlaylumiMotion.isReduced())button.setVisibility(INVISIBLE);
        else button.animate().scaleX(.1f).scaleY(.1f).alpha(0f)
                .setDuration(240).withEndAction(()->button.setVisibility(INVISIBLE)).start();
        updateStatus();
        if(game.solved() && !awarded) {
            awarded=true;
            listener.onSolved();
        }
    }
    private void updateStatus() {
        if(count==null)return;
        if(game.solved())count.setText("🎉 Geweldig! "+game.target+" bellen geknapt!");
        else count.setText("✨ "+game.popped()+" van "+game.target+" geknapt");
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        for(ObjectAnimator m:floating)if(!m.isStarted())m.start();
    }
    @Override protected void onDetachedFromWindow() {
        for(ObjectAnimator m:floating)m.cancel();
        super.onDetachedFromWindow();
    }
}
