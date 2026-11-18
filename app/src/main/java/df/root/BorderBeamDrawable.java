package df.root;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.view.animation.LinearInterpolator;

public class BorderBeamDrawable extends Drawable {

    private final float radius;
    private final Path path = new Path();
    private final PathMeasure measure = new PathMeasure();
    private final Paint base = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint beam = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] pos = new float[2];
    private final float[] tan = new float[2];
    private float phase;
    private float length;
    private float px, py;

    public BorderBeamDrawable(Context context, float radiusPx) {
        radius = radiusPx;
        base.setStyle(Paint.Style.STROKE);
        base.setStrokeWidth(1.5f);
        base.setColor(0x14FFFFFF);
        beam.setStyle(Paint.Style.STROKE);
        beam.setStrokeWidth(3.2f);
        beam.setStrokeCap(Paint.Cap.ROUND);

        boolean reduced = Settings.Global.getFloat(context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f;
        if (reduced) {
            phase = 0.1f;
        } else {
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(3600);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setInterpolator(new LinearInterpolator());
            anim.addUpdateListener(a -> {
                phase = (Float) a.getAnimatedValue();
                invalidateSelf();
            });
            anim.start();
        }
    }

    @Override
    protected void onBoundsChange(android.graphics.Rect bounds) {
        super.onBoundsChange(bounds);
        float inset = 1.5f;
        path.reset();
        path.addRoundRect(new RectF(bounds.left + inset, bounds.top + inset,
                bounds.right - inset, bounds.bottom - inset), radius, radius, Path.Direction.CW);
        measure.setPath(path, false);
        length = measure.getLength();
    }

    @Override
    public void draw(Canvas canvas) {
        if (length <= 0) return;
        canvas.drawPath(path, base);
        float seg = Math.min(length * 0.13f, 260f);
        float start = phase * length;
        int steps = 44;
        for (int i = 0; i < steps; i++) {
            float t0 = i / (float) steps;
            float t1 = (i + 1) / (float) steps;
            float d0 = (start + seg * t0) % length;
            float d1 = (start + seg * t1) % length;
            if (d1 < d0) continue;
            measure.getPosTan(d0, pos, tan);
            px = pos[0]; py = pos[1];
            float x0 = px, y0 = py;
            measure.getPosTan(d1, pos, tan);
            float fade = (float) Math.sin(Math.PI * t0);
            int color = Color.argb((int) (190 * fade), (int) (80 + 120 * t0), 210,
                    (int) (255 - 70 * t0));
            beam.setColor(color);
            canvas.drawLine(x0, y0, pos[0], pos[1], beam);
        }
    }

    @Override public void setAlpha(int a) { }
    @Override public void setColorFilter(ColorFilter f) { beam.setColorFilter(f); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
