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
import android.widget.FrameLayout;
import java.util.ArrayList;

/** Animated stars with generous tap targets; no countdown or fast reaction needed. */
public final class PlaylumiStarCatchBoard extends FrameLayout {
    public interface Listener {
        void onStar(int collected);
        void onFinished();
        void onMistake();
    }
    private static final String STAR="⭐",HEART="💜";
    private static final int[] COLORS={0xffffedab,0xffd6eafa,0xffffd5e6,
            0xffd7f5de,0xffe8ddff,0xffffe6c6};
    private final ArrayList<Button> stars=new ArrayList<>();
    private final ArrayList<ObjectAnimator> motions=new ArrayList<>();
    private final PlaylumiStarCatch game;
    private final Listener listener;

    public PlaylumiStarCatchBoard(Context context,PlaylumiStarCatch game,Listener listener) {
        super(context);
        this.game=game;this.listener=listener;
        setClipChildren(false);setClipToPadding(false);
        GradientDrawable backdrop=new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xffe5f6ff,0xfff6eaff,0xffe7fae8});
        backdrop.setCornerRadius(dp(24));
        setBackground(backdrop);
        setContentDescription("Vang alleen de gouden sterren");
        for(int i=0;i<game.total();i++){
            final int index=i;
            Button star=new Button(context);
            star.setAllCaps(false);
            star.setText(game.isStar(index)?STAR:HEART);
            star.setTextSize(32);
            star.setGravity(Gravity.CENTER);
            star.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            star.setPadding(0,0,0,0);
            star.setContentDescription((game.isStar(index)?"Vang ster ":"Hartje ")+(index+1));
            GradientDrawable shape=new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(COLORS[i%COLORS.length]);
            shape.setStroke(dp(2),0xb5ffffff);
            star.setBackground(new RippleDrawable(
                    ColorStateList.valueOf(0x66ffffff),shape,null));
            star.setElevation(dp(3));
            FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(dp(72),dp(72),
                    Gravity.TOP|Gravity.LEFT);
            addView(star,p);
            stars.add(star);
            if(game.caught(i)) {
                star.setVisibility(INVISIBLE);
                motions.add(null);
            } else if(game.finished()){
                star.setEnabled(false);motions.add(null);
            } else {
                star.setOnClickListener(v->catchStar(star,index));
                if(PlaylumiMotion.isReduced()){
                    motions.add(null);
                }else{
                    ObjectAnimator floaty=ObjectAnimator.ofFloat(star,View.TRANSLATION_Y,
                            0,-dp(6+(i%3)*2),0);
                    floaty.setDuration(1750+i*150L);
                    floaty.setStartDelay(i*120L);
                    floaty.setRepeatCount(ValueAnimator.INFINITE);
                    floaty.setInterpolator(new AccelerateDecelerateInterpolator());
                    motions.add(floaty);
                }
            }
        }
    }
    private int dp(int val){
        return (int)(getResources().getDisplayMetrics().density*val+.5f);
    }
    private void catchStar(Button star,int index) {
        if(!game.isStar(index)) {
            if(!game.finished()){PlaylumiDelight.wiggle(star);listener.onMistake();}
            return;
        }
        if(!game.catchStar(index))return;
        ObjectAnimator floating=motions.get(index);
        if(floating!=null)floating.cancel();
        star.setEnabled(false);
        listener.onStar(game.collected());
        PlaylumiDelight.sprinkle(this,star);
        if(PlaylumiMotion.isReduced())star.setVisibility(INVISIBLE);
        else star.animate().alpha(0f).scaleX(.15f).scaleY(.15f)
                .rotation(28f).setDuration(270)
                .withEndAction(()->star.setVisibility(INVISIBLE)).start();
        if(game.finished()){
            for(Button button:stars)button.setEnabled(false);
            for(ObjectAnimator motion:motions)if(motion!=null)motion.cancel();
            listener.onFinished();
        }
    }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
        if(w<1||h<1)return;
        boolean wide=PlaylumiLayout.useWideLayout(
                getResources().getConfiguration().screenWidthDp,
                getResources().getConfiguration().orientation==
                        android.content.res.Configuration.ORIENTATION_LANDSCAPE);
        int columns=wide?4:3;
        int rows=(game.total()+columns-1)/columns;
        int cellWidth=w/columns,cellHeight=h/rows;
        int target=Math.max(dp(54),Math.min(dp(76),
                Math.min(cellWidth,cellHeight)-dp(9)));
        for(int i=0;i<stars.size();i++){
            Button star=stars.get(i);
            FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)star.getLayoutParams();
            p.width=target;p.height=target;
            int col=i%columns,row=i/columns;
            p.leftMargin=col*cellWidth+(cellWidth-target)/2;
            p.topMargin=row*cellHeight+(cellHeight-target)/2;
            star.setLayoutParams(p);
        }
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(PlaylumiMotion.isReduced())return;
        for(ObjectAnimator motion:motions)
            if(motion!=null && !motion.isStarted())motion.start();
    }
    @Override protected void onDetachedFromWindow() {
        for(ObjectAnimator motion:motions)
            if(motion!=null)motion.cancel();
        for(Button star:stars)star.animate().cancel();
        super.onDetachedFromWindow();
    }
}
