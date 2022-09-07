package com.chillingvan.samples.composevideo.main.model

import com.chillingvan.samples.composevideo.core.ui.list.ListItemData
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer

/**
 * Created by Chilling on 2022/7/28.
 */
data class VideoStateItemData(
    val vid: Long,
    val title: String,
    var state: VideoState = VideoState.Released,
    val url: String,
    var player: CoreVideoPlayer? = null,
    var triggerResume: Boolean = false
): ListItemData

class BottomTakeUpData : ListItemData

sealed class VideoState {
    object Playing : VideoState()
    object Pausing : VideoState()
    object Released : VideoState()
}

data class ListStateData(
    private val inputList: List<ListItemData>,
    // Use this to trigger livedata to change. Composition won't be triggered for only list changing
    var changeCnt: Int = 0
    ) {
    val list = inputList + listOf(BottomTakeUpData())
}