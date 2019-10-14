package com.nan.testyoutube.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.nan.testyoutube.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void openMainActivity(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void openTestTouchEvent(View view) {
        startActivity(new Intent(this, TestTouchEvent.class));
    }

    public void openGoogleAdMobActivity(View view) {
        startActivity(new Intent(this, GoogleAdMobActivity.class));
    }
}
