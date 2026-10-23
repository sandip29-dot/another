package com.block.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.block.app.ui.screens.dashboard.DashboardScreen
import com.block.app.ui.screens.learn.LearnModeScreen
import com.block.app.ui.screens.lock.LockSetupScreen
import com.block.app.ui.screens.settings.ShieldSettingsScreen
import com.block.app.ui.screens.stats.StatsScreen
import com.block.app.ui.theme.BlockColors

/**
 * Main application navigation graph with neo-brutalist bottom bar.
 */
@Composable
fun BlockNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.LearnMode.route) {
                NeoBrutalistBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { tab ->
                        navController.navigate(tab.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToShield = { navController.navigate(Screen.Shield.route) }
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen()
            }
            composable(Screen.Shield.route) {
                ShieldSettingsScreen(
                    onNavigateToLearnMode = { navController.navigate(Screen.LearnMode.route) }
                )
            }
            composable(Screen.Lock.route) {
                LockSetupScreen()
            }
            composable(Screen.LearnMode.route) {
                LearnModeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun NeoBrutalistBottomBar(
    currentRoute: String?,
    onTabSelected: (BottomTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(width = 2.dp, color = BlockColors.OffWhite, shape = RectangleShape)
            .background(BlockColors.Black)
            .semantics { contentDescription = "Navigation bar" },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomTab.entries.forEach { tab ->
            val isSelected = currentRoute == tab.screen.route
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .background(
                        if (isSelected) BlockColors.ElectricLime else BlockColors.Black,
                        RectangleShape
                    )
                    .clickable { onTabSelected(tab) }
                    .semantics {
                        contentDescription = "${tab.label} tab${if (isSelected) ", selected" else ""}"
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) BlockColors.Black else BlockColors.OffWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = tab.label.uppercase(),
                        color = if (isSelected) BlockColors.Black else BlockColors.OffWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
