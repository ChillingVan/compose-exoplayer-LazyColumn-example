package com.chillingvan.samples.composevideo.core.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.CoreVideoView
import com.chillingvan.samples.composevideo.video.PlayControlViewModel
import com.chillingvan.samples.composevideo.video.rememberFullScreenController

/**
 * Created by Chilling on 2022/8/12.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenView(player: CoreVideoPlayer,
                   playControlViewModel: PlayControlViewModel,
                   onBackClick: (() -> Unit)
) {
    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            val fullScreenController = rememberFullScreenController()
            CoreVideoView(coreVideoPlayer = player)
            PlayControlView(Modifier.fillMaxSize(), playControlViewModel,
                onBackClick = {
                    playControlViewModel.hide()
                    onBackClick.invoke()
                },
                onExitFullClick = {
                    fullScreenController.toPortrait()
                }
            )
        }
    }
}
