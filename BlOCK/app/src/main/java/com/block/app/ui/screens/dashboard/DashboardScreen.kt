package com.block.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.block.app.ui.components.BlockStamp
import com.block.app.ui.components.BrutalistCard
import com.block.app.ui.components.TimeReclaimed
import com.block.app.ui.theme.BlockColors

/**
 * Dashboard screen — The bouncer's headquarters.
 * Shows Tower, today's block count, streak, shield status, and time reclaimed.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToShield: () -> Unit = {}
) {
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakDays.collectAsStateWithLifecycle()
    val isShieldActive by viewModel.isShieldActive.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlockColors.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .semantics { contentDescription = "Dashboard screen" }
    ) {
        // ── Top Header ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BlOCK",
                    color = BlockColors.ElectricLime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "BRICK BY BRICK.",
                    color = BlockColors.OffWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            // Shield status badge
            Box(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = if (isShieldActive) BlockColors.ElectricLime else BlockColors.DangerRed,
                        shape = RectangleShape
                    )
                    .background(
                        color = if (isShieldActive) BlockColors.ElectricLime.copy(alpha = 0.15f) else BlockColors.DangerRed.copy(alpha = 0.15f),
                        shape = RectangleShape
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .semantics {
                        contentDescription = if (isShieldActive) "Shield is active" else "Shield is down"
                    }
            ) {
                Text(
                    text = if (isShieldActive) "SHIELD UP" else "SHIELD DOWN",
                    color = if (isShieldActive) BlockColors.ElectricLime else BlockColors.DangerRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── The Tower Canvas ────────────────────────────────────
        BrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            shadowColor = BlockColors.ElectricLime
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "THE TOWER",
                        color = BlockColors.OffWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "$totalCount BLOCKS TALL",
                        color = BlockColors.ElectricLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TowerCanvas(
                    blockCount = totalCount,
                    streakDays = streakDays,
                    isShieldActive = isShieldActive,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Today's Count & Streak Row ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's blocks
            BrutalistCard(
                modifier = Modifier.weight(1f),
                shadowColor = BlockColors.ElectricLime
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Today: $todayCount blocks" }
                ) {
                    Text(
                        text = "TODAY",
                        color = BlockColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todayCount.toString(),
                        color = BlockColors.ElectricLime,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "SHORTS BLOCKED",
                        color = BlockColors.OffWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Streak card
            BrutalistCard(
                modifier = Modifier.weight(1f),
                shadowColor = BlockColors.StreakGold
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Streak: $streakDays days" }
                ) {
                    Text(
                        text = "STREAK",
                        color = BlockColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${streakDays}d",
                        color = BlockColors.StreakGold,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "CLEAN SCROLLING",
                        color = BlockColors.OffWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Time Reclaimed ──────────────────────────────────────
        TimeReclaimed(
            blockCount = totalCount,
            secondsPerReel = 35,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Bouncer Stamp Banner ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            BlockStamp(
                color = BlockColors.ElectricLime,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
