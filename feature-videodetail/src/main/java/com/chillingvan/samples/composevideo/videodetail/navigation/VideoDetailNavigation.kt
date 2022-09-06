package com.chillingvan.samples.composevideo.videodetail.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chillingvan.samples.composevideo.videodetail.VideoDetailRoute
import com.chillingvan.samples.composevideo.core.navigation.INavigationDestination

/**
 * Created by Chilling on 2022/8/1.
 */
object VideoDetailDestination : INavigationDestination {
    override val route: String = "video_detail"
    override val destination: String = "video_detail_destination"

    const val vidArg = "vid"
    const val urlArg = "url"
    const val titleArg = "title"
}

fun NavGraphBuilder.videoDetailGraph(
    windowSizeClass: WindowSizeClass,
    onBackClick: () -> Unit
) {
    composable(
        route = "${VideoDetailDestination.route}/{${VideoDetailDestination.vidArg}}?" +
                "${VideoDetailDestination.urlArg}={${VideoDetailDestination.urlArg}}" +
                "&${VideoDetailDestination.titleArg}={${VideoDetailDestination.titleArg}}" ,
        arguments = listOf(
            navArgument(VideoDetailDestination.vidArg) {
                type = NavType.LongType
            },
            navArgument(VideoDetailDestination.urlArg) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { navBackStackEntry ->
        // navBackStackEntry can get the arguments through bundle
        VideoDetailRoute(windowSizeClass,
            onBackClick = onBackClick)
    }
}