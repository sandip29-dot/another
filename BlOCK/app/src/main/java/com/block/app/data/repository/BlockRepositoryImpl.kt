package com.block.app.data.repository

import com.block.app.data.db.AppCount
import com.block.app.data.db.BlockAttemptDao
import com.block.app.data.db.DailyCount
import com.block.app.data.db.HourlyCount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockRepositoryImpl @Inject constructor(
    private val blockAttemptDao: BlockAttemptDao
) : BlockRepository {

    override fun observeTotalCount(): Flow<Int> =
        blockAttemptDao.observeTotalCount()

    override suspend fun getTotalCount(): Int =
        blockAttemptDao.getTotalCount()

    override suspend fun getDailyCountsSince(sinceMs: Long): List<DailyCount> =
        blockAttemptDao.getDailyCountsSince(sinceMs)

    override suspend fun getCountsByDay(): List<DailyCount> =
        blockAttemptDao.getCountsByDay()

    override suspend fun getCountsByHour(): List<HourlyCount> =
        blockAttemptDao.getCountsByHour()

    override suspend fun getCountsByApp(): List<AppCount> =
        blockAttemptDao.getCountsByApp()
}
