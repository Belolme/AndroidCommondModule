package com.billin.www.library.ui.recyclerview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 使得某个 item view 顶部显示特定文字的 decoration
 * <p/>
 * Created by Billin on 2018/1/26.
 */
public class VerticalListTextDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = "VerticalListTextDecoration";

    private Strategy mStrategy;

    private Paint mPaint;

    private int mTextColor;

    private int mBackgroundColor;

    private int mDecorationHeight;

    private int mPaddingLeft;

    private int mPaddingRight;

    public VerticalListTextDecoration(Strategy strategy) {
        mStrategy = strategy;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);

        mTextColor = Color.BLACK;

        mDecorationHeight = (int) (mPaint.getTextSize() + mPaint.descent());
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        final int backgroundLeft = parent.getLeft() + parent.getPaddingLeft();
        final int backgroundRight = parent.getRight() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();

            int viewAdapterPosition = params.getViewAdapterPosition();

            if (mStrategy.isDraw(viewAdapterPosition)) {

                final int backgroundBottom = child.getTop() - params.topMargin;
                CharSequence cs = mStrategy.getText(viewAdapterPosition);

                mPaint.setColor(mBackgroundColor);
                c.drawRect(backgroundLeft, backgroundBottom - mDecorationHeight,
                        backgroundRight, backgroundBottom, mPaint);

                mPaint.setColor(mTextColor);
                int textLeft = backgroundLeft + mPaddingLeft;
                int textRight = backgroundRight - mPaddingRight;

                // 文字置中，需要考虑 descent 空间。
                // 这里有个问题，如果设置 descent 那么中文就会显得不是居中的
                // 这个问题暂时放一边
                int baseLine = (int) (backgroundBottom
                        - (mDecorationHeight - mPaint.getTextSize()) / 2
                        - mPaint.descent() + 2);

                // 裁剪文字绘制的范围
                c.saveLayer(textLeft, backgroundBottom - mDecorationHeight,
                        textRight, backgroundBottom, mPaint, Canvas.ALL_SAVE_FLAG);
                c.drawText(cs, 0, cs.length(),
                        textLeft, baseLine, mPaint);
                c.restore();
            }
        }
    }


    public int getPaddingLeft() {
        return mPaddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.mPaddingLeft = paddingLeft;
    }

    public int getPaddingRight() {
        return mPaddingRight;
    }

    public void setPaddingRight(int paddingRight) {
        this.mPaddingRight = paddingRight;
    }

    /**
     * 设置 decoration 的高度，如果设置的高度小于文字大小，那么此高度不会生效
     *
     * @param height 大于文字大小的高度
     */
    public void setTextDecorationHeight(int height) {
        mDecorationHeight = height;
    }

    /**
     * 设置文字的大小，单位是 px
     */
    public void setTextSize(int size) {
        mPaint.setTextSize(size);

        if (size > mDecorationHeight)
            mDecorationHeight = size;
    }

    public void setTextColor(int color) {
        mTextColor = color;
    }

    public void setBackground(int mBackground) {
        this.mBackgroundColor = mBackground;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int adapterPosition =
                ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();

        if (mStrategy.isDraw(adapterPosition)) {
            outRect.top = mDecorationHeight;
        }
    }

    public interface Strategy {

        /**
         * 是否需要在位于 position 处的 item view 顶部显示文字
         *
         * @param position {@link RecyclerView} 中的某个视图的位置
         * @return draw text if true otherwise
         */
        boolean isDraw(int position);

        /**
         * 在 position 处的 item view 需要显示什么文字
         *
         * @param position {@link RecyclerView} 中的某个视图的位置
         * @return 需要显示的文字
         */
        CharSequence getText(int position);
    }
}
