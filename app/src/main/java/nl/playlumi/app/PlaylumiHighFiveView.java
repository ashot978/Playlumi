package nl.playlumi.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

/**
 * A brief cartoon high-five that floats near the existing mascot.
 * The animation is an untouchable decoration and never controls a game.
 */
public final class PlaylumiHighFiveView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;
    private int elapsedMillis;
    private boolean removed;

    private PlaylumiHighFiveView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public static void show(FrameLayout stage, View mascotAnchor) {
        if (stage == null || PlaylumiMotion.isReduced() ||
                (Build.VERSION.SDK_INT >= 26 && !ValueAnimator.areAnimatorsEnabled()) ||
                !stage.isAttachedToWindow() || stage.getWidth() <= 0 || stage.getHeight() <= 0) {
            return;
        }
        final int width = Math.min(stage.getWidth(), dp(stage, 198));
        final int height = Math.min(stage.getHeight(), dp(stage, 158));
        if (width <= 0 || height <= 0) return;
        final int[] stageAt = new int[2];
        stage.getLocationOnScreen(stageAt);
        int right = stage.getWidth() - dp(stage, 8);
        int top = dp(stage, 62);
        if (mascotAnchor != null && mascotAnchor.isAttachedToWindow()) {
            int[] anchorAt = new int[2];
            mascotAnchor.getLocationOnScreen(anchorAt);
            right = anchorAt[0] - stageAt[0] + mascotAnchor.getWidth() / 2 + width / 2;
            top = anchorAt[1] - stageAt[1] - dp(stage, 4);
        }
        int left = Math.max(0, Math.min(stage.getWidth() - width, right - width));
        int safeTop = Math.max(dp(stage, 46), Math.min(stage.getHeight() - height, top));
        FrameLayout.LayoutParams placement = new FrameLayout.LayoutParams(width, height);
        placement.leftMargin = left;
        placement.topMargin = safeTop;
        PlaylumiHighFiveView effect = new PlaylumiHighFiveView(stage.getContext());
        stage.addView(effect, placement);
        effect.start();
    }

    private static int dp(View view, int amount) {
        return (int) (amount * view.getResources().getDisplayMetrics().density + .5f);
    }

    private void start() {
        animator = ValueAnimator.ofInt(0, HighFiveMotion.DURATION_MILLIS);
        animator.setDuration(HighFiveMotion.DURATION_MILLIS);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            elapsedMillis = (Integer) a.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator a) { remove(); }
            @Override public void onAnimationCancel(Animator a) { remove(); }
        });
        animator.start();
    }

    private void remove() {
        if (removed) return;
        removed = true;
        if (getParent() instanceof ViewGroup) ((ViewGroup) getParent()).removeView(this);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;
        HighFiveMotion.Frame frame = HighFiveMotion.sample(elapsedMillis, false);
        if (frame.finished || frame.opacity <= 0) return;

        canvas.save();
        canvas.scale(getWidth() / 220f, getHeight() / 180f);
        canvas.saveLayerAlpha(0, 0, 220, 180, (int) (255 * frame.opacity));

        // A gentle pastel backdrop makes the reward readable on busy screens.
        paint.setColor(0xfff8e7ff);
        canvas.drawRoundRect(new RectF(10, 9, 210, 166), 33, 33, paint);
        paint.setColor(0xfffff7dd);
        canvas.drawRoundRect(new RectF(17, 16, 203, 157), 29, 29, paint);
        paint.setColor(0xff70509f);
        paint.setTextSize(21);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("3 OP RIJ!", 110, 43, paint);
        paint.setFakeBoldText(false);

        float bounce = (float) frame.hop;
        float sx = 79, sy = 105 - bounce * 25f;
        canvas.save();
        canvas.rotate((float) frame.tiltDegrees, sx, sy);
        star(canvas, sx, sy, 56f, 0xfff3b748);
        circle(canvas, sx - 17, sy - 2, 5.4f, 0xff4e3973);
        circle(canvas, sx + 17, sy - 2, 5.4f, 0xff4e3973);
        paint.setColor(0xff4e3973);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(new RectF(sx - 15, sy + 2, sx + 15, sy + 23),
                12, 155, false, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.restore();

        // Two hands approach, tap, and relax. They do not obscure touch input.
        float handY = 104 - bounce * 12f;
        float mine = 112 + 36 * (float) frame.handsMeet;
        float buddy = 192 - 42 * (float) frame.handsMeet;
        line(canvas, sx + 36, sy + 7, mine, handY, 0xff8d67bc, 10);
        line(canvas, 210, 126, buddy, handY, 0xff8d67bc, 10);
        circle(canvas, mine, handY, 11, 0xffffdfb9);
        circle(canvas, buddy, handY, 11, 0xffffdfb9);

        if (frame.sparkle > 0) {
            int alpha = (int) (245 * frame.sparkle);
            float dist = 13 + 17 * (float) frame.sparkle;
            for (int i = 0; i < 5; i++) {
                double angle = 2 * Math.PI * i / 5;
                float x = 150 + (float) Math.cos(angle) * dist;
                float y = handY + (float) Math.sin(angle) * dist;
                paint.setColor(i % 2 == 0 ? 0xffeaa75f : 0xffc189ec);
                paint.setAlpha(alpha);
                canvas.drawCircle(x, y, 3.8f, paint);
            }
        }
        canvas.restore();
        canvas.restore();
    }

    private void star(Canvas canvas, float x, float y, float radius, int color) {
        Path path = new Path();
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5 - Math.PI / 2;
            float length = radius * (i % 2 == 0 ? 1f : .58f);
            float px = x + (float) Math.cos(angle) * length;
            float py = y + (float) Math.sin(angle) * length;
            if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
        }
        path.close();
        paint.setColor(0xffc58c52);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawPath(path, paint);
    }

    private void circle(Canvas canvas, float x, float y, float radius, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, radius, paint);
    }

    private void line(Canvas canvas, float x, float y, float x2, float y2,
                      int color, float width) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(x, y, x2, y2, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }

    @Override protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }
}
