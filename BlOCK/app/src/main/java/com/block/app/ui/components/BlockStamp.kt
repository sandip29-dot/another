package com.block.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.block.app.ui.theme.LocalBlockColors
import com.block.app.ui.theme.LocalReduceMotion

/**
 * Random deadpan bouncer lines.
 * One is picked each time the stamp appears.
 */
private val bouncerLines = listOf(
    "Not tonight.",
    "Nope.",
    "Future you says no.",
    "Nice try.",
    "Door's closed.",
    "Hard pass.",
    "Denied.",
    "Walk away.",
    "You shall not scroll.",
    "Blocked. Next.",
)

/**
 * A "BLOCKED" stamp composable with neo-brutalist styling.
 *
 * Rotated ~-12°, thick border, chunky uppercase text.
 * Appears with a spring scale-in animation.
 * Optionally shows a random bouncer line underneath.
 *
 * @param color Stamp color (lime or danger red).
 * @param showBouncerLine Whether to show a witty subtitle.
 * @param visible Controls animated entry.
 */
@Composable
fun BlockStamp(
    modifier: Modifier = Modifier,
    color: Color = LocalBlockColors.current.dangerRed,
    showBouncerLine: Boolean = true,
    visible: Boolean = true,
) {
    val reduceMotion = LocalReduceMotion.current
    var appeared by remember { mutableStateOf(false) }
    val bouncerLine = remember { bouncerLines.random() }

    LaunchedEffect(visible) {
        appeared = visible
    }

    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = if (reduceMotion) {
            spring(stiffness = Spring.StiffnessHigh)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
        },
        label = "stamp_scale",
    )

    if (scale > 0.01f) {
        Box(
            modifier = modifier
                .scale(scale)
                .rotate(-12f)
                .border(width = 3.dp, color = color)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .semantics {
                    contentDescription = "Blocked stamp. $bouncerLine"
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BLOCKED",
                    color = color,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (showBouncerLine) {
                    Text(
                        text = bouncerLine,
                        color = color.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
