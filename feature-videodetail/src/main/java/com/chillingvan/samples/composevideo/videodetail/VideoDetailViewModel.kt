package com.chillingvan.samples.composevideo.videodetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.PlayParam
import com.chillingvan.samples.composevideo.video.VideoSingleton
import com.chillingvan.samples.composevideo.videodetail.navigation.VideoDetailDestination
import com.chillingvan.samples.composevideo.core.navigation.VideoDetailContinueData
import com.chillingvan.samples.composevideo.video.VideoState
import com.chillingvan.samples.composevideo.videodetail.model.VideoDetailItemData
import com.chillingvan.samples.composevideo.videodetail.model.VideoDetailListStateData
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

    private val mListLiveData = MutableLiveData<VideoDetailListStateData>()

    init {
        play(PlayParam(mVid, mUrl))
        mVideoDetailRepository.initPlayingItem(mVid, videoSingleton)
        mListLiveData.value = VideoDetailListStateData(mVideoDetailRepository.getDataList(), 0)
        prepareNext()
    }

    private fun play(playParam: PlayParam) {
        videoSingleton.play(playParam)
    }

    // First Frame from 1500ms -> 200ms
    fun prepareNext() {
        val dataList = mVideoDetailRepository.getDataList()
        var nextOne: VideoDetailItemData? = null
        for ((index, videoDetailItemData) in dataList.withIndex()) {
            val isLastOne = index == dataList.size - 1
            if (videoDetailItemData.player != null && !isLastOne) {
                nextOne = dataList[index + 1]
                break
            }
        }
        nextOne?.let { itemData ->
            videoSingleton.prepareNotCurrent(PlayParam(itemData.vid, itemData.url, replayIfEnd = true))
        }
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

    fun getEnterDetailData(): VideoDetailItemData? {
        return mVideoDetailRepository.getDetailData(mVid)
    }

    fun getListLiveData(): LiveData<VideoDetailListStateData> = mListLiveData

    fun changePlayingItem(page: Int) {
        Log.i(TAG, "changePlayingItem:page=$page")
        val itemData: VideoDetailItemData = mVideoDetailRepository.getDataList()[page]
        mVideoDetailRepository.recreateList()
        mVideoDetailRepository.initPlayingItem(itemData.vid, videoSingleton)
        play(PlayParam(itemData.vid, itemData.url, replayIfEnd = true))
        mListLiveData.value = VideoDetailListStateData(mVideoDetailRepository.getDataList(), (mListLiveData.value?.changeCnt ?: 0) + 1)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onCleared")
        videoSingleton.clearVideoView()
        mVideoDetailRepository.getDataList().find { it.state == VideoState.Playing }?.let {
            VideoDetailContinueData.produce(VideoDetailContinueData.ContinueData(it.vid))
        }
    }
}