package com.billin.www.commondmodual.ui.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 可以进行数据设置的 ViewHolder
 * <p>
 * Created by Billin on 2018/1/26.
 */
public abstract class DataViewHolder<V extends View, D> extends RecyclerView.ViewHolder {

    protected V contentView;

    public DataViewHolder(V itemView) {
        super(itemView);
        contentView = itemView;
    }

    public abstract void setData(D data);
}
