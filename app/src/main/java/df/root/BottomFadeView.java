package df.root;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Bottom fade for the console panel. The gradient ends in the console's own
 * colour, and the bottom corners are rounded to match its outline, so the fade
 * never paints over the panel's rounded bottom edge with a square block.
 */
public class BottomFadeView extends View {

    private final Paint paint = new Paint();
    private final Path clip = new Path();
    private final float density;

    private static final int[] COLORS = {
            0x000A0F18,
            0x550A0F18,
            0xB30A0F18,
            0xF20A0F18,
            0xFF0A0F18
    };
    private static final float[] POSITIONS = {0f, 0.48f, 0.74f, 0.94f, 1f};

    public BottomFadeView(Context context) {
        this(context, null);
    }

    public BottomFadeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
    }

    public BottomFadeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        paint.setShader(new LinearGradient(0, 0, 0, h, COLORS, POSITIONS, Shader.TileMode.CLAMP));
        float r = 18f * density;
        clip.reset();
        clip.addRoundRect(0, 0, w, h,
                new float[]{0, 0, 0, 0, r, r, r, r}, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int save = canvas.save();
        canvas.clipPath(clip);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        canvas.restoreToCount(save);
    }
}
