package com.block.app.ui.screens.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.block.app.ui.theme.BlockColors
import com.block.app.ui.theme.LocalReduceMotion
import kotlin.random.Random

/**
 * Visualizes the Block Tower:
 * - Each blocked short video adds a solid block stacked brick-by-brick.
 * - Each streak day adds a glowing golden foundation floor line.
 * - If shield is inactive today, crack lines shatter the top block.
 * - Physics/spring drop-in animation when reduce-motion is disabled.
 */
@Composable
fun TowerCanvas(
    blockCount: Int,
    streakDays: Int,
    isShieldActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    val dropProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(blockCount, reduceMotion) {
        if (!reduceMotion) {
            dropProgress.snapTo(0f)
            dropProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        } else {
            dropProgress.snapTo(1f)
        }
    }

    val accessibilityDesc = "Tower height: $blockCount blocks. Streak: $streakDays days. Shield is ${if (isShieldActive) "active" else "down"}."

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .semantics { contentDescription = accessibilityDesc }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Background grid floor line
        val groundY = canvasHeight - 16.dp.toPx()
        drawLine(
            color = BlockColors.OffWhite,
            start = Offset(0f, groundY),
            end = Offset(canvasWidth, groundY),
            strokeWidth = 3.dp.toPx()
        )

        val visibleBlocks = blockCount.coerceIn(0, 48)
        if (visibleBlocks == 0) {
            // Draw empty foundation marker
            val baseWidth = 120.dp.toPx()
            val baseX = (canvasWidth - baseWidth) / 2
            drawRect(
                color = BlockColors.SurfaceElevated,
                topLeft = Offset(baseX, groundY - 6.dp.toPx()),
                size = Size(baseWidth, 6.dp.toPx())
            )
            return@Canvas
        }

        val blockHeight = 14.dp.toPx()
        val blockWidth = 90.dp.toPx()
        val centerX = canvasWidth / 2f

        // Pseudo-random deterministic jitter per block index
        for (i in 0 until visibleBlocks) {
            val random = Random(i * 31L + 7)
            val jitterX = (random.nextFloat() - 0.5f) * 12.dp.toPx()
            val blockLeft = centerX - (blockWidth / 2f) + jitterX

            // Drop animation for the topmost block
            val isTopBlock = (i == visibleBlocks - 1)
            val yOffset = if (isTopBlock && !reduceMotion) {
                (1f - dropProgress.value) * -80.dp.toPx()
            } else 0f

            val blockTop = groundY - ((i + 1) * (blockHeight + 2.dp.toPx())) + yOffset

            // Block color: alternate neo-brutalist palette
            val blockColor = when {
                i % 4 == 0 -> BlockColors.ElectricLime
                i % 4 == 1 -> BlockColors.OffWhite
                i % 4 == 2 -> BlockColors.ElectricLime.copy(alpha = 0.85f)
                else -> Color(0xFFD4E157)
            }

            // Draw block body
            drawRect(
                color = blockColor,
                topLeft = Offset(blockLeft, blockTop),
                size = Size(blockWidth, blockHeight)
            )

            // Draw heavy black brutalist outline
            drawRect(
                color = BlockColors.Black,
                topLeft = Offset(blockLeft, blockTop),
                size = Size(blockWidth, blockHeight),
                style = Stroke(width = 2.dp.toPx())
            )

            // Every 5th block or per streak day, draw a glowing gold reinforcement band
            if (i > 0 && i % 5 == 0 && (i / 5) <= streakDays) {
                drawLine(
                    color = BlockColors.StreakGold,
                    start = Offset(blockLeft - 8.dp.toPx(), blockTop + blockHeight / 2f),
                    end = Offset(blockLeft + blockWidth + 8.dp.toPx(), blockTop + blockHeight / 2f),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // If shield is DOWN today and this is the top block, draw crack lines
            if (isTopBlock && !isShieldActive) {
                val crackPath = Path().apply {
                    moveTo(blockLeft + 15.dp.toPx(), blockTop)
                    lineTo(blockLeft + 35.dp.toPx(), blockTop + blockHeight * 0.6f)
                    lineTo(blockLeft + 55.dp.toPx(), blockTop + blockHeight * 0.3f)
                    lineTo(blockLeft + 70.dp.toPx(), blockTop + blockHeight)
                }
                drawPath(
                    path = crackPath,
                    color = BlockColors.DangerRed,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }
    }
}
