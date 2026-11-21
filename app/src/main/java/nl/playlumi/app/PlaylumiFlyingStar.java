package nl.playlumi.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

/**
 * One non-interactive, temporary star painted over the existing game screen.
 * This view cannot change scores, question state, or touch targets.
 */
public final class PlaylumiFlyingStar extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final double fromX, fromY, toX, toY;
    private ValueAnimator animator;
    private long elapsedMillis;
    private boolean cleaned;

    private PlaylumiFlyingStar(Context context, double fromX, double fromY,
                               double toX, double toY) {
        super(context);
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    /**
     * Call on the UI thread only after the existing game has accepted a correct
     * answer and updated its star bar. No action for reduced motion.
     */
    public static void launch(FrameLayout stage, View touched,
                              PlaylumiStarTrail progress, int completed, int total) {
        if (stage == null || touched == null || progress == null ||
                total <= 0 || completed <= 0 || PlaylumiMotion.isReduced() ||
                (Build.VERSION.SDK_INT >= 26 && !ValueAnimator.areAnimatorsEnabled())) {
            return;
        }
        if (!stage.isAttachedToWindow() || !touched.isAttachedToWindow() ||
                !progress.isAttachedToWindow() ||
                stage.getWidth() <= 0 || stage.getHeight() <= 0 ||
                touched.getWidth() <= 0 || touched.getHeight() <= 0 ||
                progress.getWidth() <= 0 || progress.getHeight() <= 0) {
            return;
        }

        int[] origin = new int[2];
        int[] button = new int[2];
        int[] bar = new int[2];
        stage.getLocationOnScreen(origin);
        touched.getLocationOnScreen(button);
        progress.getLocationOnScreen(bar);

        final int width = stage.getWidth();
        final int height = stage.getHeight();
        // Keep the moving decoration visible if the star bar has been scrolled
        // out of view. Do not scroll or move the real touchable widgets.
        final float margin = Math.min(width, height) * .035f;
        final double sx = within(button[0] - origin[0] + touched.getWidth() * .5f,
                margin, width - margin) / width;
        final double sy = within(button[1] - origin[1] + touched.getHeight() * .5f,
                margin, height - margin) / height;
        final double fx = within(bar[0] - origin[0] +
                progress.getWidth() * StarFlightMotion.slotCenterFraction(completed, total),
                margin, width - margin) / width;
        final double fy = within(bar[1] - origin[1] + progress.getHeight() * .5f,
                margin, height - margin) / height;

        PlaylumiFlyingStar overlay = new PlaylumiFlyingStar(
                stage.getContext(), sx, sy, fx, fy);
        stage.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.start();
    }

    private static double within(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void start() {
        animator = ValueAnimator.ofInt(0, StarFlightMotion.DURATION_MILLIS);
        animator.setDuration(StarFlightMotion.DURATION_MILLIS);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            elapsedMillis = (Integer) a.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator a) { cleanup(); }
            @Override public void onAnimationCancel(Animator a) { cleanup(); }
        });
        animator.start();
    }

    private void cleanup() {
        if (cleaned) return;
        cleaned = true;
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;
        StarFlightMotion.Frame current = StarFlightMotion.sample(
                fromX, fromY, toX, toY, elapsedMillis, false);
        if (current.finished || current.opacity <= 0) return;

        // Three little points follow the same flight curve and fade out gently.
        for (int i = StarFlightMotion.TRAIL_PARTICLES; i >= 1; i--) {
            long past = Math.max(0, elapsedMillis - 64L * i);
            StarFlightMotion.Frame tail = StarFlightMotion.sample(
                    fromX, fromY, toX, toY, past, false);
            int alpha = (int) (155 * current.trailOpacity * (1.0 - .16 * i));
            if (alpha <= 0) continue;
            paint.setColor(0xffffe2a3);
            paint.setAlpha(alpha);
            canvas.drawCircle((float) (tail.x * getWidth()),
                    (float) (tail.y * getHeight()),
                    dp(2.8f) + (StarFlightMotion.TRAIL_PARTICLES - i), paint);
        }

        float cx = (float) (current.x * getWidth());
        float cy = (float) (current.y * getHeight());
        float radius = dp(12f) * (float) current.scale;
        canvas.save();
        canvas.rotate((float) current.rotationDegrees, cx, cy);
        drawStar(canvas, cx, cy, radius * 1.23f, 0xffffedb2, current.opacity);
        drawStar(canvas, cx, cy, radius, 0xfff4b83d, current.opacity);
        canvas.restore();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private void drawStar(Canvas canvas, float x, float y, float radius,
                          int color, double opacity) {
        Path star = new Path();
        for (int point = 0; point < 10; point++) {
            double angle = Math.PI * point / 5 - Math.PI / 2;
            float length = point % 2 == 0 ? radius : radius * .44f;
            float px = x + (float) Math.cos(angle) * length;
            float py = y + (float) Math.sin(angle) * length;
            if (point == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }
        star.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAlpha((int) Math.round(opacity * 255));
        canvas.drawPath(star, paint);
    }

    // Return false even during a flight: answer buttons remain usable immediately.
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }

    @Override protected void onDetachedFromWindow() {
        // A screen change must not leak an animator or retain the old Activity.
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }
}
