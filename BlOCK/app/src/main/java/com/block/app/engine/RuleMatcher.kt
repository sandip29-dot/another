package com.block.app.engine

import com.block.app.data.rules.DetectionRule
import com.block.app.data.rules.SignalType

/**
 * Pure-logic rule matcher. No Android dependencies, no Hilt — easy to unit test.
 *
 * Called on every accessibility event, so it must be fast:
 * - Pre-compiled regex cache
 * - Early exits when possible
 * - No allocations in the hot path where avoidable
 */
class RuleMatcher {

    /**
     * Result of attempting to match an accessibility node against rules.
     */
    data class MatchResult(
        val matched: Boolean,
        val rule: DetectionRule? = null,
        val signalCount: Int = 0,
        val matchedSignals: List<String> = emptyList()
    )

    /** Cache compiled [Regex] objects keyed by pattern string. */
    private val regexCache = HashMap<String, Regex>(64)

    /**
     * Try to match a single accessibility node's properties against the
     * provided [rules].
     *
     * @param packageName   The foreground app's package name.
     * @param nodeViewId    The node's viewIdResourceName (nullable).
     * @param nodeClassName The node's className (nullable).
     * @param nodeContentDescription The node's contentDescription (nullable).
     * @param nodeText      The node's text (nullable).
     * @param rules         All currently enabled detection rules.
     * @return [MatchResult] — [MatchResult.matched] is true if any rule fired.
     */
    fun matchNode(
        packageName: String,
        nodeViewId: String?,
        nodeClassName: String?,
        nodeContentDescription: String?,
        nodeText: String?,
        rules: List<DetectionRule>
    ): MatchResult {
        for (rule in rules) {
            if (!rule.enabled) continue

            // ── Package gate ─────────────────────────────────
            if (rule.appPackage != "*" && rule.appPackage != packageName) continue

            // ── Browser URL gate (for wildcard / browser rules) ──
            if (rule.browserRule && rule.urlPatterns.isNotEmpty()) {
                val addressBarText = nodeContentDescription ?: nodeText ?: ""
                val urlMatched = rule.urlPatterns.any { pattern ->
                    getRegex(pattern).containsMatchIn(addressBarText)
                }
                if (!urlMatched) continue
            }

            // ── Signal matching ──────────────────────────────
            var signalCount = 0
            val matchedSignals = mutableListOf<String>()

            for (signal in rule.signals) {
                val target = when (signal.type) {
                    SignalType.viewId -> nodeViewId
                    SignalType.className -> nodeClassName
                    SignalType.contentDescription -> nodeContentDescription
                }
                if (target != null && getRegex(signal.pattern).containsMatchIn(target)) {
                    signalCount++
                    matchedSignals.add("${signal.type}:${signal.pattern}")
                }
                // Early exit: if we've already hit minSignals, no need to check more
                if (signalCount >= rule.minSignals) break
            }

            if (signalCount >= rule.minSignals) {
                return MatchResult(
                    matched = true,
                    rule = rule,
                    signalCount = signalCount,
                    matchedSignals = matchedSignals
                )
            }
        }

        return NO_MATCH
    }

    /** Clear the regex cache (e.g. when rules are reloaded). */
    fun clearCache() {
        regexCache.clear()
    }

    // ── Private helpers ──────────────────────────────────────

    private fun getRegex(pattern: String): Regex =
        regexCache.getOrPut(pattern) {
            Regex(pattern, RegexOption.IGNORE_CASE)
        }

    companion object {
        private val NO_MATCH = MatchResult(matched = false)
    }
}
