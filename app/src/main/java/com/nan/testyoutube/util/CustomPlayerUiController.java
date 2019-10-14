package com.nan.testyoutube.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.nan.testyoutube.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class CustomPlayerUiController extends AbstractYouTubePlayerListener implements YouTubePlayerFullScreenListener {

    private final View playerUi;
    private final YouTubePlayerTracker playerTracker;
    private Context context;
    private YouTubePlayer youTubePlayer;
    private YouTubePlayerView youTubePlayerView;
    // panel is used to intercept clicks on the WebView, I don't want the user to be able to click the WebView directly.
    private View panel;
    private View progressbar;
    private TextView videoCurrentTimeTextView;
    private TextView videoDurationTextView;
    private boolean fullscreen = false;

    public CustomPlayerUiController(Context context, View customPlayerUi, YouTubePlayer youTubePlayer, YouTubePlayerView youTubePlayerView) {
        this.playerUi = customPlayerUi;
        this.context = context;
        this.youTubePlayer = youTubePlayer;
        this.youTubePlayerView = youTubePlayerView;

        playerTracker = new YouTubePlayerTracker();
        youTubePlayer.addListener(playerTracker);

        initViews(customPlayerUi);
    }

    private void initViews(View playerUi) {
        panel = playerUi.findViewById(R.id.panel);
        progressbar = playerUi.findViewById(R.id.progressbar);
        videoCurrentTimeTextView = playerUi.findViewById(R.id.video_current_time);
        videoDurationTextView = playerUi.findViewById(R.id.video_duration);
        Button playPauseButton = playerUi.findViewById(R.id.play_pause_button);
//        Button enterExitFullscreenButton = playerUi.findViewById(R.id.enter_exit_fullscreen_button);

        playPauseButton.setOnClickListener((view) -> {
            if (playerTracker.getState() == PlayerConstants.PlayerState.PLAYING)
                youTubePlayer.pause();
            else youTubePlayer.play();
        });

//        enterExitFullscreenButton.setOnClickListener((view) -> {
//            if (fullscreen) youTubePlayerView.exitFullScreen();
//            else youTubePlayerView.enterFullScreen();
//
//            fullscreen = !fullscreen;
//        });
    }

    @Override
    public void onReady(YouTubePlayer youTubePlayer) {
        super.onReady(youTubePlayer);
        progressbar.setVisibility(View.GONE);
    }

    @Override
    public void onStateChange(YouTubePlayer youTubePlayer, PlayerConstants.PlayerState state) {
        super.onStateChange(youTubePlayer, state);
        if (state == PlayerConstants.PlayerState.PLAYING || state == PlayerConstants.PlayerState.PAUSED || state == PlayerConstants.PlayerState.VIDEO_CUED)
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        else if (state == PlayerConstants.PlayerState.BUFFERING)
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
    }

    @Override
    public void onCurrentSecond(YouTubePlayer youTubePlayer, float second) {
        super.onCurrentSecond(youTubePlayer, second);
        videoCurrentTimeTextView.setText(second + "");
    }

    @Override
    public void onVideoDuration(YouTubePlayer youTubePlayer, float duration) {
        super.onVideoDuration(youTubePlayer, duration);
        videoDurationTextView.setText(duration + "");
    }

    @Override
    public void onYouTubePlayerEnterFullScreen() {
        ViewGroup.LayoutParams viewParams = playerUi.getLayoutParams();
        viewParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        playerUi.setLayoutParams(viewParams);
    }

    @Override
    public void onYouTubePlayerExitFullScreen() {
        ViewGroup.LayoutParams viewParams = playerUi.getLayoutParams();
        viewParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        playerUi.setLayoutParams(viewParams);
    }
}
