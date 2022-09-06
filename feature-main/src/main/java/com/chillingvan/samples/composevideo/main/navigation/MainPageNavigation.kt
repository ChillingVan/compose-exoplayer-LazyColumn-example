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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.chillingvan.samples.composevideo.core.navigation.INavigationDestination
import com.chillingvan.samples.composevideo.main.MainPageRoute
import com.chillingvan.samples.composevideo.core.navigation.VideoDetailNavParam

object MainPageDestination : INavigationDestination {
    override val route = "main_page_route"
    override val destination = "main_page_destination"
}

fun NavGraphBuilder.mainPageGraph(
    windowSizeClass: WindowSizeClass,
    navigateToDetail: (VideoDetailNavParam) -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = MainPageDestination.route,
        startDestination = MainPageDestination.destination
    ) {
        composable(route = MainPageDestination.destination) {
            MainPageRoute(windowSizeClass, navigateToDetail)
        }
        nestedGraphs()
    }
}
