package com.block.app.ui.screens.lock

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.block.app.ui.components.BrutalistButton
import com.block.app.ui.components.BrutalistCard
import com.block.app.ui.theme.BlockColors

/**
 * Friction challenge modal:
 * Stage 1: Past-self note display.
 * Stage 2: 10-minute wait countdown timer.
 * Stage 3: Type verbatim deterrent sentence.
 */
@Composable
fun DisableChallenge(
    lockNote: String,
    waitRemainingSeconds: Long,
    requiredSentence: String,
    typedSentence: String,
    onTypedSentenceChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val waitMinutes = waitRemainingSeconds / 60
    val waitSeconds = waitRemainingSeconds % 60
    val timerDone = waitRemainingSeconds <= 0L
    val sentenceMatches = typedSentence.trim() == requiredSentence.trim()
    val canConfirm = timerDone && sentenceMatches

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BlockColors.Black.copy(alpha = 0.95f))
                .padding(20.dp)
                .semantics { contentDescription = "Commitment lock disable challenge" },
            contentAlignment = Alignment.Center
        ) {
            BrutalistCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                borderColor = BlockColors.DangerRed,
                shadowColor = BlockColors.DangerRed
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "THE BOUNCER ASKS:",
                        color = BlockColors.DangerRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "\"ARE YOU SURE?\"",
                        color = BlockColors.OffWhite,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Note From Past Self ─────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BlockColors.ElectricLime, RectangleShape)
                            .background(BlockColors.SurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "NOTE FROM PAST YOU:",
                                color = BlockColors.ElectricLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\"$lockNote\"",
                                color = BlockColors.OffWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ── 10-Minute Countdown Timer ───────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, if (timerDone) BlockColors.ElectricLime else BlockColors.DarkGray, RectangleShape)
                            .background(BlockColors.Black)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (timerDone) "COOLDOWN COMPLETED" else "COOLING DOWN...",
                                color = if (timerDone) BlockColors.ElectricLime else BlockColors.TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%02d:%02d", waitMinutes, waitSeconds),
                                color = if (timerDone) BlockColors.ElectricLime else BlockColors.DangerRed,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (!timerDone) {
                                Text(
                                    text = "Take a breath. Walk away. You locked this for a reason.",
                                    color = BlockColors.TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ── Deterrent Sentence ──────────────────────────
                    Text(
                        text = "TYPE THIS SENTENCE TO UNLOCK:",
                        color = BlockColors.OffWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlockColors.SurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = requiredSentence,
                            color = BlockColors.TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = typedSentence,
                        onValueChange = onTypedSentenceChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = timerDone,
                        placeholder = {
                            Text(
                                text = if (timerDone) "Type the sentence here..." else "Wait for the cooldown timer...",
                                color = BlockColors.TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (sentenceMatches) BlockColors.ElectricLime else BlockColors.DangerRed,
                            unfocusedBorderColor = BlockColors.DarkGray,
                            focusedTextColor = BlockColors.OffWhite,
                            unfocusedTextColor = BlockColors.OffWhite,
                            focusedContainerColor = BlockColors.Black,
                            unfocusedContainerColor = BlockColors.Black
                        ),
                        shape = RectangleShape
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Actions ─────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BrutalistButton(
                            text = "TURN BACK",
                            onClick = onDismiss,
                            backgroundColor = BlockColors.ElectricLime,
                            textColor = BlockColors.Black,
                            modifier = Modifier.weight(1f)
                        )

                        BrutalistButton(
                            text = "DISABLE",
                            onClick = onConfirm,
                            enabled = canConfirm,
                            backgroundColor = if (canConfirm) BlockColors.DangerRed else BlockColors.DarkGray,
                            textColor = BlockColors.OffWhite,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
