package com.chillingvan.samples.composevideo.main.model

import com.chillingvan.samples.composevideo.main.R
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.VideoState
import com.chillingvan.samples.composevideo.video.VideoTestData
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
        val item0 = VideoTestData.list[0]
        val item1 = VideoTestData.list[1]
        val item2 = VideoTestData.list[2]
        val item3 = VideoTestData.list[3]
        val obtainList = listOf(
            VideoStateItemData(
                item0.vid,
                title = item0.title,
                url = item0.url,
                cover = R.drawable.first_video
            ),
            VideoStateItemData(
                item1.vid,
                title = item1.title,
                url = item1.url,
                cover = R.drawable.second_video
            ),
            VideoStateItemData(
                item2.vid,
                title = item2.title,
                url = item2.url,
                cover = R.drawable.third_video
            ),
            VideoStateItemData(
                item3.vid,
                title = item3.title,
                url = item3.url,
                cover = R.drawable.fourth_video
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