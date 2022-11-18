package com.chillingvan.samples.composevideo.video

/**
 * Created by Chilling on 2022/11/17.
 */
object VideoTestData {
    val list: List<Item> = listOf(
        Item(
            1234,
            title = "First Video",
            url = "https://v-cdn.zjol.com.cn/280123.mp4",
        ),
        Item(
            1235,
            title = "Second Video",
            url = "https://media.w3.org/2010/05/sintel/trailer.mp4",
        ),
        Item(
            1236,
            title = "Third Video",
            url = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.webm",
        ),
        Item(
            1237,
            title = "Fourth",
            url = "http://techslides.com/demos/sample-videos/small.mp4",
        ),
    )

    data class Item(
        val vid: Long,
        val title: String,
        val url: String
    )
}