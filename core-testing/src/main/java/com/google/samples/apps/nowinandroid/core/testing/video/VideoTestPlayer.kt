package com.google.samples.apps.nowinandroid.core.testing.video

import android.view.View
import com.chillingvan.samples.composevideo.video.CoreVideoPlayer
import com.chillingvan.samples.composevideo.video.PlayParam

/**
 * Created by Chilling on 2022/9/23.
 */
class VideoTestPlayer : CoreVideoPlayer {

    private var mTestPlayState: TestPlayState? = null

    override fun bindVideoView(view: View?) {
    }

    override fun getPlayer(): Any {
        return this
    }

    override fun play(playParam: PlayParam) {
        mTestPlayState = TestPlayState.Playing
    }

    override fun resume() {
        mTestPlayState = TestPlayState.Playing
    }

    override fun pause() {
        mTestPlayState = TestPlayState.Pausing
    }

    override fun release() {
        mTestPlayState = TestPlayState.Release
    }

    fun getPlayState() = mTestPlayState

    sealed class TestPlayState {
        object Playing : TestPlayState()
        object Pausing : TestPlayState()
        object Release : TestPlayState()
    }
}