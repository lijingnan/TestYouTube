package com.nan.testyoutube.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Author:jingnan
 * Time:2019-08-29/10
 */
public class TestViewGroup extends FrameLayout {
    public TestViewGroup(@NonNull Context context) {
        super(context);
    }

    public TestViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TestViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("test", "ViewGroup dispatchTouchEvent");
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.i("test", "ViewGroup onInterceptTouchEvent");
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("test", "ViewGroup onTouchEvent");
        return super.onTouchEvent(event);
    }
}
