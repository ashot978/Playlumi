package nl.playlumi.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.Random;

/**
 * Small, finite interactive delights. All effects are decorative, never required
 * to answer a question, and respect the parent's reduced-motion setting.
 */
public final class PlaylumiDelight {
    private static final String[] TWINKLES={"✨","⭐","💛","🌟","🫧","💜"};
    private PlaylumiDelight() {}
    private static boolean available(){
        return !PlaylumiMotion.isReduced() &&
                (Build.VERSION.SDK_INT<26 || ValueAnimator.areAnimatorsEnabled());
    }
    private static int dp(View view,int amount) {
        return (int)(view.getResources().getDisplayMetrics().density*amount+.5f);
    }
    /** A happy wiggle, rather than a jarring shake, for discovery. */
    public static void wiggle(View view) {
        if(!available() || !view.isAttachedToWindow())return;
        ObjectAnimator anim=ObjectAnimator.ofFloat(view,View.ROTATION,
                0,-4,4,-3,3,0);
        anim.setDuration(460);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();
    }
    /** A little card flip for memory, without relying on the flip for input. */
    public static void flip(View card) {
        if(!available() || !card.isAttachedToWindow())return;
        card.setCameraDistance(dp(card,1100));
        ObjectAnimator effect=ObjectAnimator.ofFloat(card,View.ROTATION_Y,
                0f,82f,0f);
        effect.setDuration(350);
        effect.setInterpolator(new DecelerateInterpolator());
        effect.start();
    }
    /** A brief soft bounce used when a child chooses something. */
    public static void hop(View view) {
        if(!available() || !view.isAttachedToWindow())return;
        ObjectAnimator anim=ObjectAnimator.ofFloat(view,View.TRANSLATION_Y,
                0,-dp(view,9),0);
        anim.setDuration(420);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }
    /** One short pirouette when invited, never constant spinning. */
    public static void spin(View view) {
        if(!available() || !view.isAttachedToWindow())return;
        ObjectAnimator anim=ObjectAnimator.ofFloat(view,View.ROTATION,0f,360f);
        anim.setDuration(780);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();
    }
    /** Gentle idle pulse on one primary action; cancels when its screen disappears. */
    public static void invite(View view) {
        if(!available())return;
        ValueAnimator pulse=ValueAnimator.ofFloat(1f,1.035f,1f);
        pulse.setDuration(2300);
        pulse.setStartDelay(400);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.addUpdateListener(animation->{
            if(!view.isAttachedToWindow())return;
            float scale=(Float)animation.getAnimatedValue();
            view.setScaleX(scale);view.setScaleY(scale);
        });
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View v) {
                if(available() && !pulse.isStarted())pulse.start();
            }
            @Override public void onViewDetachedFromWindow(View v) {
                pulse.cancel();v.setScaleX(1f);v.setScaleY(1f);
                v.removeOnAttachStateChangeListener(this);
            }
        });
        if(view.isAttachedToWindow())pulse.start();
    }
    /** Four short particles rising from an actual touched widget. */
    public static void sprinkle(FrameLayout layer,View source) {
        if(!available() || layer==null || source==null ||
                !layer.isAttachedToWindow() || !source.isAttachedToWindow())return;
        int[] rootPosition=new int[2],sourcePosition=new int[2];
        layer.getLocationOnScreen(rootPosition);
        source.getLocationOnScreen(sourcePosition);
        int centerX=sourcePosition[0]-rootPosition[0]+source.getWidth()/2;
        int centerY=sourcePosition[1]-rootPosition[1]+source.getHeight()/2;
        Random random=new Random();
        int spread=dp(layer,18);
        for(int index=0;index<4;index++) {
            TextView star=new TextView(layer.getContext());
            star.setText(TWINKLES[random.nextInt(TWINKLES.length)]);
            star.setTextSize(20);
            star.setGravity(Gravity.CENTER);
            star.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(
                    dp(layer,38),dp(layer,38));
            params.leftMargin=Math.max(0,centerX-dp(layer,19)+
                    random.nextInt(spread*2+1)-spread);
            params.topMargin=Math.max(0,centerY-dp(layer,19));
            layer.addView(star,params);
            float rise=dp(layer,55+random.nextInt(42));
            float drift=dp(layer,(index-2)*24+random.nextInt(18));
            AnimatorSet animation=new AnimatorSet();
            animation.playTogether(
                    ObjectAnimator.ofFloat(star,View.ALPHA,0f,1f,.9f,0f),
                    ObjectAnimator.ofFloat(star,View.TRANSLATION_Y,0f,-rise),
                    ObjectAnimator.ofFloat(star,View.TRANSLATION_X,0f,drift),
                    ObjectAnimator.ofFloat(star,View.SCALE_X,.45f,1.05f),
                    ObjectAnimator.ofFloat(star,View.SCALE_Y,.45f,1.05f));
            animation.setDuration(540+index*90L);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.addListener(new AnimatorListenerAdapter(){
                @Override public void onAnimationEnd(Animator a){remove();}
                @Override public void onAnimationCancel(Animator a){remove();}
                private void remove() {
                    if(star.getParent()==layer)layer.removeView(star);
                }
            });
            animation.start();
        }
    }
}
