package com.android.libramanage.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;

public abstract class FireRecyclerAdapter<T> extends FirebaseRecyclerAdapter<T, FireRecyclerAdapter.BaseHolder> {

    private View.OnClickListener onClickListener;

    public FireRecyclerAdapter(Class<T> modelClass, int modelLayout, Class<BaseHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public FireRecyclerAdapter(Class<T> modelClass, int modelLayout, Class<BaseHolder> viewHolderClass, Firebase ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseHolder holder = super.onCreateViewHolder(parent, viewType);
        if (onClickListener != null) {
            holder.itemView.setOnClickListener(onClickListener);
        }
        return holder;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    protected static class BaseHolder extends RecyclerView.ViewHolder {
        private Context context;

        public BaseHolder(View itemView) {
            super(itemView);
            this.context = itemView.getContext();
        }

        public Context getContext() {
            return context;
        }
    }
}
