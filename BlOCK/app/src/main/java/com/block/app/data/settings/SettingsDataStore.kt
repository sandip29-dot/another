package com.block.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Where the bouncer goes after kicking you out.
 */
enum class EscapeTarget {
    /** Press the home button. */
    HOME_SCREEN,
    /** Try to navigate to the app's own home/feed tab first. */
    APP_HOME
}

/**
 * DataStore-backed settings for the BlOCK app.
 * All preferences are reactive via [Flow].
 */
@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // ── Keys ─────────────────────────────────────────────────

    private companion object {
        val KEY_SHIELD_ENABLED = booleanPreferencesKey("shield_enabled")
        val KEY_ESCAPE_TARGET = stringPreferencesKey("escape_target")
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val KEY_LEARN_MODE_ACTIVE = booleanPreferencesKey("learn_mode_active")
    }

    // ── Flows ────────────────────────────────────────────────

    /** Whether the shield (blocking) is enabled by the user. */
    val shieldEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_SHIELD_ENABLED] ?: true // on by default
    }

    /** What happens after a block: go home or try app's home tab. */
    val escapeTarget: Flow<EscapeTarget> = dataStore.data.map { prefs ->
        val name = prefs[KEY_ESCAPE_TARGET] ?: EscapeTarget.HOME_SCREEN.name
        try {
            EscapeTarget.valueOf(name)
        } catch (_: IllegalArgumentException) {
            EscapeTarget.HOME_SCREEN
        }
    }

    /** Whether the user has completed onboarding. */
    val onboardingComplete: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETE] ?: false
    }

    /** Respect the user's reduce-motion preference. */
    val reduceMotion: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_REDUCE_MOTION] ?: false
    }

    /** Whether Learn Mode is currently capturing nodes. */
    val learnModeActive: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_LEARN_MODE_ACTIVE] ?: false
    }

    // ── Setters ──────────────────────────────────────────────

    suspend fun setShieldEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SHIELD_ENABLED] = enabled }
    }

    suspend fun setEscapeTarget(target: EscapeTarget) {
        dataStore.edit { it[KEY_ESCAPE_TARGET] = target.name }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setReduceMotion(reduce: Boolean) {
        dataStore.edit { it[KEY_REDUCE_MOTION] = reduce }
    }

    suspend fun setLearnModeActive(active: Boolean) {
        dataStore.edit { it[KEY_LEARN_MODE_ACTIVE] = active }
    }
}
