package com.billin.www.library.ui.sticker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import com.billin.www.library.R;

import java.lang.reflect.Field;

/**
 * 贴纸视图，提供放大缩小、旋转和关闭操作。
 * <p>
 * Create by Billin on 2019/5/14
 */
public class EmojiView extends AppCompatImageView {

    private static final String TAG = "EmojiView";

    private static final int ACTION_CLOSE = 0;

    private static final int ACTION_SCALE = 1;

    private static final int ACTION_ROTATE = 2;

    private static final int ACTION_NONE = -1;

    private final int controlIconSize;

    private final int controlLineWidth;

    /**
     * emoji 贴图大小的最小值. 缩放操作缩小的贴图不能小于这个数值.
     */
    private int emojiMinSize = 76;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Path framePath = new Path();

    private Matrix contentMatrix = new Matrix();

    private Drawable closeDrawable;

    private Drawable rotateDrawable;

    private Drawable scaleDrawable;

    private int controlAction = ACTION_NONE;

    /**
     * 临时变量，支持储存 2 个点
     */
    private float[] tmpPoints = new float[4];

    /**
     * viewRatio = getWidth() / getHeight();
     */
    private float viewRatio = -1;

    /**
     * view 在 ViewGroup 的中心点，目前仅在放大缩小操作中做状态记录用。
     */
    private float centerX, centerY;

    public EmojiView(Context context) {
        this(context, null);
    }

