package com.block.app.data.rules

import android.app.Application
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for detection rules.
 *
 * On first run, copies assets/rules.json → internal storage.
 * After that, always reads/writes the internal-storage copy so user
 * edits (Learn Mode, manual toggles) persist.
 */
@Singleton
class RulesRepository @Inject constructor(
    private val application: Application
) {
    companion object {
        private const val TAG = "RulesRepository"
        private const val RULES_FILE_NAME = "rules.json"
    }

    private val internalFile: File
        get() = File(application.filesDir, RULES_FILE_NAME)

    private val _rules = MutableStateFlow<List<DetectionRule>>(emptyList())
    val rules: StateFlow<List<DetectionRule>> = _rules.asStateFlow()

    init {
        ensureInternalFile()
        loadRules()
    }

    // ── Public API ───────────────────────────────────────────

    /** Returns a snapshot of all rules. */
    fun getRules(): List<DetectionRule> = _rules.value

    /** Find all rules matching [pkg] (exact match or "*" catch-all). */
    fun getRulesForPackage(pkg: String): List<DetectionRule> =
        _rules.value.filter { it.appPackage == pkg || it.appPackage == "*" }

    /** Find a single rule by ID. */
    fun getRuleById(id: String): DetectionRule? =
        _rules.value.firstOrNull { it.id == id }

    /** Add a new rule and persist. */
    fun addRule(rule: DetectionRule) {
        val current = _rules.value.toMutableList()
        current.add(rule)
        _rules.value = current
        saveRules()
    }

    /** Toggle a rule's enabled state and persist. */
    fun toggleRule(ruleId: String, enabled: Boolean) {
        val rule = getRuleById(ruleId) ?: return
        updateRule(rule.copy(enabled = enabled))
    }

    /** Update an existing rule (matched by id) and persist. */
    fun updateRule(rule: DetectionRule) {
        val current = _rules.value.toMutableList()
        val index = current.indexOfFirst { it.id == rule.id }
        if (index >= 0) {
            current[index] = rule
            _rules.value = current
            saveRules()
        } else {
            Log.w(TAG, "updateRule: no rule found with id=${rule.id}")
        }
    }

    /** Delete a rule by id and persist. */
    fun deleteRule(id: String) {
        val current = _rules.value.toMutableList()
        val removed = current.removeAll { it.id == id }
        if (removed) {
            _rules.value = current
            saveRules()
        }
    }

    /** Persist current in-memory rules to internal storage. */
    fun saveRules() {
        try {
            val json = RulesParser.toJson(_rules.value)
            internalFile.writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save rules", e)
        }
    }

    // ── Private helpers ──────────────────────────────────────

    /** Copy asset to internal storage on first launch. */
    private fun ensureInternalFile() {
        if (!internalFile.exists()) {
            try {
                application.assets.open(RULES_FILE_NAME).use { input ->
                    internalFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Copied assets/$RULES_FILE_NAME → ${internalFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to copy asset rules file", e)
                // Write a minimal empty-rules file so we don't crash
                internalFile.writeText("""{"rules":[]}""")
            }
        }
    }

    /** Load rules from internal storage into memory. */
    private fun loadRules() {
        try {
            val json = internalFile.readText()
            _rules.value = RulesParser.parseRules(json)
            Log.d(TAG, "Loaded ${_rules.value.size} rules")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load rules", e)
            _rules.value = emptyList()
        }
    }
}
