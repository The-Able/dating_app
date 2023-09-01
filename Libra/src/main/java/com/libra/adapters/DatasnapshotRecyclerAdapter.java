/*
package com.libra.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.client.DataSnapshot;

public abstract class DataSnapshotRecyclerAdapter<T> extends RecyclerView.Adapter<DataSnapshotRecyclerAdapter.BaseHolder> {

    protected DataSnapshot mData;
    protected View.OnClickListener mOnClick;

    public DataSnapshotRecyclerAdapter() {
        this(null);
    }

    public DataSnapshotRecyclerAdapter(DataSnapshot data) {
        this.mData = data;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.mOnClick = listener;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClick;
    }

    @Override
    public int getItemCount() {
        return (int) mData.getChildrenCount();
    }

    public DataSnapshot getData() {
        return mData;
    }

    public T getItem(int position) {
        return (T) mData.getValue();
    }


    public void setData(DataSnapshot data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    protected abstract static class BaseHolder extends RecyclerView.ViewHolder {
        private Context mContext;

        public BaseHolder(View v) {
            super(v);
            mContext = v.getContext();
        }

        public Context getContext() {
            return mContext;
        }
    }

}
*/
