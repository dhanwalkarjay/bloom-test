package com.bloom.customer.ui.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bloom.R;

public class AspectRatioConstraintLayout extends ConstraintLayout {

    private float widthRatio = 1f;
    private float heightRatio = 1f;

    public AspectRatioConstraintLayout(Context context) {
        super(context);
    }

    public AspectRatioConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttributes(context, attrs);
    }

    public AspectRatioConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttributes(context, attrs);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioConstraintLayout);
        widthRatio = typedArray.getFloat(R.styleable.AspectRatioConstraintLayout_bloomWidthRatio, widthRatio);
        heightRatio = typedArray.getFloat(R.styleable.AspectRatioConstraintLayout_bloomHeightRatio, heightRatio);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width > 0 && widthRatio > 0f && heightRatio > 0f) {
            int height = Math.round(width * heightRatio / widthRatio);
            int exactHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, exactHeightSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
