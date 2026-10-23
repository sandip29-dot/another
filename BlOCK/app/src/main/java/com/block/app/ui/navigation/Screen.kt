package com.block.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation destinations for the BlOCK app.
 * Sealed class keeps routes type-safe.
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Stats : Screen("stats")
    data object Shield : Screen("shield")
    data object Lock : Screen("lock")
    data object LearnMode : Screen("learn")
}

/**
 * Bottom nav tabs with icon + label.
 */
enum class BottomTab(
    val screen: Screen,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String,
) {
    DASHBOARD(Screen.Dashboard, Icons.Filled.Home, "Home", "Dashboard tab"),
    STATS(Screen.Stats, Icons.Filled.BarChart, "Stats", "Statistics tab"),
    SHIELD(Screen.Shield, Icons.Filled.Shield, "Shield", "Shield settings tab"),
    LOCK(Screen.Lock, Icons.Filled.Lock, "Lock", "Lock settings tab"),
}
