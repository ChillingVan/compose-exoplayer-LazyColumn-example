package com.chillingvan.samples.composevideo.videodetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.PlayParam
import com.chillingvan.samples.composevideo.video.VideoSingleton
import com.chillingvan.samples.composevideo.videodetail.navigation.VideoDetailDestination
import com.chillingvan.samples.composevideo.core.navigation.VideoDetailContinueData
import com.chillingvan.samples.composevideo.videodetail.model.VideoDetailData
import com.chillingvan.samples.composevideo.videodetail.model.VideoDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLDecoder
import javax.inject.Inject

/**
 * Created by Chilling on 2022/8/3.
 */
@HiltViewModel
class VideoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val videoSingleton: VideoSingleton,
    private val mVideoDetailRepository: VideoDetailRepository
): ViewModel() {
    companion object {
        private const val TAG = "VideoDetailViewModel"
    }

    private val mVid: Long = checkNotNull(
        savedStateHandle[VideoDetailDestination.vidArg]
    )

    private val mUrl: String = URLDecoder.decode(
        checkNotNull(
            savedStateHandle[VideoDetailDestination.urlArg]
        ), Charsets.UTF_8.name()
    )

    private val mTitle: String = URLDecoder.decode(
        checkNotNull(
            savedStateHandle[VideoDetailDestination.titleArg]
        ), Charsets.UTF_8.name()
    )

    init {
        play(PlayParam(mVid, mUrl))
    }

    private fun play(playParam: PlayParam) {
        videoSingleton.play(playParam)
    }

    fun resume() {
        Log.i(TAG, "resume")
        videoSingleton.resume()
    }

    fun pause() {
        Log.i(TAG, "pause")
        videoSingleton.pause()
    }

    fun getPlayer(): CoreVideoPlayer {
        return videoSingleton
    }

    fun getTitle() = mTitle

    fun getDetailData(): VideoDetailData? {
        return mVideoDetailRepository.getDetailData(mVid)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onCleared")
        videoSingleton.bindVideoView(null)
        VideoDetailContinueData.produce(VideoDetailContinueData.ContinueData(mVid))
    }
}