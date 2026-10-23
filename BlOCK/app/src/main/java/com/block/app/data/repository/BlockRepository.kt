package com.block.app.data.repository

import com.block.app.data.db.AppCount
import com.block.app.data.db.DailyCount
import com.block.app.data.db.HourlyCount
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for block attempt data.
 * The bouncer's logbook.
 */
interface BlockRepository {
    fun observeTotalCount(): Flow<Int>
    suspend fun getTotalCount(): Int
    suspend fun getDailyCountsSince(sinceMs: Long): List<DailyCount>
    suspend fun getCountsByDay(): List<DailyCount>
    suspend fun getCountsByHour(): List<HourlyCount>
    suspend fun getCountsByApp(): List<AppCount>
}
