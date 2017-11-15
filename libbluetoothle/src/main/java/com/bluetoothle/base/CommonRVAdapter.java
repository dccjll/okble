package com.bluetoothle.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dccjll on 2017/3/22.
 * 通用的RecyclerView适配器
 */

@SuppressWarnings("ALL")
public abstract class CommonRVAdapter<T> extends RecyclerView.Adapter {

    private final Context context;
    private final int itemLayoutResId;
    private int nullItemLayoutResId = -1;
    private List<T> data;
    private static final int VIEW_TYPE_NULL = -1;

    public List<T> getData() {
        if (data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    public CommonRVAdapter(Context context, List<T> data, int itemLayoutResId, int nullItemLayoutResId) {
        this.context = context;
        this.data = data;
        this.itemLayoutResId = itemLayoutResId;
        this.nullItemLayoutResId = nullItemLayoutResId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_NULL) {
            return new RVViewHolder(LayoutInflater.from(context).inflate(nullItemLayoutResId, parent, false));
        } else {
            return new RVViewHolder(LayoutInflater.from(context).inflate(itemLayoutResId, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_NULL) {
            onBindNullDataViewHolder(this, (RVViewHolder) holder, position, null, null);
        } else {
            onBindViewHolder(this, (RVViewHolder) holder, position, data.get(position), data);
        }
    }

    @Override
    public int getItemCount() {
        return data == null || data.size() == 0 ? 1 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (data == null || data.size() == 0) {
            return VIEW_TYPE_NULL;
        }
        return super.getItemViewType(position);
    }

    public abstract void onBindNullDataViewHolder(RecyclerView.Adapter adapter, RVViewHolder rvViewHolder, int position, T entry, List<T> data);

    public abstract void onBindViewHolder(RecyclerView.Adapter adapter, RVViewHolder rvViewHolder, int position, T entry, List<T> data);

    public abstract T getSelectEntry();

    public void setData(List<T> data) {
        this.data = data;
    }

    public void flush(List<T> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void addFlush(T item) {
        if (data == null) {
            data = new ArrayList<>();
        }
        data.add(item);
        notifyDataSetChanged();
    }

    public void addFlush(T item,int position) {
        if (data == null) {
            data = new ArrayList<>();
        }
        data.add(position,item);
        notifyDataSetChanged();
    }

    public void update(T item){
        int position = data.indexOf(item);
        data.add(position+1,item);
        data.remove(position);
        notifyDataSetChanged();
    }

    public void update(T item,int position){
        data.add(position+1,item);
        data.remove(position);
        notifyDataSetChanged();
    }

    protected class RVViewHolder extends RecyclerView.ViewHolder {

        private final SparseArray<View> viewList;
        private final View itemView;

        RVViewHolder(View itemView) {
            super(itemView);
            viewList = new SparseArray<>();
            this.itemView = itemView;
        }

        public View findViewById(int viewResId) {
            View view = viewList.get(viewResId);
            if (view == null) {
                view = itemView.findViewById(viewResId);
                viewList.put(viewResId, view);
            }
            return view;
        }
    }
}
