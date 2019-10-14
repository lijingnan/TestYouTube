package com.nan.testyoutube.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.nan.testyoutube.R;

public class GoogleAdMobActivity extends AppCompatActivity {
    private static final String TAG = "GoogleAdMobActivity";

    private AdView adView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_ad_mob);
        adView1 = findViewById(R.id.adView1);
        MobileAds.initialize(this, initializationStatus -> {

        });
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("33BE2250B43518CCDA7DE426D04EE231")
                .build();
        adView1.loadAd(request);
        adView1.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.i(TAG, "onAdLoaded");
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                super.onAdFailedToLoad(errorCode);
                Log.i(TAG, "onAdFailedToLoad errorCode = " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                super.onAdOpened();
                Log.i(TAG, "onAdOpened");
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                super.onAdClicked();
                Log.i(TAG, "onAdClicked");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                super.onAdLeftApplication();
                Log.i(TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                super.onAdClosed();
                Log.i(TAG, "onAdClosed");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView1.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        adView1.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adView1.destroy();
    }
}
