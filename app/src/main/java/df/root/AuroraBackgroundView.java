package df.root;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Living background: three slow aurora blobs over a near-black field, plus a
 * faint dot grid and a vignette. Everything is painted with shaders, so the
 * animation is one invalidate per frame and no view hierarchy churn.
 */
public class AuroraBackgroundView extends View {

    private static final int BASE = 0xFF05070B;

    private final Paint basePaint = new Paint();
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint vignettePaint = new Paint();
    private final float density;

    private float w, h;
    private float phase;
    private ValueAnimator animator;

    public AuroraBackgroundView(Context context) {
        this(context, null);
    }

    public AuroraBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        basePaint.setColor(BASE);
        gridPaint.setColor(0x0DFFFFFF);
        boolean reduced = Settings.Global.getFloat(context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f;
        if (reduced) {
            phase = 0.18f;
        } else {
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(18000);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                phase = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        super.onSizeChanged(width, height, oldW, oldH);
        w = width;
        h = height;
        vignettePaint.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{0x22000000, 0x00000000, 0x00000000, 0x66000000},
                new float[]{0f, 0.18f, 0.72f, 1f}, Shader.TileMode.CLAMP));
    }

    private void blob(float cx, float cy, float radius, int inner, int outer) {
        blobPaint.setShader(new RadialGradient(cx, cy, radius, inner, outer,
                Shader.TileMode.CLAMP));
        canvasRef.drawCircle(cx, cy, radius, blobPaint);
    }

    private Canvas canvasRef;

    @Override
    protected void onDraw(Canvas canvas) {
        canvasRef = canvas;
        if (w <= 0 || h <= 0) return;
        double t = phase * Math.PI * 2;
        canvas.drawRect(0, 0, w, h, basePaint);

        // Faint dot grid: reads as depth without competing with the cards.
        float step = 30f * density;
        for (float x = step * 0.5f; x < w; x += step) {
            for (float y = step * 0.5f; y < h; y += step) {
                canvas.drawCircle(x, y, 0.8f * density, gridPaint);
            }
        }

        blob((float) (w * 0.12 + Math.sin(t) * w * 0.10),
                (float) (h * 0.10 + Math.cos(t * 0.7) * h * 0.06),
                w * 0.85f, 0x382BE8B0, 0x002BE8B0);

        blob((float) (w * 0.95 - Math.sin(t * 1.15) * w * 0.10),
                (float) (h * 0.34 + Math.cos(t * 0.9) * h * 0.08),
                w * 0.90f, 0x337C5CFF, 0x007C5CFF);

        blob((float) (w * 0.38 + Math.cos(t * 0.85) * w * 0.12),
                (float) (h * 0.92 + Math.sin(t * 1.05) * h * 0.05),
                w * 0.80f, 0x262BE8FF, 0x002BE8FF);

        canvas.drawRect(0, 0, w, h, vignettePaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animator == null) {
            boolean reduced = Settings.Global.getFloat(getContext().getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f;
            if (!reduced) {
                animator = ValueAnimator.ofFloat(phase, phase + 1f);
                animator.setDuration(18000);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setInterpolator(new LinearInterpolator());
                animator.addUpdateListener(a -> {
                    phase = (float) a.getAnimatedValue();
                    invalidate();
                });
                animator.start();
            }
        }
    }
}
