package com.billin.www.commondmodual.ui.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多类型 Adapter 的封装。
 * (有一个很难解决的问题: ViewHolder 会重复创建，针对这个问题可以选择使用 Decoration 方案解决。
 * 这一个方案还有很多不成熟的设计，最好使用 Decoration 这个复杂度低的方案）
 * <p>
 * Created by Billin on 2018/1/26.
 */
public class MultiGroupAdapter extends RecyclerView.Adapter<DataViewHolder> {

    @SuppressWarnings("NumericOverflow")
    private static final int GROUP_HEADER_FLAG = 1 << 31;

    private static final int GROUP_FOOTER_FLAG = 1 << 30;

    private static final int INVALIDATE_TYPE = 0;

    private static final int INVALIDATE_GROUP_INDEX = 0;

    private static final int GROUP_HEADER_INDEX = -1;

    private static final int GROUP_FOOTER_INDEX = -2;

    private LinkedHashMap<Integer, GroupDataItem> mDataViewHolder;

    private GroupDataItem.Observable mGroupDataObservable;


    public MultiGroupAdapter() {
        mDataViewHolder = new LinkedHashMap<>();

        mGroupDataObservable = new GroupDataItem.Observable() {
            @Override
            public void onHeaderAndFooterUpdate() {
                notifyDataSetChanged();
            }
        };
    }

    /**
     * @param tag 取值范围为 [1, 1073741823]
     */
    public void addGroup(int tag, GroupDataItem groupHolder) {
        if (tag <= 0 && tag >= GROUP_FOOTER_FLAG) {
            throw new RuntimeException("tag range is [1, 1073741823]");
        }

        mDataViewHolder.put(tag, groupHolder);

        // 为了 DataAdapter 的 footer and header 设定更新的时候，类型数据能得到及时更新
        groupHolder.setObservable(mGroupDataObservable);

        notifyDataSetChanged();
    }

    public GroupDataItem removeGroup(int tag) {
        GroupDataItem res = mDataViewHolder.remove(tag);

        // 不知道如果不设 null，垃圾回收会不会不去回收这个 Adapter
        res.setObservable(null);

        notifyDataSetChanged();
        return res;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == INVALIDATE_TYPE) {
            return null;
        }

        int groupIndex = getGroupIndex(viewType);

        if (isGroupHeaderType(viewType)) {
            return getDataViewHolder().get(groupIndex).onCreateGroupHeaderViewHolder(parent);
        } else if (isGroupFooterType(viewType)) {
            return getDataViewHolder().get(groupIndex).onCrateGroupFooterViewHolder(parent);
        } else {
            return getDataViewHolder().get(groupIndex).onCreateViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        int[] groupDataIndicator = getLocation(position);

        // 如果获取到位置不是 group header 或者 footer, 进行数据绑定工作
        if (isGroupContentType(getItemViewType(groupDataIndicator))) {
            //noinspection unchecked
            holder.setData(getDataViewHolder().get(groupDataIndicator[0])
                    .getData().get(groupDataIndicator[1]));
        } else {
            // 如果是 footer 或者 header, 传入 group tag 即可
            //noinspection unchecked
            holder.setData(groupDataIndicator[0]);
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (Integer i : getDataViewHolder().keySet()) {
            GroupDataItem groupData = getDataViewHolder().get(i);
            count += groupData.getItemCount();

            // 如果包含 group header 数量加 1
            if (groupData.isShowGroupHeader()) {
                count += 1;
            }

            // 如果包含 footer
            if (groupData.isShowGroupFooter()) {
                count += 1;
            }
        }

        return count;
    }

    private boolean isGroupHeaderType(int type) {
        return type != INVALIDATE_TYPE && (type & GROUP_HEADER_FLAG) != 0;
    }

    private boolean isGroupFooterType(int type) {
        return type != INVALIDATE_TYPE && (type & GROUP_FOOTER_FLAG) != 0;
    }

    private boolean isGroupContentType(int type) {
        return !isGroupFooterType(type) && !isGroupHeaderType(type);
    }

    private int getGroupIndex(int type) {
        // 把最高两位置为 0
        return ~GROUP_HEADER_FLAG & type & ~GROUP_FOOTER_FLAG;
    }

    /**
     * {@link MultiGroupAdapter#getItemViewType(int)}
     */
    private int getItemViewType(int[] indicator) {
        if (indicator[0] == INVALIDATE_GROUP_INDEX) {
            return INVALIDATE_TYPE;
        }

        if (indicator[1] == GROUP_HEADER_INDEX) {
            return GROUP_HEADER_FLAG | indicator[0];
        } else if (indicator[1] == GROUP_FOOTER_INDEX) {
            return GROUP_FOOTER_FLAG | indicator[0];
        }

        return indicator[0];
    }

    /**
     * 如果 position 对应的是内容，返回的 type 等于 group 对应的 tag
     * <p>
     * 如果 position 对应的是 group header, 返回的类型等于
     * {@link MultiGroupAdapter#GROUP_HEADER_FLAG} | tag.
     * <p>
     * 如果 position 对应的时 group footer, 返回的类型等于
     * {@link MultiGroupAdapter#GROUP_FOOTER_FLAG} | tag.
     * <p>
     * 如果 position 大于所有组数据加起来的数据长度将会返回
     * {@link MultiGroupAdapter#INVALIDATE_TYPE}.
     */
    @Override
    public final int getItemViewType(int position) {
        return getItemViewType(getLocation(position));
    }

    /**
     * 返回 [x, y] 表示这是 x 组第 y 个数据,
     * <p>
     * 返回[x, -1] 则表示这是 x 组的 group mark item.
     * <p>
     * 返回[x, {@link MultiGroupAdapter#GROUP_FOOTER_INDEX}] 表示这是 x 组的 footer item
     * <p>
     * 返回 [{@link MultiGroupAdapter#INVALIDATE_GROUP_INDEX},
     * {@link MultiGroupAdapter#GROUP_HEADER_INDEX} 表示 position 比所有组的数据加起来还大,
     */
    private int[] getLocation(int position) {
        int groupPosition = position;

        for (Integer i : getDataViewHolder().keySet()) {
            GroupDataItem groupData = getDataViewHolder().get(i);

            // 判断 position 位置是否是 mask，如果是直接返回就好了
            if (groupData.isShowGroupHeader()) {
                if (groupPosition == 0)
                    return new int[]{i, GROUP_HEADER_INDEX};
                else
                    groupPosition -= 1;
            }

            // 判断 position 位置是否在当前 group 里面
            int groupSize = groupData.getItemCount();
            if (groupPosition < groupSize) {
                return new int[]{i, groupPosition};
            } else {
                groupPosition -= groupSize;
            }

            // 判断 position 位置是否是 footer
            if (groupData.isShowGroupFooter()) {
                // 这里是 footer 的位置
                if (groupPosition == 0)
                    return new int[]{i, GROUP_FOOTER_INDEX};
                else
                    groupPosition -= 1;
            }
        }

        return new int[]{INVALIDATE_GROUP_INDEX, GROUP_HEADER_INDEX};
    }

    private int getInGroupIndex(int position) {
        return getLocation(position)[1];
    }

    private Map<Integer, GroupDataItem> getDataViewHolder() {
        return mDataViewHolder;
    }
}
