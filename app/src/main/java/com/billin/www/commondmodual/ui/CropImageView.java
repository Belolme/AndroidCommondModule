package com.billin.www.commondmodual.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;

/**
 * 显示裁剪框的 ImageView.
 * 这个版本应该是最简单的版本，后续应该添加的功能有旋转图片、移动图片、放大缩小图片和裁剪框自适应大小。
 * <p>
 * Create by Billin on 2019/3/5
 */
public class CropImageView extends AppCompatImageView {

    /**
     * 边框交点宽度,单位 dp。
     */
    private static final int FRAME_NODE_WIDTH = 10;

    /**
     * 边框的边的宽度，单位 dp。
     */
    private static final int FRAME_EDGE_WIDTH = 2;

    private float nodeWidth;

    private float edgeWith;

    /**
     * 裁剪框坐标，其坐标系基于图片尺寸坐标系。
     */
    private RectF frameCoord = new RectF();

    private RectF touchArea = new RectF();

    private Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float[] matrixValue = new float[16];

    private Matrix inverseMatrix = new Matrix();

    /**
     * 记录点击的裁剪点。<p>
     * 0 - 没有被点击<p>
     * 1 - left top 被点击<p>
     * 2 - left bottom 被点击<p>
     * 3 - right top 被点击<p>
     * 4 - right bottom 被点击
     */
    private int touchPoint = 0;

    /**
     * 记录触摸位置是否位于裁剪框内部。如果是那么移动触摸位置将会移动裁剪框。
     */
    private boolean touchFrame = false;

    private float lastPointX = 0;

    private float lastPointY = 0;

    /**
     * 裁剪框的宽高比例，范围为 [0, oo), 如果等于 0，那么表示无固定宽高比例，裁剪框比例可任意改变。
     */
    private float ratio = 0f;

