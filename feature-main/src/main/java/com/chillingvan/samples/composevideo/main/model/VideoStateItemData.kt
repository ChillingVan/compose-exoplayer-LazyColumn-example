package com.chillingvan.samples.composevideo.main.model

import com.chillingvan.samples.composevideo.core.ui.list.ListItemData
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.VideoState

/**
 * Created by Chilling on 2022/7/28.
 */
data class VideoStateItemData(
    val vid: Long,
    val title: String,
    var state: VideoState = VideoState.Released,
    val url: String,
    val cover: Int? = null,
    var player: CoreVideoPlayer? = null,
    var triggerResume: Boolean = false
): ListItemData

class BottomTakeUpData : ListItemData

data class ListStateData(
    private val inputList: List<ListItemData>,
    // Use this to trigger livedata to change. Composition won't be triggered for only list changing
    var changeCnt: Int = 0
    ) {
    val list = inputList + listOf(BottomTakeUpData())
}