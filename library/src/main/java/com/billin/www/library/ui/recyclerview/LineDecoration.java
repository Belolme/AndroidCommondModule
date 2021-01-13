package com.billin.www.library.ui.recyclerview;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 使用线条修饰 RecyclerView, 目前只适用于 LinearLayoutManager 管理的 RecyclerView.
 * </p>
 * Created by Billin on 2018/1/25.
 */
public class LineDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    private final int mLinePx;

    private int mLeftPadding;

    private int mRightPadding;

    private int mTopPadding;

    private int mBottomPadding;

    private final boolean mDrawLastItem;

    /**
     * @param color              线条的颜色
     * @param px                 线条的粗度
     * @param drawBehindLastItem 是否在最后一个 item 后面绘制线条
     */
    public LineDecoration(int color, int px, boolean drawBehindLastItem) {
        mDivider = new ColorDrawable(color);
        mLinePx = px;
        mDrawLastItem = drawBehindLastItem;
    }

    /**
     * 设置绘制线条偏移值
     */
    private void setPadding(int left, int top, int right, int bottom) {
        mLeftPadding = left;
        mRightPadding = right;
        mTopPadding = top;
        mBottomPadding = bottom;
    }

    /**
     * 设置线条左偏移值
     */
    public void setPaddingLeft(int left) {
        setPadding(left, mTopPadding, mRightPadding, mBottomPadding);
    }

    /**
     * 设置线条右偏移值
     */
    public void setPaddingRight(int right) {
        setPadding(mLeftPadding, mTopPadding, right, mBottomPadding);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final boolean isVertical = ((LinearLayoutManager) parent.getLayoutManager())
                .getOrientation() == LinearLayoutManager.VERTICAL;

        // 这里的 getChildCount 仅仅是当前显示的数量，不是全部 item 的数量
        // 要获得 item 的全部数量，需要使用 getAdapter.getCount 方法
        final int childCount = parent.getChildCount();

        if (isVertical) {
            final int left = parent.getLeft() + parent.getPaddingLeft() + mLeftPadding;
            final int right = parent.getRight() - parent.getPaddingRight();

            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params =
                        (RecyclerView.LayoutParams) child.getLayoutParams();

                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mLinePx;

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        } else {
            // TODO: 2018/1/25 完成横向布局的 decorator
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (mDrawLastItem || !isAdapterLastView(view, parent)) {
            if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation()
                    == LinearLayoutManager.VERTICAL) {
                outRect.set(0, 0, 0, mLinePx);
            } else {
                outRect.set(0, 0, mLinePx, 0);
            }
        } else {
            outRect.set(0, 0, 0, 0);
        }
    }

    private boolean isAdapterLastView(View view, RecyclerView parent) {
        return ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition()
                == parent.getAdapter().getItemCount() - 1;
    }

}
