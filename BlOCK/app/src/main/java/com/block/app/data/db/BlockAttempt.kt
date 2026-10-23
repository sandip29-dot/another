package com.block.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity tracking every blocked short-video attempt.
 * Each row = one time the bouncer said "Nope."
 */
@Entity(tableName = "block_attempts")
data class BlockAttempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val appPackage: String,
    val appName: String,
    val ruleId: String,
    val dayOfYear: Int,   // 1-366, for daily aggregation
    val hourOfDay: Int,   // 0-23, for danger-hours chart
    val year: Int         // for cross-year queries
)
