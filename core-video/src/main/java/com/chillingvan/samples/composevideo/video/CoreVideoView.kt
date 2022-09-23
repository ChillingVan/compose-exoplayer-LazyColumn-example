package com.chillingvan.samples.composevideo.video

import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView

/**
 * Created by Chilling on 2022/8/3.
 */
@Composable
fun CoreVideoView(modifier: Modifier = Modifier,
                  onViewUpdate: () -> Unit = {}, coreVideoPlayer: CoreVideoPlayer) {
    AndroidView(
        factory = { context ->
            StyledPlayerView(context).also { playerView ->
                playerView.useController = false
                playerView.controllerAutoShow = false
                playerView.setShowNextButton(false)
                playerView.setShowPreviousButton(false)
                playerView.player = coreVideoPlayer.getPlayer() as? Player
                coreVideoPlayer.bindVideoView(playerView)
            }
        },
        modifier = modifier.fillMaxSize(),
        update = {
            Log.i("CoreVideoView", "update")
            coreVideoPlayer.bindVideoView(it)
            onViewUpdate.invoke()
        }
    )
}