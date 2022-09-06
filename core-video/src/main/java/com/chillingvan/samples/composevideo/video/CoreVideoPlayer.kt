package com.chillingvan.samples.composevideo.video

import android.view.View
import com.google.android.exoplayer2.Player

/**
 * Created by Chilling on 2022/8/3.
 */
interface CoreVideoPlayer {
    fun bindVideoView(view: View?)
    fun getPlayer(): Player
    fun resume()
}