package com.block.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Core palette ────────────────────────────────────────────
val Black = Color(0xFF000000)
val OffWhite = Color(0xFFF5F0E8)
val ElectricLime = Color(0xFFCCFF00)
val ElectricLimeDark = Color(0xFF99CC00)
val DangerRed = Color(0xFFFF3333)
val SurfaceDark = Color(0xFF1A1A1A)
val SurfaceDarkElevated = Color(0xFF2A2A2A)
val TextMuted = Color(0xFF888888)
val StreakGold = Color(0xFFFFD700)
val CrackOrange = Color(0xFFFF6600)

// Material 3 dark scheme overrides
val md_dark_primary = ElectricLime
val md_dark_onPrimary = Black
val md_dark_primaryContainer = ElectricLimeDark
val md_dark_onPrimaryContainer = Black
val md_dark_secondary = OffWhite
val md_dark_onSecondary = Black
val md_dark_secondaryContainer = SurfaceDarkElevated
val md_dark_onSecondaryContainer = OffWhite
val md_dark_background = Black
val md_dark_onBackground = OffWhite
val md_dark_surface = SurfaceDark
val md_dark_onSurface = OffWhite
val md_dark_surfaceVariant = SurfaceDarkElevated
val md_dark_onSurfaceVariant = TextMuted
val md_dark_error = DangerRed
val md_dark_onError = Black
val md_dark_outline = ElectricLime

// Material 3 light scheme overrides (secondary, but available)
val md_light_primary = Black
val md_light_onPrimary = ElectricLime
val md_light_primaryContainer = ElectricLime
val md_light_onPrimaryContainer = Black
val md_light_secondary = SurfaceDark
val md_light_onSecondary = OffWhite
val md_light_secondaryContainer = Color(0xFFE0DDD5)
val md_light_onSecondaryContainer = Black
val md_light_background = OffWhite
val md_light_onBackground = Black
val md_light_surface = OffWhite
val md_light_onSurface = Black
val md_light_surfaceVariant = Color(0xFFE0DDD5)
val md_light_onSurfaceVariant = TextMuted
val md_light_error = DangerRed
val md_light_onError = OffWhite
val md_light_outline = Black

/**
 * Extended color palette for neo-brutalist components
 * that need more than Material3's scheme provides.
 */
@Immutable
data class BlockColorScheme(
    val lime: Color = ElectricLime,
    val limeDark: Color = ElectricLimeDark,
    val offWhite: Color = OffWhite,
    val black: Color = Black,
    val dangerRed: Color = DangerRed,
    val surfaceDark: Color = SurfaceDark,
    val surfaceElevated: Color = SurfaceDarkElevated,
    val textMuted: Color = TextMuted,
    val streakGold: Color = StreakGold,
    val crackOrange: Color = CrackOrange,
)

val LocalBlockColors = staticCompositionLocalOf { BlockColorScheme() }
