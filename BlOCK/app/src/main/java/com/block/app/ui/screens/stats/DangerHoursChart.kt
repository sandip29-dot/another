package com.block.app.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.block.app.data.db.HourlyCount
import com.block.app.ui.theme.BlockColors

/**
 * Bar chart showing blocked attempts by hour of day (0-23).
 * Identifies "Danger Hours" when you are most prone to scrolling.
 */
@Composable
fun DangerHoursChart(
    hourlyCounts: List<HourlyCount>,
    modifier: Modifier = Modifier
) {
    val hourMap = hourlyCounts.associate { it.hourOfDay to it.count }
    val maxCount = hourlyCounts.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val peakHour = hourlyCounts.maxByOrNull { it.count }?.takeIf { it.count > 0 }?.hourOfDay

    val summary = if (peakHour != null) {
        "Peak danger hour: ${formatHour(peakHour)} with ${hourMap[peakHour]} blocks"
    } else {
        "No danger hours recorded yet"
    }

    Column(
        modifier = modifier.semantics { contentDescription = summary }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 4.dp)
        ) {
            val totalBars = 24
            val barSpacing = 2.dp.toPx()
            val totalSpacing = barSpacing * (totalBars - 1)
            val barWidth = ((size.width - totalSpacing) / totalBars).coerceAtLeast(1f)
            val canvasHeight = size.height

            for (hour in 0 until 24) {
                val count = hourMap[hour] ?: 0
                val barHeight = if (maxCount > 0 && count > 0) {
                    (count.toFloat() / maxCount.toFloat()) * (canvasHeight - 8.dp.toPx())
                } else {
                    2.dp.toPx() // baseline pip
                }

                val x = hour * (barWidth + barSpacing)
                val y = canvasHeight - barHeight

                val isPeak = peakHour != null && hour == peakHour && count > 0
                val barColor = when {
                    isPeak -> BlockColors.DangerRed
                    count > 0 -> BlockColors.ElectricLime
                    else -> BlockColors.SurfaceElevated
                }

                // Draw filled bar
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )

                // Draw outline for neo-brutalist feel
                if (count > 0) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }

        // Hour labels below chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(0, 6, 12, 18, 23).forEach { hour ->
                Text(
                    text = String.format("%02d:00", hour),
                    color = BlockColors.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    return String.format("%02d:00", hour)
}
