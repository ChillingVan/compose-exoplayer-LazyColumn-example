/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chillingvan.samples.composevideo.main.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.chillingvan.samples.composevideo.videodetail.navigation.VideoDetailDestination
import com.chillingvan.samples.composevideo.videodetail.navigation.videoDetailGraph
import com.chillingvan.samples.composevideo.core.navigation.VideoDetailNavParam

/**
 * Top-level navigation graph. Navigation is organized as explained at
 * https://d.android.com/jetpack/compose/nav-adaptive
 *
 * The navigation graph defined in this file defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@Composable
fun AppNavHost(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = MainPageDestination.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        mainPageGraph(
            windowSizeClass = windowSizeClass,
            navigateToDetail = { arg: VideoDetailNavParam ->
                navController.navigate("${VideoDetailDestination.route}/${arg.vid}?${VideoDetailDestination.urlArg}=${arg.url}&${VideoDetailDestination.titleArg}=${arg.title}")
            },
            nestedGraphs = {
                videoDetailGraph(onBackClick = { navController.popBackStack() }, windowSizeClass = windowSizeClass)
            }
        )
    }
}
