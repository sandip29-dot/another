package com.block.app.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DebounceManager].
 * Verifies per-app cooldown, global cooldown, and reset mechanisms.
 */
class DebounceManagerTest {

    private lateinit var debounce: DebounceManager

    @Before
    fun setUp() {
        // 2000ms per-app cooldown, 1000ms global cooldown
        debounce = DebounceManager(cooldownMs = 2_000L, globalCooldownMs = 1_000L)
    }

    @Test
    fun `first block attempt is allowed`() {
        val allowed = debounce.shouldBlock("com.instagram.android", currentTimeMs = 10_000L)
        assertTrue("Initial block attempt must be allowed", allowed)
    }

    @Test
    fun `immediate second block attempt for same app is debounced`() {
        val t0 = 10_000L
        assertTrue(debounce.shouldBlock("com.instagram.android", t0))
        debounce.recordBlock("com.instagram.android", t0)

        // 500ms later: blocked by both global (1000ms) and app (2000ms) cooldown
        assertFalse(
            "Rapid second block attempt must be suppressed",
            debounce.shouldBlock("com.instagram.android", t0 + 500L)
        )
    }

    @Test
    fun `block attempt after cooldown passes is allowed`() {
        val t0 = 10_000L
        debounce.recordBlock("com.instagram.android", t0)

        // 2500ms later (> 2000ms cooldown)
        assertTrue(
            "Block after full cooldown must be allowed",
            debounce.shouldBlock("com.instagram.android", t0 + 2_500L)
        )
    }

    @Test
    fun `different app blocked during global cooldown is suppressed`() {
        val t0 = 10_000L
        debounce.recordBlock("com.instagram.android", t0)

        // 500ms later: Instagram is cooling down, YouTube tries to block
        // Should be suppressed because global cooldown is 1000ms
        assertFalse(
            "Global cooldown should prevent block from another app within 1s",
            debounce.shouldBlock("com.google.android.youtube", t0 + 500L)
        )
    }

    @Test
    fun `different app allowed after global cooldown but before first app cooldown`() {
        val t0 = 10_000L
        debounce.recordBlock("com.instagram.android", t0)

        // 1500ms later: global cooldown (1000ms) has passed, but Instagram cooldown (2000ms) hasn't
        assertFalse(
            "First app still in cooldown",
            debounce.shouldBlock("com.instagram.android", t0 + 1_500L)
        )
        assertTrue(
            "Second app allowed because global cooldown elapsed",
            debounce.shouldBlock("com.google.android.youtube", t0 + 1_500L)
        )
    }

    @Test
    fun `reset clears all debounce memory`() {
        val t0 = 10_000L
        debounce.recordBlock("com.instagram.android", t0)

        debounce.reset()

        assertTrue(
            "Immediately allowed after reset",
            debounce.shouldBlock("com.instagram.android", t0 + 100L)
        )
    }
}
