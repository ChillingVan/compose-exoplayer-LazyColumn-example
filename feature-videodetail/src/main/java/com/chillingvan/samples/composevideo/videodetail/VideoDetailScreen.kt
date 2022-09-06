package com.chillingvan.samples.composevideo.videodetail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.chillingvan.samples.composevideo.core.ui.BackPressHandler
import com.chillingvan.samples.composevideo.core.ui.component.AppGradientBackground
import com.chillingvan.samples.composevideo.core.ui.video.FullScreenView
import com.chillingvan.samples.composevideo.core.ui.video.PlayControlView
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.CoreVideoView
import com.chillingvan.samples.composevideo.video.PlayControlViewModel
import com.chillingvan.samples.composevideo.video.rememberFullScreenController
import com.chillingvan.samples.composevideo.videodetail.model.VideoDetailData

/**
 * Created by Chilling on 2022/8/1.
 */

@Composable
fun VideoDetailRoute(
    windowSizeClass: WindowSizeClass,
    onBackClick: () -> Unit,
    videoDetailViewModel: VideoDetailViewModel = hiltViewModel()
) {
    val fullScreenController = rememberFullScreenController()
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        VideoDetailScreen(windowSizeClass = windowSizeClass, videoDetailViewModel.getPlayer(),
            title = videoDetailViewModel.getTitle(), detailData = videoDetailViewModel.getDetailData(), onBackClick = onBackClick)
    } else {
        FullScreenView(videoDetailViewModel.getPlayer(), hiltViewModel(), onBackClick = {
            fullScreenController.toPortrait()
        })
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                videoDetailViewModel.resume()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                videoDetailViewModel.pause()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        BackPressHandler {
            fullScreenController.toPortrait()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    windowSizeClass: WindowSizeClass,
    coreVideoPlayer: CoreVideoPlayer,
    title: String,
    detailData: VideoDetailData?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppGradientBackground(modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SmallTopAppBar(title = {
                    Text("")
                })
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .consumedWindowInsets(innerPadding)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .aspectRatio(16 / 9f)
                ) {
                    val fullScreenController = rememberFullScreenController()
                    CoreVideoView(coreVideoPlayer = coreVideoPlayer)
                    val playControlViewModel = hiltViewModel<PlayControlViewModel>()
                    PlayControlView(Modifier.fillMaxSize(), playControlViewModel,
                        onBackClick = {
                            playControlViewModel.hide()
                            onBackClick.invoke()
                        },
                        onFullClick = {
                            fullScreenController.toFull()
                        })
                }
                Text(text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth().padding(10.dp)
                )
                detailData?.let {
                    Text(text = it.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth().padding(10.dp)
                    )
                }
            }
        }
    }
}
