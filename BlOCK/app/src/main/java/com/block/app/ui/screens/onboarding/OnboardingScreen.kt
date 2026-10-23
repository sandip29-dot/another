package com.block.app.ui.screens.onboarding

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.block.app.ui.components.BlockStamp
import com.block.app.ui.components.BrutalistButton
import com.block.app.ui.components.BrutalistCard
import com.block.app.ui.theme.BlockColors

/**
 * Animated onboarding sequence:
 * Explains purpose, architecture, privacy guarantees, and permissions.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val totalPages = viewModel.totalPages

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlockColors.Black)
            .padding(24.dp)
            .semantics { contentDescription = "Onboarding screen, page ${currentPage + 1} of $totalPages" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top Indicator Dots ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 0 until totalPages) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (i == currentPage) 24.dp else 10.dp, height = 10.dp)
                        .border(1.dp, BlockColors.Black, RectangleShape)
                        .background(
                            if (i == currentPage) BlockColors.ElectricLime else BlockColors.DarkGray,
                            RectangleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Main Page Content ───────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_page"
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (page) {
                        0 -> PageWelcome()
                        1 -> PageHowItWorks()
                        2 -> PagePrivacy()
                        3 -> PageAccessibility(onEnable = { viewModel.openAccessibilitySettings(context) })
                        4 -> PageBattery(onExempt = { viewModel.requestBatteryExemption(context) })
                        5 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            PageRestrictedSettings(onOpenDetails = { viewModel.openAppDetailsSettings(context) })
                        } else {
                            PageDone()
                        }
                        6 -> PageDone()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Bottom Action Button ────────────────────────────────
        val isLastPage = currentPage == totalPages - 1
        BrutalistButton(
            text = if (isLastPage) "LET'S GO" else "NEXT",
            onClick = {
                if (isLastPage) {
                    viewModel.completeOnboarding()
                    onComplete()
                } else {
                    viewModel.nextPage()
                }
            },
            backgroundColor = BlockColors.ElectricLime,
            textColor = BlockColors.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun PageWelcome() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BlockStamp(
            color = BlockColors.ElectricLime,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "HEY.",
            color = BlockColors.OffWhite,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "BlOCK stops you from doom-scrolling short videos.\n\nEvery. Single. Time.",
            color = BlockColors.OffWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun PageHowItWorks() {
    BrutalistCard(
        modifier = Modifier.fillMaxWidth(),
        shadowColor = BlockColors.ElectricLime
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "HOW IT WORKS",
                color = BlockColors.ElectricLime,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "When an Instagram Reel, YouTube Short, or TikTok player appears on your screen:\n\n" +
                        "1. BlOCK intercepts the window within milliseconds.\n\n" +
                        "2. Calls Home immediately so you never see a second reel.\n\n" +
                        "3. Slams a BLOCKED stamp with haptic thud & sound.\n\n" +
                        "4. Adds a solid brick to your permanent tower.",
                color = BlockColors.OffWhite,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun PagePrivacy() {
    BrutalistCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = BlockColors.ElectricLime,
        shadowColor = BlockColors.ElectricLime
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "PRIVACY PROMISE",
                color = BlockColors.ElectricLime,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Offline only. Permanent.\n\n" +
                        "• NO internet permission declared in AndroidManifest\n" +
                        "• NO analytics or tracking SDKs\n" +
                        "• NO accounts, logins, or cloud sync\n\n" +
                        "Every stat and rule stays 100% on your local phone storage.",
                color = BlockColors.OffWhite,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun PageAccessibility(onEnable: () -> Unit) {
    BrutalistCard(
        modifier = Modifier.fillMaxWidth(),
        shadowColor = BlockColors.ElectricLime
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ONE THING I NEED",
                color = BlockColors.ElectricLime,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "BlOCK uses Android's Accessibility Service to detect short-video signatures.\n\n" +
                        "It strictly inspects view IDs and class names. It NEVER reads your private chats, passwords, or personal feeds.",
                color = BlockColors.OffWhite,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            BrutalistButton(
                text = "ENABLE ACCESSIBILITY",
                onClick = onEnable,
                backgroundColor = BlockColors.ElectricLime,
                textColor = BlockColors.Black,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PageBattery(onExempt: () -> Unit) {
    BrutalistCard(
        modifier = Modifier.fillMaxWidth(),
        shadowColor = BlockColors.ElectricLime
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "KEEP ME ALIVE",
                color = BlockColors.ElectricLime,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Phone manufacturers aggressively kill background services.\n\n" +
                        "Exempt BlOCK from battery optimization so the guard and watchdog stay vigilant 24/7.",
                color = BlockColors.OffWhite,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            BrutalistButton(
                text = "EXEMPT FROM BATTERY KILLER",
                onClick = onExempt,
                backgroundColor = BlockColors.OffWhite,
                textColor = BlockColors.Black,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PageRestrictedSettings(onOpenDetails: () -> Unit) {
    BrutalistCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = BlockColors.StreakGold,
        shadowColor = BlockColors.StreakGold
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ANDROID 13+ SIDELOAD NOTE",
                color = BlockColors.StreakGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Because BlOCK is sideloaded directly via APK:\n\n" +
                        "If Android shows \"Restricted setting\":\n" +
                        "1. Open App Info\n" +
                        "2. Tap the 3-dot menu at top-right\n" +
                        "3. Select \"Allow restricted settings\"",
                color = BlockColors.OffWhite,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            BrutalistButton(
                text = "OPEN APP INFO",
                onClick = onOpenDetails,
                backgroundColor = BlockColors.SurfaceElevated,
                textColor = BlockColors.OffWhite,
                borderColor = BlockColors.OffWhite,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PageDone() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "BRICK BY BRICK.",
            color = BlockColors.ElectricLime,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You're all set.\n\nEvery reel you don't scroll builds your tower.\n\nFuture you says thank you.",
            color = BlockColors.OffWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp
        )
    }
}
