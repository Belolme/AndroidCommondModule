package com.billin.www.library.ui.recyclerview.adapter;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 单独一个 Group 的 ViewHolder 实现, 需要配合 {@link MultiGroupAdapter} 使用
 * <p>
 * Created by Billin on 2018/1/26.
 */
public abstract class GroupDataItem<D> {

    private Observable mObservable;

    private boolean mIsShowGroupHeader;

    private boolean mIsShowGroupFooter;

    private List<D> mData;

    public GroupDataItem(List<D> data) {
        this(data, false, false);
    }

    public GroupDataItem(List<D> data, boolean isShowGroupHeader, boolean isShowGroupFooter) {
        mData = data;

        mIsShowGroupHeader = isShowGroupHeader;
        mIsShowGroupFooter = isShowGroupFooter;
    }

    /**
     * 这一个方法是专门为 {@link MultiGroupAdapter} 设置的，用于当 isShowGroupXX 发生改变时，
     * Adapter 的数据能得到及时更新。
     */
    void setObservable(Observable observable) {
        this.mObservable = observable;
    }

    public boolean isShowGroupHeader() {
        return mIsShowGroupHeader;
    }

    public boolean isShowGroupFooter() {
        return mIsShowGroupFooter;
    }

    // FIXME: 2018/1/26 如果 mData 的数据长度改变了不调用 Notification，将会导致 bug
    public List<D> getData() {
        return mData;
    }

    public void updateIsShowGroupFooter(boolean isShow) {
        if (isShow != mIsShowGroupFooter) {
            mIsShowGroupFooter = isShow;

            if (mObservable != null)
                mObservable.onHeaderAndFooterUpdate();
        }
    }

    public void updateIsShowGroupHeader(boolean isShow) {
        if (isShow != mIsShowGroupHeader) {
            mIsShowGroupHeader = isShow;

            if (mObservable != null)
                mObservable.onHeaderAndFooterUpdate();
        }
    }

    protected abstract DataViewHolder<? extends View, D> onCreateViewHolder(ViewGroup parent);

    /**
     * MultiGroup 的 data 是该 group 的 tag
     */
    protected abstract DataViewHolder<? extends View, Integer> onCreateGroupHeaderViewHolder(ViewGroup parent);

    /**
     * 同 {@link GroupDataItem#onCreateGroupHeaderViewHolder}, MultiGroup 的 data 是该 group 的 tag
     * <p/>
     * 一个 group 的 footer (例如网易云音乐中热门 group 下面的显示更多按钮)
     */
    protected abstract DataViewHolder<? extends View, Integer> onCrateGroupFooterViewHolder(ViewGroup parent);

    public int getItemCount() {
        return mData.size();
    }

    interface Observable {
        void onHeaderAndFooterUpdate();
    }
}
