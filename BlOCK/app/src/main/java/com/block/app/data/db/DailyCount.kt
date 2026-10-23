package com.block.app.data.db

/**
 * Query result: blocks per day (for heatmap calendar / streak).
 */
data class DailyCount(
    val dayOfYear: Int,
    val year: Int,
    val count: Int
)

/**
 * Query result: blocks per hour of day (for danger-hours chart).
 */
data class HourlyCount(
    val hourOfDay: Int,
    val count: Int
)

/**
 * Query result: blocks per app (per-app stats).
 */
data class AppCount(
    val appPackage: String,
    val appName: String,
    val count: Int
)
