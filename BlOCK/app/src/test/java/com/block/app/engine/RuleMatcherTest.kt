package com.block.app.engine

import com.block.app.data.rules.DetectionRule
import com.block.app.data.rules.Signal
import com.block.app.data.rules.SignalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [RuleMatcher].
 * Verifies multi-signal matching, package gating, browser URL interception,
 * debounce thresholds, and regex accuracy.
 */
class RuleMatcherTest {

    private lateinit var matcher: RuleMatcher

    private val instagramRule = DetectionRule(
        id = "instagram_reels",
        appPackage = "com.instagram.android",
        appName = "Instagram",
        enabled = true,
        signals = listOf(
            Signal(SignalType.viewId, "clips_viewer_view_pager"),
            Signal(SignalType.className, "androidx.viewpager2.widget.ViewPager2"),
            Signal(SignalType.contentDescription, "(?i).*reel.*")
        ),
        minSignals = 2
    )

    private val youtubeShortsRule = DetectionRule(
        id = "youtube_shorts",
        appPackage = "com.google.android.youtube",
        appName = "YouTube Shorts",
        enabled = true,
        signals = listOf(
            Signal(SignalType.viewId, "reel_recycler"),
            Signal(SignalType.className, "androidx.viewpager2.widget.ViewPager2"),
            Signal(SignalType.contentDescription, "(?i).*shorts.*")
        ),
        minSignals = 2
    )

    private val browserRule = DetectionRule(
        id = "browser_shorts",
        appPackage = "*",
        appName = "Browser",
        enabled = true,
        signals = listOf(
            Signal(SignalType.contentDescription, "(?i).*(/shorts/|/reels/).*")
        ),
        minSignals = 1,
        browserRule = true,
        urlPatterns = listOf("(?i).*/shorts/.*", "(?i).*/reels/.*")
    )

    @Before
    fun setUp() {
        matcher = RuleMatcher()
    }

    @Test
    fun `two matching signals fires block`() {
        val result = matcher.matchNode(
            packageName = "com.instagram.android",
            nodeViewId = "com.instagram.android:id/clips_viewer_view_pager",
            nodeClassName = "androidx.viewpager2.widget.ViewPager2",
            nodeContentDescription = null,
            nodeText = null,
            rules = listOf(instagramRule)
        )

        assertTrue("Expected rule to match", result.matched)
        assertEquals("instagram_reels", result.rule?.id)
        assertEquals(2, result.signalCount)
    }

    @Test
    fun `only one matching signal does not fire block when minSignals is 2`() {
        val result = matcher.matchNode(
            packageName = "com.instagram.android",
            nodeViewId = "com.instagram.android:id/some_random_feed_view",
            nodeClassName = "androidx.viewpager2.widget.ViewPager2", // 1 match
            nodeContentDescription = "Home feed",
            nodeText = null,
            rules = listOf(instagramRule)
        )

        assertFalse("Single signal should not trigger block", result.matched)
    }

    @Test
    fun `wrong package does not match even if viewId matches`() {
        val result = matcher.matchNode(
            packageName = "com.twitter.android",
            nodeViewId = "com.instagram.android:id/clips_viewer_view_pager",
            nodeClassName = "androidx.viewpager2.widget.ViewPager2",
            nodeContentDescription = "reel",
            nodeText = null,
            rules = listOf(instagramRule)
        )

        assertFalse("Mismatched package name should be rejected", result.matched)
    }

    @Test
    fun `disabled rule is ignored`() {
        val disabledRule = instagramRule.copy(enabled = false)
        val result = matcher.matchNode(
            packageName = "com.instagram.android",
            nodeViewId = "clips_viewer_view_pager",
            nodeClassName = "androidx.viewpager2.widget.ViewPager2",
            nodeContentDescription = "reel",
            nodeText = null,
            rules = listOf(disabledRule)
        )

        assertFalse("Disabled rule must never fire", result.matched)
    }

    @Test
    fun `browser wildcard rule triggers on short URL`() {
        val result = matcher.matchNode(
            packageName = "com.android.chrome",
            nodeViewId = "com.android.chrome:id/url_bar",
            nodeClassName = "android.widget.EditText",
            nodeContentDescription = "https://m.youtube.com/shorts/dQw4w9WgXcQ",
            nodeText = "https://m.youtube.com/shorts/dQw4w9WgXcQ",
            rules = listOf(browserRule)
        )

        assertTrue("Browser URL matching /shorts/ should fire", result.matched)
        assertEquals("browser_shorts", result.rule?.id)
    }

    @Test
    fun `browser wildcard rule does not trigger on normal video URL`() {
        val result = matcher.matchNode(
            packageName = "com.android.chrome",
            nodeViewId = "com.android.chrome:id/url_bar",
            nodeClassName = "android.widget.EditText",
            nodeContentDescription = "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            nodeText = "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            rules = listOf(browserRule)
        )

        assertFalse("Regular video URLs must never be blocked", result.matched)
    }

    @Test
    fun `empty or null node returns NO_MATCH`() {
        val result = matcher.matchNode(
            packageName = "com.google.android.youtube",
            nodeViewId = null,
            nodeClassName = null,
            nodeContentDescription = null,
            nodeText = null,
            rules = listOf(youtubeShortsRule)
        )

        assertFalse(result.matched)
    }
}
