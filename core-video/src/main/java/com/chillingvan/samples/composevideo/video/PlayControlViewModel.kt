package com.chillingvan.samples.composevideo.video

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Created by Chilling on 2022/8/20.
 */
@HiltViewModel
class PlayControlViewModel @Inject constructor(
    private val videoSingleton: VideoSingleton
): ViewModel() {

    companion object {
        private const val MAX_UPDATE_INTERVAL_MS = 1000L
        private const val DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS = 200L

        fun getPauseDrawable() = R.drawable.exo_icon_pause
        fun getPlayDrawable() = R.drawable.exo_icon_play
        fun getFullScreenEnter() = R.drawable.exo_ic_fullscreen_enter
        fun getFullScreenExit() = R.drawable.exo_ic_fullscreen_exit
        fun getPauseDesc() = R.string.exo_controls_pause_description
        fun getPlayDesc() = R.string.exo_controls_play_description
    }

    private val mListener =
        object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                if (events.containsAny(
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED
                    )
                ) {
                    mPlayPauseStateLiveData.value = shouldShowPauseButton(player).also { playing ->
                        if (playing) {
                            if (mShowHideControlLiveData.value == true) {
                                postAutoHide()
                            }
                            updateProgress(player)
                        } else {
                            mShowControlPadJob?.cancel()
                        }
                    }
                } else if (events.containsAny(
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                        Player.EVENT_IS_PLAYING_CHANGED
                    )
                ) {
                    updateProgress(player)
                } else if (events.containsAny(
                        Player.EVENT_POSITION_DISCONTINUITY,
                        Player.EVENT_TIMELINE_CHANGED
                    )
                ) {
                    updateProgress(player)
                }

            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                mErrorLiveData.value = error.errorCodeName
            }
        }

    private val mPlayPauseStateLiveData = MutableLiveData<Boolean>()
    private val mShowHideControlLiveData = MutableLiveData<Boolean>()
    private val mProgressLiveData = MutableLiveData<ProgressData>()
    private var mErrorLiveData = MutableLiveData<String?>()
    private var mShowControlPadJob: Job? = null
    private var mUpdateProgressJob: Job? = null

    private var mPreferredUpdateDelay: Long? = null
    private val mFormatBuilder = StringBuilder()
    private val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
    private var mSeekPercent: Float? = null

    fun getShowHideControlLiveData() = mShowHideControlLiveData

    fun getPlayPauseStateLiveData() = mPlayPauseStateLiveData

    fun getProgressLiveData() = mProgressLiveData

    fun getErrorLiveData() = mErrorLiveData

    init {
        videoSingleton.addListener(mListener)
    }

    private fun shouldShowPauseButton(player: Player?): Boolean {
        return player != null && player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE && player.playWhenReady
    }

    fun clickToShowHide() {
        if (mShowHideControlLiveData.value == true) {
            mShowHideControlLiveData.value = false
        } else {
            mShowHideControlLiveData.value = true
            postAutoHide()
        }
    }

    fun hide() {
        mShowHideControlLiveData.value = false
    }

    private fun postAutoHide() {
        mShowControlPadJob?.cancel()
        mShowControlPadJob = viewModelScope.launch {
            delay(3000)
            mShowHideControlLiveData.postValue(false)
        }
    }

    fun dispatchPlayPause() {
        val player = videoSingleton.getPlayer()
        val state = player.playbackState
        if (state == Player.STATE_IDLE || state == Player.STATE_ENDED || !player.playWhenReady) {
            dispatchPlay(player)
        } else {
            dispatchPause(player)
        }
    }

    private fun dispatchPlay(player: Player) {
        val state = player.playbackState
        if (state == Player.STATE_IDLE) {
            player.prepare()
        } else if (state == Player.STATE_ENDED) {
            seekTo(player, player.currentMediaItemIndex, C.TIME_UNSET)
        }
        player.play()
    }

    private fun seekTo(player: Player, windowIndex: Int, positionMs: Long) {
        player.seekTo(windowIndex, positionMs)
    }

    private fun dispatchPause(player: Player) {
        player.pause()
    }

    fun setPreferredUpdateDelay(preferredUpdateDelay: Long) {
        mPreferredUpdateDelay = preferredUpdateDelay
    }

    private fun updateProgress(player: Player) {
        val position: Long = player.contentPosition
        val bufferedPosition: Long = player.contentBufferedPosition

        mProgressLiveData.value = ProgressData(position, bufferedPosition,
            Util.getStringForTime(mFormatBuilder, mFormatter, position) + "/" + Util.getStringForTime(mFormatBuilder, mFormatter, player.duration), player.duration)

        mUpdateProgressJob?.cancel()
        mUpdateProgressJob = viewModelScope.launch {
            val playbackState = player?.playbackState ?: Player.STATE_IDLE
            if (player.isPlaying) {
                var mediaTimeDelayMs =
                    if (mPreferredUpdateDelay != null) mPreferredUpdateDelay!! else MAX_UPDATE_INTERVAL_MS

                // Limit delay to the start of the next full second to ensure position display is smooth.
                val mediaTimeUntilNextFullSecondMs = 1000 - position % 1000
                mediaTimeDelayMs = mediaTimeDelayMs.coerceAtMost(mediaTimeUntilNextFullSecondMs)

                // Calculate the delay until the next update in real time, taking playback speed into account.
                val playbackSpeed = player.playbackParameters.speed
                var delayMs =
                    if (playbackSpeed > 0) (mediaTimeDelayMs / playbackSpeed).toLong() else MAX_UPDATE_INTERVAL_MS

                // Constrain the delay to avoid too frequent / infrequent updates.
                delayMs = Util.constrainValue(
                    delayMs,
                    DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS,
                    MAX_UPDATE_INTERVAL_MS
                )

                delay(delayMs)
                updateProgress(player)
            } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
                delay(MAX_UPDATE_INTERVAL_MS)
                updateProgress(player)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        videoSingleton.removeListener(mListener)
    }

    fun dragStart() {
        mUpdateProgressJob?.cancel()
        mShowControlPadJob?.cancel()
    }

    fun dragToPercent(percent: Float) {
        mSeekPercent = percent
        val player = videoSingleton.getPlayer()
        val position = (player.duration * percent).toLong()
        mProgressLiveData.value = ProgressData(position, 0,
            Util.getStringForTime(mFormatBuilder, mFormatter, position) + "/" + Util.getStringForTime(mFormatBuilder, mFormatter, player.duration), player.duration)
    }

    fun seekToPercent() {
        mSeekPercent?.let { percent ->
            val player = videoSingleton.getPlayer()
            seekTo(player, player.currentMediaItemIndex, (percent * player.duration).toLong())
            updateProgress(player)
            mSeekPercent = null
        }
    }

    fun clickToPlayOverError() {
        mErrorLiveData.value = null
        val player = videoSingleton.getPlayer()
        dispatchPlay(player)
    }

    data class ProgressData(
        val position: Long,
        val bufferPosition: Long,
        val timeStr: String,
        val duration: Long
    )
}