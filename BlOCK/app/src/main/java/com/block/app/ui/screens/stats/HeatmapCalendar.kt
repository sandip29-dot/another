package com.block.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.block.app.data.db.DailyCount
import com.block.app.ui.theme.BlockColors
import java.util.Calendar

/**
 * GitHub-style contribution heatmap calendar for the last 90 days.
 * Displays weeks as columns and 7 days as rows.
 */
@Composable
fun HeatmapCalendar(
    dailyCounts: List<DailyCount>,
    modifier: Modifier = Modifier
) {
    val countMap = dailyCounts.associate { "${it.year}-${it.dayOfYear}" to it.count }
    val maxCount = dailyCounts.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    // Build the grid of last 91 days (13 weeks x 7 days)
    val today = Calendar.getInstance()
    val weeks = 13
    val totalDays = weeks * 7

    val dayCells = (totalDays - 1 downTo 0).map { offsetDays ->
        val cal = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_YEAR, -offsetDays)
        }
        val year = cal.get(Calendar.YEAR)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat
        val count = countMap["$year-$dayOfYear"] ?: 0
        Triple(dayOfWeek, "$year-$dayOfYear", count)
    }

    // Group into 13 columns of 7 days
    val weekColumns = dayCells.chunked(7)
    val daysOfWeekLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Day-of-week labels
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                daysOfWeekLabels.forEach { label ->
                    Box(
                        modifier = Modifier.size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = BlockColors.TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Week columns
            weekColumns.forEachIndexed { weekIndex, week ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { (_, dateKey, count) ->
                        val cellColor = when {
                            count == 0 -> BlockColors.SurfaceElevated
                            count < maxCount * 0.25 -> BlockColors.DimLime
                            count < maxCount * 0.6 -> BlockColors.ElectricLime.copy(alpha = 0.6f)
                            count < maxCount * 0.9 -> BlockColors.ElectricLime.copy(alpha = 0.85f)
                            else -> BlockColors.ElectricLime
                        }

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (count > 0) BlockColors.Black else BlockColors.DarkGray,
                                    shape = RectangleShape
                                )
                                .background(cellColor, RectangleShape)
                                .semantics {
                                    contentDescription = "$dateKey: $count blocked attempts"
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "LESS",
                color = BlockColors.TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            listOf(
                BlockColors.SurfaceElevated,
                BlockColors.DimLime,
                BlockColors.ElectricLime.copy(alpha = 0.6f),
                BlockColors.ElectricLime
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .border(1.dp, BlockColors.DarkGray, RectangleShape)
                        .background(color, RectangleShape)
                )
            }
            Text(
                text = "MORE",
                color = BlockColors.TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
