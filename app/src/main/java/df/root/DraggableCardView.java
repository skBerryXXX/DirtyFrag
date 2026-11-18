package df.root;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Glass card: a soft vertical gradient fill, a hairline gradient border and a
 * travelling light beam (BorderBeamDrawable) on the foreground.
 */
public class DraggableCardView extends FrameLayout {

    private static final float REST_RADIUS_DP = 24f;
    private static final float STROKE_W_DP = 1f;
    private static final float LIFT_Z_DP = 18f;
    private static final float DIM_ALPHA = 0.42f;
    private static final float PUSHED_SCALE = 0.94f;
    private static final long VISUAL_MS = 150;

    private static final int DEFAULT_FILL = 0xE6101622;

    private final GradientDrawable shape;
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF frame = new RectF();
    private final float density;
    private final int maxStroke;
    private final float liftZ;

    private int baseColor = DEFAULT_FILL;
    private int strokeColor = 0x552BE8B0;
    private int strokeWidth;
    private float radius;

    private boolean held, dimmed, pushed;
    private boolean hideable = true;

    public DraggableCardView(Context context) {
        this(context, null);
    }

    public DraggableCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DraggableCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = context.getResources().getDisplayMetrics().density;
        float bg = DEFAULT_FILL;
        radius = REST_RADIUS_DP * density;
        int sw = (int) (STROKE_W_DP * density + 0.5f);
        int sc = 0x552BE8B0;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DraggableCardView);
            bg = a.getColor(R.styleable.DraggableCardView_cardBackgroundColor, bg);
            radius = a.getDimension(R.styleable.DraggableCardView_cardCornerRadius, radius);
            sw = a.getDimensionPixelSize(R.styleable.DraggableCardView_strokeWidth, sw);
            sc = a.getColor(R.styleable.DraggableCardView_strokeColor, sc);
            a.recycle();
        }
        baseColor = bg;
        strokeColor = sc;
        strokeWidth = sw;
        maxStroke = Math.max(sw, (int) (1.2f * density + 0.5f));
        liftZ = LIFT_Z_DP * density;

        shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radius);
        shape.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        shape.setColors(new int[]{mix(bg, Color.WHITE, 0.06f), bg,
                mix(bg, Color.BLACK, 0.28f)});
        super.setBackground(shape);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1f, 0.9f * density));
        sheenPaint.setStyle(Paint.Style.STROKE);
        sheenPaint.setStrokeWidth(Math.max(1f, 0.8f * density));

        setForeground(new BorderBeamDrawable(context, radius));
        setClipToPadding(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        frame.set(0.5f, 0.5f, w - 0.5f, h - 0.5f);
        borderPaint.setShader(new LinearGradient(0, 0, w, h,
                new int[]{held ? 0xCC2BE8B0 : 0x552BE8B0,
                        held ? 0x997C5CFF : 0x337C5CFF,
                        held ? 0x663AA0FF : 0x1A3AA0FF},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        sheenPaint.setShader(new LinearGradient(0, 0, w, 0,
                0x1AFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawRoundRect(frame, radius, radius, borderPaint);
        // A short top sheen: one hairline that fades out before the right edge.
        float inset = radius * 0.8f;
        canvas.drawLine(inset, 1f * density, getWidth() * 0.62f, 1f * density, sheenPaint);
    }

    /** Held = lifted card: brighter glass and a warm outline. */
    public void setHeld(boolean held) {
        if (this.held == held) return;
        this.held = held;
        shape.setColors(held
                ? new int[]{0xF2263048, 0xF21A2133, 0xF2121826}
                : new int[]{mix(baseColor, Color.WHITE, 0.06f), baseColor,
                        mix(baseColor, Color.BLACK, 0.28f)});
        strokeWidth = held ? maxStroke : 0;
        setTranslationZ(held ? liftZ : 0f);
        onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
        invalidate();
    }

    public boolean isHeld() {
        return held;
    }

    /** Slight black-out so the lifted card reads as the selection. */
    public void setDimmed(boolean dimmed) {
        if (this.dimmed == dimmed) return;
        this.dimmed = dimmed;
        animate().alpha(dimmed ? DIM_ALPHA : 1f).setDuration(VISUAL_MS).start();
    }

    /** Cards pushed out of the slot shrink a touch, then bounce back. */
    public void setPushed(boolean pushed) {
        if (this.pushed == pushed) return;
        this.pushed = pushed;
        float s = pushed ? PUSHED_SCALE : 1f;
        animate().scaleX(s).scaleY(s).setDuration(VISUAL_MS).start();
    }

    public boolean isHideable() {
        return hideable;
    }

    public void setHideable(boolean hideable) {
        this.hideable = hideable;
    }

    public void setCardBackgroundColor(int color) {
        baseColor = color;
        shape.setColors(new int[]{mix(color, Color.WHITE, 0.06f), color,
                mix(color, Color.BLACK, 0.28f)});
        invalidate();
    }

    public void setStrokeColor(ColorStateList colors) {
        if (colors == null) return;
        strokeColor = colors.getColorForState(getDrawableState(), strokeColor);
        onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
        invalidate();
    }

    public void setStrokeWidth(int px) {
        strokeWidth = px;
        invalidate();
    }

    private static int mix(int color, int to, float amount) {
        int r = (int) (Color.red(color) * (1 - amount) + Color.red(to) * amount);
        int g = (int) (Color.green(color) * (1 - amount) + Color.green(to) * amount);
        int b = (int) (Color.blue(color) * (1 - amount) + Color.blue(to) * amount);
        return Color.argb(Color.alpha(color), r, g, b);
    }
}
