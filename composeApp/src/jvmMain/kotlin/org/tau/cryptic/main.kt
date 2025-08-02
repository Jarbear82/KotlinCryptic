package org.tau.cryptic

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.tau.cryptic.data.DefaultAppContainer

fun main() = application {
    val appContainer = DefaultAppContainer()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Cryptic",
    ) {
        App(appContainer)
    }
}