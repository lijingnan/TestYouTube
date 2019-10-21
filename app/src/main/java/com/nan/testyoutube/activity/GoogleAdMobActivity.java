package com.nan.testyoutube.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.nan.testyoutube.R;

public class GoogleAdMobActivity extends AppCompatActivity {
    private static final String TAG = "GoogleAdMobActivity";

    private LinearLayout linearLayout;
    private AdView adView1;
    private AdView adView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_ad_mob);
        linearLayout = findViewById(R.id.linearLayout);
        adView1 = findViewById(R.id.adView1);
        adView2 = findViewById(R.id.adView2);
        MobileAds.initialize(this, initializationStatus -> {

        });
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("33BE2250B43518CCDA7DE426D04EE231")
                .build();
        AdLoader adLoader = new AdLoader.Builder(this, "33BE2250B43518CCDA7DE426D04EE231")
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int i) {
                        super.onAdFailedToLoad(i);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .setRequestCustomMuteThisAd(true)
                        .build())
                .build();
//        adView1.loadAd(adLoader);
        adView1.loadAd(request);
        adView2.loadAd(request);

        //直接使用java代码给布局中添加广告
        AdRequest request3 = new AdRequest.Builder()
                .addTestDevice("33BE2250B43518CCDA7DE426D04EE231")
                .build();
        AdView adView3 = new AdView(this);
        adView3.setAdSize(AdSize.SMART_BANNER);
        adView3.setAdUnitId(getString(R.string.test_unit_id));
        adView3.loadAd(request3);
        linearLayout.addView(adView3);
        adView1.setAdListener(new MyAdListener(1));
        adView2.setAdListener(new MyAdListener(2));
        adView3.setAdListener(new MyAdListener(3));
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

    class MyAdListener extends AdListener {
        private int id;

        public MyAdListener(int id) {
            this.id = id;
        }

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            Log.d(TAG, "onAdLoaded_" + id);
            // Code to be executed when an ad finishes loading.
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            // Code to be executed when an ad request fails.
            super.onAdFailedToLoad(errorCode);
            Log.d(TAG, "onAdFailedToLoad errorCode = " + errorCode);
        }

        @Override
        public void onAdOpened() {
            // Code to be executed when an ad opens an overlay that
            // covers the screen.
            super.onAdOpened();
            Log.d(TAG, "onAdOpened");
        }

        @Override
        public void onAdClicked() {
            // Code to be executed when the user clicks on an ad.
            super.onAdClicked();
            Log.d(TAG, "onAdClicked");
        }

        @Override
        public void onAdLeftApplication() {
            // Code to be executed when the user has left the app.
            super.onAdLeftApplication();
            Log.d(TAG, "onAdLeftApplication");
        }

        @Override
        public void onAdClosed() {
            // Code to be executed when the user is about to return
            // to the app after tapping on an ad.
            super.onAdClosed();
            Log.d(TAG, "onAdClosed");
        }
    }
}
