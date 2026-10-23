package com.block.app.ui.screens.lock

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.block.app.ui.components.BrutalistButton
import com.block.app.ui.components.BrutalistCard
import com.block.app.ui.theme.BlockColors

/**
 * Commitment Lock screen — Voluntary friction to stop impulsive toggling.
 */
@Composable
fun LockSetupScreen(
    viewModel: LockSetupViewModel = hiltViewModel()
) {
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val remainingLockTime by viewModel.remainingLockTime.collectAsStateWithLifecycle()
    val lockNote by viewModel.lockNote.collectAsStateWithLifecycle()
    val selectedDays by viewModel.selectedDays.collectAsStateWithLifecycle()
    val noteInput by viewModel.noteInput.collectAsStateWithLifecycle()
    val isChallengeOpen by viewModel.isChallengeModalOpen.collectAsStateWithLifecycle()
    val waitRemainingSeconds by viewModel.disableWaitRemainingSeconds.collectAsStateWithLifecycle()
    val typedSentence by viewModel.typedSentence.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlockColors.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .semantics { contentDescription = "Commitment Lock screen" }
    ) {
        Text(
            text = "COMMITMENT LOCK",
            color = BlockColors.OffWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLocked) {
            // ── Already Locked View ─────────────────────────────────
            BrutalistCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = BlockColors.ElectricLime,
                shadowColor = BlockColors.ElectricLime
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "🔒 LOCKED IN",
                        color = BlockColors.ElectricLime,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "REMAINING TIME:",
                        color = BlockColors.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = remainingLockTime,
                        color = BlockColors.OffWhite,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "NOTE FROM PAST YOU:",
                        color = BlockColors.ElectricLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlockColors.SurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "\"$lockNote\"",
                            color = BlockColors.OffWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    BrutalistButton(
                        text = "I WANT TO DISABLE...",
                        onClick = viewModel::startChallenge,
                        backgroundColor = BlockColors.SurfaceElevated,
                        textColor = BlockColors.TextMuted,
                        borderColor = BlockColors.DarkGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // ── Unlocked: Setup New Lock ────────────────────────────
            BrutalistCard(
                modifier = Modifier.fillMaxWidth(),
                shadowColor = BlockColors.ElectricLime
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PREVENT REGRET",
                        color = BlockColors.OffWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lock BlOCK on for a fixed period. Switching it off requires a 10-minute cooldown and typing a commitment sentence.",
                        color = BlockColors.TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Duration selector
                    Text(
                        text = "SELECT COMMITMENT PERIOD:",
                        color = BlockColors.ElectricLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1 to "1 DAY", 7 to "7 DAYS", 30 to "30 DAYS").forEach { (days, label) ->
                            val isSelected = selectedDays == days
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) BlockColors.ElectricLime else BlockColors.DarkGray,
                                        shape = RectangleShape
                                    )
                                    .background(
                                        color = if (isSelected) BlockColors.ElectricLime else BlockColors.SurfaceElevated,
                                        shape = RectangleShape
                                    )
                                    .clickable { viewModel.setSelectedDays(days) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) BlockColors.Black else BlockColors.OffWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Note to future self
                    Text(
                        text = "NOTE TO FUTURE YOU:",
                        color = BlockColors.ElectricLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "What would you tell yourself when you are tempted to disable this?",
                        color = BlockColors.TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = viewModel::setNoteInput,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "e.g. Remember why you started. Finish the paper. Don't throw away your focus.",
                                color = BlockColors.TextMuted,
                                fontSize = 13.sp
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
                        shape = RectangleShape,
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    BrutalistButton(
                        text = "LOCK IT IN (${selectedDays}d)",
                        onClick = viewModel::activateLock,
                        backgroundColor = BlockColors.ElectricLime,
                        textColor = BlockColors.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (isChallengeOpen) {
        DisableChallenge(
            lockNote = lockNote,
            waitRemainingSeconds = waitRemainingSeconds,
            requiredSentence = viewModel.disableSentence,
            typedSentence = typedSentence,
            onTypedSentenceChange = viewModel::setTypedSentence,
            onConfirm = viewModel::confirmDisable,
            onDismiss = viewModel::dismissChallenge
        )
    }
}
