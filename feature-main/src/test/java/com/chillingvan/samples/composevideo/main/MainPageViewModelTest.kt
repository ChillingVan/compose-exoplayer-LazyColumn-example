package com.chillingvan.samples.composevideo.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chillingvan.samples.composevideo.main.model.VideoItemsRepository
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import com.google.samples.apps.nowinandroid.core.testing.video.VideoTestPlayer
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by Chilling on 2022/9/20.
 */
class MainPageViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()


    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainPageViewModel

    @Before
    fun setup() {
        viewModel = MainPageViewModel(
            VideoTestPlayer(),
            VideoItemsRepository()
        )
    }

    @Test
    fun stateGetData() {
        viewModel.getListLiveData().value?.let {
            assertNotNull(it.list)
            assert(it.list.isNotEmpty())
        }
    }
}