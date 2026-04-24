/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 */
package xyz.gobliggg.gost

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import gost.composeapp.generated.resources.gostLogoPainter
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import xyz.gobliggg.gost.ui.WindowTitleState
import xyz.gobliggg.gost.ui.applyUnifiedTitleBarIfSupported

fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(1280.dp, 820.dp),
        position = WindowPosition(Alignment.Center),
    )

    val windowTitle by WindowTitleState.title.collectAsState()

    val exitApp = {
        xyz.gobliggg.gost.data.ProcessManager.stopAll()
        exitApplication()
    }

    Window(
        onCloseRequest = exitApp,
        title = "", // Removed text on title bar
        state = windowState,
        icon = gostLogoPainter(),
    ) {
        DisposableEffect(window) {
            applyUnifiedTitleBarIfSupported(window)
            onDispose { }
        }
        App()
    }
}
