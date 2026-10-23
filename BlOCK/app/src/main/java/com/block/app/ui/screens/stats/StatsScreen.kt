package com.block.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.block.app.ui.components.BrutalistCard
import com.block.app.ui.theme.BlockColors

/**
 * Stats tab — the bouncer's logbook visualized.
 * Shows heatmap, danger hours, and per-app breakdown.
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val totalBlocks by viewModel.totalBlocks.collectAsStateWithLifecycle()
    val dailyCounts by viewModel.dailyCounts.collectAsStateWithLifecycle()
    val hourlyCounts by viewModel.hourlyCounts.collectAsStateWithLifecycle()
    val appCounts by viewModel.appCounts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlockColors.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .semantics { contentDescription = "Statistics screen" }
    ) {
        // ── Title ──
        Text(
            text = "STATS",
            color = BlockColors.OffWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Period Selector ──
        PeriodSelector(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = viewModel::setPeriod
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Total Blocks Counter ──
        BrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            shadowColor = BlockColors.ElectricLime
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "$totalBlocks total blocks in ${selectedPeriod.label} period"
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = totalBlocks.toString(),
                    color = BlockColors.ElectricLime,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "BLOCKS",
                    color = BlockColors.TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Heatmap Calendar ──
        BrutalistCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ACTIVITY",
                    color = BlockColors.OffWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                HeatmapCalendar(
                    dailyCounts = dailyCounts,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Danger Hours Chart ──
        BrutalistCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DANGER HOURS",
                    color = BlockColors.OffWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                DangerHoursChart(
                    hourlyCounts = hourlyCounts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Per-App Breakdown ──
        BrutalistCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "BY APP",
                    color = BlockColors.OffWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (appCounts.isEmpty()) {
                    Text(
                        text = "No blocks yet. The bouncer's waiting.",
                        color = BlockColors.TextMuted,
                        fontSize = 14.sp
                    )
                } else {
                    appCounts.forEach { appCount ->
                        AppCountRow(appCount = appCount)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Segmented period selector — neo-brutalist chunky toggle buttons.
 */
@Composable
private fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Time period selector" },
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        StatsPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) BlockColors.ElectricLime else BlockColors.OffWhite,
                        shape = RectangleShape
                    )
                    .background(
                        color = if (isSelected) BlockColors.ElectricLime else BlockColors.SurfaceDark,
                        shape = RectangleShape
                    )
                    .semantics {
                        contentDescription = "${period.label} period${if (isSelected) ", selected" else ""}"
                    }
                    .clickable { onPeriodSelected(period) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.label,
                    color = if (isSelected) BlockColors.Black else BlockColors.OffWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                )
            }
        }
    }
}

/**
 * Single row in the per-app breakdown showing app name and block count.
 */
@Composable
private fun AppCountRow(
    appCount: com.block.app.data.db.AppCount
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BlockColors.DarkGray, shape = RectangleShape)
            .background(BlockColors.SurfaceElevated)
            .padding(12.dp)
            .semantics {
                contentDescription = "${appCount.appName}: ${appCount.count} blocks"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = appCount.appName.uppercase(),
            color = BlockColors.OffWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = appCount.count.toString(),
            color = BlockColors.ElectricLime,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
        )
    }
}
