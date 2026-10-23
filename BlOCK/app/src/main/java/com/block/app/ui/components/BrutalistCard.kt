package com.block.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.block.app.ui.theme.BlockColors

/**
 * Neo-brutalist card: thick border, hard offset shadow, no rounding.
 * The visual brick in the wall.
 */
@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BlockColors.SurfaceDark,
    borderColor: Color = BlockColors.OffWhite,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    shadowColor: Color = BlockColors.ElectricLime,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                // Hard offset shadow — no blur, pure brutalism
                drawRect(
                    color = shadowColor,
                    topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                    size = Size(size.width, size.height)
                )
            }
            .border(width = borderWidth, color = borderColor, shape = RectangleShape)
            .background(color = backgroundColor, shape = RectangleShape)
            .padding(16.dp),
        content = content
    )
}
