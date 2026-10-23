package com.block.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.block.app.ui.theme.BlockColors

/**
 * Displays total time reclaimed from intercepted reels/shorts
 * (each reel saved ~35s) translated into tangible real-world equivalents.
 */
@Composable
fun TimeReclaimed(
    blockCount: Int,
    secondsPerReel: Int = 35,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (blockCount.toLong() * secondsPerReel).coerceAtLeast(0)
    val totalMinutes = totalSeconds / 60
    val totalHours = totalMinutes / 60

    val timeString = when {
        totalHours > 0 -> "${totalHours}h ${totalMinutes % 60}m"
        totalMinutes > 0 -> "${totalMinutes}m ${totalSeconds % 60}s"
        else -> "${totalSeconds}s"
    }

    // Tangible real-world conversion
    val equivalentText = when {
        totalMinutes >= 240 -> "${totalMinutes / 45} lectures watched"
        totalMinutes >= 60 -> "${totalMinutes / 20} chapters read"
        totalMinutes >= 15 -> "${totalMinutes / 10} walks around the block"
        totalMinutes >= 5 -> "${totalMinutes / 3} songs listened to"
        totalSeconds >= 60 -> "${totalSeconds / 15} push-ups completed"
        else -> "a breath of fresh air"
    }

    BrutalistCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Time reclaimed: $timeString. Equivalent to $equivalentText"
            },
        shadowColor = BlockColors.ElectricLime
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "TIME RECLAIMED",
                color = BlockColors.TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = timeString,
                    color = BlockColors.ElectricLime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "saved from the void",
                    color = BlockColors.OffWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "≈ $equivalentText",
                color = BlockColors.OffWhite.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