    public CropImageView(Context context) {
        super(context);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        DisplayMetrics m = getContext().getResources().getDisplayMetrics();
        edgeWith = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, FRAME_EDGE_WIDTH, m);
        nodeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, FRAME_NODE_WIDTH, m);

        framePaint.setStrokeCap(Paint.Cap.ROUND);

        // 可能在初始化的时候配置了图片，所以这里同步一下尺寸
        configFrame();
    }

    /**
     * 继承 {@link android.widget.ImageView#setFrame(int, int, int, int)} 方法
     * 不继承 {@link android.view.View#onSizeChanged(int, int, int, int)}
     * 方法的原因是 {@link android.widget.ImageView} 是在 {@link android.widget.ImageView#setFrame(int, int, int, int)}
     * 方法里面处理 {@link Drawable} 尺寸相关设置的。
     */
    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean change = super.setFrame(l, t, r, b);

        // 有些 Drawable 依赖于 View 的尺寸
        configFrame();

        return change;
    }

    /**
     * 设置裁剪框的宽高比例，ratio 的范围为 [0, oo)。当设置为 0 时，表示裁剪框无固定宽高比。
     */
    public void setRatio(float ratio) {
        this.ratio = ratio;
        if (frameCoord != null) frameCoord.setEmpty();
        configFrame();
    }

    /**
     * 设置裁剪框的颜色
     */
    public void setFrameTint(int tint) {
        framePaint.setColor(tint);
    }

    /**
     * 获取裁剪框相对于图片矩形坐标
     */
    public RectF getFrameRect() {
        return new RectF(frameCoord);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if (frameCoord != null) frameCoord.setEmpty();
        configFrame();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (frameCoord != null) frameCoord.setEmpty();
        configFrame();
    }

    private void configFrame() {
        Drawable d = getDrawable();
        // ImageView 在初始化调用了 setImageDrawable 所以 frameCoord 会出现 null！！
        if (d != null && frameCoord != null) {
            float dW = d.getIntrinsicWidth();
            dW = dW < 0 ? d.getBounds().width() : dW;
            float dH = d.getIntrinsicHeight();
            dH = dH < 0 ? d.getBounds().height() : dH;

            // 如果符合以下判断不重新生成裁剪框，
            // 这么做是为了在旋转屏幕等操作重新生成这个视图的时候裁剪框状态保持不变。
            // 不过这里会出现一个问题，就是设置不同图片时，裁剪框的状态会随着
            // 设置的图片尺寸变换而变换（目前的解决方案是在设置图片的时候把 frameCoord 重置了）。
            if (frameCoord.width() != 0
                    && frameCoord.height() != 0
                    && frameCoord.width() <= dW
                    && frameCoord.height() <= dH) {
                return;
            }

            // 根据 ratio 生成对应的裁剪框
            if (ratio == 0) {
                frameCoord.left = 0;
                frameCoord.right = dW;
                frameCoord.top = 0;
                frameCoord.bottom = dH;
            } else {
                if (dW / ratio < dH) {
                    frameCoord.left = 0;
                    frameCoord.right = dW;
                    frameCoord.top = 0;
                    frameCoord.bottom = dW / ratio;
                } else {
                    frameCoord.top = 0;
                    frameCoord.bottom = dH;
                    frameCoord.left = 0;
                    frameCoord.right = ratio * dH;
                }
            }

            // 移动到中间
            frameCoord.offset(dW / 2f - frameCoord.width() / 2f,
                    dH / 2f - frameCoord.height() / 2f);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Drawable d = getDrawable();
        if (d == null) return super.onTouchEvent(event);

        float dW = d.getIntrinsicWidth();
        dW = dW < 0 ? d.getBounds().width() : dW;
        float dH = d.getIntrinsicHeight();
        dH = dH < 0 ? d.getBounds().height() : dH;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Matrix matrix = getImageMatrix();
            float touchWidth = nodeWidth * 3f;

            inverseMatrix.reset();
            if (matrix != null) {
                matrix.invert(inverseMatrix);
                inverseMatrix.getValues(matrixValue);
                touchWidth *= Math.max(matrixValue[Matrix.MSCALE_Y], matrixValue[Matrix.MSCALE_X]);
            }

            matrixValue[0] = event.getX() - getPaddingLeft();
            matrixValue[1] = event.getY() - getPaddingTop();
            inverseMatrix.mapPoints(matrixValue);

            float xInImg = matrixValue[0];
            float yInImg = matrixValue[1];
            lastPointX = xInImg;
            lastPointY = yInImg;

            setTouchArea(xInImg, yInImg, touchWidth);
            if (xInImg < dW / 2) {
                // 如果触摸点是在图片左半部分，优先检测位于 3 位置和 4 位置的点。
                // 这么做的原因是如果位于左半部分优先检测 1 位置和 2 位置的点，那么在 ratio
                // 不为 0 的情况下会导致裁剪框超出图片边界。
                touchPoint = touchArea.contains(frameCoord.left, frameCoord.top) ? 1 : 0;
                touchPoint = touchArea.contains(frameCoord.left, frameCoord.bottom) ? 2 : touchPoint;
                touchPoint = touchArea.contains(frameCoord.right, frameCoord.top) ? 3 : touchPoint;
                touchPoint = touchArea.contains(frameCoord.right, frameCoord.bottom) ? 4 : touchPoint;
            } else {
                // 如果触摸点是在图片右半部分，优先检测位于 1 位置和 2 位置的点，原因同上。
                touchPoint = touchArea.contains(frameCoord.right, frameCoord.top) ? 3 : 0;
                touchPoint = touchArea.contains(frameCoord.right, frameCoord.bottom) ? 4 : touchPoint;
                touchPoint = touchArea.contains(frameCoord.left, frameCoord.top) ? 1 : touchPoint;
                touchPoint = touchArea.contains(frameCoord.left, frameCoord.bottom) ? 2 : touchPoint;
            }

            if (touchPoint > 0) return true;

            touchArea.set(frameCoord);
            touchFrame = touchArea.contains(xInImg, yInImg);
            if (touchFrame) return true;

        } else if ((touchPoint > 0 || touchFrame) && event.getAction() == MotionEvent.ACTION_MOVE) {
            matrixValue[0] = event.getX() - getPaddingLeft();
            matrixValue[1] = event.getY() - getPaddingTop();

            inverseMatrix.mapPoints(matrixValue);
            float xInImg = matrixValue[0];
            float yInImg = matrixValue[1];

            // 移动裁剪框
            if (touchFrame) {
                float offsetX = xInImg - lastPointX;
                float offsetY = yInImg - lastPointY;

                if (frameCoord.left + offsetX < 0) offsetX = 0 - frameCoord.left;
                if (frameCoord.right + offsetX > dW) offsetX = dW - frameCoord.right;
                if (frameCoord.top + offsetY < 0) offsetY = 0 - frameCoord.top;
                if (frameCoord.bottom + offsetY > dH) offsetY = dH - frameCoord.bottom;

                frameCoord.offset(offsetX, offsetY);
                lastPointX = xInImg;
                lastPointY = yInImg;
                invalidate();

                return true;
            }

            // 移动裁剪点
            // 限制裁剪框在图片内部
            if (xInImg < 0) xInImg = 0;
            if (yInImg < 0) yInImg = 0;
            if (xInImg > dW) xInImg = dW;
            if (yInImg > dH) yInImg = dH;

            float tmp;
            switch (touchPoint) {

                case 1:
                    frameCoord.top = yInImg;
                    if (ratio == 0) {
                        frameCoord.left = xInImg;
                    } else {
                        tmp = frameCoord.right - frameCoord.height() * ratio;
                        if (tmp > frameCoord.right) {
                            // 判断 tmp > frameCoord.right 的原因是计算有误差，
                            // 下面这样操作的原因也是如此。
                            frameCoord.left = frameCoord.right;
                        } else if (tmp < 0) {
                            frameCoord.left = 0;
                            frameCoord.top = frameCoord.bottom - frameCoord.width() / ratio;
                        } else {
                            frameCoord.left = tmp;
                        }
                    }
                    break;

                case 2:
                    frameCoord.bottom = yInImg;
                    if (ratio == 0) {
                        frameCoord.left = xInImg;
                    } else {
                        tmp = frameCoord.right - frameCoord.height() * ratio;
                        if (tmp > frameCoord.right) {
                            frameCoord.left = frameCoord.right;
                        } else if (tmp < 0) {
                            frameCoord.left = 0;
                            frameCoord.bottom = frameCoord.width() / ratio + frameCoord.top;
                        } else {
                            frameCoord.left = tmp;
                        }
                    }
                    break;

                case 3:
                    frameCoord.top = yInImg;
                    if (ratio == 0) {
                        frameCoord.right = xInImg;
                    } else {
                        tmp = frameCoord.left + frameCoord.height() * ratio;
                        if (tmp < frameCoord.left) {
                            frameCoord.right = frameCoord.left;
                        } else if (tmp > dW) {
                            frameCoord.right = dW;
                            frameCoord.top = frameCoord.bottom - frameCoord.width() / ratio;
                        } else {
                            frameCoord.right = tmp;
                        }
                    }
                    break;

                case 4:
                    frameCoord.bottom = yInImg;
                    if (ratio == 0) {
                        frameCoord.right = xInImg;
                    } else {
                        tmp = frameCoord.left + frameCoord.height() * ratio;
                        if (tmp < frameCoord.left) {
                            frameCoord.right = frameCoord.left;
                        } else if (tmp > dW) {
                            frameCoord.right = dW;
                            frameCoord.bottom = frameCoord.top + frameCoord.width() / ratio;
                        } else {
                            frameCoord.right = tmp;
                        }
                    }
                    break;
            }

            // 出现翻转，需要处理一下映射关系
            // 如果是 ratio 不为 0 的情况，那么不需要左右转换
            if (frameCoord.left > frameCoord.right && ratio == 0) {
                touchPoint = touchPoint <= 2 ? touchPoint + 2 : touchPoint - 2;
            }
            if (frameCoord.top > frameCoord.bottom) {
                touchPoint = touchPoint % 2 == 0 ? touchPoint - 1 : touchPoint + 1;
            }
            frameCoord.sort();

            invalidate();

            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            touchFrame = false;
            touchPoint = 0;
        }

        return super.onTouchEvent(event);
    }

    /**
     * @param x          触摸点的 x 坐标
     * @param y          触摸点的 y 坐标
     * @param touchWidth 构建触摸矩形的宽度
     */
    private void setTouchArea(float x, float y, float touchWidth) {
        float offset = touchWidth / 2f;
        touchArea.left = x - offset;
        touchArea.right = x + offset;
        touchArea.top = y - offset;
        touchArea.bottom = y + offset;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getDrawable() == null) return;

        // 以下代码完全照搬 ImageView onDraw 代码
        Matrix matrix = getImageMatrix();
        if (matrix == null && getPaddingTop() == 0 && getPaddingLeft() == 0) {
            drawFrame(canvas, 1);
        } else {
            final int saveCount = canvas.getSaveCount();
            canvas.save();

            if (getCropToPadding()) {
                final int scrollX = getScrollX();
                final int scrollY = getScrollY();
                canvas.clipRect(scrollX + getPaddingLeft(), scrollY + getPaddingTop(),
                        scrollX + getRight() - getLeft() - getPaddingRight(),
                        scrollY + getBottom() - getTop() - getPaddingBottom());
            }

            canvas.translate(getPaddingLeft(), getPaddingTop());

            if (matrix != null) {
                canvas.concat(matrix);
                matrix.getValues(matrixValue);
                drawFrame(canvas, Math.max(matrixValue[Matrix.MSCALE_X], matrixValue[Matrix.MSCALE_Y]));
            } else {
                drawFrame(canvas, 1);
            }
            canvas.restoreToCount(saveCount);
        }
    }

    /**
     * @param scale 画布放大的比例。这个参数用于计算裁剪框的宽度，如果这个参数为 1，那么裁剪框边框宽度将随着
     *              图片的放大缩小而放大缩小。
     */
    private void drawFrame(Canvas canvas, float scale) {
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(edgeWith / scale);
        canvas.drawRect(frameCoord, framePaint);

        framePaint.setStyle(Paint.Style.FILL);
        framePaint.setStrokeWidth(nodeWidth / scale);
        canvas.drawPoint(frameCoord.left, frameCoord.top, framePaint);
        canvas.drawPoint(frameCoord.left, frameCoord.bottom, framePaint);
        canvas.drawPoint(frameCoord.right, frameCoord.top, framePaint);
        canvas.drawPoint(frameCoord.right, frameCoord.bottom, framePaint);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable p = super.onSaveInstanceState();
        SavedState s = new SavedState(p);
        s.frameLeft = frameCoord.left;
        s.frameRight = frameCoord.right;
        s.frameTop = frameCoord.top;
        s.frameBottom = frameCoord.bottom;

        return s;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState s = (SavedState) state;
        super.onRestoreInstanceState(((SavedState) state).getSuperState());

        frameCoord.left = s.frameLeft;
        frameCoord.top = s.frameTop;
        frameCoord.right = s.frameRight;
        frameCoord.bottom = s.frameBottom;
        invalidate();
    }

    private static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        float frameLeft;

        float frameTop;

        float frameRight;

        float frameBottom;

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        SavedState(Parcel in) {
            super(in);
            frameLeft = in.readFloat();
            frameTop = in.readFloat();
            frameRight = in.readFloat();
            frameBottom = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(frameLeft);
            out.writeFloat(frameTop);
            out.writeFloat(frameRight);
            out.writeFloat(frameBottom);
        }
    }
}
