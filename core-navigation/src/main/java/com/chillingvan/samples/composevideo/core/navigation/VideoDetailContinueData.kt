package com.chillingvan.samples.composevideo.core.navigation

/**
 * Created by Chilling on 2022/8/6.
 */
object VideoDetailContinueData {
    private var mContinueData: ContinueData? = null

    fun produce(continueData: ContinueData) {
        mContinueData = continueData
    }

    fun consume(): ContinueData? {
        val result = mContinueData
        mContinueData = null
        return result
    }

    data class ContinueData(val vid: Long)
}