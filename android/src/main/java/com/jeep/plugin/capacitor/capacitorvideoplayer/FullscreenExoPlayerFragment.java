package com.jeep.plugin.capacitor.capacitorvideoplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PictureInPictureParams;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.cast.CastPlayer;
import androidx.media3.cast.SessionAvailabilityListener;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MergingMediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.source.SingleSampleMediaSource;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory;
import androidx.media3.extractor.ts.TsExtractor;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import com.getcapacitor.JSObject;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.jeep.plugin.capacitor.capacitorvideoplayer.Notifications.NotificationCenter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.json.JSONException;

@UnstableApi
public class FullscreenExoPlayerFragment extends Fragment {

    public String videoPath;
    public Float videoRate;
    public String playerId;
    public String subTitle;
    public String preferredLanguage;
    public JSObject subTitleOptions;
    public JSObject headers;
    public Boolean isTV;
    public Boolean isInternal;
    public Long videoId;
    public Boolean exitOnEnd;
    public Boolean loopOnEnd;
    public Boolean pipEnabled;
    public Boolean bkModeEnabled;
    public Boolean showControls;
    public String displayMode = "all";
    public String title;
    public String smallTitle;
    public String accentColor;
    public Boolean chromecast;
    public String artwork;

    private static final String TAG = FullscreenExoPlayerFragment.class.getName();
    public static final long UNKNOWN_TIME = -1L;
    private final List<String> supportedFormat = Arrays.asList(
        "mp4",
        "webm",
        "ogv",
        "3gp",
        "flv",
        "dash",
        "mpd",
        "m3u8",
        "ism",
        "ytube",
        ""
    );
    private Player.Listener listener;
    private PlayerView playerView;
    private String vType = null;
    private static ExoPlayer player;
    private boolean playWhenReady = true;
    private boolean firstReadyToPlay = true;
    private boolean isEnded = false;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private Uri uri = null;
    private Uri sturi = null;
    private ProgressBar Pbar;
    private View view;
    private ImageButton closeBtn;
    private ImageButton pipBtn;
    private ImageButton resizeBtn;
    private ConstraintLayout constLayout;
    private LinearLayout linearLayout;
    private TextView header_tv;
    private TextView header_below;
    private static ImageView cast_image;
    private DefaultTimeBar exo_progress;
    private TextView exo_position;
    private TextView exo_duration;
    private TextView exo_label_separation;
    private TextView live_text;
    private Context context;
    private boolean isMuted = false;
    private float curVolume = (float) 0.5;
    private String stForeColor = "";
    private String stBackColor = "";
    private Integer stFontSize = 16;
    private boolean isInPictureInPictureMode = false;
    private TrackSelector trackSelector;
    // Current playback position (in milliseconds).
    private int mCurrentPosition;
    private int mDuration;
    private static final int videoStep = 10000;
    private boolean isCastSession = false;

    // Tag for the instance state bundle.
    private static final String PLAYBACK_TIME = "play_time";

