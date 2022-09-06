package com.chillingvan.samples.composevideo.main.model

import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.VideoSingleton
import javax.inject.Inject

/**
 * Created by Chilling on 2022/7/28.
 */
class VideoItemsRepository @Inject constructor() {

    private val mItemList = mutableListOf<VideoStateItemData>()

    init {
        createList(null, null)
    }

    private fun createList(playingId: Long?, player: CoreVideoPlayer?) {
        mItemList.clear()
        val obtainList = listOf(
            VideoStateItemData(
                1234,
                title = "First Video",
                url = "https://v-cdn.zjol.com.cn/280123.mp4"
            ),
            VideoStateItemData(
                1235,
                title = "Second Video",
                url = "https://media.w3.org/2010/05/sintel/trailer.mp4"
            ),
            VideoStateItemData(
                1236,
                title = "Third Video",
                url = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.webm"
            ),
            VideoStateItemData(
                1237,
                title = "Fourth",
                url = "http://techslides.com/demos/sample-videos/small.mp4"
            ),
        )
        for (stateData in obtainList) {
            if (stateData.vid == playingId) {
                stateData.state = VideoState.Playing
                stateData.player = player
            }
        }
        mItemList.addAll(obtainList)
    }

    fun getList() = mItemList

    fun changeVideo(target: VideoStateItemData, player: CoreVideoPlayer): List<VideoStateItemData> {
        createList(target.vid, player)
        return mItemList
    }

    fun findAndContinuePlay(vid: Long, player: CoreVideoPlayer): List<VideoStateItemData>? {
        var result = false
        for (videoStateItemData in mItemList) {
            if (vid == videoStateItemData.vid) {
                result = true
            }
        }
        if (result) {
            createList(vid, player)
            for (videoStateItemData in mItemList) {
                if (vid == videoStateItemData.vid) {
                    videoStateItemData.triggerResume = true
                }
            }
            return mItemList
        }
        return null
    }
}