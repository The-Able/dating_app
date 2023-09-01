package com.libra.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;


public class DividerItem extends RecyclerView.ItemDecoration {

    private int mColor;

    private int mBottom;
    private int mTop;
    private int mLeft;
    private int mRight;
    private Paint mPaint = new Paint();
    private Context mContext;
    private int mPadding;

    public DividerItem(Context context) {
        this.mContext = context;
    }

    public void setHeight(@DimenRes int height) {
        this.mBottom = mContext.getResources().getDimensionPixelSize(height);
    }

    public void setColor(@ColorRes int color) {
        this.mColor = ContextCompat.getColor(mContext, color);
    }

    public void setPadding(@DimenRes int padding) {
        this.mPadding = mContext.getResources().getDimensionPixelSize(padding);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);

        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mBottom;
            c.drawRect(left + mPadding, top, right - mPadding, bottom, mPaint);
        }
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(mLeft, mTop, mRight, mBottom);
    }

}
