package com.nan.testyoutube.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nan.testyoutube.R;

public class WebYouTubeSearchActivity extends AppCompatActivity {
    private static final String TAG = "WebYouTubeSearch";
    private TextView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_you_tube_search);
        search = findViewById(R.id.search);
        search.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", "china");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 100);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (data != null) {
            Log.i(TAG, "data = " + data);
        }
    }
}
