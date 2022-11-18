package com.chillingvan.samples.composevideo.videodetail

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.chillingvan.samples.composevideo.video.*
import com.chillingvan.samples.composevideo.videodetail.model.VideoDetailItemData
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Created by Chilling on 2022/8/1.
 */


private const val TAG = "VideoDetailScreen"

@OptIn(ExperimentalPagerApi::class)
@Composable
fun VideoDetailRoute(
    windowSizeClass: WindowSizeClass,
    onBackClick: () -> Unit,
    videoDetailViewModel: VideoDetailViewModel = hiltViewModel()
) {
    val fullScreenController = rememberFullScreenController()
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        VideoDetailScreen(
            onBackClick = onBackClick,
            onPageChange = { page ->
                videoDetailViewModel.changePlayingItem(page)
            }
        )
    } else {
        FullScreenView(videoDetailViewModel.getPlayer(), hiltViewModel(), onBackClick = {
            fullScreenController.toPortrait()
        })
    }
    HandleVideoPlay(videoDetailViewModel, fullScreenController)
}

@Composable
private fun HandleVideoPlay(
    videoDetailViewModel: VideoDetailViewModel,
    fullScreenController: IFullScreenController
) {
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

@ExperimentalPagerApi
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    onBackClick: () -> Unit,
    onPageChange: (Int) -> Unit,
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
            Box(modifier.padding(innerPadding)) {
                DetailPager(onPageChange, onBackClick)
            }
        }
    }
}

@ExperimentalPagerApi
@Composable
private fun DetailPager(onPageChange: (Int) -> Unit, onBackClick: () -> Unit) {
    val videoDetailViewModel: VideoDetailViewModel = hiltViewModel()
    val title = videoDetailViewModel.getTitle()
    val detailListState = videoDetailViewModel.getListLiveData().observeAsState()
    val enterDetailData = videoDetailViewModel.getEnterDetailData()
    val detailList = detailListState.value?.dataList
    detailList?.let { dataList ->
        val pagerState =
            rememberPagerState(dataList.indexOfFirst { it.state == VideoState.Playing })
        LaunchedEffect(pagerState) {
            // Collect from the pager state a snapshotFlow reading the currentPage
            snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { page ->
                if (page != dataList.indexOfFirst { it.state == VideoState.Playing }) {
                    Log.i(TAG, "onPageChange: page=$page")
                    onPageChange(page)
                }
            }
        }
        VerticalPager(
            modifier = Modifier,
            state = pagerState,
            count = dataList.size
        ) { page ->
            val itemData = dataList[page]
            DetailItem(
                modifier = Modifier,
                coreVideoPlayer = itemData.player,
                onBackClick = onBackClick,
                title = if (itemData.vid == enterDetailData?.vid) title else itemData.title,
                detailData = itemData
            )
        }
    }
}

@Composable
private fun DetailItem(
    modifier: Modifier,
    coreVideoPlayer: CoreVideoPlayer?,
    onBackClick: () -> Unit,
    title: String,
    detailData: VideoDetailItemData?
) {
    Column(
        modifier = modifier
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .aspectRatio(16 / 9f)
        ) {
            if (coreVideoPlayer != null) {
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
            } else {
                Text(
                    text = "Waiting",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        )
        detailData?.let {
            Text(
                text = it.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        }
    }
}
