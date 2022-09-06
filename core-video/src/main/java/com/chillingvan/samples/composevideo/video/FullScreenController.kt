package com.chillingvan.samples.composevideo.video

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Created by Chilling on 2022/8/11.
 */
@Composable
fun rememberFullScreenController(): IFullScreenController {
    val context = LocalView.current.context
    val systemUiController = rememberSystemUiController()
    return remember(context) { FullScreenController(context, systemUiController) }
}

interface IFullScreenController {
    fun toFull()
    fun toPortrait()
}

class FullScreenController(private val context: Context, private val mSystemUiController: SystemUiController) : IFullScreenController {
    override fun toFull() {
        // SystemUIController cannot keep hiding status bar. But we keep here and let's wait for later update since it's still alpha now.
        mSystemUiController.isSystemBarsVisible = false // Status & Navigation bars
        context.findWindow()?.let { window ->
            hideSysStatusBar(window)
        }
        (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun toPortrait() {
        mSystemUiController.isSystemBarsVisible = true // Status & Navigation bars
        context.findWindow()?.let { window ->
            showSysStatusBar(window)
        }
        (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun isSysStatusBarShowing(window: Window): Boolean {
        val flag = window.attributes.flags
        return flag and WindowManager.LayoutParams.FLAG_FULLSCREEN != WindowManager.LayoutParams.FLAG_FULLSCREEN
    }

    private fun showSysStatusBar(window: Window) {
        if (isSysStatusBarShowing(window)) {
            return
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    }

    private fun hideSysStatusBar(window: Window) {
        if (!isSysStatusBarShowing(window)) {
            return
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


    private tailrec fun Context.findWindow(): Window? =
        when (this) {
            is Activity -> window
            is ContextWrapper -> baseContext.findWindow()
            else -> null
        }
}