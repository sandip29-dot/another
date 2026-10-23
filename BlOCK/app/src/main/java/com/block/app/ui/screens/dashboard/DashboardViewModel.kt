package com.block.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.block.app.data.db.BlockAttemptDao
import com.block.app.data.settings.SettingsDataStore
import com.block.app.engine.BlockServiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the bouncer's headquarters (Dashboard).
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val blockAttemptDao: BlockAttemptDao,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val cal: Calendar = Calendar.getInstance()
    private val currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    private val currentYear = cal.get(Calendar.YEAR)

    val todayCount: StateFlow<Int> = blockAttemptDao
        .observeCountToday(currentDayOfYear, currentYear)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCount: StateFlow<Int> = blockAttemptDao
        .observeTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isShieldActive: StateFlow<Boolean> = combine(
        settingsDataStore.shieldEnabled,
        BlockServiceState.isRunning
    ) { enabled, running ->
        enabled && running
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _streakDays = MutableStateFlow(0)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    init {
        calculateStreak()
    }

    private fun calculateStreak() {
        viewModelScope.launch {
            // Get past 30 days of counts
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val dailyCounts = blockAttemptDao.getDailyCountsSince(thirtyDaysAgo)

            val dayMap = dailyCounts.associate { "${it.year}-${it.dayOfYear}" to it.count }
            var streak = 0
            val checkCal = Calendar.getInstance()

            // Count consecutive days going backwards
            for (i in 0 until 30) {
                val y = checkCal.get(Calendar.YEAR)
                val d = checkCal.get(Calendar.DAY_OF_YEAR)
                val count = dayMap["$y-$d"] ?: 0
                // If today has 0, streak isn't broken yet if yesterday had blocks
                if (i == 0 && count == 0) {
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    continue
                }
                if (count > 0) {
                    streak++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
            _streakDays.value = streak.coerceAtLeast(1)
        }
    }
}
