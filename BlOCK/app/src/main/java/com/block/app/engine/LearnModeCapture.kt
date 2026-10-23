package com.block.app.engine

import android.view.accessibility.AccessibilityNodeInfo
import com.block.app.data.rules.DetectionRule
import com.block.app.data.rules.Signal
import com.block.app.data.rules.SignalType
import java.util.UUID

/**
 * Captures the accessibility node tree for Learn Mode.
 *
 * When the user enables Learn Mode and opens a short-video feed,
 * we snapshot the node tree and try to auto-generate a [DetectionRule]
 * based on known heuristics (ViewPager2, video surfaces, swipe patterns).
 */
object LearnModeCapture {

    /**
     * A flattened snapshot of one accessibility node.
     */
    data class CapturedNode(
        val viewId: String?,
        val className: String?,
        val contentDescription: String?,
        val text: String?,
        val depth: Int
    )

    // ── Known heuristics for short-video feeds ───────────────

    /** Class names that strongly indicate a short-video UI. */
    private val VIDEO_CLASS_PATTERNS = listOf(
        "ViewPager2",
        "RecyclerView",
        "SurfaceView",
        "TextureView",
        "ExoPlayerView",
        "PlayerView",
        "VideoView",
        "StyledPlayerView"
    )

    /** View-ID fragments that suggest short-video chrome. */
    private val VIDEO_VIEW_ID_PATTERNS = listOf(
        "reel",
        "short",
        "pager",
        "video_player",
        "player_view",
        "swipe",
        "feed_video",
        "vertical_feed"
    )

    /** Content-description fragments for video chrome. */
    private val VIDEO_CD_PATTERNS = listOf(
        "reel",
        "short",
        "video",
        "swipe up",
        "swipe to see next",
        "double tap to like"
    )

    private const val MAX_DEPTH = 12

    // ── Public API ───────────────────────────────────────────

    /**
     * Recursively walk the accessibility tree from [rootNode] and return
     * a flat list of [CapturedNode]s.
     */
    fun captureNodeTree(rootNode: AccessibilityNodeInfo): List<CapturedNode> {
        val nodes = mutableListOf<CapturedNode>()
        walkNode(rootNode, depth = 0, nodes)
        return nodes
    }

    /**
     * Analyze [nodes] captured from a short-video feed and produce a
     * best-effort [DetectionRule].
     *
     * @param packageName The app's package name.
     * @param appName     Human-readable app name.
     * @param nodes       Captured nodes from [captureNodeTree].
     */
    fun generateRule(
        packageName: String,
        appName: String,
        nodes: List<CapturedNode>
    ): DetectionRule {
        val signals = mutableListOf<Signal>()

        // ── Scan for class-name matches ──────────────────────
        val classHits = mutableSetOf<String>()
        for (node in nodes) {
            val cn = node.className ?: continue
            for (pattern in VIDEO_CLASS_PATTERNS) {
                if (cn.contains(pattern, ignoreCase = true) && pattern !in classHits) {
                    classHits.add(pattern)
                    signals.add(
                        Signal(
                            type = SignalType.className,
                            pattern = ".*${Regex.escape(pattern)}.*"
                        )
                    )
                }
            }
        }

        // ── Scan for view-ID matches ─────────────────────────
        val viewIdHits = mutableSetOf<String>()
        for (node in nodes) {
            val vid = node.viewId ?: continue
            for (pattern in VIDEO_VIEW_ID_PATTERNS) {
                if (vid.contains(pattern, ignoreCase = true) && pattern !in viewIdHits) {
                    viewIdHits.add(pattern)
                    signals.add(
                        Signal(
                            type = SignalType.viewId,
                            pattern = ".*${Regex.escape(pattern)}.*"
                        )
                    )
                }
            }
        }

        // ── Scan for content-description matches ─────────────
        val cdHits = mutableSetOf<String>()
        for (node in nodes) {
            val cd = node.contentDescription ?: continue
            for (pattern in VIDEO_CD_PATTERNS) {
                if (cd.contains(pattern, ignoreCase = true) && pattern !in cdHits) {
                    cdHits.add(pattern)
                    signals.add(
                        Signal(
                            type = SignalType.contentDescription,
                            pattern = ".*${Regex.escape(pattern)}.*"
                        )
                    )
                }
            }
        }

        // If we didn't find any strong signals, add generic ones
        if (signals.isEmpty()) {
            // Fallback: look for any RecyclerView + any video surface
            signals.add(
                Signal(
                    type = SignalType.className,
                    pattern = ".*RecyclerView.*"
                )
            )
            signals.add(
                Signal(
                    type = SignalType.className,
                    pattern = ".*(SurfaceView|TextureView).*"
                )
            )
        }

        // Determine minSignals: require at least 2, but cap at signal count
        val minSignals = minOf(2, signals.size)

        return DetectionRule(
            id = "learn_${packageName}_${UUID.randomUUID().toString().take(8)}",
            appPackage = packageName,
            appName = appName,
            enabled = true,
            signals = signals,
            minSignals = minSignals,
            browserRule = false,
            urlPatterns = emptyList()
        )
    }

    // ── Private helpers ──────────────────────────────────────

    private fun walkNode(
        node: AccessibilityNodeInfo,
        depth: Int,
        result: MutableList<CapturedNode>
    ) {
        if (depth > MAX_DEPTH) return

        result.add(
            CapturedNode(
                viewId = node.viewIdResourceName,
                className = node.className?.toString(),
                contentDescription = node.contentDescription?.toString(),
                text = node.text?.toString(),
                depth = depth
            )
        )

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                walkNode(child, depth + 1, result)
            } finally {
                child.recycle()
            }
        }
    }
}
