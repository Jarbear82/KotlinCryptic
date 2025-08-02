package org.tau.cryptic

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A singleton object to hold the global application configuration state.
 *
 * This object provides a single source of truth for UI-related constants and
 * theme settings.
 */
object Config {

    //region Theme Configuration
    enum class AppTheme {
        LIGHT,
        DARK
    }

    private val _themeState = MutableStateFlow(AppTheme.LIGHT)
    val themeState = _themeState.asStateFlow()

    var theme: AppTheme
        get() = _themeState.value
        set(value) {
            _themeState.value = value
        }


    object Padding {
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
        val extraLarge = 32.dp
    }

    data class ColorPalette(
        val primary: Color,
        val secondary: Color,
        val background: Color,
        val surface: Color,
        val onPrimary: Color,
        val onBackground: Color,
        val onSurface: Color,
        val textPrimary: Color,
        val textSecondary: Color,
    )

    private val LightColorPalette = ColorPalette(
        primary = Color(0xffbf2424),
        secondary = Color(0xffff6464),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF0F0F0),
        onPrimary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        textPrimary = Color(0xFF212121),
        textSecondary = Color(0xFF757575)
    )

    private val DarkColorPalette = ColorPalette(
        primary = Color(0xffbf2424),
        secondary = Color(0xffff6464),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        textPrimary = Color(0xFFE0E0E0),
        textSecondary = Color(0xFFBDBDBD)
    )

    val colors: ColorPalette
        get() = when (theme) {
            AppTheme.LIGHT -> LightColorPalette
            AppTheme.DARK -> DarkColorPalette
        }
    //endregion
}