package com.block.app.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared state holder for the BlockAccessibilityService.
 * Lives as a singleton so both the service and UI can observe status
 * without a direct dependency between them.
 */
object BlockServiceState {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _lastCapturedNodes = MutableStateFlow<List<LearnModeCapturedNode>>(emptyList())
    val lastCapturedNodes: StateFlow<List<LearnModeCapturedNode>> = _lastCapturedNodes.asStateFlow()

    private val _capturedPackage = MutableStateFlow("")
    val capturedPackage: StateFlow<String> = _capturedPackage.asStateFlow()

    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }

    fun setCapturedNodes(pkg: String, nodes: List<LearnModeCapturedNode>) {
        _capturedPackage.value = pkg
        _lastCapturedNodes.value = nodes
    }

    fun clearCapture() {
        _capturedPackage.value = ""
        _lastCapturedNodes.value = emptyList()
    }
}

/**
 * Represents a single node captured during Learn Mode.
 * Kept separate from accessibility internals to avoid circular dependencies.
 */
data class LearnModeCapturedNode(
    val viewId: String?,
    val className: String?,
    val contentDescription: String?,
    val text: String?,
    val depth: Int
)
