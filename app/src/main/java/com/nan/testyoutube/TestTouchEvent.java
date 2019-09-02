package com.nan.testyoutube;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class TestTouchEvent extends AppCompatActivity {

    TestView testView;

    TestViewGroup testViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_touch_event);

        testView = findViewById(R.id.testView);
        testViewGroup = findViewById(R.id.testViewGroup);

        testView.setOnClickListener(v -> Log.i("test", "View setOnClickListener"));
        testViewGroup.setOnClickListener(v -> Log.i("test", "ViewGroup setOnClickListener"));

        testView.setOnTouchListener((v, event) -> {
            Log.i("test", "View setOnTouchListener");
            return false;
        });
        testViewGroup.setOnTouchListener((v, event) -> {
            Log.i("test", "ViewGroup setOnTouchListener");
            return false;
        });
    }
}
