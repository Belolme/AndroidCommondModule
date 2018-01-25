package com.billin.www.commondmodual.ui.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 比原始 Adapter 多加一个 loading more item
 * <p/>
 * Created by Billin on 2018/1/22.
 */
public abstract class LoadMoreAdapterWrapper extends RecyclerView.Adapter {

    public interface OnLoadingListener {
        void onLoading();
    }

    private class LoadMoreHolder extends RecyclerView.ViewHolder {
        LoadMoreHolder(View itemView) {
            super(itemView);
        }
    }

    private static int LOADING_MORE_TYPE = Integer.MAX_VALUE - 153;

    private RecyclerView.Adapter mInnerAdapter;

    private OnLoadingListener mOnLoadingListener;

    public LoadMoreAdapterWrapper(RecyclerView.Adapter adapter) {
        mInnerAdapter = adapter;

        // 为了原来的 adapter 的功能保持不变
        mInnerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                LoadMoreAdapterWrapper.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                LoadMoreAdapterWrapper.this.notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                LoadMoreAdapterWrapper.this.notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                LoadMoreAdapterWrapper.this.notifyItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                LoadMoreAdapterWrapper.this.notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    /**
     * @return load more item layout id
     */
    abstract int loadMoreRes();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOADING_MORE_TYPE && isLoadMore()) {
            View loadingMoreView = LayoutInflater.from(parent.getContext())
                    .inflate(loadMoreRes(), parent, false);
            return new LoadMoreHolder(loadingMoreView);
        }

        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof LoadMoreHolder)) {
            //noinspection unchecked
            mInnerAdapter.onBindViewHolder(holder, position);
            return;
        }

        if (mOnLoadingListener != null) {
            mOnLoadingListener.onLoading();
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = mInnerAdapter.getItemCount();
        if (itemCount == 0)
            return 0;

        return isLoadMore() ? itemCount + 1 : itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadMore() && isLastPosition(position)) {
            return LOADING_MORE_TYPE;
        }

        return mInnerAdapter.getItemViewType(position);
    }

    private boolean isLastPosition(int position) {
        return getItemCount() - 1 == position;
    }

    public void setOnLoadingListener(OnLoadingListener listener) {
        mOnLoadingListener = listener;
    }

    /**
     * 重写这一个方法，这个方法会在 {@link RecyclerView.Adapter} 初始化或者
     * {@link RecyclerView.Adapter#notifyDataSetChanged()} 被调用时调用。重绘后的 {@link RecyclerView}
     * 将会以这个方法的返回值决定是否添加一个额外的 load more item 到尾部。
     *
     * @return true will display view more item, other wise.
     */
    abstract boolean isLoadMore();
}
