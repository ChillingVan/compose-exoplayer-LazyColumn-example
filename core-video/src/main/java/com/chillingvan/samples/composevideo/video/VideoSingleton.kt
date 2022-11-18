package com.chillingvan.samples.composevideo.video

import android.app.Application
import android.util.Log
import android.view.View
import androidx.collection.LruCache
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by Chilling on 2022/8/1.
 */
@Singleton
class VideoSingleton  @Inject constructor(
    private val application: Application
): CoreVideoPlayer {

    companion object {
        private const val TAG = "VideoSingleton"
    }

    private var exoPlayer = createPlayer(application)

    private fun createPlayer(application: Application) =
        ExoPlayer.Builder(application.applicationContext).build()

    private var mPlayerView: View? = null
    private val mPositionRecorder = PositionRecorder(this)
    private var mIsReleased = false

    init {
        exoPlayer.addListener(object : Player.Listener {

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                Log.i(TAG, "onEvents:${events.get(0)}; position=${player.currentPosition}")
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                Log.i(TAG, "onMediaItemTransition:mediaId=${mediaItem?.mediaId};position=${exoPlayer.currentPosition};reason=$reason;")
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.i(TAG, "onPlayerError:error=${error.errorCodeName}; position=${exoPlayer.currentPosition}")
            }
        })
        mPositionRecorder.progressRecordStart()
    }

    override fun play(playParam: PlayParam) {
        if (mIsReleased) {
            mIsReleased = false
            exoPlayer = createPlayer(application)
        }
        val mediaItem = MediaItem.Builder()
            .setUri(playParam.url)
            .setMediaId(playParam.vid.toString())
            .build()
        if (exoPlayer.currentMediaItem?.mediaId == mediaItem.mediaId) {
            return
        }
        exoPlayer.setMediaItem(mediaItem)
//        exoPlayer.release()
        exoPlayer.prepare()
        exoPlayer.play()
        val progress = mPositionRecorder.getProgress(mediaItem.mediaId)
        if (playParam.replayIfEnd && progress >= mPositionRecorder.getDuration(mediaItem.mediaId) - 1000) {
            exoPlayer.seekTo(0)
        } else {
            exoPlayer.seekTo(progress.toLong())
        }
    }

    override fun resume() {
        if (mPlayerView != null) {
            exoPlayer.playWhenReady = true
        }
    }

    override fun pause() {
        exoPlayer.playWhenReady = false
    }

    override fun release() {
        exoPlayer.release()
        mIsReleased = true
    }

    override fun getPlayer(): Player = exoPlayer

    override fun bindVideoView(view: View?) {
        Log.i("VideoSingleton", "bindVideoView:$view")
        mPlayerView = view
    }

    fun getPlayerView() = mPlayerView

    fun progressChangeFlow() = flow<Int> {
        while (true) {
            emit(exoPlayer?.currentPosition?.toInt()!!)
            delay(500)
        }
    }.flowOn(Dispatchers.Main)

    fun addListener(listener: Player.Listener) {
        exoPlayer.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        exoPlayer.removeListener(listener)
    }

    private class PositionRecorder(private val player: VideoSingleton) {
        private val mCache: LruCache<String, ProgressData> = LruCache(10)

        private object PositionRecorderScope : CoroutineScope {
            /**
             * Returns [EmptyCoroutineContext].
             */
            override val coroutineContext: CoroutineContext
                get() = EmptyCoroutineContext
        }

        fun progressRecordStart() {
            PositionRecorderScope.launch(Dispatchers.Main) {
                player.progressChangeFlow().collect { progress ->
                    player.exoPlayer.currentMediaItem?.let {
                        mCache.put(it.mediaId, ProgressData(progress, player.exoPlayer.duration))
                    }
                }
            }
        }

        fun getProgress(mediaId: String): Int {
            return mCache[mediaId]?.progress ?: 0
        }

        fun getDuration(mediaId: String): Long {
            return mCache[mediaId]?.duration ?: 0L
        }

        private data class ProgressData(val progress: Int, val duration: Long)
    }
}