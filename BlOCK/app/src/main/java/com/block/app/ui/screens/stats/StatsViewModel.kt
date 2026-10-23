package com.block.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.block.app.data.db.AppCount
import com.block.app.data.db.DailyCount
import com.block.app.data.db.HourlyCount
import com.block.app.data.repository.BlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the Stats screen.
 * Exposes block statistics filtered by the selected time period.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val blockRepository: BlockRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.WEEK)
    val selectedPeriod: StateFlow<StatsPeriod> = _selectedPeriod.asStateFlow()

    /** Daily block counts for heatmap — always last 90 days. */
    val dailyCounts: StateFlow<List<DailyCount>> = flow {
        val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
        emit(blockRepository.getDailyCountsSince(ninetyDaysAgo))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    /** Hourly block counts for the danger-hours chart. */
    val hourlyCounts: StateFlow<List<HourlyCount>> = _selectedPeriod.flatMapLatest {
        flow { emit(blockRepository.getCountsByHour()) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    /** Per-app block breakdown, sorted descending. */
    val appCounts: StateFlow<List<AppCount>> = _selectedPeriod.flatMapLatest {
        flow { emit(blockRepository.getCountsByApp()) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    /** Total blocks based on selected period. */
    val totalBlocks: StateFlow<Int> = _selectedPeriod.flatMapLatest { period ->
        flow {
            val count = when (period) {
                StatsPeriod.WEEK -> {
                    val weekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                    blockRepository.getDailyCountsSince(weekAgo).sumOf { it.count }
                }
                StatsPeriod.MONTH -> {
                    val monthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                    blockRepository.getDailyCountsSince(monthAgo).sumOf { it.count }
                }
                StatsPeriod.ALL_TIME -> {
                    blockRepository.getTotalCount()
                }
            }
            emit(count)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )

    fun setPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
    }
}
