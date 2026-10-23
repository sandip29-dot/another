package com.block.app.ui.screens.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.block.app.data.settings.CommitmentLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Commitment Lock:
 * 1/7/30 day commitment locks, 10-minute wait challenge, typing deterrence.
 */
@HiltViewModel
class LockSetupViewModel @Inject constructor(
    private val lockManager: CommitmentLockManager
) : ViewModel() {

    val isLocked: StateFlow<Boolean> = lockManager.isLocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lockEndTime: StateFlow<Long> = lockManager.lockEndTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val remainingLockTime: StateFlow<String> = lockManager.remainingLockTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "No lock")

    val lockNote: StateFlow<String> = lockManager.lockNote
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val disableChallengeStartTime: StateFlow<Long> = lockManager.disableChallengeStartTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _selectedDays = MutableStateFlow(7)
    val selectedDays: StateFlow<Int> = _selectedDays.asStateFlow()

    private val _noteInput = MutableStateFlow("")
    val noteInput: StateFlow<String> = _noteInput.asStateFlow()

    private val _typedSentence = MutableStateFlow("")
    val typedSentence: StateFlow<String> = _typedSentence.asStateFlow()

    private val _isChallengeModalOpen = MutableStateFlow(false)
    val isChallengeModalOpen: StateFlow<Boolean> = _isChallengeModalOpen.asStateFlow()

    val disableSentence: String = lockManager.getDisableSentence()

    // 1-second ticker for counting down the 10-minute (600s) wait challenge
    val disableWaitRemainingSeconds: StateFlow<Long> = flow {
        while (true) {
            val startTime = disableChallengeStartTime.value
            if (startTime > 0L) {
                val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                val remaining = (600L - elapsedSeconds).coerceAtLeast(0L)
                emit(remaining)
            } else {
                emit(600L)
            }
            delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 600L)

    fun setSelectedDays(days: Int) {
        _selectedDays.value = days
    }

    fun setNoteInput(note: String) {
        _noteInput.value = note
    }

    fun setTypedSentence(sentence: String) {
        _typedSentence.value = sentence
    }

    fun activateLock() {
        viewModelScope.launch {
            val note = _noteInput.value.ifBlank { "You locked this to protect your future self. Stay strong." }
            lockManager.activateLock(_selectedDays.value, note)
            _noteInput.value = ""
        }
    }

    fun startChallenge() {
        viewModelScope.launch {
            lockManager.startDisableChallenge()
            _typedSentence.value = ""
            _isChallengeModalOpen.value = true
        }
    }

    fun dismissChallenge() {
        _isChallengeModalOpen.value = false
        _typedSentence.value = ""
    }

    fun confirmDisable() {
        viewModelScope.launch {
            if (_typedSentence.value.trim() == disableSentence.trim() && disableWaitRemainingSeconds.value == 0L) {
                lockManager.confirmDisable()
                _isChallengeModalOpen.value = false
                _typedSentence.value = ""
            }
        }
    }
}
