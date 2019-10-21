package com.nan.testyoutube.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nan.testyoutube.R;

public class WebYouTubeSearchActivity extends AppCompatActivity {
    private static final String TAG = "WebYouTubeSearch";
    private TextView search_app;
    private TextView search_web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_you_tube_search);
        search_app = findViewById(R.id.search_app);
        search_app.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", "china");
            intent.putExtra("result", "id,snippet");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 100);
        });
        search_web = findViewById(R.id.search_web);
        search_web.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.youtube.com#searching"));
//            intent.setPackage("com.google.android.youtube");
//            intent.putExtra("query", "china");
//            intent.putExtra("result", "id,snippet");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 101);
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
