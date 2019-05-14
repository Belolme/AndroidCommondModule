package com.billin.www.commondmodual.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.billin.www.commondmodual.R;

/**
 * 贴纸视图，提供放大缩小、旋转和关闭操作。
 * <p>
 * Create by Billin on 2019/5/14
 */
public class EmojiView extends AppCompatImageView {

    private static final String TAG = "EmojiView";

    private static final int CONTROL_ICON_SIZE = 64;

    private static final int CONTROL_LINE_WIDTH = 8;

    private static final int ACTION_CLOSE = 0;

    private static final int ACTION_SCALE = 1;

    private static final int ACTION_ROTATE = 2;

    private static final int ACTION_NONE = -1;

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

    public EmojiView(Context context) {
        this(context, null);
    }

    public EmojiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(CONTROL_LINE_WIDTH);
        paint.setAntiAlias(true);

        closeDrawable = AppCompatResources.getDrawable(context, R.mipmap.emoji_delete);
        rotateDrawable = AppCompatResources.getDrawable(context, R.mipmap.emoji_rotate);
        scaleDrawable = AppCompatResources.getDrawable(context, R.mipmap.emoji_zoom);

        setPivotX(CONTROL_ICON_SIZE / 2f);
        setPivotY(CONTROL_ICON_SIZE / 2f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 长宽加上两个控制按钮的大小
        int width = getMeasuredWidth() + CONTROL_ICON_SIZE * 2;
        int height = getMeasuredHeight() + CONTROL_ICON_SIZE * 2;

        // 当 MeasureSpec Mode 为 AT_MOST 的时候，应该加上控制框的空间
        if (getAdjustViewBounds()) {
            // TODO: 2019/5/14  这里不知道为什么 ImageView 会限制 view 的范围在 ViewGroup 范围内
        } else {
            width = resolveSize(width, widthMeasureSpec);
            height = resolveSize(height, heightMeasureSpec);
        }

        setMeasuredDimension(width, height);
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
                    (w - 2 * CONTROL_ICON_SIZE) * 1f / w,
                    (h - 2 * CONTROL_ICON_SIZE) * 1f / h,
                    w / 2f,
                    h / 2f);

            // 设置 control icon 绘制的位置
            closeDrawable.setBounds(0, 0, CONTROL_ICON_SIZE, CONTROL_ICON_SIZE);
            rotateDrawable.setBounds(w - CONTROL_ICON_SIZE, 0, w, CONTROL_ICON_SIZE);
            scaleDrawable.setBounds(w - CONTROL_ICON_SIZE, h - CONTROL_ICON_SIZE, w, h);
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
                    float origin = CONTROL_ICON_SIZE / 2f;

                    tmpPoints[0] = event.getX();
                    tmpPoints[1] = event.getY();

                    getMatrix().mapPoints(tmpPoints);

                    float eventX = tmpPoints[0];
                    float eventY = tmpPoints[1];
                    float vectorX = eventX - origin;
                    float vectorY = eventY - origin;

                    double l = Math.sqrt(vectorX * vectorX + vectorY * vectorY);
                    double x = vectorX / l;
                    double angle = Math.acos(x) * 180.0 / Math.PI;
                    if (eventY == origin) {
                        if (eventX < origin) {
                            angle = 180;
                        } else {
                            angle = 0;
                        }
                    } else if (eventY < origin) {
                        angle = -angle;
                    }

                    if (!Double.isNaN(angle)) {
                        setRotation((float) angle);
                    }
                }

                break;

            case ACTION_SCALE:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    viewRatio = 1f * getWidth() / getHeight();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    // 只限制最小，不限制最大
                    // 保证 View 比例不变, 优先高依据移动的坐标点设置
                    float minSize = CONTROL_ICON_SIZE * 2f;
                    float h = Math.max(event.getY() + CONTROL_ICON_SIZE / 2f, minSize);
                    float w = h * viewRatio;
                    if (w < minSize) {
                        w = minSize;
                        h = w / viewRatio;
                    }

                    getLayoutParams().width = (int) w;
                    getLayoutParams().height = (int) h;
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
            framePath.moveTo(CONTROL_ICON_SIZE / 2f, CONTROL_ICON_SIZE / 2f);
            framePath.rLineTo(getWidth() - CONTROL_ICON_SIZE, 0);
            framePath.rLineTo(0, getHeight() - CONTROL_ICON_SIZE);
            framePath.rLineTo(-(getWidth() - CONTROL_ICON_SIZE), 0);
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
