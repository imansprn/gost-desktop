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
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import gost.composeapp.generated.resources.gostLogoPainter
import xyz.gobliggg.gost.ui.applyUnifiedTitleBarIfSupported

fun main() =
    application {
        val isRuntimeValid by xyz.gobliggg.gost.data.AppState.isRuntimeValid
            .collectAsState()
        val windowState =
            rememberWindowState(
                size = if (isRuntimeValid) DpSize(1280.dp, 820.dp) else DpSize(520.dp, 640.dp),
                position = WindowPosition(Alignment.Center),
            )

        androidx.compose.runtime.LaunchedEffect(isRuntimeValid) {
            windowState.size = if (isRuntimeValid) DpSize(1280.dp, 820.dp) else DpSize(520.dp, 640.dp)
            windowState.position = WindowPosition(Alignment.Center)
        }

        val exitApp = {
            xyz.gobliggg.gost.data.ProcessManager.default()
                .stopAll()
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
