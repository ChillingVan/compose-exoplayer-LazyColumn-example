package com.chillingvan.samples.composevideo.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chillingvan.samples.composevideo.main.model.ListStateData
import com.chillingvan.samples.composevideo.main.model.VideoStateItemData
import com.chillingvan.samples.composevideo.main.model.VideoItemsRepository
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.PlayParam
import com.chillingvan.samples.composevideo.core.navigation.VideoDetailContinueData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Created by Chilling on 2022/7/27.
 */
@HiltViewModel
class MainPageViewModel @Inject constructor(
    private val videoSingleton: CoreVideoPlayer,
    private val videoRepository: VideoItemsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MainPageViewModel"
    }

    private val mListLiveData = MutableLiveData<ListStateData>()

    init {
        mListLiveData.value = ListStateData(videoRepository.getList())
    }

    fun getListLiveData() = mListLiveData

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

    override fun onCleared() {
        super.onCleared()
        videoSingleton.release()
    }

    fun changeVideo(target: VideoStateItemData) {
        mListLiveData.value = ListStateData(videoRepository.changeVideo(target, videoSingleton),
            (mListLiveData.value?.changeCnt ?: 0) + 1)
        play(PlayParam(target.vid, target.url))
    }

    fun findAndTryContinuePlayItem() {
        VideoDetailContinueData.consume()?.let {
            val vid = it.vid
            videoRepository.findAndContinuePlay(vid, videoSingleton)?.let { list ->
                mListLiveData.value?.let { stateData ->
                    Log.i(TAG, "findAndTryContinuePlayItem trigger")
                    mListLiveData.setValue(ListStateData(list, stateData.changeCnt + 1))
                }
            }
        }
    }

    fun getPlayer(): CoreVideoPlayer {
        return videoSingleton
    }

    fun scrollFinish(focusIndex: Int) {
        val list = videoRepository.getList()
        list.getOrNull(focusIndex)?.let {
            changeVideo(it)
        }
    }
}