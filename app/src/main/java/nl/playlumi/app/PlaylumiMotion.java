package nl.playlumi.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.Random;

/** Native Android motion; avoids webviews, long-running loops, assets and permissions. */
public final class PlaylumiMotion {
    private PlaylumiMotion() {}
    private static volatile boolean reduced;
    public static void setReduced(boolean disabled) {reduced=disabled;}
    public static boolean isReduced() {return reduced;}
    private static int dp(View view, int amount) {
        return (int) (view.getResources().getDisplayMetrics().density * amount + .5f);
    }
    /** Gentle staggered reveal, using real Android ViewPropertyAnimator. */
    public static void reveal(ViewGroup root) {
        if(reduced)return;
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            child.animate().cancel();
            child.setAlpha(0f);
            child.setTranslationY(dp(child, 8));
            child.animate().alpha(1f).translationY(0)
                    .setStartDelay(Math.min(i, 4) * 24L)
                    .setDuration(190)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }
    /** Press and release feedback; a button continues to handle click/accessibility normally. */
    public static void tactile(Button button) {
        button.setOnTouchListener((view, event) -> {
            if(reduced)return false;
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                view.animate().cancel();
                view.animate().scaleX(.94f).scaleY(.94f).setDuration(100).start();
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                view.animate().cancel();
                view.animate().scaleX(1f).scaleY(1f).setDuration(170)
                        .setInterpolator(new DecelerateInterpolator()).start();
            }
            return false;
        });
    }
    /** Friendly floating mascot; stops cleanly when its screen is removed. */
    public static void floatMascot(View view) {
        if(reduced)return;
        if (android.os.Build.VERSION.SDK_INT>=26 && !ValueAnimator.areAnimatorsEnabled()) return;
        ObjectAnimator bob=ObjectAnimator.ofFloat(view,View.TRANSLATION_Y,0f,-dp(view,8),0f);
        bob.setDuration(2300);
        bob.setInterpolator(new AccelerateDecelerateInterpolator());
        bob.setRepeatCount(ValueAnimator.INFINITE);
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View v) { if(!bob.isStarted()) bob.start(); }
            @Override public void onViewDetachedFromWindow(View v) {
                bob.cancel();
                v.removeOnAttachStateChangeListener(this);
            }
        });
        if(view.isAttachedToWindow())bob.start();
    }
    public static void shake(View view) {
        if(reduced)return;
        ObjectAnimator x = ObjectAnimator.ofFloat(view, View.TRANSLATION_X,
                0, -dp(view, 12), dp(view, 9), -dp(view, 6), dp(view, 4), 0);
        x.setDuration(340);
        x.start();
    }
    public static void pop(View view) {
        if(reduced)return;
        view.animate().cancel();
        view.setScaleX(.8f); view.setScaleY(.8f);
        view.animate().scaleX(1.10f).scaleY(1.10f).setDuration(210)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f)
                        .setDuration(190).start()).start();
    }
    /** A short, non-blocking reward. Particles are automatically removed. */
    public static void celebrate(FrameLayout layer, int streak) {
        if(reduced)return;
        final String[] symbols = streak > 0 && streak % 10 == 0
                ? new String[]{"🏆","⭐","🎉","🌟","💛","🎊","✨","⭐","🎉"}
                : new String[]{"⭐","✨","🌟","💛","⭐","✨"};
        Random random = new Random();
        int width = Math.max(layer.getWidth(), dp(layer, 280));
        int height = Math.max(layer.getHeight(), dp(layer, 450));
        for (String symbol : symbols) {
            TextView particle = new TextView(layer.getContext());
            particle.setText(symbol);
            particle.setTextSize(29);
            particle.setGravity(Gravity.CENTER);
            particle.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(dp(layer, 50), dp(layer, 50));
            p.leftMargin = random.nextInt(Math.max(1, width - dp(layer, 55)));
            p.topMargin = Math.max(dp(layer, 90), height / 3 + random.nextInt(Math.max(1, height / 4)));
            layer.addView(particle, p);
            particle.setAlpha(0f);
            particle.setScaleX(.45f); particle.setScaleY(.45f);
            float rise = dp(layer, 80 + random.nextInt(160));
            float sway = dp(layer, random.nextInt(101) - 50);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(particle, View.ALPHA, 0f, 1f, 1f, 0f),
                    ObjectAnimator.ofFloat(particle, View.TRANSLATION_Y, 0f, -rise),
                    ObjectAnimator.ofFloat(particle, View.TRANSLATION_X, 0f, sway),
                    ObjectAnimator.ofFloat(particle, View.ROTATION, -20f, 30f),
                    ObjectAnimator.ofFloat(particle, View.SCALE_X, .45f, 1.15f),
                    ObjectAnimator.ofFloat(particle, View.SCALE_Y, .45f, 1.15f));
            set.setDuration(780 + random.nextInt(220));
            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animator) {
                    if (particle.getParent() == layer) layer.removeView(particle);
                }
            });
            set.start();
        }
    }
}