    private PictureInPictureParams.Builder pictureInPictureParams;
    private MediaSession mediaSession;
    // private MediaSessionConnector mediaSessionConnector;
    private PlayerControlView.VisibilityListener visibilityListener;
    private PackageManager packageManager;
    private Boolean isPIPModeeEnabled = true;
    final Handler handler = new Handler();
    final Runnable mRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {
            checkPIPPermission();
        }
    };

    private Integer resizeStatus = AspectRatioFrameLayout.RESIZE_MODE_FIT;
    private MediaRouteButton mediaRouteButton;
    private CastContext castContext;
    private CastPlayer castPlayer;
    private MediaItem mediaItem;
    private MediaRouter mRouter;
    private MediaRouter.Callback mCallback = new EmptyCallback();
    private MediaRouteSelector mSelector;
    private CastStateListener castStateListener = null;
    private Boolean playerReady = false;

    // Track selection fields
    private String subtitleTrackId;
    private String subtitleLocale;
    private String audioTrackId;
    private String audioLocale;

    /**
     * Create Fragment View
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        packageManager = context.getPackageManager();
        view = inflater.inflate(isTV ? R.layout.fragment_fs_exoplayer_tv : R.layout.fragment_fs_exoplayer, container, false);
        constLayout = view.findViewById(R.id.fsExoPlayer);
        linearLayout = view.findViewById(R.id.linearLayout);
        playerView = view.findViewById(R.id.videoViewId);
        header_tv = view.findViewById(R.id.header_tv);
        header_below = view.findViewById(R.id.header_below);
        Pbar = view.findViewById(R.id.indeterminateBar);
        exo_progress = view.findViewById(R.id.exo_progress);
        exo_progress.setVisibility(View.GONE);
        exo_position = view.findViewById(R.id.exo_position);
        exo_position.setVisibility(View.GONE);
        exo_duration = view.findViewById(R.id.exo_duration);
        exo_duration.setVisibility(View.GONE);
        exo_label_separation = view.findViewById(R.id.exo_label_separation);
        exo_label_separation.setVisibility(View.GONE);
        live_text = view.findViewById(R.id.live_text);
        resizeBtn = view.findViewById(R.id.exo_resize);
        cast_image = view.findViewById(R.id.cast_image);
        mediaRouteButton = view.findViewById(R.id.media_route_button);
        playerView.setShowPreviousButton(false);
        playerView.setShowNextButton(false);
        playerView.setShowFastForwardButton(false);
        playerView.setShowRewindButton(false);

        Activity mAct = getActivity();
        if (displayMode.equals("landscape")) {
            assert mAct != null;
            mAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (displayMode.equals("portrait")) {
            mAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (!showControls) {
            playerView.setUseController(false);
        } else {
            playerView.setUseController(true);
        }

        if (!chromecast) {
            mediaRouteButton.setVisibility(View.GONE);
        } else {
            initializeCastService();
        }

        if (!Objects.equals(title, "")) {
            header_tv.setText(title);
        }
        if (!Objects.equals(smallTitle, "")) {
            header_below.setText(smallTitle);
        }
        if (!Objects.equals(accentColor, "")) {
            Pbar.getIndeterminateDrawable().setColorFilter(Color.parseColor(accentColor), android.graphics.PorterDuff.Mode.MULTIPLY);
            exo_progress.setPlayedColor(Color.parseColor(accentColor));
            exo_progress.setScrubberColor(Color.parseColor(accentColor));
        }

        closeBtn = view.findViewById(R.id.exo_close);
        pipBtn = view.findViewById(R.id.exo_pip);
        playerView.requestFocus();
        linearLayout.setVisibility(View.INVISIBLE);
        playerView.setControllerShowTimeoutMs(3000);
        playerView.setControllerVisibilityListener(
            new PlayerView.ControllerVisibilityListener() {
                @Override
                public void onVisibilityChanged(int visibility) {
                    linearLayout.setVisibility(visibility);
                }
            }
        );

        listener =
            new Player.Listener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int state) {
                    String stateString;
                    Map<String, Object> info = new HashMap<String, Object>() {
                        {
                            put("fromPlayerId", playerId);
                            put("currentTime", String.valueOf(player.getCurrentPosition() / 1000));
                        }
                    };

                    switch (state) {
                        case ExoPlayer.STATE_IDLE:
                            stateString = "ExoPlayer.STATE_IDLE      -";
                            Toast.makeText(context, "Video Url not found", Toast.LENGTH_SHORT).show();
                            playerExit();
                            break;
                        case ExoPlayer.STATE_BUFFERING:
                            stateString = "ExoPlayer.STATE_BUFFERING -";
                            Pbar.setVisibility(View.VISIBLE);
                            break;
                        case ExoPlayer.STATE_READY:
                            stateString = "ExoPlayer.STATE_READY     -";
                            Pbar.setVisibility(View.GONE);
                            playerReady = true;
                            if (!showControls) {
                                playerView.setUseController(false);
                            } else {
                                playerView.setUseController(true);
                            }
                            linearLayout.setVisibility(View.INVISIBLE);
                            Log.v(TAG, "**** in ExoPlayer.STATE_READY firstReadyToPlay " + firstReadyToPlay);

                            if (firstReadyToPlay) {
                                firstReadyToPlay = false;
                                NotificationCenter.defaultCenter().postNotification("playerItemReady", info);

                                play();
                                Log.v(TAG, "**** in ExoPlayer.STATE_READY firstReadyToPlay player.isPlaying" + player.isPlaying());
                                player.seekTo(currentWindow, playbackPosition);

                                selectTracks();

                                // We show progress bar, position and duration only when the video is not live
                                if (!player.isCurrentMediaItemLive()) {
                                    exo_progress.setVisibility(View.VISIBLE);
                                    exo_position.setVisibility(View.VISIBLE);
                                    exo_duration.setVisibility(View.VISIBLE);
                                    exo_label_separation.setVisibility(View.VISIBLE);
                                    playerView.setShowFastForwardButton(true);
                                    playerView.setShowRewindButton(true);
                                } else {
                                    live_text.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Log.v(TAG, "**** in ExoPlayer.STATE_READY isPlaying " + player.isPlaying());
                                if (player.isPlaying()) {
                                    Log.v(TAG, "**** in ExoPlayer.STATE_READY going to notify playerItemPlay ");
                                    NotificationCenter.defaultCenter().postNotification("playerItemPlay", info);
                                    resizeBtn.setVisibility(View.VISIBLE);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && pipEnabled) {
                                        pipBtn.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    Log.v(TAG, "**** in ExoPlayer.STATE_READY going to notify playerItemPause ");
                                    NotificationCenter.defaultCenter().postNotification("playerItemPause", info);
                                }
                            }
                            break;
                        case ExoPlayer.STATE_ENDED:
                            stateString = "ExoPlayer.STATE_ENDED     -";
                            Log.v(TAG, "**** in ExoPlayer.STATE_ENDED going to notify playerItemEnd ");

                            player.seekTo(0);
                            player.setVolume(curVolume);
                            player.setPlayWhenReady(false);
                            if (exitOnEnd) {
                                releasePlayer();
                                /*
                Activity mAct = getActivity();
                int mOrient = mAct.getRequestedOrientation();
                if (mOrient == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                  mAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
*/
                                NotificationCenter.defaultCenter().postNotification("playerItemEnd", info);
                            }
                            break;
                        default:
                            stateString = "UNKNOWN_STATE             -";
                            break;
                    }
                }

                @Override
                public void onTracksChanged(Tracks tracks) {
                    // Log current audio track
                    TrackGroup currentAudioTrack = null;
                    for (Tracks.Group trackGroup : tracks.getGroups()) {
                        if (trackGroup.isSelected() && trackGroup.getType() == C.TRACK_TYPE_AUDIO) {
                            currentAudioTrack = trackGroup.getMediaTrackGroup();
                            break;
                        }
                    }

                    // Log current subtitle track
                    TrackGroup currentSubtitleTrack = null;
                    for (Tracks.Group trackGroup : tracks.getGroups()) {
                        if (trackGroup.isSelected() && trackGroup.getType() == C.TRACK_TYPE_TEXT) {
                            currentSubtitleTrack = trackGroup.getMediaTrackGroup();
                            break;
                        }
                    }

                    // Create event data
                    Map<String, Object> trackInfo = new HashMap<String, Object>();
                    trackInfo.put("fromPlayerId", playerId);

                    if (currentAudioTrack != null) {
                        Format audioFormat = currentAudioTrack.getFormat(0);
                        Map<String, Object> audioInfo = new HashMap<String, Object>();
                        audioInfo.put("id", audioFormat.id);
                        audioInfo.put("language", audioFormat.language);
                        audioInfo.put("label", audioFormat.label);
                        audioInfo.put("codecs", audioFormat.codecs);
                        audioInfo.put("bitrate", audioFormat.bitrate);
                        audioInfo.put("channelCount", audioFormat.channelCount);
                        audioInfo.put("sampleRate", audioFormat.sampleRate);
                        trackInfo.put("audioTrack", audioInfo);
                    }

                    if (currentSubtitleTrack != null) {
                        Format subtitleFormat = currentSubtitleTrack.getFormat(0);
                        Map<String, Object> subtitleInfo = new HashMap<String, Object>();
                        subtitleInfo.put("id", subtitleFormat.id);
                        subtitleInfo.put("language", subtitleFormat.language);
                        subtitleInfo.put("label", subtitleFormat.label);
                        subtitleInfo.put("codecs", subtitleFormat.codecs);
                        subtitleInfo.put("containerMimeType", subtitleFormat.containerMimeType);
                        subtitleInfo.put("sampleMimeType", subtitleFormat.sampleMimeType);
                        trackInfo.put("subtitleTrack", subtitleInfo);
                    }

                    NotificationCenter.defaultCenter().postNotification("playerTracksChanged", trackInfo);
                }
            };

        if (!isInternal) {
            uri = Uri.parse(videoPath);
            sturi = subTitle != null ? Uri.parse(subTitle) : null;

            stForeColor = subTitleOptions.has("foregroundColor") ? subTitleOptions.getString("foregroundColor") : "rgba(255,255,255,1)";
            stBackColor = subTitleOptions.has("backgroundColor") ? subTitleOptions.getString("backgroundColor") : "rgba(0,0,0,1)";
            stFontSize = subTitleOptions.has("fontSize") ? subTitleOptions.getInteger("fontSize") : 16;
            // get video type
            vType = getVideoType(uri);
            Log.v(TAG, "display url: " + uri);
            Log.v(TAG, "display subtitle url: " + sturi);
            Log.v(TAG, "display isTV: " + isTV);
            Log.v(TAG, "display vType: " + vType);
        }
        if (uri != null || isInternal) {
            // go fullscreen
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (savedInstanceState != null) {
                mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME);
            }

            getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // Set the onKey listener
                            view.setFocusableInTouchMode(true);
                            view.requestFocus();
                            view.setOnKeyListener(
                                new View.OnKeyListener() {
                                    @Override
                                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                                        if (event.getAction() == KeyEvent.ACTION_UP) {
                                            long videoPosition = player.getCurrentPosition();
                                            Log.v(TAG, "$$$$ onKey " + keyCode + " $$$$");
                                            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
                                                Log.v(TAG, "$$$$ Going to backpress $$$$");
                                                backPressed();
                                            } else if (isTV) {
                                                switch (keyCode) {
                                                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                                                        fastForward(videoPosition, 1);
                                                        break;
                                                    case KeyEvent.KEYCODE_DPAD_LEFT:
                                                        rewind(videoPosition, 1);
                                                        break;
                                                    case KeyEvent.KEYCODE_DPAD_CENTER:
                                                        play_pause();
                                                        break;
                                                    case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                                                        fastForward(videoPosition, 2);
                                                        break;
                                                    case KeyEvent.KEYCODE_MEDIA_REWIND:
                                                        rewind(videoPosition, 2);
                                                        break;
                                                }
                                            }
                                            return true;
                                        } else {
                                            return false;
                                        }
                                    }
                                }
                            );

                            // initialize the player
                            initializePlayer();

                            closeBtn.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        playerExit();
                                    }
                                }
                            );
                            pipBtn.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        pictureInPictureMode();
                                    }
                                }
                            );
                            resizeBtn.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        resizePressed();
                                    }
                                }
                            );
                        }
                    }
                );
        } else {
            Log.d(TAG, "Video path wrong or type not supported");
            Toast.makeText(context, "Video path wrong or type not supported", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    /**
     * Sets the cast image in playerView when it is connected to a cast device
     */
    private class setCastImage extends AsyncTask<Void, Void, Bitmap> {

        protected Bitmap doInBackground(Void... params) {
            final String image = artwork;
            if (image != "") {
                try {
                    URL url = new URL(image);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            cast_image.setImageBitmap(result);
        }
    }

    /**
     * Show controller
     */
    public void showController() {
        playerView.showController();
    }

    /**
     * isControllerIsFullyVisible
     */
    public boolean isControllerIsFullyVisible() {
        return playerView.isControllerFullyVisible();
    }

    /**
     * Perform backPressed Action
     */
    private void backPressed() {
        if (isCastSession) {
            playerExit();
            return;
        }
        if (
            !isInPictureInPictureMode &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) &&
            isPIPModeeEnabled &&
            pipEnabled &&
            playerReady // <- playerReady: this prevents a crash if the user presses back before the player is ready (when enters in pip mode and tries to get the aspect ratio)
        ) {
            pictureInPictureMode();
        } else {
            playerExit();
        }
    }

    private void resizePressed() {
        if (resizeStatus == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            resizeStatus = AspectRatioFrameLayout.RESIZE_MODE_FILL;
            resizeBtn.setImageResource(R.drawable.ic_zoom);
        } else if (resizeStatus == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            resizeStatus = AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
            resizeBtn.setImageResource(R.drawable.ic_fit);
        } else {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            resizeStatus = AspectRatioFrameLayout.RESIZE_MODE_FIT;
            resizeBtn.setImageResource(R.drawable.ic_expand);
        }
    }

    public void playerExit() {
        Map<String, Object> info = new HashMap<String, Object>() {
            {
                put("dismiss", "1");
                put("currentTime", getCurrentTime());
            }
        };
        if (player != null) {
            player.seekTo(0);
            player.setVolume(curVolume);
        }
        releasePlayer();
        /* 
    Activity mAct = getActivity();
    int mOrient = mAct.getRequestedOrientation();
    if (mOrient == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
      mAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
*/
        // We control if the user lock the screen when the player is in pip mode
        try {
            NotificationCenter.defaultCenter().postNotification("playerFullscreenDismiss", info);
        } catch (Exception e) {
            Log.e(TAG, "Error in posting notification");
        }
    }

    /**
     * Perform pictureInPictureMode Action
     */
    private void pictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            playerView.setUseController(false);
            playerView.setControllerAutoShow(false);
            linearLayout.setVisibility(View.INVISIBLE);
            Log.v(TAG, "PIP break 1");
            // require android O or higher
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            ) {
                pictureInPictureParams = new PictureInPictureParams.Builder();
                // setup height and width of the PIP window
                Rational aspectRatio = new Rational(player.getVideoFormat().width, player.getVideoFormat().height);
                pictureInPictureParams.setAspectRatio(aspectRatio).build();
                getActivity().enterPictureInPictureMode(pictureInPictureParams.build());
                Log.v(TAG, "PIP break 2");
            } else {
                getActivity().enterPictureInPictureMode();
                Log.v(TAG, "PIP break 3");
            }
            isInPictureInPictureMode = getActivity().isInPictureInPictureMode();
            if (sturi != null) {
                setSubtitle(true);
            }
            if (player != null) play();

            handler.postDelayed(mRunnable, 100);
            Log.v(TAG, "PIP break 4");
        } else {
            Log.v(TAG, "pictureInPictureMode: doesn't support PIP");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkPIPPermission() {
        isPIPModeeEnabled = isInPictureInPictureMode;
        if (!isInPictureInPictureMode) {
            backPressed();
        }
    }

    /**
     * Perform onStart Action
     */
    @Override
    public void onStart() {
        super.onStart();
        //if (chromecast && castContext != null) mRouter.addCallback(mSelector, mCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        if (Util.SDK_INT >= 24) {
            if (playerView != null) {
                // If cast is playing then it doesn't start the local player once get backs from background
                if (castContext != null && chromecast && castPlayer.isCastSessionAvailable()) return;

                initializePlayer();
                if (player.getCurrentPosition() != 0) {
                    firstReadyToPlay = false;
                    play();
                }
            } else {
                getActivity().finishAndRemoveTask();
            }
        }
    }

    /**
     * Perform onStop Action
     */
    @Override
    public void onStop() {
        super.onStop();
        boolean isAppBackground = false;
        if (bkModeEnabled) isAppBackground = isApplicationSentToBackground(context);
        if (isInPictureInPictureMode) {
            linearLayout.setVisibility(View.VISIBLE);
            playerExit();
            getActivity().finishAndRemoveTask();
        }
    }

    /**
     * Perform onDestroy Action
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chromecast) mRouter.removeCallback(mCallback);
        releasePlayer();
    }

    /**
     * Perform onPause Action
     */
    @Override
    public void onPause() {
        super.onPause();
        if (chromecast) castContext.removeCastStateListener(castStateListener);
        boolean isAppBackground = false;
        if (bkModeEnabled) isAppBackground = isApplicationSentToBackground(context);

        if (!isInPictureInPictureMode) {
            if (Util.SDK_INT < 24) {
                if (player != null) player.setPlayWhenReady(false);
                releasePlayer();
            } else {
                if (isAppBackground) {
                    if (player != null) {
                        if (player.isPlaying()) play();
                    }
                } else {
                    pause();
                }
            }
        } else {
            if (linearLayout.getVisibility() == View.VISIBLE) {
                linearLayout.setVisibility(View.INVISIBLE);
            }
            if ((isInPictureInPictureMode || isAppBackground) && player != null) play();
        }
    }

    /**
     * Release the player
     */
    public void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            if (mediaSession != null) {
                mediaSession.release();
                mediaSession = null;
            }
            player.setRepeatMode(player.REPEAT_MODE_OFF);
            player.removeListener(listener);
            player.release();
            player = null;
            showSystemUI();
            resetVariables();
            if (chromecast) {
                castPlayer.release();
                castPlayer = null;
            }
        }
    }

    /**
     * Perform onResume Action
     */
    @Override
    public void onResume() {
        super.onResume();
        //if (chromecast && castContext != null) castContext.addCastStateListener(castStateListener);
        if (!isInPictureInPictureMode) {
            hideSystemUi();
            if ((Util.SDK_INT < 24 || player == null)) {
                initializePlayer();
            }
        } else {
            isInPictureInPictureMode = false;
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            ) {
                if (!showControls) {
                    playerView.setUseController(false);
                } else {
                    playerView.setUseController(true);
                }
            }
            if (sturi != null) {
                setSubtitle(false);
            }
        }
    }

    /**
     * Hide System UI
     */
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        if (playerView != null) playerView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LOW_PROFILE |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    /**
     * Leave the fullsreen mode and reset the status bar to visible
     */
    private void showSystemUI() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
    }

    /**
     * Initialize the player
     */
    private void initializePlayer() {
        if (player == null) {
            // Enable audio libs
            DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory()
                .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS)
                .setTsExtractorTimestampSearchBytes(1500 * TsExtractor.TS_PACKET_SIZE);

            @SuppressLint("WrongConstant")
            RenderersFactory renderersFactory = new DefaultRenderersFactory(context)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);

            playerView.setShowSubtitleButton(true);

            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
            AdaptiveTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();

            trackSelector = new DefaultTrackSelector(context, videoTrackSelectionFactory);

            LoadControl loadControl = new DefaultLoadControl();

            player =
                new ExoPlayer.Builder(context, renderersFactory)
                    .setSeekBackIncrementMs(10000)
                    .setSeekForwardIncrementMs(10000)
                    .setTrackSelector(trackSelector)
                    .setLoadControl(loadControl)
                    .setBandwidthMeter(bandwidthMeter)
                    .build();
        }

        playerView.setPlayer(player);

        MediaSource mediaSource;
        if (!isInternal) {
            if (videoPath.substring(0, 21).equals("file:///android_asset") || videoPath.substring(0, 15).equals("content://media")) {
                mediaSource = buildAssetMediaSource(uri);
            } else {
                mediaSource = buildHttpMediaSource();
            }
        } else {
            Uri videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId);
            mediaSource = buildInternalMediaSource(videoUri);
        }

        if (mediaSource != null) {
            player.setAudioAttributes(AudioAttributes.DEFAULT, true);
            player.addListener(listener);
            player.prepare(mediaSource, false, false);

            if (loopOnEnd) {
                player.setRepeatMode(player.REPEAT_MODE_ONE);
            } else {
                player.setRepeatMode(player.REPEAT_MODE_OFF);
            }
        }
        Map<String, Object> info = new HashMap<String, Object>() {
            {
                put("fromPlayerId", playerId);
            }
        };
        if (sturi != null) {
            setSubtitle(false);
        }
        //Use Media Session Connector from the EXT library to enable MediaSession Controls in PIP.
        //  mediaSession = new MediaSession.Builder(context, player).setId("capacitorvideoplayer").build();
        // mediaSessionConnector = new MediaSessionConnector(mediaSession);
        //  mediaSessionConnector.setPlayer(player);
        //   mediaSession.setActive(true);

        NotificationCenter.defaultCenter().postNotification("initializePlayer", info);
    }

    private void setSubtitle(boolean transparent) {
        int foreground;
        int background;
        if (!transparent) {
            foreground = Color.WHITE;
            background = Color.BLACK;
            if (stForeColor.length() > 4 && stForeColor.substring(0, 4).equals("rgba")) {
                foreground = getColorFromRGBA(stForeColor);
            }
            if (stBackColor.length() > 4 && stBackColor.substring(0, 4).equals("rgba")) {
                background = getColorFromRGBA(stBackColor);
            }
        } else {
            foreground = Color.TRANSPARENT;
            background = Color.TRANSPARENT;
        }
        playerView
            .getSubtitleView()
            .setStyle(
                new CaptionStyleCompat(foreground, background, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_NONE, Color.WHITE, null)
            );
        playerView.getSubtitleView().setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, stFontSize);
        playerView.setShowSubtitleButton(true);

        if (player != null && trackSelector != null) {
            DefaultTrackSelector.Parameters parameters =
                ((DefaultTrackSelector) trackSelector).getParameters().buildUpon().setSelectUndeterminedTextLanguage(true).build();
            ((DefaultTrackSelector) trackSelector).setParameters(parameters);
        }

        playerView.getSubtitleView().setVisibility(View.VISIBLE);
        playerView.setShowSubtitleButton(true);
    }

    /**
     * Build the Asset MediaSource
     */
    private MediaSource buildAssetMediaSource(Uri uri) {
        MediaSource mediaSource = null;
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "jeep-exoplayer-plugin");
        mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
        // Get the subtitles if any
        if (sturi != null) {
            mediaSource = getSubTitle(mediaSource, sturi, dataSourceFactory);
        }
        return mediaSource;
    }

    /**
     * Build the Internal MediaSource
     */
    private MediaSource buildInternalMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "jeep-exoplayer-plugin");
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
    }

    /**
     * Build the Http MediaSource
     * @return MediaSource
     */
    private MediaSource buildHttpMediaSource() {
        MediaSource mediaSource = null;

        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
            .setUserAgent("jeep-media3-plugin")
            .setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
            .setReadTimeoutMs(1800000)
            .setAllowCrossProtocolRedirects(true);

        // If headers is not null and has data we pass them to the HttpDataSourceFactory
        if (headers != null && headers.length() > 0) {
            // We map the headers(JSObject) to a Map<String, String>
            Map<String, String> headersMap = new HashMap<String, String>();
            for (int i = 0; i < headers.names().length(); i++) {
                try {
                    headersMap.put(headers.names().getString(i), headers.get(headers.names().getString(i)).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            httpDataSourceFactory.setDefaultRequestProperties(headersMap);
        }

        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context, httpDataSourceFactory);

        if (
            vType.equals("mp4") ||
            vType.equals("webm") ||
            vType.equals("ogv") ||
            vType.equals("3gp") ||
            vType.equals("flv") ||
            vType.equals("")
        ) {
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
        } else if (vType.equals("dash") || vType.equals("mpd")) {
            /* adaptive streaming Dash stream */
            mediaSource = new DashMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
        } else if (vType.equals("m3u8")) {
            mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
        } else if (vType.equals("ism")) {
            /* adaptive streaming SmoothStreaming stream */
            //  mediaSource = new SmoothStreamingMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
        }

        // Get the subtitles if any
        if (sturi != null) {
            mediaSource = getSubTitle(mediaSource, sturi, dataSourceFactory);
        }
        return mediaSource;
    }

    /**
     * Save instance state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current playback position (in milliseconds) to the
        // instance state bundle.
        if (player != null) {
            outState.putInt(PLAYBACK_TIME, (int) player.getCurrentPosition());
        }
    }

    private int getColorFromRGBA(String rgbaColor) {
        int ret = 0;
        String color = rgbaColor.substring(rgbaColor.indexOf("(") + 1, rgbaColor.indexOf(")"));
        List<String> colors = Arrays.asList(color.split(","));
        if (colors.size() == 4) {
            ret =
                (Math.round(Float.parseFloat(colors.get(3).trim()) * 255) & 0xff) << 24 |
                (Integer.parseInt(colors.get(0).trim()) & 0xff) << 16 |
                (Integer.parseInt(colors.get(1).trim()) & 0xff) << 8 |
                (Integer.parseInt(colors.get(2).trim()) & 0xff);
        }
        return ret;
    }

    private MediaSource getSubTitle(MediaSource mediaSource, Uri sturi, DataSource.Factory dataSourceFactory) {
        // Create mediaSource with subtitle
        MediaSource[] mediaSources = new MediaSource[2];
        mediaSources[0] = mediaSource;
        String mimeType = getMimeType(sturi);

        // We get the language label from the language code
        String languageLabel = Locale.forLanguageTag(preferredLanguage).getDisplayLanguage();
        MediaItem.SubtitleConfiguration subConfig = new MediaItem.SubtitleConfiguration.Builder(sturi)
            .setMimeType(mimeType)
            .setUri(sturi)
            .setId(subTitle)
            .setLabel(languageLabel)
            .setRoleFlags(C.ROLE_FLAG_SUBTITLE)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT | C.SELECTION_FLAG_AUTOSELECT)
            .setLanguage(preferredLanguage)
            .build();

        SingleSampleMediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory)
            .createMediaSource(subConfig, C.TIME_UNSET);

        mediaSources[1] = subtitleSource;

        mediaSource = new MergingMediaSource(mediaSources);
        return mediaSource;
    }

    private String getMimeType(Uri sturi) {
        String lastSegment = sturi.getLastPathSegment();
        String extension = lastSegment.substring(lastSegment.lastIndexOf(".") + 1);
        String mimeType = "";
        if (extension.equals("vtt")) {
            mimeType = MimeTypes.TEXT_VTT;
        } else if (extension.equals("srt")) {
            mimeType = MimeTypes.APPLICATION_SUBRIP;
        } else if (extension.equals("ssa") || extension.equals("ass")) {
            mimeType = MimeTypes.TEXT_SSA;
        } else if (extension.equals("ttml") || extension.equals("dfxp") || extension.equals("xml")) {
            mimeType = MimeTypes.APPLICATION_TTML;
        }
        return mimeType;
    }

    /**
     * Fast Forward TV
     */
    private void fastForward(long position, int times) {
        if (position < mDuration - videoStep) {
            if (player.isPlaying()) {
                player.setPlayWhenReady(false);
            }
            player.seekTo(position + (long) times * videoStep);
            play();
        }
    }

    /**
     * Rewind TV
     */
    private void rewind(long position, int times) {
        if (position > videoStep) {
            if (player.isPlaying()) {
                player.setPlayWhenReady(false);
            }
            player.seekTo(position - (long) times * videoStep);
            play();
        }
    }

    /**
     * Play Pause TV
     */
    private void play_pause() {
        player.setPlayWhenReady(!player.isPlaying());
    }

    /**
     * Check if the player is playing
     * @return boolean
     */
    public boolean isPlaying() {
        return player.isPlaying();
    }

    /**
     * Start the player
     */
    public void play() {
        PlaybackParameters param = new PlaybackParameters(videoRate);
        player.setPlaybackParameters(param);

        /* If the user start the cast before the player is ready and playing, then the video will start
          in the device and chromecast at the same time. This is to avoid that behaviour.*/
        if (!isCastSession) player.setPlayWhenReady(true);
    }

    /**
     * Pause the player
     */
    public void pause() {
        if (player != null) player.setPlayWhenReady(false);
    }

    /**
     * Get the player duration
     * @return int in seconds
     */
    public int getDuration() {
        return player.getDuration() == UNKNOWN_TIME ? 0 : (int) (player.getDuration() / 1000);
    }

    /**
     * Get the player current position
     * @return int in seconds
     */
    public int getCurrentTime() {
        return player.getCurrentPosition() == UNKNOWN_TIME ? 0 : (int) (player.getCurrentPosition() / 1000);
    }

    /**
     * Set the player current position
     * @param timeSecond int
     */
    public void setCurrentTime(int timeSecond) {
        if (isInPictureInPictureMode) {
            playerView.setUseController(false);
            linearLayout.setVisibility(View.INVISIBLE);
        }
        long seekPosition = player.getCurrentPosition() == UNKNOWN_TIME
            ? 0
            : Math.min(Math.max(0, timeSecond * 1000), player.getDuration());
        player.seekTo(seekPosition);
    }

    /**
     * Return the player volume
     * @return float
     */
    public float getVolume() {
        return player.getVolume();
    }

    /**
     * Set the player volume
     * @param _volume float range 0,1
     */
    public void setVolume(float _volume) {
        float volume = Math.min(Math.max(0, _volume), 1L);
        player.setVolume(volume);
    }

    /**
     * Return the player rate
     * @return float
     */
    public float getRate() {
        return videoRate;
    }

    /**
     * Set the player rate
     * @param _rate float range [0.25f, 0.5f, 0.75f, 1f, 2f, 4f]
     */
    public void setRate(float _rate) {
        videoRate = _rate;
        PlaybackParameters param = new PlaybackParameters(videoRate);
        player.setPlaybackParameters(param);
    }

    /**
     * Switch Off/On the player volume
     * @param _isMuted boolean
     */
    public void setMuted(boolean _isMuted) {
        isMuted = _isMuted;
        if (isMuted) {
            curVolume = player.getVolume();
            player.setVolume(0L);
        } else {
            player.setVolume(curVolume);
        }
    }

    /**
     * Check if the player is muted
     * @return boolean
     */
    public boolean getMuted() {
        return isMuted;
    }

    /**
     * Apply white color to MediaRouteButton
     * @param button
     */
    private void mediaRouteButtonColorWhite(MediaRouteButton button) {
        if (button == null) return;
        Context castContext = new ContextThemeWrapper(getContext(), androidx.mediarouter.R.style.Theme_MediaRouter);

        TypedArray a = castContext.obtainStyledAttributes(
            null,
            androidx.mediarouter.R.styleable.MediaRouteButton,
            androidx.mediarouter.R.attr.mediaRouteButtonStyle,
            0
        );
        Drawable drawable = a.getDrawable(androidx.mediarouter.R.styleable.MediaRouteButton_externalRouteEnabledDrawable);
        a.recycle();
        DrawableCompat.setTint(drawable, getContext().getResources().getColor(R.color.white));
        drawable.setState(button.getDrawableState());
        button.setRemoteIndicatorDrawable(drawable);
    }

    /**
     * Get video Type from Uri
     * @param uri
     * @return video type
     */
    private String getVideoType(Uri uri) {
        String ret = null;
        Object obj = uri.getLastPathSegment();
        String lastSegment = (obj == null) ? "" : uri.getLastPathSegment();
        for (String type : supportedFormat) {
            if (ret != null) break;
            if (lastSegment.length() > 0 && lastSegment.contains(type)) ret = type;
            if (ret == null) {
                List<String> segments = uri.getPathSegments();
                if (segments.size() > 0) {
                    String segment;
                    if (segments.get(segments.size() - 1).equals("manifest")) {
                        segment = segments.get(segments.size() - 2);
                    } else {
                        segment = segments.get(segments.size() - 1);
                    }
                    for (String sType : supportedFormat) {
                        if (segment.contains(sType)) {
                            ret = sType;
                            break;
                        }
                    }
                }
            }
        }
        ret = (ret != null) ? ret : "";
        return ret;
    }

    /**
     * Reset Variables for multiple runs
     */
    private void resetVariables() {
        vType = null;
        playerView = null;
        playWhenReady = true;
        firstReadyToPlay = true;
        isEnded = false;
        currentWindow = 0;
        playbackPosition = 0;
        uri = null;
        isMuted = false;
        curVolume = (float) 0.5;
        mCurrentPosition = 0;
    }

    /**
     * Check if the application has been sent to the background
     * @param context
     * @return boolean
     */
    public boolean isApplicationSentToBackground(final Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = am.getRunningAppProcesses();
        if (procInfos != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : procInfos) {
                if (appProcess.pid == pid) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Function to initialize the cast service and everything related to it
     * @return void
     */
    private void initializeCastService() {
        Executor executor = Executors.newSingleThreadExecutor();
        Task<CastContext> task = CastContext.getSharedInstance(context, executor);

        task.addOnCompleteListener(
            new OnCompleteListener<CastContext>() {
                @Override
                public void onComplete(Task<CastContext> task) {
                    if (task.isSuccessful()) {
                        castContext = task.getResult();
                        castPlayer = new CastPlayer(castContext);
                        mRouter = MediaRouter.getInstance(context);
                        mSelector =
                            new MediaRouteSelector.Builder()
                                .addControlCategories(
                                    Arrays.asList(MediaControlIntent.CATEGORY_LIVE_AUDIO, MediaControlIntent.CATEGORY_LIVE_VIDEO)
                                )
                                .build();

                        mediaRouteButtonColorWhite(mediaRouteButton);
                        if (
                            castContext != null && castContext.getCastState() != CastState.NO_DEVICES_AVAILABLE
                        ) mediaRouteButton.setVisibility(View.VISIBLE);

                        castStateListener =
                            state -> {
                                if (state == CastState.NO_DEVICES_AVAILABLE) {
                                    mediaRouteButton.setVisibility(View.GONE);
                                } else {
                                    if (mediaRouteButton.getVisibility() == View.GONE) {
                                        mediaRouteButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            };
                        CastButtonFactory.setUpMediaRouteButton(context, mediaRouteButton);

                        MediaMetadata movieMetadata;
                        if (artwork != "") {
                            movieMetadata =
                                new MediaMetadata.Builder()
                                    .setTitle(title)
                                    .setSubtitle(smallTitle)
                                    .setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
                                    .setArtworkUri(Uri.parse(artwork))
                                    .build();
                            new setCastImage().execute();
                        } else {
                            movieMetadata = new MediaMetadata.Builder().setTitle(title).setSubtitle(smallTitle).build();
                        }
                        mediaItem =
                            new MediaItem.Builder()
                                .setUri(videoPath)
                                .setMimeType(MimeTypes.VIDEO_UNKNOWN)
                                .setMediaMetadata(movieMetadata)
                                .build();

                        castPlayer.setSessionAvailabilityListener(
                            new SessionAvailabilityListener() {
                                @Override
                                public void onCastSessionAvailable() {
                                    isCastSession = true;
                                    final Long videoPosition = player.getCurrentPosition();
                                    if (pipEnabled) {
                                        pipBtn.setVisibility(View.GONE);
                                    }
                                    resizeBtn.setVisibility(View.GONE);
                                    player.setPlayWhenReady(false);
                                    cast_image.setVisibility(View.VISIBLE);
                                    castPlayer.setMediaItem(mediaItem, videoPosition);
                                    playerView.setPlayer(castPlayer);
                                    playerView.setControllerShowTimeoutMs(0);
                                    playerView.setControllerHideOnTouch(false);
                                    //We perform a click because for some weird reason, the layout is black until the user clicks on it
                                    playerView.performClick();
                                }

                                @Override
                                public void onCastSessionUnavailable() {
                                    isCastSession = false;
                                    final Long videoPosition = castPlayer.getCurrentPosition();
                                    if (pipEnabled) {
                                        pipBtn.setVisibility(View.VISIBLE);
                                    }
                                    resizeBtn.setVisibility(View.VISIBLE);
                                    cast_image.setVisibility(View.GONE);
                                    playerView.setPlayer(player);
                                    player.setPlayWhenReady(true);
                                    player.seekTo(videoPosition);
                                    playerView.setControllerShowTimeoutMs(3000);
                                    playerView.setControllerHideOnTouch(true);
                                }
                            }
                        );

                        castPlayer.addListener(
                            new Player.Listener() {
                                @Override
                                public void onPlayerStateChanged(boolean playWhenReady, int state) {
                                    Map<String, Object> info = new HashMap<String, Object>() {
                                        {
                                            put("fromPlayerId", playerId);
                                            put("currentTime", String.valueOf(player.getCurrentPosition() / 1000));
                                        }
                                    };
                                    switch (state) {
                                        case CastPlayer.STATE_READY:
                                            if (castPlayer.isPlaying()) {
                                                NotificationCenter.defaultCenter().postNotification("playerItemPlay", info);
                                            } else {
                                                NotificationCenter.defaultCenter().postNotification("playerItemPause", info);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        );

                        castContext.addCastStateListener(castStateListener);
                        mRouter.addCallback(mSelector, mCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
                    } else {
                        Exception e = task.getException();
                        e.printStackTrace();
                    }
                }
            }
        );
    }

    private final class EmptyCallback extends MediaRouter.Callback {}

    // Add method to select tracks based on ID or locale
    private void selectTracks() {
        if (player != null && trackSelector != null) {
            Tracks tracks = player.getCurrentTracks();

            boolean audioSelected = false;
            boolean subtitleSelected = false;

            // Select audio track
            if (audioTrackId != null || audioLocale != null) {
                Format selectedFormat = null;

                // First try to find by ID and check locale if specified
                if (audioTrackId != null) {
                    for (Tracks.Group trackGroup : tracks.getGroups()) {
                        if (trackGroup.getType() == C.TRACK_TYPE_AUDIO) {
                            Format format = trackGroup.getMediaTrackGroup().getFormat(0);
                            if (format.id != null && format.id.equals(audioTrackId)) {
                                selectedFormat = format;
                                break;
                            }
                        }
                    }
                }

                if (selectedFormat != null && audioLocale != null && !selectedFormat.language.equals(audioLocale)) {
                    selectedFormat = null;
                }

                // If not found and locale specified, try by locale only
                if (selectedFormat == null && audioLocale != null) {
                    for (Tracks.Group trackGroup : tracks.getGroups()) {
                        if (trackGroup.getType() == C.TRACK_TYPE_AUDIO) {
                            Format format = trackGroup.getMediaTrackGroup().getFormat(0);
                            if (format.language != null && format.language.equals(audioLocale)) {
                                selectedFormat = format;
                                break;
                            }
                        }
                    }
                }

                // Apply the selected format if found
                if (selectedFormat != null) {
                    ((DefaultTrackSelector) trackSelector).setParameters(
                            ((DefaultTrackSelector) trackSelector).buildUponParameters().setPreferredAudioLanguage(selectedFormat.language)
                        );
                    audioSelected = true;
                }
            }

            // Select subtitle track
            if (subtitleTrackId != null || subtitleLocale != null) {
                Format selectedFormat = null;

                // First try to find by ID and check locale if specified
                if (subtitleTrackId != null) {
                    for (Tracks.Group trackGroup : tracks.getGroups()) {
                        if (trackGroup.getType() == C.TRACK_TYPE_TEXT) {
                            Format format = trackGroup.getMediaTrackGroup().getFormat(0);
                            if (format.id != null && format.id.equals(subtitleTrackId)) {
                                selectedFormat = format;
                                break;
                            }
                        }
                    }
                }

                if (selectedFormat != null && subtitleLocale != null && !selectedFormat.language.equals(subtitleLocale)) {
                    selectedFormat = null;
                }

                // If not found and locale specified, try by locale only
                if (selectedFormat == null && subtitleLocale != null) {
                    for (Tracks.Group trackGroup : tracks.getGroups()) {
                        if (trackGroup.getType() == C.TRACK_TYPE_TEXT) {
                            Format format = trackGroup.getMediaTrackGroup().getFormat(0);
                            if (format.language != null && format.language.equals(subtitleLocale)) {
                                selectedFormat = format;
                                break;
                            }
                        }
                    }
                }

                // Apply the selected format if found
                if (selectedFormat != null) {
                    ((DefaultTrackSelector) trackSelector).setParameters(
                            ((DefaultTrackSelector) trackSelector).buildUponParameters().setPreferredTextLanguage(selectedFormat.language)
                        );
                    subtitleSelected = true;
                }
            }

            if (!audioSelected && !subtitleSelected && preferredLanguage != null) {
                Boolean trackFound = false;
                // First try to find the audio with the same language
                for (Tracks.Group trackGroup : tracks.getGroups()) {
                    if (trackGroup.getType() == C.TRACK_TYPE_AUDIO) {
                        Format format = trackGroup.getMediaTrackGroup().getFormat(0);
                        if (format.language != null && format.language.equals(preferredLanguage)) {
                            ((DefaultTrackSelector) trackSelector).setParameters(
                                    ((DefaultTrackSelector) trackSelector).buildUponParameters()
                                        .setPreferredAudioLanguage(preferredLanguage)
                                );
                            trackFound = true;
                            break;
                        }
                    }
                }

                if (!trackFound) {
                    // Then try to find the subtitle with the same language
                    for (Tracks.Group trackGroup : tracks.getGroups()) {
                        if (trackGroup.getType() == C.TRACK_TYPE_TEXT) {
                            Format format = trackGroup.getMediaTrackGroup().getFormat(0);
                            if (format.language != null && format.language.equals(preferredLanguage)) {
                                ((DefaultTrackSelector) trackSelector).setParameters(
                                        ((DefaultTrackSelector) trackSelector).buildUponParameters()
                                            .setPreferredTextLanguage(preferredLanguage)
                                    );
                            }
                        }
                    }
                } else {
                    // Disable subtitles by setting preferred text language to null
                    ((DefaultTrackSelector) trackSelector).setParameters(
                            ((DefaultTrackSelector) trackSelector).buildUponParameters()
                                .setPreferredTextLanguage(null)
                                .setSelectUndeterminedTextLanguage(false)
                        );
                }
            }
        }
    }

    // Add setter methods
    public void setSubtitleTrackId(String trackId) {
        this.subtitleTrackId = trackId;
    }

    public void setSubtitleLocale(String locale) {
        this.subtitleLocale = locale;
    }

    public void setAudioTrackId(String trackId) {
        this.audioTrackId = trackId;
    }

    public void setAudioLocale(String locale) {
        this.audioLocale = locale;
    }
}
