package com.chillingvan.samples.composevideo.video

import android.app.Application
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.PlaybackStatsListener

/**
 * Created by Chilling on 2022/12/12.
 */
class PlayerController(
    private val application: Application
) {

    companion object {
        private const val TAG = "VideoController"
    }

    private var exoPlayer = createPlayer(application)

    private fun createPlayer(application: Application) =
        ExoPlayer.Builder(application.applicationContext).build()

    private var mPlayerView: View? = null
    private var mIsReleased = false

    private var mPrepareItem: MediaItem? = null

    private var playStartTime: Long = 0L

    init {
        exoPlayer.addListener(object : Player.Listener {

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                // 0=EVENT_TIMELINE_CHANGED, 2=EVENT_TRACKS_CHANGED
                // 3=EVENT_IS_LOADING_CHANGED, 4=EVENT_PLAYBACK_STATE_CHANGED
                // 5=EVENT_PLAY_WHEN_READY_CHANGED, 24=EVENT_SURFACE_SIZE_CHANGED
                // 25=EVENT_VIDEO_SIZE_CHANGED, 26=EVENT_RENDERED_FIRST_FRAME
                Log.i(TAG, "id=${getCurrentMediaId()} onEvents:${events.get(0)}; position=${player.currentPosition}")
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                Log.i(TAG, "onMediaItemTransition:mediaId=${mediaItem?.mediaId};position=${exoPlayer.currentPosition};reason=$reason;")
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.i(TAG, "onPlayerError:error=${error.errorCodeName}; position=${exoPlayer.currentPosition}")
            }

            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
                Log.i(TAG, "id=${getCurrentMediaId()} onRenderedFirstFrame: time=${SystemClock.elapsedRealtime() - playStartTime}")
            }
        })
        exoPlayer.addAnalyticsListener(PlaybackStatsListener(true
        ) { eventTime, playbackStats ->
            val historyStateLog = playbackStats.playbackStateHistory.map {
                "${it.playbackState}"
            }.reduce { acc, state ->
                "$acc, $state"
            }

            Log.i(TAG, "PlaybackStatsListener: stateHistory=$historyStateLog, totalElapsedTimeMs=${playbackStats.totalElapsedTimeMs}; " +
                    "position=${exoPlayer.currentPosition}; firstReportedTimeMs=${playbackStats.firstReportedTimeMs}; meanElapsedTimeMs=${playbackStats.meanElapsedTimeMs}; " +
                    "meanBandwidth=${playbackStats.meanBandwidth}; totalPausedTimeMs=${playbackStats.totalPausedTimeMs}; totalBandwidthBytes=${playbackStats.totalBandwidthBytes}"
            )
        })
    }


    fun prepare(playParam: PlayParam) {
        Log.i(TAG, "curObj=${this.hashCode()} prepare id=${playParam.vid}")
        if (mIsReleased) {
            mIsReleased = false
            exoPlayer = createPlayer(application)
        }
        val mediaItem = MediaItem.Builder()
            .setUri(playParam.url)
            .setMediaId(playParam.vid.toString())
            .build()
        exoPlayer.setMediaItem(mediaItem)
//        exoPlayer.release()
        exoPlayer.prepare()
        mPrepareItem = mediaItem
    }

    fun play(playParam: PlayParam, positionHandler: PositionProvider) {
        playStartTime = SystemClock.elapsedRealtime()
        Log.i(TAG, "curObj=${this.hashCode()} play id=${playParam.vid}")
        if (mPrepareItem == null || playParam.vid.toString() != mPrepareItem?.mediaId) {
            prepare(playParam)
        }
        mPrepareItem?.let { mediaItem ->
            exoPlayer.play()
            val progress = positionHandler.getPosition(mediaItem.mediaId)
            if (playParam.replayIfEnd && progress >= positionHandler.getDuration(mediaItem.mediaId) - 1000) {
                exoPlayer.seekTo(0)
            } else {
                exoPlayer.seekTo(progress)
            }
            mPrepareItem = null
        }
    }

    fun resume() {
        Log.i(TAG, "resume, id=${getCurrentMediaId()}")
        if (mPlayerView != null) {
            exoPlayer.playWhenReady = true
        }
    }

    fun pause() {
        Log.i(TAG, "pause, id=${getCurrentMediaId()}")
        if (exoPlayer.isPlaying || exoPlayer.isPlayingAd) {
            exoPlayer.playWhenReady = false
        }
    }

    fun stop() {
        Log.i(TAG, "curObj=${this.hashCode()} stop, id=${getCurrentMediaId()}, isPlaying=${exoPlayer.isPlaying}")
        if (exoPlayer.isPlaying || exoPlayer.isPlayingAd) {
            exoPlayer.stop()
        }
    }

    fun release() {
        exoPlayer.release()
        mIsReleased = true
    }

    fun getPlayer(): Player {
        Log.i(TAG, "curObj=${this.hashCode()} getPlayer, id=${getCurrentMediaId()}")
        return exoPlayer
    }

    fun bindVideoView(view: View?) {
        Log.i(TAG, "curObj=${this.hashCode()} bindVideoView:$view, id=${getCurrentMediaId()}")
        mPlayerView = view
    }

    fun addListener(listener: Player.Listener) {
        exoPlayer.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        exoPlayer.removeListener(listener)
    }

    fun getCurrentMediaId(): String? {
        return exoPlayer.currentMediaItem?.mediaId
    }

    fun getCurPosition() = exoPlayer.currentPosition

    fun getDuration() = exoPlayer.duration

    interface PositionProvider {
        fun getPosition(mediaId: String): Long
        fun getDuration(mediaId: String): Long
    }
}