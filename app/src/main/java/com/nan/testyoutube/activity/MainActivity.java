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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.commit451.youtubeextractor.YouTubeExtractionResult;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
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
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetailsRegionRestriction;
import com.google.api.services.youtube.model.VideoListResponse;
import com.nan.testyoutube.util.CustomPlayerUiController;
import com.nan.testyoutube.view.AudioVolumeSetView;
import com.nan.testyoutube.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String BUTTON_TEXT = "Call YouTube Data API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE_READONLY};
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    List<Data> data = new ArrayList<>();
    Adapter adapter = new Adapter();
    YouTubePlayer youTube;
    AudioVolumeSetView audioVolumeSetView;
    Locale locale = Locale.getDefault();
    private Button mCallApiButton;
    private EditText editText;
    private RecyclerView recyclerView;
    private PlayerView playerView;
    private ExoPlayer player;
    private ImageView youtubeStart;
    private ImageView youtube_pause;
    private com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView threeYouTubeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerView);
        playerView = findViewById(R.id.playerView);
        youtubeStart = findViewById(R.id.youtube_start);
        youtube_pause = findViewById(R.id.youtube_pause);
        youtubeStart.setOnClickListener(v -> {
            if (youTube != null) {
                if (!youTube.isPlaying()) {
                    Log.i("result", "youtubeStart");
                    youTube.play();
                }
            }
        });
        youtube_pause.setOnClickListener(v -> {
            if (youTube != null) {
                if (youTube.isPlaying()) {
                    Log.i("result", "youtube_pause");
                    youTube.pause();
                }
            }
        });

        threeYouTubeView = findViewById(R.id.three_you_tube_player_view);
        View customPlayerUi = threeYouTubeView.inflateCustomPlayerUi(R.layout.layout_youtube_controll);
        threeYouTubeView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
                super.onReady(youTubePlayer);
                CustomPlayerUiController customPlayerUiController = new CustomPlayerUiController(MainActivity.this, customPlayerUi, youTubePlayer, threeYouTubeView);
                youTubePlayer.addListener(customPlayerUiController);
                threeYouTubeView.addFullScreenListener(customPlayerUiController);

                youTubePlayer.loadVideo("6JYIGclVQdw", 0);
            }
        });
        getLifecycle().addObserver(threeYouTubeView);

        audioVolumeSetView = findViewById(R.id.audioVolumeSetView);

        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());
        player.setPlayWhenReady(false);
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
                Log.i("ExoPlayer player", "timeline = " + timeline + ", manifest = " + manifest + ", reason = " + reason);
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.i("ExoPlayer player", "isLoading = " + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i("ExoPlayer player", "playWhenReady = " + playWhenReady + ", playbackState = " + playbackState);
//                Player.STATE_ENDED;
                Log.i("ExoPlayer player", "CurrentPosition = " + player.getCurrentPosition());
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.i("ExoPlayer player", "repeatMode = " + repeatMode);
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.i("ExoPlayer player", "shuffleModeEnabled = " + shuffleModeEnabled);
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.i("ExoPlayer player", "reason = " + reason);
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {
                Log.i("ExoPlayer player", "onSeekProcessed");
            }
        });

        playerView.setPlayer(player);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        mCallApiButton = findViewById(R.id.button);
        mCallApiButton.setOnClickListener(v -> {
            mCallApiButton.setEnabled(false);
            getResultsFromApi();
            mCallApiButton.setEnabled(true);
        });

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling YouTube Data API ...");

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        YouTubePlayerFragment fragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        fragment.initialize("552494725055-quiibufudb983mslretos8hbqqrcf1to.apps.googleusercontent.com", new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTube = youTubePlayer;
                youTube.setPlaylistEventListener(new YouTubePlayer.PlaylistEventListener() {
                    @Override
                    public void onPrevious() {

                    }

                    @Override
                    public void onNext() {

                    }

                    @Override
                    public void onPlaylistEnded() {

                    }
                });
                youTube.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                    @Override
                    public void onLoading() {

                    }

                    @Override
                    public void onLoaded(String s) {
                        Log.i("onLoaded", s);
                    }

                    @Override
                    public void onAdStarted() {

                    }

                    @Override
                    public void onVideoStarted() {

                    }

                    @Override
                    public void onVideoEnded() {

                    }

                    @Override
                    public void onError(YouTubePlayer.ErrorReason errorReason) {

                    }
                });
                youTube.setShowFullscreenButton(false);
                youTube.getCurrentTimeMillis();
                youTube.getDurationMillis();
//                youTubePlayer.set
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                youTubeInitializationResult.getErrorDialog(MainActivity.this, 10).show();
                Log.d("onInitializationFailure", "");
            }
        });

//        onPlayer("http://www.youtube.com/get_video?video_id=h71sBojjcaI&t=SIGNATURE&fmt=18");
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
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

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
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

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
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

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private void onPlayer(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            MediaSource source = new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory("exoplayer-codelab")).createMediaSource(uri);
            player.prepare(source);
