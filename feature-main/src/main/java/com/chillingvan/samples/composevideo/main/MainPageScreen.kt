package com.chillingvan.samples.composevideo.main

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.chillingvan.samples.composevideo.core.ui.BackPressHandler
import com.chillingvan.samples.composevideo.core.ui.component.AppGradientBackground
import com.chillingvan.samples.composevideo.core.ui.theme.AppTheme
import com.chillingvan.samples.composevideo.core.ui.video.FullScreenView
import com.chillingvan.samples.composevideo.core.ui.video.PlayControlView
import com.chillingvan.samples.composevideo.main.model.ListStateData
import com.chillingvan.samples.composevideo.main.model.VideoStateItemData
import com.chillingvan.samples.composevideo.main.model.VideoState
import com.chillingvan.samples.composevideo.video.CoreVideoView
import com.chillingvan.samples.composevideo.video.rememberFullScreenController
import com.chillingvan.samples.composevideo.core.navigation.VideoDetailNavParam
import java.net.URLEncoder

private const val TAG = "MainPageScreen"

/**
 * Created by Chilling on 2022/7/20.
 */
@Composable
fun MainPageRoute(
    windowSizeClass: WindowSizeClass,
    navigateToDetail: (VideoDetailNavParam) -> Unit,
    mainPageViewModel: MainPageViewModel = hiltViewModel()
) {
    val videoListState = mainPageViewModel.getListLiveData().observeAsState()
    val fullScreenController = rememberFullScreenController()
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        MainPageScreen(windowSizeClass, listState = videoListState, onClickItem = {
            mainPageViewModel.changeVideo(it)
        }, onScrollFinish = { focusIndex ->
            mainPageViewModel.scrollFinish(focusIndex)
        }, navigateToDetail)
    } else {
        FullScreenView(mainPageViewModel.getPlayer(), hiltViewModel(), onBackClick = {
            fullScreenController.toPortrait()
        })
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
            } else if (event == Lifecycle.Event.ON_RESUME) {
                mainPageViewModel.findAndTryContinuePlayItem()
                mainPageViewModel.resume()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                mainPageViewModel.pause()
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainPageScreen(
    windowSizeClass: WindowSizeClass,
    listState: State<ListStateData?>,
    onClickItem: (VideoStateItemData) -> Unit,
    onScrollFinish: (index: Int) -> Unit,
    navigateToDetail: (VideoDetailNavParam) -> Unit,
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
                if (listState.value != null) {
                    VideoList(listState, onClickItem, onScrollFinish, navigateToDetail)
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center),
                            text = "No Videos")
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoList(
    listState: State<ListStateData?>,
    onClickItem: (VideoStateItemData) -> Unit,
    onScrollFinish: (index: Int) -> Unit,
    navigateToDetail: (VideoDetailNavParam) -> Unit
) {
    val listenList = rememberLazyListState()
    if (listenList.isScrollInProgress) {
        DisposableEffect(Unit) {
            onDispose {
                // on Scroll Idle
                val viewportStartOffset = listenList.layoutInfo.viewportStartOffset
                val viewportEndOffset = listenList.layoutInfo.viewportEndOffset
                val viewportSize = listenList.layoutInfo.viewportSize
                val totalItemsCount = listenList.layoutInfo.totalItemsCount
                val visibleItemsInfo = listenList.layoutInfo.visibleItemsInfo
                Log.i(TAG, "ScrollIdle:viewportStartOffset=$viewportStartOffset, viewportEndOffset=$viewportEndOffset, viewportSize=$viewportSize, totalItemsCount=$totalItemsCount")
                for (itemInfo in visibleItemsInfo) {
                    Log.i(TAG, "itemInfo:key=${itemInfo.key},index=${itemInfo.index},offset=${itemInfo.offset},size=${itemInfo.size}")
                    val visibleEnoughSize = itemInfo.size * 0.7f
                    val endEdge = itemInfo.offset + itemInfo.size
                    val isVisibleEnough = (endEdge - viewportStartOffset) > visibleEnoughSize && (endEdge - viewportEndOffset) < (itemInfo.size - visibleEnoughSize)
                    if (isVisibleEnough) {
                        onScrollFinish(itemInfo.index)
                        break
                    }
                }
            }
        }
    }
    Log.i(TAG, "Scroll: recompose")
    LazyColumn(state = listenList) {
        items(
            listState.value!!.list
        ) { itemData ->
            if (itemData is VideoStateItemData) {
                VideoItemView(onClickItem, itemData, navigateToDetail)
            } else {
                Column(modifier = Modifier
                    .fillParentMaxHeight()
                    .fillMaxWidth()) {
                    Text("Simulate infinite list so that the last item can scroll out of screen",
                        Modifier.align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoItemView(
    onClickItem: (VideoStateItemData) -> Unit,
    itemData: VideoStateItemData,
    navigateToDetail: (VideoDetailNavParam) -> Unit
) {
    Column(
        modifier = Modifier.clickable {
            onClickItem.invoke(itemData)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
                .clickable {
                    navigateToDetail.invoke(
                        VideoDetailNavParam(
                            itemData.vid,
                            URLEncoder.encode(itemData.url, Charsets.UTF_8.name()),
                            itemData.title
                        )
                    )
                }
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = itemData.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .aspectRatio(16 / 9f)
                .onGloballyPositioned { layoutCoordinates ->
                    val top = layoutCoordinates.positionInParent().y
                    val height = layoutCoordinates.size.height
                    Log.i(TAG, "VideoViewPosition:top=$top, height=$height")
                },
        ) {
            val fullScreenController = rememberFullScreenController()
            when (itemData.state) {
                VideoState.Playing -> {
                    CenterText("Playing", Modifier.align(Alignment.Center))
                    itemData.player?.let {
                        CoreVideoView(
                            coreVideoPlayer = it,
                            onViewUpdate = {
                                if (itemData.triggerResume) {
                                    it.resume()
                                    itemData.triggerResume = false
                                }
                            })
                        PlayControlView(Modifier.fillMaxSize(), hiltViewModel(), onFullClick = {
                            fullScreenController.toFull()
                        })
                    }
                }
                VideoState.Pausing -> {
                    CenterText("Pausing", Modifier.align(Alignment.Center))
                }
                VideoState.Released -> {
                    itemData.cover?.let { cover ->
                        AsyncImage(
                            model = cover,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    CenterText("Click to Play", Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
private fun CenterText(content: String, modifier: Modifier) {
    Text(
        modifier = modifier,
        text = content,
        color = Color.White,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Composable
fun ForYouScreenPreview() {
    BoxWithConstraints {
        AppTheme {
            MainPageScreen(
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight)),
                listState = object : State<ListStateData?> {
                    override val value: ListStateData?
                        get() = null
                },
                onClickItem = {
                },
                onScrollFinish = {},
                navigateToDetail = {
                }
            )
        }
    }
}