package com.billin.www.library.ui.recyclerview.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

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