//                            player.seekTo(10000);
        }
    }

    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
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

        /**
         * Background task to call YouTube Data API.
         *
         * @param params no parameters needed for this task.
         */
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
                YouTube.Search.List search = mService.search().list("id,snippet");
                search.setKey(getString(R.string.client_id));
                String text = "mp6E_avCnXc,XDFaA9ujKwg"; //这是一个videoid
                if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                    text = editText.getText().toString().trim();
                }

                Log.i("result", "YouTube.Videos.List id = " + search.getVideoDuration());
                search.setQ(text);
//                search.set
                search.setType("video"); //搜索的类型
                //返回的数据有哪些，根据需求自己设定
//
                search.setFields("items(id/kind,id/videoId,snippet/title,snippet/description," +
                        "snippet/publishedAt,snippet/liveBroadcastContent," +
                        "snippet/channelTitle," +
                        "etag," +
                        "snippet/thumbnails/default/url)");
                search.setMaxResults(10L);
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
                    //Snippet: ChannelId = null, ChannelTitle = null
                    // , Description = null
                    // , Title = 赵浴辰《可乐》谢安琪演唱版
                    // , Thumbnails = {"default":{"url":"https://i.ytimg.com/vi/XDFaA9ujKwg/default.jpg"}}
                    // , LiveBroadcastContent = null
                    Log.i("result", "Snippet: ChannelId = " + result.getSnippet().getChannelId()
                            + ", ChannelTitle = " + result.getSnippet().getChannelTitle()
                            + ", Description = " + result.getSnippet().getDescription()
                            + ", Title = " + result.getSnippet().getTitle()
                            + ", Thumbnails = " + result.getSnippet().getThumbnails()
                            + ", PublishedAt = " + result.getSnippet().getPublishedAt()
                            + ", LiveBroadcastContent = " + result.getSnippet().getLiveBroadcastContent());

                    //Thumbnails Default: url = https://i.ytimg.com/vi/XDFaA9ujKwg/default.jpg, width = null, height = null
                    Thumbnail thumbnail = result.getSnippet().getThumbnails().getDefault();
                    Log.i("result", "Thumbnails Default: url = " + thumbnail.getUrl() + ", width = " + thumbnail.getWidth() + ", height = " + thumbnail.getHeight());
                    data.add(0, new Data(resourceId.getVideoId(), result.getSnippet().getTitle(), thumbnail.getUrl()));

                    YouTube.Videos.List list1 = mService.videos().list("id,snippet,contentDetails");
                    list1.setId(resourceId.getVideoId());
                    list1.setFields("items(contentDetails/duration,snippet/title,contentDetails/regionRestriction)");
                    VideoListResponse execute = list1.execute();
                    for (Video video : execute.getItems()) {
                        Log.i("result", "video title = " + video.getSnippet().getTitle() +
                                ", duration = " + video.getContentDetails().getDuration());
                        VideoContentDetailsRegionRestriction restriction = video.getContentDetails().getRegionRestriction();
                        if (restriction == null) {
                            Log.i("result", "restriction = null");
                        } else {
                            List<String> allowed = restriction.getAllowed();
                            StringBuilder stringBuilder = new StringBuilder();
                            for (String s : allowed) {
                                stringBuilder.append(s).append(',');
                            }
                            Log.i("result", "restriction.getAllowed = " + stringBuilder.toString());
                        }
                    }
                    //W/System.err:{
                    //W/System.err:   "code" : 403,
                    //W/System.err:   "errors" : [ {
                    //W/System.err:     "domain" : "global",
                    //W/System.err:     "message" : "Insufficient Permission: Request had insufficient authentication scopes.",
                    //W/System.err:     "reason" : "insufficientPermissions"
                    //W/System.err:   } ],
                    //W/System.err:   "message" : "Insufficient Permission: Request had insufficient authentication scopes."
                    //W/System.err: }
                    //这个方法需要用户权限
