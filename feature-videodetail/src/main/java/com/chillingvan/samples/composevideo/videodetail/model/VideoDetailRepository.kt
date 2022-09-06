package com.chillingvan.samples.composevideo.videodetail.model

import javax.inject.Inject

/**
 * Created by Chilling on 2022/9/6.
 */
class VideoDetailRepository @Inject constructor()  {
    private val mDataList = listOf(
        VideoDetailData(
            1234,
            "The data sources from Internet. You can change it in VideoItemsRepository.kt."
        ),
        VideoDetailData(
            1236,
            "It is so empty here so I add some description."
        ),
        VideoDetailData(
            1237,
            "It is so empty here so I add some description."
        ),
        VideoDetailData(
            1235,
            "It is so empty here so I add some description."
        ),
    )

    fun getDetailData(vid: Long): VideoDetailData? {
        for (detailData in mDataList) {
            if (detailData.vid == vid) {
                return detailData
            }
        }
        return null
    }
}

data class VideoDetailData(val vid: Long, val description: String)