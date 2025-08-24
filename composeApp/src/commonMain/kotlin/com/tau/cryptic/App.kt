package com.tau.cryptic

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.tau.cryptic.components.NavDrawer
import com.tau.cryptic.data.AppContainer
import com.tau.cryptic.data.DefaultAppContainer

@Composable
@Preview
fun App(appContainer: AppContainer = DefaultAppContainer()) {
    val theme by Config.themeState.collectAsState()
    val colors = if (theme == Config.AppTheme.DARK) {
        darkColorScheme(
            primary = Config.colors.primary,
            secondary = Config.colors.secondary,
            background = Config.colors.background,
            surface = Config.colors.surface,
            onPrimary = Config.colors.onPrimary,
            onBackground = Config.colors.onBackground,
            onSurface = Config.colors.onSurface
        )
    } else {
        lightColorScheme(
            primary = Config.colors.primary,
            secondary = Config.colors.secondary,
            background = Config.colors.background,
            surface = Config.colors.surface,
            onPrimary = Config.colors.onPrimary,
            onBackground = Config.colors.onBackground,
            onSurface = Config.colors.onSurface
        )
    }

    MaterialTheme(colorScheme = colors) {
        Surface {
            NavDrawer(appContainer)
        }
    }
}