package com.chillingvan.samples.composevideo.core.ui.video

import android.app.Activity
import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.chillingvan.samples.composevideo.core.ui.noRippleClickable
import com.chillingvan.samples.composevideo.video.PlayControlViewModel
import com.github.krottv.compose.sliders.DefaultThumb
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.SliderValueHorizontal
import com.chillingvan.samples.composevideo.core.ui.R
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

private const val TAG = "PlayControlView"

/**
 * Created by Chilling on 2022/8/20.
 */
@Composable
fun PlayControlView(modifier: Modifier = Modifier, playControlViewModel: PlayControlViewModel,
                    onBackClick: (() -> Unit)? = null,
                    onFullClick: (() -> Unit)? = null,
                    onExitFullClick: (() -> Unit)? = null
) {
    val playPauseState = playControlViewModel.getPlayPauseStateLiveData().observeAsState()
    val window = (LocalContext.current as? Activity)?.window
    if (playPauseState.value == true) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    val showHideState = playControlViewModel.getShowHideControlLiveData().observeAsState()
    val progressState = playControlViewModel.getProgressLiveData().observeAsState()
    val errorState = playControlViewModel.getErrorLiveData().observeAsState()

    Box(modifier.noRippleClickable {
        if (errorState.value == null) {
            playControlViewModel.clickToShowHide()
        } else {
            playControlViewModel.clickToPlayOverError()
        }
    }) {
        if (showHideState.value == true) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x98000000))) {
                if (onBackClick != null) {
                    IconButton(modifier = Modifier.align(Alignment.TopStart), onClick = { onBackClick.invoke() }) {
                        Icon(
                            tint = Color.White,
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
                PlayPauseIcon(
                    Modifier.Companion
                        .align(Alignment.Center)
                        .noRippleClickable {
                            playControlViewModel.dispatchPlayPause()
                        }, playPauseState)
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.BottomCenter)) {
                PlayProgressBar(
                    Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    progressState,
                    onDragStart = {
                        playControlViewModel.dragStart()
                    },
                    onProgressChange = { percent ->
                        playControlViewModel.dragToPercent(percent)
                    },
                    onProgressChangeFinish = {
                        playControlViewModel.seekToPercent()
                    },
                    onFullClick = onFullClick?.let {
                        {
                            playControlViewModel.hide()
                            onFullClick.invoke()
                        }
                    },
                    onExitFullClick = onExitFullClick?.let {
                        {
                            playControlViewModel.hide()
                            onExitFullClick.invoke()
                        }
                    }
                )
            }
        }
        if (errorState.value != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0x98000000))
            ) {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                    text = "Play Error:" + (errorState.value ?: ""))
            }
        }
    }
}

@Composable
private fun PlayProgressBar(modifier: Modifier, progressState: State<PlayControlViewModel.ProgressData?>,
                            onDragStart: () -> Unit,
                            onProgressChange: (percent: Float) -> Unit,
                            onProgressChangeFinish: () -> Unit,
                            onFullClick: (() -> Unit)? = null,
                            onExitFullClick: (() -> Unit)? = null
) {
    progressState.value?.let { progressData ->
        Row(modifier = modifier) {
            SliderValueHorizontal(
                value = progressData.position.toFloat() / progressData.duration,
                secondValue = progressData.bufferPosition.toFloat() / progressData.duration,
                onValueChange = { percent ->
                    onProgressChange.invoke(percent)
                },
                onValueChangeFinished = {
                    onProgressChangeFinish.invoke()
                },
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                interactionSource = remember {
                    object : MutableInteractionSource {
                        override val interactions = MutableSharedFlow<Interaction>(
                            extraBufferCapacity = 16,
                            onBufferOverflow = BufferOverflow.DROP_OLDEST
                        )

                        override suspend fun emit(interaction: Interaction) {
                            Log.i(TAG, "emit=$interaction")
                            if (interaction is DragInteraction.Start) {
                                onDragStart.invoke()
                            }
                            interactions.emit(interaction)
                        }

                        override fun tryEmit(interaction: Interaction): Boolean {
                            Log.i(TAG, "tryEmit=$interaction")
                            return interactions.tryEmit(interaction)
                        }
                    }
                },
                track = { modifier: Modifier,
                          fraction: Float,
                          secondFraction: Float?,
                          interactionSource: MutableInteractionSource,
                          tickFractions: List<Float>,
                          enabled: Boolean ->

                    DefaultTrack(
                        modifier,
                        fraction,
                        secondFraction,
                        colorProgress = Color.White,
                        colorProgressSecond = Color(0x8fffffff),
                        colorTrack = Color.Gray,
                        interactionSource = interactionSource,
                        tickFractions = tickFractions,
                        enabled = enabled,
                        height = 4.dp
                    )
                },
                thumb = { modifier: Modifier,
                          offset: Dp,
                          interactionSource: MutableInteractionSource,
                          enabled: Boolean,
                          thumbSize: DpSize ->
                    DefaultThumb(
                        modifier, offset, interactionSource, enabled, thumbSize,
                        color = Color.White,
                        scaleOnPress = 1.3f
                    )
                }
            )
            Box(Modifier
                .fillMaxHeight()) {
                    Text(modifier = Modifier.align(Alignment.Center),
                        text = progressData.timeStr, color = Color.White, maxLines = 1)
                }
            IconButton(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                onFullClick?.invoke()
                onExitFullClick?.invoke()
            }) {
                Image(
                    painter = painterResource(
                        if (onFullClick != null) {
                            PlayControlViewModel.getFullScreenEnter()
                        } else {
                            PlayControlViewModel.getFullScreenExit()
                        }
                    ),
                    contentDescription = stringResource(id = R.string.fullscreen)
                )
            }
        }
    }
}

@Composable
private fun PlayPauseIcon(modifier: Modifier, playPauseState: State<Boolean?>) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (playPauseState.value == true) {
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                painter = painterResource(PlayControlViewModel.getPauseDrawable()),
                contentDescription = stringResource(PlayControlViewModel.getPauseDesc())
            )
        } else {
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                painter = painterResource(PlayControlViewModel.getPlayDrawable()),
                contentDescription = stringResource(PlayControlViewModel.getPlayDesc())
            )
        }
    }
}

@Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Composable
fun PlayControlPreview() {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable {
                }) {
            PlayPauseIcon(
                Modifier.Companion
                    .align(Alignment.Center), object : State<Boolean?> {
                    override val value: Boolean?
                        get() = true
                })
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .align(Alignment.BottomCenter)) {
            PlayProgressBar(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                object : State<PlayControlViewModel.ProgressData?> {
                    override val value: PlayControlViewModel.ProgressData?
                        get() = PlayControlViewModel.ProgressData(1000, 2000, "10:11/12:30", 5000)
                },
                onDragStart = {},
                onProgressChange = { percent ->
                },
                onProgressChangeFinish = {
                }
            )
        }
    }
}
