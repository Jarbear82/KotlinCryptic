package org.tau.cryptic

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.tau.cryptic.components.NavDrawer

@Composable
@Preview
fun App() {
    val colors = if (Config.theme == Config.AppTheme.DARK) {
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
            NavDrawer()
        }
    }
}