package com.block.app.engine

/**
 * Prevents the bouncer from slamming the door 60 times a second.
 *
 * Tracks last-block time per package and a global cooldown to avoid
 * re-triggering during the "go home" / overlay animation.
 *
 * Pure logic — no Android deps, no Hilt. Easy to unit test.
 */
class DebounceManager(
    private val cooldownMs: Long = 2_000L,      // 2 s per-app cooldown
    private val globalCooldownMs: Long = 1_000L  // 1 s global cooldown
) {
    /** Last block timestamp per package. */
    private val perPackage = HashMap<String, Long>(16)

    /** Last block timestamp globally. */
    private var lastGlobalBlock: Long = 0L

    /**
     * Returns `true` if enough time has elapsed since the last block
     * for [packageName] **and** globally.
     *
     * Call this *before* performing the block action.
     */
    fun shouldBlock(
        packageName: String,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Boolean {
        // Global cooldown check
        if (currentTimeMs - lastGlobalBlock < globalCooldownMs) return false

        // Per-app cooldown check
        val lastForPkg = perPackage[packageName] ?: 0L
        if (currentTimeMs - lastForPkg < cooldownMs) return false

        return true
    }

    /**
     * Record that a block just happened for [packageName].
     *
     * Call this *after* performing the block action.
     */
    fun recordBlock(
        packageName: String,
        currentTimeMs: Long = System.currentTimeMillis()
    ) {
        perPackage[packageName] = currentTimeMs
        lastGlobalBlock = currentTimeMs
    }

    /**
     * Clear all recorded timestamps (e.g. on service restart).
     */
    fun reset() {
        perPackage.clear()
        lastGlobalBlock = 0L
    }
}
