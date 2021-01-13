package com.billin.www.library.ui.recyclerview;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 使用空隔修饰 RecyclerView,
 * 目前只适用于 LinearLayoutManager 管理的 RecyclerView.
 * <p/>
 * Created by Billin on 2018/1/25.
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    private final int mSpaceInterval;

    private final int mOrientation;

    private final boolean mDrawLastItem;

    /**
     * @param px           空隔的像素大小
     * @param orientation  {@link LinearLayoutManager#HORIZONTAL} or {@link LinearLayoutManager#VERTICAL}
     * @param drawLastItem 是否在最后一个 item 后面绘制空隔
     */
    public SpacesItemDecoration(int px, int orientation, boolean drawLastItem) {
        mSpaceInterval = px;
        mOrientation = orientation;
        mDrawLastItem = drawLastItem;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        boolean isLastItem = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition()
                == parent.getAdapter().getItemCount() - 1;

        if (mDrawLastItem || !isLastItem) {
            if (mOrientation == LinearLayoutManager.HORIZONTAL) {
                outRect.right = mSpaceInterval;
            } else {
                outRect.bottom = mSpaceInterval;
            }
        }
    }
}