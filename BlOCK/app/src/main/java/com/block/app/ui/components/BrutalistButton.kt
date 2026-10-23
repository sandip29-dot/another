package com.block.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.block.app.ui.theme.BlockColors

/**
 * Neo-brutalist button: thick border, hard offset shadow, chunky text.
 * No rounded corners. No mercy.
 */
@Composable
fun BrutalistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = BlockColors.ElectricLime,
    textColor: Color = BlockColors.Black,
    borderColor: Color = BlockColors.Black,
    borderWidth: Dp = 3.dp,
    shadowOffset: Dp = 4.dp,
    shadowColor: Color = BlockColors.Black,
    fontSize: TextUnit = 16.sp,
    accessibilityLabel: String = text
) {
    Box(
        modifier = modifier
            .semantics {
                contentDescription = accessibilityLabel
                role = Role.Button
            }
            .drawBehind {
                if (enabled) {
                    drawRect(
                        color = shadowColor,
                        topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                        size = Size(size.width, size.height)
                    )
                }
            }
            .border(
                width = borderWidth,
                color = if (enabled) borderColor else BlockColors.TextMuted,
                shape = RectangleShape
            )
            .background(
                color = if (enabled) backgroundColor else BlockColors.DarkGray,
                shape = RectangleShape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else BlockColors.TextMuted,
            fontWeight = FontWeight.Black,
            fontSize = fontSize,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Secondary variant — dark background, lime border.
 */
@Composable
fun BrutalistSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: String = text
) {
    BrutalistButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        backgroundColor = BlockColors.SurfaceDark,
        textColor = BlockColors.OffWhite,
        borderColor = BlockColors.OffWhite,
        shadowColor = BlockColors.OffWhite,
        shadowOffset = 3.dp,
        accessibilityLabel = accessibilityLabel
    )
}

/**
 * Danger variant — red accent for destructive actions.
 */
@Composable
fun BrutalistDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: String = text
) {
    BrutalistButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        backgroundColor = BlockColors.DangerRed,
        textColor = BlockColors.Black,
        borderColor = BlockColors.Black,
        shadowColor = BlockColors.Black,
        accessibilityLabel = accessibilityLabel
    )
}
