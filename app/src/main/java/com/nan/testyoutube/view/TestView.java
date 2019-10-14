package com.nan.testyoutube.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Author:jingnan
 * Time:2019-08-29/10
 */
public class TestView extends View {
    public TestView(Context context) {
        super(context);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.i("test", "View dispatchTouchEvent");
        return super.dispatchTouchEvent(event);
//        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("test", "View onTouchEvent");
        return super.onTouchEvent(event);
    }
}
