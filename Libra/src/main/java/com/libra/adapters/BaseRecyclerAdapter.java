package com.libra.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseHolder> {

  protected List<T> mData = new ArrayList<>();
  protected View.OnClickListener mOnClick;

  public BaseRecyclerAdapter() {
    this(null);
  }

  public BaseRecyclerAdapter(List<T> data) {
    addData(data);
  }

  public void setOnClickListener(View.OnClickListener listener) {
    this.mOnClick = listener;
  }

  public View.OnClickListener getOnClickListener() {
    return mOnClick;
  }

  @Override public int getItemCount() {
    return mData.size();
  }

  public List<T> getData() {
    return mData;
  }

  public T getItem(int position) {
    return mData.get(position);
  }

  public void setData(List<T> data) {
    //mData.clear();
    clearData();
    addData(data);
  }

  public void addData(List<T> data) {
    if (data != null) {
      int previousSize = mData.size();
      mData.addAll(data);
      notifyItemRangeInserted(previousSize, data.size());
    }
    //notifyDataSetChanged();
  }

  public void clearData() {
    int previousSize = mData.size();
    mData.clear();
    notifyItemRangeRemoved(0, previousSize);
  }

  public abstract static class BaseHolder extends RecyclerView.ViewHolder {
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
