package com.block.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for all BlOCK persistence.
 * Version 1 — no migrations yet.
 */
@Database(
    entities = [BlockAttempt::class],
    version = 1,
    exportSchema = false
)
abstract class BlockDatabase : RoomDatabase() {

    abstract fun blockAttemptDao(): BlockAttemptDao

    companion object {
        const val DATABASE_NAME = "block_db"
    }
}