//                    YouTube.Comments.List snippet = mService.comments().list("snippet");
//                    snippet.setId(resourceId.getVideoId());
//                    snippet.setFields("items(snippet/authorDisplayName,snippet/textDisplay)");
//                    CommentListResponse listResponse = snippet.execute();
//                    for (Comment comment:listResponse.getItems()) {
//                        Log.i("result", "name = " + comment.getSnippet().getAuthorDisplayName()
//                        + ", TextDisplay = " + comment.getSnippet().getTextDisplay());
//                    }
                }
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
            adapter.notifyDataSetChanged();
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

    class Adapter extends RecyclerView.Adapter<Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            //        http://img.youtube.com/vi/3vcS0cMp19s/maxresdefault.jpg
            // 用这个地址也可以加载视频封面图片
            Glide.with(MainActivity.this).load("http://img.youtube.com/vi/" + data.get(i).getVideoID() + "/maxresdefault.jpg").into(holder.iv_photo);
            holder.tv_name.setText(data.get(i).getVideoName());
            holder.tv_name.setOnClickListener(v -> {
                Log.i("result", "setOnClickListener");
                if (youTube != null) {
                    youTube.cueVideo(data.get(i).getVideoID());
                } else {
                    Toast.makeText(holder.iv_photo.getContext(), "YouTube失败", Toast.LENGTH_SHORT).show();
                }
//                youtube_player_view.
                onOther(data.get(i).getVideoID());
                onOtherExtractor(holder, i);
            });
        }

        private void onOther(String video_id) {
            com.commit451.youtubeextractor.YouTubeExtractor extractor = com.commit451.youtubeextractor.YouTubeExtractor.create();
            extractor.extract(video_id).enqueue(new Callback<YouTubeExtractionResult>() {
                @Override
                public void onResponse(Call<YouTubeExtractionResult> call, Response<YouTubeExtractionResult> response) {
                    Log.i("result", "onOther = " + response.toString());
                }

                @Override
                public void onFailure(Call<YouTubeExtractionResult> call, Throwable t) {
                    Log.i("result", "onOther = " + t.toString());
                }
            });
        }

        //第三方解析工具
        private void onOtherExtractor(@NonNull Holder holder, int i) {
            //这个方法是异步请求
            new YouTubeExtractor(holder.iv_photo.getContext()) {

                @Override
                protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                    if (ytFiles != null) {
                        String url = null;
                        //返回的结果中会有不同尺寸的视频，可以根据需要去选择
                        for (int i = 0, size = ytFiles.size(); i < size; i++) {
                            if (ytFiles.get(i) != null) {
                                url = ytFiles.get(i).getUrl();
                                //部分log： Format = Format{itag=18, ext='mp4', height=360, fps=30, vCodec=null, aCodec=null, audioBitrate=96, isDashContainer=false, isHlsContent=false}, url = https://r1---sn-ipoxu-un5s.googlevideo.com/videoplayback?expire=1566921200&ei=kP1kXZXgMIfgqQGPnpFo&ip=61.222.32.25&id=o-AAUvz8eS3dUA_gxOjfWOjUSFTwnGnmD72ivVDa3La_1K&itag=18&source=youtube&requiressl=yes&mm=31%2C29&mn=sn-ipoxu-un5s%2Csn-un57sn7s&ms=au%2Crdu&mv=m&mvi=0&pl=24&initcwndbps=1163750&mime=video%2Fmp4&gir=yes&clen=21312193&ratebypass=yes&dur=359.862&lmt=1557569508280291&mt=1566899509&fvip=5&c=WEB&txp=5531432&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cmime%2Cgir%2Cclen%2Cratebypass%2Cdur%2Clmt&sig=ALgxI2wwRgIhAK1NJR5va9ENHMXRwxd6ZBTj_DeFCWfTiDmIPeOqkEk2AiEAmiP32W6w7jPRMnpmstel-Tsez-GfH7D3DSTPG4ddj20%3D&lsparams=mm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AHylml4wRQIhAInqtw-eNm9EI2nyArD-kUgmooqQxPF-rQlQhNZu0VumAiA5kw2w8hGLwf_CcZM5jOpvLlxAj-lu6UNL8KXwccg2LQ%3D%3D
                                Log.i("result", "onExtractionComplete: Format = " + ytFiles.get(i).getFormat() + ", url = " + ytFiles.get(i).getUrl());
                            } else {
                                Log.i("result", "ytFiles.get(i) = null, i = " + i);
                            }
                        }
                        Log.i("result", "url = " + url);

                        //根据需求选择不同尺寸的视频地址去播放，我这里随便选了一个
                        onPlayer(url);
                    } else {
                        Log.i("result", "没有该视频");
                    }
                }
//            }.extract("https://www.youtube.com/get_video_info?&video_id=" + data.get(i).getVideoID(), true, true);
//            }.extract("https://www.youtube.com/watch?v=" + data.get(i).getVideoID(), true, false);
            }.extract(data.get(i).getVideoID(), true, true);
//            }.extract(data.get(i).getVideoID(), true, true);
//            Uri uri = Uri.parse("https://www.youtube.com/watch?v=" + data.get(i).getVideoID());
//            MediaSource source = new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory("exoplayer-codelab")).createMediaSource(uri);
//            player.prepare(source);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class Holder extends RecyclerView.ViewHolder {
        private ImageView iv_photo;
        private TextView tv_name;

        public Holder(@NonNull View itemView) {
            super(itemView);
            iv_photo = itemView.findViewById(R.id.iv_photo);
            tv_name = itemView.findViewById(R.id.tv_name);
        }
    }

    class Data {
        private String videoID;
        private String videoName;
        private String photoUrl;

        public Data(String videoID, String videoName, String photoUrl) {
            this.videoID = videoID;
            this.videoName = videoName;
            this.photoUrl = photoUrl;
        }

        public String getVideoID() {
            return videoID;
        }

        public String getVideoName() {
            return videoName;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }
    }
}
