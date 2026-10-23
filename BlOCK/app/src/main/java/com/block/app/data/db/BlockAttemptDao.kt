package com.block.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for block attempt CRUD and aggregation queries.
 * The bouncer's logbook.
 */
@Dao
interface BlockAttemptDao {

    // ── Inserts ──────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: BlockAttempt): Long

    // ── Counts ───────────────────────────────────────────────

    /** Count of blocks today (needs dayOfYear + year). */
    @Query(
        """
        SELECT COUNT(*) FROM block_attempts
        WHERE dayOfYear = :dayOfYear AND year = :year
        """
    )
    suspend fun getCountToday(dayOfYear: Int, year: Int): Int

    /** Flow version – updates the UI live. */
    @Query(
        """
        SELECT COUNT(*) FROM block_attempts
        WHERE dayOfYear = :dayOfYear AND year = :year
        """
    )
    fun observeCountToday(dayOfYear: Int, year: Int): Flow<Int>

    /** Count in a timestamp range (inclusive). */
    @Query(
        """
        SELECT COUNT(*) FROM block_attempts
        WHERE timestamp >= :startMs AND timestamp <= :endMs
        """
    )
    suspend fun getCountForRange(startMs: Long, endMs: Long): Int

    /** Total blocks ever. */
    @Query("SELECT COUNT(*) FROM block_attempts")
    suspend fun getTotalCount(): Int

    /** Flow version of total blocks ever. */
    @Query("SELECT COUNT(*) FROM block_attempts")
    fun observeTotalCount(): Flow<Int>

    // ── Aggregations ─────────────────────────────────────────

    /** Blocks grouped by hour of day (danger-hours chart). */
    @Query(
        """
        SELECT hourOfDay, COUNT(*) AS count
        FROM block_attempts
        GROUP BY hourOfDay
        ORDER BY hourOfDay ASC
        """
    )
    suspend fun getCountsByHour(): List<HourlyCount>

    /** Blocks grouped by app package (per-app stats). */
    @Query(
        """
        SELECT appPackage, appName, COUNT(*) AS count
        FROM block_attempts
        GROUP BY appPackage
        ORDER BY count DESC
        """
    )
    suspend fun getCountsByApp(): List<AppCount>

    /** Blocks grouped by day (heatmap calendar). */
    @Query(
        """
        SELECT dayOfYear, year, COUNT(*) AS count
        FROM block_attempts
        GROUP BY year, dayOfYear
        ORDER BY year DESC, dayOfYear DESC
        """
    )
    suspend fun getCountsByDay(): List<DailyCount>

    /** Daily counts for the last N days (for streak calculation).
     *  Returns rows only for days that have blocks. */
    @Query(
        """
        SELECT dayOfYear, year, COUNT(*) AS count
        FROM block_attempts
        WHERE timestamp >= :sinceMs
        GROUP BY year, dayOfYear
        ORDER BY year DESC, dayOfYear DESC
        """
    )
    suspend fun getDailyCountsSince(sinceMs: Long): List<DailyCount>

    // ── Cleanup ──────────────────────────────────────────────

    /** Delete all attempts (for debug / reset). */
    @Query("DELETE FROM block_attempts")
    suspend fun deleteAll()
}
