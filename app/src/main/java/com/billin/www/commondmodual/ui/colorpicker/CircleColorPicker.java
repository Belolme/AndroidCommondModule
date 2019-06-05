package com.billin.www.commondmodual.ui.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.ShapeDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Dimension;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Create by Billin on 2019/6/3
 */
public class CircleColorPicker extends View {

    @Dimension(unit = Dimension.DP)
    private static final int DEFAULT_SWITCH_DRAWABLE_SIZE = 24;

    /**
     * 默认的圆环半径和视图半径的比例.
     */
    private static final float DEFAULT_RING_AND_RADIUS_RATIO = 0.0382f;

    /**
     * 该值定义了在 360° 的色环中每隔多少度取色. 用户旋转色环中的开关，
     * 也应当只能获取这个色环中间隔该角度的颜色.
     */
    private static final int HSV_DEGREE_INTERVAL = 2;

    private int[] ringColor;

    private Paint ringPaint;
    private Paint switchPaint;

    @Nullable
    private ColorChangeListener colorChangeListener;

    /**
     * 该值定义了圆环半径和视图半径长度的比例.
     * 取值范围为 [0, 1]
     */
    @FloatRange(from = 0f, to = 1f)
    private float ringAndRadiusRatio;

    @Px
    private int ringRadius;

    private ShapeDrawable switchDrawable;

    private int switchSize;

    private RectF switchFrame;

    private float[] hsv;

    /**
     * The value defined degree of switch.
     * 0° 定义在 north 方向, 并且度数按顺时针方向增加
     */
    private float switchDegree = 0;

    public CircleColorPicker(Context context) {
        this(context, null);
    }

    public CircleColorPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleColorPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        switchSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_SWITCH_DRAWABLE_SIZE,
                getResources().getDisplayMetrics()
        );
        ringAndRadiusRatio = DEFAULT_RING_AND_RADIUS_RATIO;

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        switchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ringPaint.setStyle(Paint.Style.STROKE);

        switchPaint.setStyle(Paint.Style.FILL);

        switchDrawable = new ShapeDrawable(new OutlineOvalShape(Color.WHITE, Color.RED, 0.2f));
        switchDrawable.setBounds(0, 0, switchSize, switchSize);

        ringColor = new int[360 / HSV_DEGREE_INTERVAL];
        // init color ring in hsv format,
        // the ring start in east side.
        hsv = new float[3];
        hsv[1] = 1;
        hsv[2] = 1;
        for (int i = 0; i < ringColor.length; i++) {
            hsv[0] = i * 360f / ringColor.length;
            ringColor[i] = Color.HSVToColor(hsv);
        }

        switchFrame = new RectF();
    }

    @FloatRange(from = 0f, to = 1f)
    public float getRingAndRadiusRatio() {
        return ringAndRadiusRatio;
    }

    public void setRingAndRadiusRatio(@FloatRange(from = 0f, to = 1f) float ratio) {
        ratio = ratio < 0f ? 0f : ratio > 1f ? 1f : ratio;
        this.ringAndRadiusRatio = ratio;

        setupRingAndSwitch(getWidth(), getHeight());
        postInvalidate();
    }

    public void setColorChangeListener(@Nullable ColorChangeListener colorChangeListener) {
        this.colorChangeListener = colorChangeListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        int desireSize = Math.min(parentHeight, parentWidth);

        setMeasuredDimension(
                resolveSize(desireSize, widthMeasureSpec),
                resolveSize(desireSize, heightMeasureSpec)
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // init ring color
        SweepGradient gradient = new SweepGradient(w / 2f, h / 2f, ringColor, null);
        Matrix gradientMatrix = new Matrix();
        gradientMatrix.setRotate(-90, w / 2f, h / 2f);
        gradient.setLocalMatrix(gradientMatrix);
        ringPaint.setShader(gradient);

        setupRingAndSwitch(w, h);
    }

    private void setupRingAndSwitch(int w, int h) {
        // init ring radius
        int ringSize = (int) (ringAndRadiusRatio * Math.min(w, h) / 2);
        ringRadius = (Math.min(getWidth(), getHeight()) - Math.max(ringSize, switchSize)) / 2;
        ringPaint.setStrokeWidth(ringSize);

        // init switch frame
        setHue(switchDegree);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, ringRadius, ringPaint);

        canvas.translate(switchFrame.left, switchFrame.top);

        ((OutlineOvalShape) switchDrawable.getShape()).setContentColor(Color.HSVToColor(hsv));
        switchDrawable.draw(canvas);
    }

    private double getSwitchX() {
        double switchPosInRadians = switchDegree * Math.PI / 180.0;
        return Math.sin(switchPosInRadians) * ringRadius + getWidth() / 2.0;
    }

    private double getSwitchY() {
        double switchPosInRadians = switchDegree * Math.PI / 180.0;
        return -Math.cos(switchPosInRadians) * ringRadius + getHeight() / 2.0;
    }

    /**
     * @param frame switch position
     */
    private void getSwitchFrame(@NonNull RectF frame) {
        frame.left = (float) (getSwitchX() - switchSize / 2f);
        frame.top = (float) (getSwitchY() - switchSize / 2f);
        frame.right = frame.left + switchSize;
        frame.bottom = frame.top + switchSize;
    }

    /**
     * move switch to hue degree
     */
    public void setHue(@FloatRange(from = 0f, to = 360f) float hue) {
        switchDegree = hue;
        hsv[0] = hue;
        getSwitchFrame(switchFrame);
        invalidate();
    }

    /**
     * @return switch 停留地方的 hue.
     */
    public @FloatRange(from = 0f, to = 360f)
    float getHue() {
        return switchDegree;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN && switchFrame.contains(event.getX(), event.getY())) {
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            // event.getX，event.getY 坐标到 ring center 的归一化向量
            double x = event.getX() - getWidth() / 2f;
            double y = -(event.getY() - getHeight() / 2f);
            double l = Math.sqrt(x * x + y * y);

            x = x / l;
            y = y / l;

            // calculate degree
            double degree = Math.acos(y) * 180.0 / Math.PI;
            if (x < 0) degree = 360 - degree;

            // set hue value and invalidate view
            setHue((float) degree);
            if (colorChangeListener != null) colorChangeListener.onChange((float) degree);

            return true;
        }

        return super.onTouchEvent(event);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SaveState ss = new SaveState(superState);
        ss.hue = switchDegree;
        ss.ringRatio = ringAndRadiusRatio;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SaveState ss = (SaveState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        setRingAndRadiusRatio(ss.ringRatio);
        setHue(ss.hue);
    }

    public static class SaveState extends BaseSavedState {
        float hue;
        float ringRatio;

        SaveState(Parcelable superState) {
            super(superState);
        }

        private SaveState(Parcel in) {
            super(in);
            hue = in.readFloat();
            ringRatio = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(hue);
            out.writeFloat(ringRatio);
        }

        public static final Parcelable.Creator<SaveState> CREATOR
                = new Parcelable.Creator<SaveState>() {

            @Override
            public SaveState createFromParcel(Parcel source) {
                return new SaveState(source);
            }

            @Override
            public SaveState[] newArray(int size) {
                return new SaveState[size];
            }
        };
    }

    public interface ColorChangeListener {

        /**
         * Notify when hue had changed.
         */
        void onChange(float hue);
    }
}
