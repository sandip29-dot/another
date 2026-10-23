package com.block.app.ui.screens.settings

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.block.app.admin.BlockDeviceAdminReceiver
import com.block.app.data.rules.DetectionRule
import com.block.app.data.settings.EscapeTarget
import com.block.app.ui.components.BrutalistButton
import com.block.app.ui.components.BrutalistCard
import com.block.app.ui.theme.BlockColors

/**
 * Shield settings screen:
 * - Master switch & accessibility service binding
 * - Escape target selector (Phone Home vs App Home)
 * - Detection rules overview & Learn Mode entry
 * - Battery optimization and Android 13+ permissions
 */
@Composable
fun ShieldSettingsScreen(
    viewModel: ShieldSettingsViewModel = hiltViewModel(),
    onNavigateToLearnMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val isShieldEnabled by viewModel.isShieldEnabled.collectAsStateWithLifecycle()
    val isShieldActive by viewModel.isShieldActive.collectAsStateWithLifecycle()
    val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
    val escapeTarget by viewModel.escapeTarget.collectAsStateWithLifecycle()
    val rules by viewModel.rules.collectAsStateWithLifecycle()

    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
    val adminComponent = ComponentName(context, BlockDeviceAdminReceiver::class.java)
    val isAdminActive = dpm?.isAdminActive(adminComponent) == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlockColors.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .semantics { contentDescription = "Shield Settings screen" }
    ) {
        Text(
            text = "SHIELD",
            color = BlockColors.OffWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Master Status Card ──────────────────────────────────
        BrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            shadowColor = if (isShieldActive) BlockColors.ElectricLime else BlockColors.DangerRed
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isShieldActive) "SHIELD IS UP" else "SHIELD IS DOWN",
                            color = if (isShieldActive) BlockColors.ElectricLime else BlockColors.DangerRed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isShieldActive) "Intercepting short feeds actively" else "Protection is inactive",
                            color = BlockColors.TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = isShieldEnabled,
                        onCheckedChange = { viewModel.toggleShield(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BlockColors.Black,
                            checkedTrackColor = BlockColors.ElectricLime,
                            uncheckedThumbColor = BlockColors.OffWhite,
                            uncheckedTrackColor = BlockColors.DarkGray
                        )
                    )
                }

                if (!isServiceRunning) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BlockColors.DangerRed, RectangleShape)
                            .background(BlockColors.DangerRed.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = BlockColors.DangerRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Accessibility Service is OFF",
                                    color = BlockColors.DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            BrutalistButton(
                                text = "ENABLE SERVICE",
                                onClick = { viewModel.openAccessibilitySettings(context) },
                                backgroundColor = BlockColors.DangerRed,
                                textColor = BlockColors.OffWhite,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Escape Target ───────────────────────────────────────
        BrutalistCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ESCAPE TARGET",
                    color = BlockColors.OffWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Where BlOCK throws you when a short feed triggers",
                    color = BlockColors.TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EscapeTargetOption(
                        title = "PHONE HOME",
                        subtitle = "Global Home",
                        selected = escapeTarget == EscapeTarget.HOME_SCREEN,
                        onClick = { viewModel.setEscapeTarget(EscapeTarget.HOME_SCREEN) },
                        modifier = Modifier.weight(1f)
                    )
                    EscapeTargetOption(
                        title = "APP HOME",
                        subtitle = "Feed Tab",
                        selected = escapeTarget == EscapeTarget.APP_HOME,
                        onClick = { viewModel.setEscapeTarget(EscapeTarget.APP_HOME) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Detection Rules ─────────────────────────────────────
        BrutalistCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DETECTION RULES",
                        color = BlockColors.OffWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "${rules.count { it.enabled }} ACTIVE",
                        color = BlockColors.ElectricLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                rules.forEach { rule ->
                    RuleRow(
                        rule = rule,
                        onToggle = { enabled -> viewModel.toggleRule(rule.id, enabled) },
                        onDelete = { viewModel.deleteRule(rule.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                BrutalistButton(
                    text = "LEARN MODE (ADD NEW APP)",
                    onClick = onNavigateToLearnMode,
                    backgroundColor = BlockColors.SurfaceElevated,
                    textColor = BlockColors.ElectricLime,
                    borderColor = BlockColors.ElectricLime,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Battery Exemption ───────────────────────────────────
        BrutalistCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "BACKGROUND SURVIVAL",
                    color = BlockColors.OffWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Aggressive OEM battery killers can silently terminate the accessibility service. Exempt BlOCK to keep protection solid.",
                    color = BlockColors.TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                BrutalistButton(
                    text = "REQUEST BATTERY EXEMPTION",
                    onClick = { viewModel.requestBatteryExemption(context) },
                    backgroundColor = BlockColors.OffWhite,
                    textColor = BlockColors.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Android 13+ Sideload Restricted Settings ────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BrutalistCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ANDROID 13+ SIDELOAD NOTICE",
                        color = BlockColors.StreakGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "If accessibility is greyed out as \"Restricted setting\": go to App Info → tap the top right 3-dot menu → tap \"Allow restricted settings\".",
                        color = BlockColors.TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BrutalistButton(
                        text = "OPEN APP INFO",
                        onClick = { viewModel.openAppDetailsSettings(context) },
                        backgroundColor = BlockColors.SurfaceElevated,
                        textColor = BlockColors.OffWhite,
                        borderColor = BlockColors.OffWhite,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Device Admin Uninstall Protection ────────────────────
        BrutalistCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UNINSTALL FRICTION",
                            color = BlockColors.OffWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isAdminActive) "Device Admin active (uninstall blocked)" else "Optional Device Admin makes impulsive deletion harder",
                            color = BlockColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isAdminActive,
                        onCheckedChange = { enable ->
                            if (enable) {
                                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                    putExtra(
                                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                        "BlOCK uses Device Admin to prevent impulsive uninstallation during commitment periods."
                                    )
                                }
                                context.startActivity(intent)
                            } else {
                                dpm?.removeActiveAdmin(adminComponent)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BlockColors.Black,
                            checkedTrackColor = BlockColors.ElectricLime
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EscapeTargetOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                color = if (selected) BlockColors.ElectricLime else BlockColors.DarkGray,
                shape = RectangleShape
            )
            .background(
                color = if (selected) BlockColors.ElectricLime.copy(alpha = 0.15f) else BlockColors.SurfaceElevated,
                shape = RectangleShape
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
            .semantics { contentDescription = "$title option${if (selected) ", selected" else ""}" }
    ) {
        Column {
            Text(
                text = title,
                color = if (selected) BlockColors.ElectricLime else BlockColors.OffWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = subtitle,
                color = BlockColors.TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun RuleRow(
    rule: DetectionRule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BlockColors.DarkGray, RectangleShape)
            .background(BlockColors.SurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics {
                contentDescription = "${rule.appName} detection rule, ${if (rule.enabled) "enabled" else "disabled"}"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.appName.uppercase(),
                color = BlockColors.OffWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "${rule.signals.size} signals (${rule.minSignals} required)",
                color = BlockColors.TextMuted,
                fontSize = 11.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = rule.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BlockColors.Black,
                    checkedTrackColor = BlockColors.ElectricLime,
                    uncheckedThumbColor = BlockColors.OffWhite,
                    uncheckedTrackColor = BlockColors.DarkGray
                )
            )

            // Only allow deleting custom rules
            if (!rule.id.startsWith("instagram") && !rule.id.startsWith("youtube")) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete ${rule.appName} rule",
                        tint = BlockColors.TextMuted
                    )
                }
            }
        }
    }
}
