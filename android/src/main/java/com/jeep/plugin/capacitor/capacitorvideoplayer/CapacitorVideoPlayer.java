package com.jeep.plugin.capacitor.capacitorvideoplayer;

import android.content.Context;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import com.getcapacitor.JSObject;
import com.jeep.plugin.capacitor.capacitorvideoplayer.PickerVideo.PickerVideoFragment;

public class CapacitorVideoPlayer {

    CapacitorVideoPlayer(Context context) {}

    public String echo(String value) {
        return value;
    }

    @OptIn(markerClass = UnstableApi.class)
    public FullscreenExoPlayerFragment createFullScreenFragment(
        String videoPath,
        Float videoRate,
        Boolean exitOnEnd,
        Boolean loopOnEnd,
        Boolean pipEnabled,
        Boolean bkModeEnabled,
        Boolean showControls,
        String displayMode,
        String subTitle,
        String preferredLanguage,
        JSObject subTitleOptions,
        JSObject headers,
        String title,
        String smallTitle,
        String accentColor,
        Boolean chromecast,
        String artwork,
        Boolean isTV,
        String playerId,
        Boolean isInternal,
        Long videoId
    ) {
        FullscreenExoPlayerFragment fsFragment = new FullscreenExoPlayerFragment();

        fsFragment.videoPath = videoPath;
        fsFragment.videoRate = videoRate;
        fsFragment.exitOnEnd = exitOnEnd;
        fsFragment.loopOnEnd = loopOnEnd;
        fsFragment.pipEnabled = pipEnabled;
        fsFragment.bkModeEnabled = bkModeEnabled;
        fsFragment.showControls = showControls;
        fsFragment.displayMode = displayMode;
        fsFragment.subTitle = subTitle;
        fsFragment.preferredLanguage = preferredLanguage;
        fsFragment.subTitleOptions = subTitleOptions;
        fsFragment.headers = headers;
        fsFragment.title = title;
        fsFragment.smallTitle = smallTitle;
        fsFragment.accentColor = accentColor;
        fsFragment.chromecast = chromecast;
        fsFragment.artwork = artwork;
        fsFragment.isTV = isTV;
        fsFragment.playerId = playerId;
        fsFragment.isInternal = isInternal;
        fsFragment.videoId = videoId;
        return fsFragment;
    }

    public PickerVideoFragment createPickerVideoFragment() {
        return new PickerVideoFragment();
    }
}
