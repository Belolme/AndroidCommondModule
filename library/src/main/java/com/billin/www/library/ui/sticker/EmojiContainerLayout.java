package com.billin.www.library.ui.sticker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.customview.widget.ViewDragHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;

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

    private ImageView bg;

    private final ArrayList<View> mMatchParentChildren = new ArrayList<>(1);

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

        // 为了适配 drawable 尺寸大于 view 长宽，所以这里重写了 Background ImageView drawable 的测量方式
        bg = new AppCompatImageView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                if (getDrawable() != null && getDrawable().getIntrinsicWidth() != 0 && getDrawable().getIntrinsicHeight() != 0) {
                    int dw = getDrawable().getIntrinsicWidth();
                    int dh = getDrawable().getIntrinsicHeight();
                    int vw = getMeasuredWidth();
                    int vh = getMeasuredHeight();

                    // 设置 view ratio 符合 drawable ratio
                    float r = dw * 1f / dh;
                    if (vh * dw < dh * vw) {
                        setMeasuredDimension(
                                resolveSizeAndState((int) (r * vh), widthMeasureSpec, 0),
                                resolveSizeAndState(vh, heightMeasureSpec, 0)
                        );
                    } else {
                        setMeasuredDimension(
                                resolveSizeAndState(vw, widthMeasureSpec, 0),
                                resolveSizeAndState((int) (vw * 1f / r), heightMeasureSpec, 0)
                        );
                    }
                }
            }
        };
        bg.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        addView(bg);
    }

    public void setEmojiBackground(Drawable drawable) {
        bg.setImageDrawable(drawable);
    }

    public void addEmojiView(Bitmap bitmap) {
        addEmojiView(bitmap, null);
    }

    public void addEmojiView(Bitmap bitmap, @Nullable Integer tag) {
        EmojiView emojiView = new EmojiView(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        emojiView.setLayoutParams(params);
        emojiView.setImageBitmap(bitmap);
        emojiView.setTag(tag);
        addView(emojiView);
        selectView(emojiView);
    }

    /**
     * 以下方法直接取自 {@link FrameLayout}。修改为当测量 {@link EmojiView} 时不算入该 Layout 的大小。
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        final boolean measureMatchParentChildren = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE && !(child instanceof EmojiView)) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT || lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }

        // Account for padding too
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child instanceof EmojiView) {
                int widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
                int heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
                measureChild(child, widthSpec, heightSpec);
            }
        }

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Check against our foreground's minimum height and width
        final Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth()
                            - getPaddingLeft() - getPaddingRight()
                            - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight() +
                                    lp.leftMargin + lp.rightMargin, lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight()
                            - getPaddingTop() - getPaddingBottom()
                            - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() +
                                    lp.topMargin + lp.bottomMargin, lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
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
        } else {
            clearSelected();
        }

        return super.onTouchEvent(event);
    }

    /**
     * 清空所有 EmojiView 的选中状态
     */
    public void clearSelected() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof EmojiView) {
                if (child.isSelected()) child.setSelected(false);
            }
        }
    }

    /**
     * 选中指定的 {@link EmojiView}，同时清除上一个被选中 {@link EmojiView} 的状态.
     */
    private void selectView(EmojiView selectView) {
        clearSelected();
        selectView.setSelected(true);
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

            if (capturedChild instanceof EmojiView) {
                selectView((EmojiView) capturedChild);
                bringChildToFront(capturedChild);
            }
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
                } catch (NoSuchFieldException e) {
                    Log.w(TAG, "onTouchEvent: view parent layoutParam not have gravity attribution", e);
                } catch (IllegalAccessException e) {
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
