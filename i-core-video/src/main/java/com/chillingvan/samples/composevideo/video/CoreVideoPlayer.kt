package com.chillingvan.samples.composevideo.video

import android.view.View

/**
 * Created by Chilling on 2022/8/3.
 */
interface CoreVideoPlayer {
    fun bindVideoView(view: View?)
    fun getPlayer(): Any
    fun play(playParam: PlayParam)
    fun resume()
    fun pause()
    fun release()
}

data class PlayParam(val vid: Long, val url: String)
