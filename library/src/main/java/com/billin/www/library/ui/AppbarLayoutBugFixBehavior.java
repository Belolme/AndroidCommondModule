package com.billin.www.library.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ScrollerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

/**
 * 修复 API 25 以下 appbar 滑动存在的一些问题, 完全修复这些问题只能升级到 API 26 或者以上。
 * 没有修复的问题：当 appbar 出现的时候，下拉会卡顿没有 fling 效果
 * <p/>
 * Created by Billin on 2018/1/24.
 */
public class AppbarLayoutBugFixBehavior extends AppBarLayout.Behavior {

//    private boolean isPositive;

    private static final int VELOCITY_COEFFICIENT = 6;

    private ScrollerCompat mScroller;

    private FlingRunnable mFlingRunnable;

    public AppbarLayoutBugFixBehavior() {
        super();
    }

    public AppbarLayoutBugFixBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onNestedFling(@NonNull final CoordinatorLayout coordinatorLayout,
                                 @NonNull final AppBarLayout child, @NonNull final View target,
                                 float velocityX, float velocityY, boolean consumed) {
//        if (velocityY > 0 && !isPositive || velocityY < 0 && isPositive) {
//            velocityY = velocityY * -1;
//        }

        // 解决下拉滑动 appbar 被卡住的问题
        if (target instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) target;

            // only set a listener, albeit api is deprecated
            //noinspection deprecation
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy < 0
//                            && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING
                            && recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0)) == 0) {

                        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

                        // 引入下面注释解决方案存在 bug，fling 动画没有做完的时候快速往上拉不会使 appbar 位置向上滚动
//                        AppbarLayoutBugFixBehavior.super.onNestedFling(
//                                coordinatorLayout, child, target,
//                                0, dy * VELOCITY_COEFFICIENT, false);

                        // 这一个解决方案类似 API 26 的解决方案，如果能把 velocity 计算出来就完美了
                        fling(coordinatorLayout, child, recyclerView, -child.getTotalScrollRange(),
                                0, -dy * VELOCITY_COEFFICIENT);
                    }
                }
            });
        }

        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                  @NonNull AppBarLayout child, @NonNull View target,
                                  int dx, int dy, @NonNull int[] consumed) {
        if (mScroller != null)
            mScroller.abortAnimation();
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
//        isPositive = dy > 0;
    }

    private void fling(CoordinatorLayout coordinatorLayout,
                       AppBarLayout layout, RecyclerView target,
                       int minOffset, int maxOffset, float velocityY) {
        if (mFlingRunnable != null) {
            layout.removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }

        if (mScroller == null) {
            mScroller = ScrollerCompat.create(layout.getContext());
        }

        mScroller.fling(
                0, getTopAndBottomOffset(), // curr
                0, Math.round(velocityY), // velocity.
                0, 0, // x
                minOffset, maxOffset); // y

        if (mScroller.computeScrollOffset()) {
            mFlingRunnable = new FlingRunnable(coordinatorLayout, layout, target);
            ViewCompat.postOnAnimation(layout, mFlingRunnable);
        }
    }

    private class FlingRunnable implements Runnable {
        private final CoordinatorLayout mParent;
        private final AppBarLayout mLayout;
        private final RecyclerView mTarget;

        FlingRunnable(CoordinatorLayout parent, AppBarLayout layout, RecyclerView target) {
            mParent = parent;
            mLayout = layout;
            mTarget = target;
        }

        @Override
        public void run() {
            if (mLayout != null && mScroller != null) {
                if (mScroller.computeScrollOffset()) {
                    onNestedScroll(mParent, mLayout, mTarget,
                            0, 0, 0,
                            -mScroller.getCurrY() + mLayout.getTop());
                    // Post ourselves so that we run on the next animation
                    ViewCompat.postOnAnimation(mLayout, this);
                }
            }
        }
    }
}
