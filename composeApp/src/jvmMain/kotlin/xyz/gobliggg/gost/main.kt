package xyz.gobliggg.gost

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "gost",
    ) {
        App()
    }
}