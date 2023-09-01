package com.libra.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.libra.R;


public class FullWidthImageView extends AppCompatImageView {
    public float ratio;

    public FullWidthImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public FullWidthImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FullWidthImageView(Context context) {
        super(context);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FullWidthImageView);
            float ratio2 = a.getFloat(R.styleable.FullWidthImageView_ratio, (float) -1);
            if (ratio == -1 || ratio2 != -1) //If we went through setImageDrawable before
                ratio = ratio2;

            a.recycle();
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable != null)
            ratio = ((float) drawable.getIntrinsicWidth()) / ((float) drawable.getIntrinsicHeight());
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        Drawable drawable = getDrawable();
        if (drawable != null)
            ratio = ((float) drawable.getIntrinsicWidth()) / ((float) drawable.getIntrinsicHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (ratio != -1) {
            super.onMeasure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec((int) (getMeasuredWidth() / ratio), MeasureSpec.EXACTLY));
        } else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
