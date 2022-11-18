package com.chillingvan.samples.composevideo.videodetail.model

import com.chillingvan.samples.composevideo.core.ui.list.ListItemData
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.VideoState
import com.chillingvan.samples.composevideo.video.VideoTestData
import javax.inject.Inject

/**
 * Created by Chilling on 2022/9/6.
 */
class VideoDetailRepository @Inject constructor()  {
    private val mDataList = mutableListOf<VideoDetailItemData>().also {
        it.addAll(createList())
    }

    private fun createList(): List<VideoDetailItemData> {
        val item0 = VideoTestData.list[0]
        val item1 = VideoTestData.list[1]
        val item2 = VideoTestData.list[2]
        val item3 = VideoTestData.list[3]
        return listOf(
            VideoDetailItemData(
                item0.vid,
                title = item0.title,
                url = item0.url,
                "The data sources from Internet. You can change it in VideoTestData.kt."
            ),
            VideoDetailItemData(
                item1.vid,
                title = item1.title,
                url = item1.url,
                "It is so empty here so I add some description."
            ),
            VideoDetailItemData(
                item2.vid,
                title = item2.title,
                url = item2.url,
                "It is so empty here so I add some description."
            ),
            VideoDetailItemData(
                item3.vid,
                title = item3.title,
                url = item3.url,
                "It is so empty here so I add some description."
            ),
        )
    }

    fun getDetailData(vid: Long): VideoDetailItemData? {
        for (detailData in mDataList) {
            if (detailData.vid == vid) {
                return detailData
            }
        }
        return null
    }

    fun getDataList() = mDataList

    fun recreateList() {
        mDataList.clear()
        mDataList.addAll(createList())
    }

    fun initPlayingItem(vid: Long, player: CoreVideoPlayer) {
        for (stateData in mDataList) {
            if (stateData.vid == vid) {
                stateData.state = VideoState.Playing
                stateData.player = player
            } else {
                stateData.state = VideoState.Released
                stateData.player = null
            }
        }
    }
}

data class VideoDetailListStateData(
    val dataList: List<VideoDetailItemData>,
    // Use this to trigger livedata to change. Composition won't be triggered for only list changing
    var changeCnt: Int = 0
)

data class VideoDetailItemData(
    val vid: Long,
    val title: String,
    val url: String,
    val description: String,
    var state: VideoState = VideoState.Released,
    var player: CoreVideoPlayer? = null,
    ): ListItemData