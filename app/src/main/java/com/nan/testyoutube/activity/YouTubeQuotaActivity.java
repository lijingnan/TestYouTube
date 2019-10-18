package com.nan.testyoutube.activity;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.nan.testyoutube.R;
import com.nan.testyoutube.util.CustomPlayerUiController;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class YouTubeQuotaActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private final static String TAG = "YouTubeQuotaActivity";
    private static final String BUTTON_TEXT = "Call YouTube Data API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE_READONLY};
    Locale locale = Locale.getDefault();
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    private TextView tv_player_state;
    private com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView threeYouTubeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_tube_quota);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling YouTube Data API ...");

        tv_player_state = findViewById(R.id.tv_player_state);
        threeYouTubeView = findViewById(R.id.three_you_tube_player_view);
        View customPlayerUi = threeYouTubeView.inflateCustomPlayerUi(R.layout.layout_youtube_controll);
        threeYouTubeView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
                super.onReady(youTubePlayer);
                CustomPlayerUiController customPlayerUiController = new CustomPlayerUiController(YouTubeQuotaActivity.this, customPlayerUi, youTubePlayer, threeYouTubeView);
                youTubePlayer.addListener(customPlayerUiController);
                threeYouTubeView.addFullScreenListener(customPlayerUiController);
                tv_player_state.setText("player onReady");
                tv_player_state.setOnClickListener(v -> {
                    tv_player_state.setText("player onPlay");
                    YouTubePlayerUtils.loadOrCueVideo(youTubePlayer, getLifecycle(), "Z4_hUghYO_A", 0f);
                });
            }
        });
        getLifecycle().addObserver(threeYouTubeView);
    }

    public void onStartSearch(View view) {
        getResultsFromApi();
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
//            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(YouTubeQuotaActivity.this, connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
//                    mOutputText.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
//                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return query();
//                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<String> query() {
            List<String> list = new ArrayList<>();
            try {
                Log.i("result", "search start");
                YouTube.Search.List search = mService.search().list("id");
                search.setKey(getString(R.string.client_id));
//                String text = "mp6E_avCnXc"; //这是一个videoid
                String text = "china"; //这是一个videoid

                search.setQ(text);
//                search.set
                search.setType("video"); //搜索的类型
                //返回的数据有哪些，根据需求自己设定
//
                search.setFields("items(id,snippet)");
                search.setMaxResults(30L);
//                Log.i("result", "Country = " + locale.getCountry());
                search.setRegionCode(locale.getCountry());
//                search.setRegionCode("US");
                search.setRelevanceLanguage(locale.getLanguage());
//                search.setLocation(locale.getCountry());
//                search.setRelevanceLanguage(locale.getLanguage());
                SearchListResponse response = search.execute();
                for (SearchResult result : response.getItems()) {
                    list.add(result.getId().toString());
                    ResourceId resourceId = result.getId();
                    //Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"XDFaA9ujKwg"},
                    // videoId= XDFaA9ujKwg, ChannelId = null
                    Log.i("result", "Etag = " + result.getEtag()
                            + ", kind = " + result.getKind()
                            + ", id = " + resourceId + ", videoId= " + resourceId.getVideoId() + ", ChannelId = " + resourceId.getChannelId());
                }
                Log.i("result", "search end");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return list;
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         *
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> channelInfo = new ArrayList<String>();
            ChannelListResponse result = mService.channels().list("snippet,contentDetails,statistics")
                    .setForUsername("GoogleDevelopers")
//                    .setForUsername("GoogleDevelopers")
                    .execute();
            List<Channel> channels = result.getItems();
            if (channels != null) {
                Channel channel = channels.get(0);
                channelInfo.add("This channel's ID is " + channel.getId() + ". " +
                        "Its title is '" + channel.getSnippet().getTitle() + ", " +
                        "and it has " + channel.getStatistics().getViewCount() + " views.");
            }
            mService.search().list("").getVideoDuration();
            return channelInfo;
        }

        @Override
        protected void onPreExecute() {
//            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
//                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
//                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
//                    mOutputText.setText("The following error occurred:\n"
//                            + mLastError.getMessage());
                }
            } else {
//                mOutputText.setText("Request cancelled.");
            }
        }
    }
    // TODO: 2019-10-17 测试结论：
    //1. 搜索指定video ID，占用2个配额；
    //2. 搜索文本，获取一条或其他条都是占用2个配额；
    //3. 跟list(...)里的多少无关；

//    搜索china得到的30ge结果
//    I/result: search start
//    D/libc-netbsd: getaddrinfo: www.googleapis.com get result from proxy gai_error = 0
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"Z4_hUghYO_A"}, videoId= Z4_hUghYO_A, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"MHTjfFCa_-g"}, videoId= MHTjfFCa_-g, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"6kiba36fYGo"}, videoId= 6kiba36fYGo, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"a0FOLQo_NKs"}, videoId= a0FOLQo_NKs, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"DO4FxAcKZls"}, videoId= DO4FxAcKZls, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"kl5279dWqGs"}, videoId= kl5279dWqGs, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"UjM43blzaRc"}, videoId= UjM43blzaRc, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"izohpKPd9CM"}, videoId= izohpKPd9CM, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"mpW1XGVRgj8"}, videoId= mpW1XGVRgj8, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"0VR3dfZf9Yg"}, videoId= 0VR3dfZf9Yg, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"mR5MsG2eHdc"}, videoId= mR5MsG2eHdc, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"Lmp51YN-7wc"}, videoId= Lmp51YN-7wc, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"gysKE3POUv0"}, videoId= gysKE3POUv0, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"RonGknHSKkE"}, videoId= RonGknHSKkE, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"OUBk5YQP128"}, videoId= OUBk5YQP128, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"ed4ryYokLzU"}, videoId= ed4ryYokLzU, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"z67BZ1T0ehU"}, videoId= z67BZ1T0ehU, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"qKPML48WTqs"}, videoId= qKPML48WTqs, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"t2RpjHLY9Ag"}, videoId= t2RpjHLY9Ag, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"wBWKakGw338"}, videoId= wBWKakGw338, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"5JZCNhdKVvE"}, videoId= 5JZCNhdKVvE, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"7F4FDISR5Sc"}, videoId= 7F4FDISR5Sc, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"-5_jTyh-3Ng"}, videoId= -5_jTyh-3Ng, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"1kK5gC0t-bU"}, videoId= 1kK5gC0t-bU, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"ZX-Oz3mLHng"}, videoId= ZX-Oz3mLHng, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"e6fdXMTRdvo"}, videoId= e6fdXMTRdvo, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"lzAESaVqix0"}, videoId= lzAESaVqix0, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"9zAewNzZNmY"}, videoId= 9zAewNzZNmY, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"dit7zmnzAh0"}, videoId= dit7zmnzAh0, ChannelId = null
//    I/result: Etag = null, kind = null, id = {"kind":"youtube#video","videoId":"XslKxYysulY"}, videoId= XslKxYysulY, ChannelId = null
//    I/result: search end

}
