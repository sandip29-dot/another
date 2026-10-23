package com.block.app.ui.screens.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.block.app.data.rules.DetectionRule
import com.block.app.data.rules.RulesRepository
import com.block.app.data.rules.Signal
import com.block.app.data.rules.SignalType
import com.block.app.data.settings.SettingsDataStore
import com.block.app.engine.BlockServiceState
import com.block.app.engine.LearnModeCapturedNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Learn Mode:
 * Allows user to capture accessibility node trees from any app and generate custom rules.
 */
@HiltViewModel
class LearnModeViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val rulesRepository: RulesRepository
) : ViewModel() {

    val isCaptureActive: StateFlow<Boolean> = settingsDataStore.learnModeActive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val capturedNodes: StateFlow<List<LearnModeCapturedNode>> = BlockServiceState.lastCapturedNodes
    val capturedPackage: StateFlow<String> = BlockServiceState.capturedPackage

    private val _appName = MutableStateFlow("")
    val appName: StateFlow<String> = _appName.asStateFlow()

    private val _selectedSignals = MutableStateFlow<Set<Signal>>(emptySet())
    val selectedSignals: StateFlow<Set<Signal>> = _selectedSignals.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun setAppName(name: String) {
        _appName.value = name
    }

    fun startCapture() {
        viewModelScope.launch {
            BlockServiceState.clearCapture()
            settingsDataStore.setLearnModeActive(true)
            _saveSuccess.value = false
        }
    }

    fun stopCapture() {
        viewModelScope.launch {
            settingsDataStore.setLearnModeActive(false)
        }
    }

    fun toggleSignal(signal: Signal) {
        val current = _selectedSignals.value.toMutableSet()
        if (current.contains(signal)) {
            current.remove(signal)
        } else {
            current.add(signal)
        }
        _selectedSignals.value = current
    }

    /**
     * Extracts suggested signals from captured nodes.
     */
    fun generateSuggestedSignals(nodes: List<LearnModeCapturedNode>): List<Signal> {
        val signals = mutableListOf<Signal>()

        nodes.forEach { node ->
            node.viewId?.let { id ->
                if (id.isNotBlank()) {
                    val cleanId = id.substringAfter(":id/")
                    signals.add(Signal(SignalType.viewId, cleanId))
                }
            }
            node.className?.let { cls ->
                if (cls.contains("ViewPager2") || cls.contains("RecyclerView") || cls.contains("PlayerView") || cls.contains("SurfaceView")) {
                    signals.add(Signal(SignalType.className, cls))
                }
            }
            node.contentDescription?.let { desc ->
                if (desc.isNotBlank() && desc.length < 50) {
                    signals.add(Signal(SignalType.contentDescription, "(?i).*${desc.trim()}.*"))
                }
            }
        }
        return signals.distinctBy { "${it.type}_${it.pattern}" }
    }

    fun initializeSignals(suggested: List<Signal>) {
        if (_selectedSignals.value.isEmpty()) {
            _selectedSignals.value = suggested.take(4).toSet()
        }
    }

    fun saveRule() {
        viewModelScope.launch {
            val pkg = capturedPackage.value.ifBlank { "custom.app" }
            val name = _appName.value.ifBlank { pkg }
            val signals = _selectedSignals.value.toList().ifEmpty {
                listOf(Signal(SignalType.className, "androidx.viewpager2.widget.ViewPager2"))
            }

            val newRule = DetectionRule(
                id = "custom_${UUID.randomUUID().toString().take(8)}",
                appPackage = pkg,
                appName = name,
                enabled = true,
                signals = signals,
                minSignals = (signals.size.coerceAtMost(2)).coerceAtLeast(1)
            )

            rulesRepository.addRule(newRule)
            settingsDataStore.setLearnModeActive(false)
            _saveSuccess.value = true
        }
    }
}
