package com.block.app.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.block.app.data.rules.Signal
import com.block.app.ui.components.BrutalistButton
import com.block.app.ui.components.BrutalistCard
import com.block.app.ui.theme.BlockColors

/**
 * Learn Mode screen:
 * Captures screen node trees from any app and generates new rules.
 */
@Composable
fun LearnModeScreen(
    viewModel: LearnModeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val isCaptureActive by viewModel.isCaptureActive.collectAsStateWithLifecycle()
    val capturedNodes by viewModel.capturedNodes.collectAsStateWithLifecycle()
    val capturedPackage by viewModel.capturedPackage.collectAsStateWithLifecycle()
    val appName by viewModel.appName.collectAsStateWithLifecycle()
    val selectedSignals by viewModel.selectedSignals.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()

    val suggestedSignals = viewModel.generateSuggestedSignals(capturedNodes)

    LaunchedEffect(capturedNodes) {
        if (capturedNodes.isNotEmpty()) {
            viewModel.initializeSignals(suggestedSignals)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlockColors.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .semantics { contentDescription = "Learn Mode screen" }
    ) {
        Text(
            text = "LEARN MODE",
            color = BlockColors.OffWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Instructions ────────────────────────────────────────
        BrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            shadowColor = BlockColors.ElectricLime
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HOW TO TEACH THE BOUNCER",
                    color = BlockColors.ElectricLime,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Tap \"START CAPTURE\" below.\n" +
                            "2. Switch to the app and navigate to the short-video screen you want to block.\n" +
                            "3. Return to BlOCK — the screen's UI signature will be captured.\n" +
                            "4. Name the rule and tap \"SAVE RULE\".",
                    color = BlockColors.OffWhite,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isCaptureActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BlockColors.ElectricLime, RectangleShape)
                            .background(BlockColors.ElectricLime.copy(alpha = 0.2f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LISTENING... GO OPEN TARGET APP NOW",
                            color = BlockColors.ElectricLime,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    BrutalistButton(
                        text = "STOP CAPTURE",
                        onClick = viewModel::stopCapture,
                        backgroundColor = BlockColors.DangerRed,
                        textColor = BlockColors.OffWhite,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    BrutalistButton(
                        text = "START CAPTURE",
                        onClick = viewModel::startCapture,
                        backgroundColor = BlockColors.ElectricLime,
                        textColor = BlockColors.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Capture Results ─────────────────────────────────────
        if (capturedNodes.isNotEmpty()) {
            BrutalistCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = BlockColors.ElectricLime,
                shadowColor = BlockColors.ElectricLime
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CAPTURED SCREEN SIGNATURE",
                        color = BlockColors.OffWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Package: $capturedPackage (${capturedNodes.size} nodes scanned)",
                        color = BlockColors.ElectricLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "RULE NAME:",
                        color = BlockColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = appName,
                        onValueChange = viewModel::setAppName,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "e.g. Threads Reels or Reddit Videos",
                                color = BlockColors.TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlockColors.ElectricLime,
                            unfocusedBorderColor = BlockColors.DarkGray,
                            focusedTextColor = BlockColors.OffWhite,
                            unfocusedTextColor = BlockColors.OffWhite,
                            focusedContainerColor = BlockColors.Black,
                            unfocusedContainerColor = BlockColors.Black
                        ),
                        shape = RectangleShape
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "DETECTED SIGNALS (SELECT AT LEAST 2):",
                        color = BlockColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    suggestedSignals.forEach { signal ->
                        val isChecked = selectedSignals.contains(signal)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BlockColors.DarkGray, RectangleShape)
                                .background(BlockColors.SurfaceElevated)
                                .clickable { viewModel.toggleSignal(signal) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleSignal(signal) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BlockColors.ElectricLime,
                                    checkmarkColor = BlockColors.Black,
                                    uncheckedColor = BlockColors.TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = signal.type.name.uppercase(),
                                    color = BlockColors.ElectricLime,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = signal.pattern,
                                    color = BlockColors.OffWhite,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    BrutalistButton(
                        text = "SAVE RULE",
                        onClick = viewModel::saveRule,
                        backgroundColor = BlockColors.ElectricLime,
                        textColor = BlockColors.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (saveSuccess) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BlockColors.ElectricLime, RectangleShape)
                    .background(BlockColors.ElectricLime.copy(alpha = 0.2f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RULE SAVED! The bouncer is now guarding this app.",
                    color = BlockColors.ElectricLime,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
