package com.chillingvan.samples.composevideo.video

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Created by Chilling on 2022/9/22.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class VideoHiltModule {
    @Binds
    abstract fun bindVideoSingleton(
        videoSingleton: VideoSingleton
    ): CoreVideoPlayer
}