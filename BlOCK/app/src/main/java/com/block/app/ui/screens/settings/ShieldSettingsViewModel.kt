package com.block.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.block.app.data.rules.DetectionRule
import com.block.app.data.rules.RulesRepository
import com.block.app.data.settings.EscapeTarget
import com.block.app.data.settings.SettingsDataStore
import com.block.app.engine.BlockAccessibilityService
import com.block.app.engine.BlockServiceState
import com.block.app.watchdog.ServiceMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Shield screen.
 * Configures escape target, rules list, battery exemptions, and accessibility service.
 */
@HiltViewModel
class ShieldSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val rulesRepository: RulesRepository
) : ViewModel() {

    val isShieldEnabled: StateFlow<Boolean> = settingsDataStore.shieldEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isServiceRunning: StateFlow<Boolean> = BlockServiceState.isRunning

    val isShieldActive: StateFlow<Boolean> = combine(
        settingsDataStore.shieldEnabled,
        BlockServiceState.isRunning
    ) { enabled, running ->
        enabled && running
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val escapeTarget: StateFlow<EscapeTarget> = settingsDataStore.escapeTarget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EscapeTarget.HOME_SCREEN)

    val rules: StateFlow<List<DetectionRule>> = rulesRepository.rules

    fun toggleShield(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setShieldEnabled(enabled)
        }
    }

    fun setEscapeTarget(target: EscapeTarget) {
        viewModelScope.launch {
            settingsDataStore.setEscapeTarget(target)
        }
    }

    fun toggleRule(ruleId: String, enabled: Boolean) {
        rulesRepository.toggleRule(ruleId, enabled)
    }

    fun deleteRule(ruleId: String) {
        rulesRepository.deleteRule(ruleId)
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return ServiceMonitor.isAccessibilityServiceEnabled(
            context,
            BlockAccessibilityService::class.java
        )
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun isBatteryOptimized(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == false
    }

    fun requestBatteryExemption(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    fun openAppDetailsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
