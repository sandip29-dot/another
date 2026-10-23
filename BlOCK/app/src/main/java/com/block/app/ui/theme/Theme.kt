package com.block.app.ui.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// ── Composition locals ──────────────────────────────────────

/**
 * Whether the user has reduce-motion / remove-animations enabled.
 * Check at theme level and thread through the tree.
 */
val LocalReduceMotion = compositionLocalOf { false }

// ── Material 3 color schemes ────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary = md_dark_primary,
    onPrimary = md_dark_onPrimary,
    primaryContainer = md_dark_primaryContainer,
    onPrimaryContainer = md_dark_onPrimaryContainer,
    secondary = md_dark_secondary,
    onSecondary = md_dark_onSecondary,
    secondaryContainer = md_dark_secondaryContainer,
    onSecondaryContainer = md_dark_onSecondaryContainer,
    background = md_dark_background,
    onBackground = md_dark_onBackground,
    surface = md_dark_surface,
    onSurface = md_dark_onSurface,
    surfaceVariant = md_dark_surfaceVariant,
    onSurfaceVariant = md_dark_onSurfaceVariant,
    error = md_dark_error,
    onError = md_dark_onError,
    outline = md_dark_outline,
)

private val LightColorScheme = lightColorScheme(
    primary = md_light_primary,
    onPrimary = md_light_onPrimary,
    primaryContainer = md_light_primaryContainer,
    onPrimaryContainer = md_light_onPrimaryContainer,
    secondary = md_light_secondary,
    onSecondary = md_light_onSecondary,
    secondaryContainer = md_light_secondaryContainer,
    onSecondaryContainer = md_light_onSecondaryContainer,
    background = md_light_background,
    onBackground = md_light_onBackground,
    surface = md_light_surface,
    onSurface = md_light_onSurface,
    surfaceVariant = md_light_surfaceVariant,
    onSurfaceVariant = md_light_onSurfaceVariant,
    error = md_light_error,
    onError = md_light_onError,
    outline = md_light_outline,
)

// ── Theme composable ────────────────────────────────────────

/**
 * BlOCK app theme. Dark-first neo-brutalist design.
 *
 * Provides:
 * - [MaterialTheme] with custom colors / typography / shapes
 * - [LocalBlockColors] for extended palette (lime, gold, crack, etc.)
 * - [LocalReduceMotion] for accessibility-safe animations
 */
@Composable
fun BlockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val blockColors = remember { BlockColorScheme() }

    // Detect system-level reduce-motion preference
    val context = LocalContext.current
    val reduceMotion = remember {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    CompositionLocalProvider(
        LocalBlockColors provides blockColors,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BlockTypography,
            shapes = BlockShapes,
            content = content,
        )
    }
}

/**
 * Convenience accessor for the extended color palette.
 */
object BlockColors {
    val current: BlockColorScheme
        @Composable
        get() = LocalBlockColors.current
}
