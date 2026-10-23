package com.block.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the Commitment Lock — a voluntary, time-delayed "can't disable"
 * mechanism. Users lock themselves in for N days and must wait 10 minutes +
 * type a deterrent sentence to unlock early.
 */
@Singleton
class CommitmentLockManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // ── Keys ─────────────────────────────────────────────────

    private companion object {
        val KEY_LOCK_END_TIME = longPreferencesKey("lock_end_time")
        val KEY_LOCK_NOTE = stringPreferencesKey("lock_note")
        val KEY_DISABLE_CHALLENGE_START = longPreferencesKey("disable_challenge_start")

        /** How long the user must wait before they can type the deterrent. */
        const val CHALLENGE_WAIT_MS = 10L * 60L * 1_000L // 10 minutes

        /** The sentence the user must type verbatim to break the lock. */
        const val DISABLE_SENTENCE =
            "I am choosing to remove the guard I set up for myself, " +
                    "and I accept full responsibility for the time I am about to lose."
    }

    // ── Flows ────────────────────────────────────────────────

    /** Epoch-millis when the lock expires. 0 = no lock. */
    val lockEndTime: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_LOCK_END_TIME] ?: 0L
    }

    /** The "dear future me" note written when the lock was set. */
    val lockNote: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_LOCK_NOTE] ?: ""
    }

    /** When the 10-min disable challenge was started. 0 = not started. */
    val disableChallengeStartTime: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_DISABLE_CHALLENGE_START] ?: 0L
    }

    /** Whether the lock is currently active. */
    val isLocked: Flow<Boolean> = dataStore.data.map { prefs ->
        val endTime = prefs[KEY_LOCK_END_TIME] ?: 0L
        endTime > System.currentTimeMillis()
    }

    /** Human-readable remaining lock time. */
    val remainingLockTime: Flow<String> = dataStore.data.map { prefs ->
        val endTime = prefs[KEY_LOCK_END_TIME] ?: 0L
        val now = System.currentTimeMillis()
        if (endTime <= now) {
            "No lock"
        } else {
            formatDuration(endTime - now)
        }
    }

    // ── Actions ──────────────────────────────────────────────

    /**
     * Lock the shield for [durationDays] days with a [note] from past-self.
     */
    suspend fun activateLock(durationDays: Int, note: String) {
        val endTime = System.currentTimeMillis() +
                TimeUnit.DAYS.toMillis(durationDays.toLong())
        dataStore.edit { prefs ->
            prefs[KEY_LOCK_END_TIME] = endTime
            prefs[KEY_LOCK_NOTE] = note
            prefs[KEY_DISABLE_CHALLENGE_START] = 0L // reset any pending challenge
        }
    }

    /**
     * Start the 10-minute waiting period before the user can type
     * the disable sentence.
     */
    suspend fun startDisableChallenge() {
        dataStore.edit { prefs ->
            prefs[KEY_DISABLE_CHALLENGE_START] = System.currentTimeMillis()
        }
    }

    /**
     * Check whether 10 minutes have elapsed since the challenge started.
     * Returns false if the challenge was never started.
     */
    suspend fun canProceedWithDisable(): Boolean {
        var startTime = 0L
        dataStore.data.collect { prefs ->
            startTime = prefs[KEY_DISABLE_CHALLENGE_START] ?: 0L
            return@collect // collect one emission then stop
        }
        if (startTime == 0L) return false
        return (System.currentTimeMillis() - startTime) >= CHALLENGE_WAIT_MS
    }

    /**
     * Clear the lock entirely after the user has completed the challenge.
     */
    suspend fun confirmDisable() {
        dataStore.edit { prefs ->
            prefs[KEY_LOCK_END_TIME] = 0L
            prefs[KEY_LOCK_NOTE] = ""
            prefs[KEY_DISABLE_CHALLENGE_START] = 0L
        }
    }

    /**
     * Returns the sentence the user must type to break the lock.
     */
    fun getDisableSentence(): String = DISABLE_SENTENCE

    // ── Helpers ──────────────────────────────────────────────

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1_000
        val days = totalSeconds / 86_400
        val hours = (totalSeconds % 86_400) / 3_600
        val minutes = (totalSeconds % 3_600) / 60

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            append("${minutes}m")
        }.trim()
    }
}
