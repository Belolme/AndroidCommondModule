package com.billin.www.commondmodual.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.lang.reflect.Field;

/**
 * 一个可以随意移动 {@link EmojiView} 的父容器，该布局继承 {@link FrameLayout}。
 * <p>
 * Create by Billin on 2019/5/15
 */
public class EmojiContainerLayout extends FrameLayout {

    private static final String TAG = "EmojiContainerLayout";

    private ViewDragHelper viewDragHelper;

    private Matrix targetInverseMatrix;

    private View target;

    private float[] tmpPoints;

    public EmojiContainerLayout(@NonNull Context context) {
        this(context, null);
    }

    public EmojiContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        viewDragHelper = ViewDragHelper.create(this, new ViewDragCallback());
        targetInverseMatrix = new Matrix();
        tmpPoints = new float[2];
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            if ((target = findTopEmojiView(event)) != null) {
                viewDragHelper.captureChildView(target, event.getPointerId(0));
                return true;
            }
        }

        if (target != null) {
            viewDragHelper.processTouchEvent(event);
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Nullable
    private View findTopEmojiView(MotionEvent ev) {
        int childCount = getChildCount();

        float x = ev.getX();
        float y = ev.getY();
        for (int i = childCount - 1; i >= 0; --i) {
            View child = getChildAt(i);
            if (!(child instanceof EmojiView)) {
                continue;
            }

            child.getMatrix().invert(targetInverseMatrix);
            tmpPoints[0] = x - child.getLeft();
            tmpPoints[1] = y - child.getTop();
            targetInverseMatrix.mapPoints(tmpPoints);

            if (tmpPoints[0] > 0 && tmpPoints[0] < child.getWidth()
                    && tmpPoints[1] > 0 && tmpPoints[1] < child.getHeight()) {
                return child;
            }
        }

        return null;
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        private static final float DRAG_MAX_FACTOR = 0.6f;

        private float[] tmpPoints = new float[8];

        private RectF tmpRect = new RectF();

        @Override
        public boolean tryCaptureView(@NonNull View view, int i) {
            return view instanceof EmojiView;
        }

        /**
         * 输入以 [left, top : right, bottom] 坐标构建的矩形，该矩形经过变换后形成一个新的矩形，
         * 最后输出一个能以 [minX, minY : maxX, maxY] 形式表示并包含变换后矩形的矩形。
         */
        private RectF getViewFrame(Matrix transform, int left, int top, int right, int bottom) {
            transform.postTranslate(left, top);

            int w = right - left;
            int h = bottom - top;

            tmpPoints[0] = 0;
            tmpPoints[1] = 0;
            tmpPoints[2] = w;
            tmpPoints[3] = 0;
            tmpPoints[4] = 0;
            tmpPoints[5] = h;
            tmpPoints[6] = w;
            tmpPoints[7] = h;

            transform.mapPoints(tmpPoints);

            float minX = tmpPoints[0];
            float maxX = tmpPoints[0];
            for (int i = 2; i < tmpPoints.length; i += 2) {
                minX = Math.min(minX, tmpPoints[i]);
                maxX = Math.max(maxX, tmpPoints[i]);
            }

            float minY = tmpPoints[1];
            float maxY = tmpPoints[1];
            for (int i = 3; i < tmpPoints.length; i += 2) {
                minY = Math.min(minY, tmpPoints[i]);
                maxY = Math.max(maxY, tmpPoints[i]);
            }

            tmpRect.set(minX, minY, maxX, maxY);
            return tmpRect;
        }

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);

            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child instanceof EmojiView) {
                    if (child.isSelected()) child.setSelected(false);
                }
            }

            capturedChild.setSelected(true);
            bringChildToFront(capturedChild);
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);

            // 结束移动的时候把 gravity 设置为 NONE
            if (releasedChild.getLayoutParams() instanceof MarginLayoutParams && releasedChild instanceof EmojiView) {
                ((MarginLayoutParams) releasedChild.getLayoutParams()).leftMargin = releasedChild.getLeft();
                ((MarginLayoutParams) releasedChild.getLayoutParams()).topMargin = releasedChild.getTop();

                try {
                    Field gravityField = releasedChild.getLayoutParams().getClass().getDeclaredField("gravity");
                    gravityField.set(releasedChild.getLayoutParams(), Gravity.NO_GRAVITY);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    Log.w(TAG, "onTouchEvent: view parent layoutParam not have gravity attribution", e);
                }

                releasedChild.requestLayout();
            }
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {

            RectF viewFrame = getViewFrame(child.getMatrix(),
                    left, child.getTop(), left + child.getWidth(), child.getBottom());

            float externalDragRange = viewFrame.width() * DRAG_MAX_FACTOR;
            if ((viewFrame.left < 0 && dx > 0)
                    || (viewFrame.right > getWidth() && dx < 0)
                    || (viewFrame.left >= -externalDragRange && viewFrame.right <= getWidth() + externalDragRange)) {
                return left;
            } else {
                return left - dx;
            }
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            RectF viewFrame = getViewFrame(child.getMatrix(),
                    child.getLeft(), top, child.getRight(), top + child.getHeight());

            float externalDragRange = viewFrame.width() * DRAG_MAX_FACTOR;
            if ((viewFrame.top < 0 && dy > 0)
                    || (viewFrame.bottom > getHeight() && dy < 0)
                    || (viewFrame.top >= -externalDragRange && viewFrame.bottom <= getHeight() + externalDragRange)) {
                return top;
            } else {
                return top - dy;
            }
        }
    }
}