    public EmojiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        controlLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, dm);
        controlIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 21, dm);
    }

    private void init(Context context) {
        paint.setColor(Color.parseColor("#ccf5a623"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(controlLineWidth);
        paint.setAntiAlias(true);

        closeDrawable = AppCompatResources.getDrawable(context, R.mipmap.emoji_delete);
        rotateDrawable = AppCompatResources.getDrawable(context, R.mipmap.emoji_rotate);
        scaleDrawable = AppCompatResources.getDrawable(context, R.mipmap.emoji_zoom);

        // importance! 让 View 的比例和 drawable 比例一致
        setAdjustViewBounds(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int controlSize = controlIconSize * 2;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 其分配的空间为 ViewGroup 分配空间减去两个控制按钮的空间
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width - controlSize < 0 ? 0 : width - controlSize, widthMode),
                MeasureSpec.makeMeasureSpec(height - controlSize < 0 ? 0 : height - controlSize, heightMode)
        );

        // 长宽加上两个控制按钮的大小
        int measureWidth = getMeasuredWidth() + controlSize;
        int measureHeight = getMeasuredHeight() + controlSize;

        measureWidth = resolveSize(measureWidth, widthMeasureSpec);
        measureHeight = resolveSize(measureHeight, heightMeasureSpec);

        setMeasuredDimension(measureWidth, measureHeight);
    }

    /**
     * 该方法不会在 {@link #setLeft(int)}, {@link #setRight(int)} 等方法中被调用，这些方法会直接在内部调用
     * {@link #onSizeChanged(int, int, int, int)} 方法。而{@link AppCompatImageView} 的这个方法才会重新
     * 计算 Drawable 绘制的位置和缩放参数。为了统一，不得不在这里做 matrix 的相关处理，另外不能使用
     * {@link #setLeft(int)} 等方法改变该 View 的宽高，只能使用 layoutParam.Width 改变。
     */
    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean change = super.setFrame(l, t, r, b);

        int w = r - l;
        int h = b - t;
        if (change) {
            // 把画布缩小到内容区域
            contentMatrix.reset();
            contentMatrix.setScale(
                    (w - 2 * controlIconSize) * 1f / w,
                    (h - 2 * controlIconSize) * 1f / h,
                    w / 2f,
                    h / 2f);

            // 设置 control icon 绘制的位置
            closeDrawable.setBounds(0, 0, controlIconSize, controlIconSize);
            rotateDrawable.setBounds(w - controlIconSize, 0, w, controlIconSize);
            scaleDrawable.setBounds(w - controlIconSize, h - controlIconSize, w, h);

            setPivotX((r - l) / 2f);
            setPivotY((b - t) / 2f);
        }

        return change;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isSelected()) return super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isControlBtnTouch(closeDrawable, event.getX(), event.getY())) {
                controlAction = ACTION_CLOSE;
            } else if (isControlBtnTouch(scaleDrawable, event.getX(), event.getY())) {
                controlAction = ACTION_SCALE;
            } else if (isControlBtnTouch(rotateDrawable, event.getX(), event.getY())) {
                controlAction = ACTION_ROTATE;
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            controlAction = ACTION_NONE;
        }

        if (controlAction == ACTION_NONE) return super.onTouchEvent(event);

        ViewGroup parent = ((ViewGroup) getParent());
        switch (controlAction) {
            case ACTION_CLOSE:
                if (event.getAction() == MotionEvent.ACTION_UP
                        && isControlBtnTouch(closeDrawable, event.getX(), event.getY())) {
                    parent.removeView(this);
                }
                break;

            case ACTION_ROTATE:
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float originX = getWidth() / 2f;
                    float originY = getHeight() / 2f;

                    tmpPoints[0] = event.getX();
                    tmpPoints[1] = event.getY();

                    getMatrix().mapPoints(tmpPoints);

                    float eventX = tmpPoints[0];
                    float eventY = tmpPoints[1];
                    float vectorX = eventX - originX;
                    float vectorY = eventY - originY;

                    double l = Math.sqrt(vectorX * vectorX + vectorY * vectorY);
                    double x = vectorX / l;
                    double angle = Math.acos(x) * 180.0 / Math.PI;
                    if (eventY == originX) {
                        if (eventX < originX) {
                            angle = -45;
                        } else {
                            angle = 45;
                        }
                    } else if (eventY < originX) {
                        angle = -(angle - 45);
                    } else {
                        angle = angle + 45;
                    }

                    if (!Double.isNaN(angle)) {
                        setRotation((float) angle);
                    }
                }

                break;

            case ACTION_SCALE:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    viewRatio = 1f * getWidth() / getHeight();

                    // 如果添加到 ViewGroup 的时候添加了 Gravity 属性，那么移除掉这个属性
                    // 这么做的原因是存在该属性的时候，是依据设置的 Gravity 属性放大缩小的，不是
                    // 以 closeDrawable 为坐标中心放大缩小。
                    if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) getLayoutParams()).leftMargin = getLeft();
                        ((ViewGroup.MarginLayoutParams) getLayoutParams()).topMargin = getTop();

                        // 记录当前 view 中心点，避免在放大缩小过程中出现太大的计算误差
                        centerX = getWidth() / 2f + getLeft();
                        centerY = getHeight() / 2f + getTop();
                    }

                    try {
                        Field gravityField = getLayoutParams().getClass().getDeclaredField("gravity");
                        gravityField.set(getLayoutParams(), Gravity.NO_GRAVITY);
                    } catch (NoSuchFieldException e) {
                        Log.w(TAG, "onTouchEvent: view parent layoutParam not have gravity attribution", e);
                    } catch (IllegalAccessException e) {
                        Log.w(TAG, "onTouchEvent: view parent layoutParam not have gravity attribution", e);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    // 只限制最小，不限制最大
                    // 保证 View 比例不变, 优先高依据移动的坐标点设置
                    float minSize = controlIconSize * 2f + emojiMinSize;
//                    float h = Math.max(event.getY() + controlIconSize / 2f, minSize);
//                    float w = h * viewRatio;

                    float h = Math.max(2 * event.getY() - getHeight() + controlIconSize, minSize);
                    float w = h * viewRatio;

                    if (w < minSize) {
                        w = minSize;
                        h = w / viewRatio;
                    }

                    if (getWidth() == w && getHeight() == h) {
                        break;
                    }

                    getLayoutParams().width = (int) w;
                    getLayoutParams().height = (int) h;
                    if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                        float leftMargin = centerX - w / 2f;
                        ((ViewGroup.MarginLayoutParams) getLayoutParams()).leftMargin = (int) leftMargin;
                        float topMargin = centerY - h / 2f;
                        ((ViewGroup.MarginLayoutParams) getLayoutParams()).topMargin = (int) topMargin;
                    }
                    requestLayout();
                }
                break;
        }

        // 重新初始化 controlAction
        if (event.getAction() == MotionEvent.ACTION_UP) {
            controlAction = ACTION_NONE;
        }

        return true;
    }

    private boolean isControlBtnTouch(Drawable controlBtn, float x, float y) {
        return controlBtn.getBounds().contains((int) x, (int) y);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (isSelected()) {
            // 绘制控制边框
            framePath.reset();
            framePath.moveTo(controlIconSize / 2f, controlIconSize / 2f);
            framePath.rLineTo(getWidth() - controlIconSize, 0);
            framePath.rLineTo(0, getHeight() - controlIconSize);
            framePath.rLineTo(-(getWidth() - controlIconSize), 0);
            framePath.close();
            canvas.drawPath(framePath, paint);

            // 绘制控制按钮

            closeDrawable.draw(canvas);
            rotateDrawable.draw(canvas);
            scaleDrawable.draw(canvas);
        }

        // 把画布缩小到内容区域再让 ImageView 绘制
        int saveCount = canvas.save();
        canvas.concat(contentMatrix);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
