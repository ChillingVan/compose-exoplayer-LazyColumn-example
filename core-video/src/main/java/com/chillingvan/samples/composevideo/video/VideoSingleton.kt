package com.chillingvan.samples.composevideo.video

import android.app.Application
import android.view.View
import androidx.collection.LruCache
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
class VideoSingleton @Inject constructor(
    private val application: Application
) : CoreVideoPlayer {

    companion object {
        private const val TAG = "VideoSingleton"
        private const val POOL_SIZE = 2;
    }

    // POOL_SIZE = 2
    private val mPlayerPool = mutableListOf<PlayerController>()
    private var mCurrentPlayer: PlayerController = PlayerController(application)
    init {
        mPlayerPool.add(mCurrentPlayer)
        mPlayerPool.add(PlayerController(application))
    }

    private val mListenerList = mutableListOf<Player.Listener>()
    private val mPositionRecorder = PositionRecorder(this)

    init {
        mPositionRecorder.progressRecordStart()
    }

    override fun play(playParam: PlayParam) {
        val vid = playParam.vid
        if (mCurrentPlayer.getCurrentMediaId() == vid.toString()) {
            // Avoid play the same, do resume instead
            resume()
            return
        }
        val playOne = if (mPlayerPool.size == 0) {
            mCurrentPlayer
        } else {
            getReadyPlayerFromPool(vid) ?: mCurrentPlayer
        }
        mCurrentPlayer = playOne
        stopAllOthers()
        for (listener in mListenerList) {
            playOne.addListener(listener)
        }
        playOne.play(playParam, object : PlayerController.PositionProvider {
            override fun getPosition(mediaId: String): Long {
                return mPositionRecorder.getProgress(mediaId).toLong()
            }

            override fun getDuration(mediaId: String): Long {
                return mPositionRecorder.getDuration(mediaId)
            }
        })
    }

    private fun getReadyPlayerFromPool(vid: Long): PlayerController? {
        for (videoController in mPlayerPool) {
            if (videoController.getCurrentMediaId() == vid.toString()) {
                return videoController
            }
        }
        return null
    }

    private fun acquireNotPlayOneFromPool(): PlayerController? {
        val entries = mPlayerPool
        for (entry in entries) {
            if (entry !== mCurrentPlayer) {
                return entry
            }
        }
        return null
    }

    private fun stopAllOthers() {
        for (entry in mPlayerPool) {
            if (entry !== mCurrentPlayer) {
                entry.stop()
                for (listener in mListenerList) {
                    entry.removeListener(listener)
                }
            }
        }
    }

    override fun resume() {
        mCurrentPlayer.resume()
    }

    override fun pause() {
        mCurrentPlayer.pause()
    }

    override fun release() {
        for (player in mPlayerPool) {
            player.release()
        }
    }

    override fun prepareNotCurrent(playParam: PlayParam) {
        acquireNotPlayOneFromPool()?.let {
            if (it.getCurrentMediaId() == playParam.vid.toString()) {
                // Avoid prepare a prepared one, such as the stopped one.
                return
            }
            it.prepare(playParam)
        }
    }

    override fun getPlayer(): Player {
        return mCurrentPlayer.getPlayer()
    }

    override fun bindVideoView(view: View?) {
        mCurrentPlayer.bindVideoView(view)
    }

    override fun clearVideoView() {
        for (player in mPlayerPool) {
            player.bindVideoView(null)
        }
    }

    private fun progressChangeFlow() = flow<Int> {
        while (true) {
            emit(mCurrentPlayer.getCurPosition().toInt())
            delay(500)
        }
    }.flowOn(Dispatchers.Main)

    fun addListener(listener: Player.Listener) {
        mListenerList.add(listener)
        mCurrentPlayer.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        for (l in mListenerList) {
            mCurrentPlayer.removeListener(l)
        }
        mListenerList.remove(listener)
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
                    player.mCurrentPlayer.let { videoController ->
                        videoController.getCurrentMediaId()?.let {
                            mCache.put(it, ProgressData(progress, videoController.getDuration()))
                        }
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