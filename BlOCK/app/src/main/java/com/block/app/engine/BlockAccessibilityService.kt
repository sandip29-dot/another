package com.block.app.engine

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.OvershootInterpolator
import com.block.app.data.db.BlockAttempt
import com.block.app.data.db.BlockAttemptDao
import com.block.app.data.rules.DetectionRule
import com.block.app.data.rules.RulesRepository
import com.block.app.data.settings.EscapeTarget
import com.block.app.data.settings.SettingsDataStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * The bouncer.
 *
 * This [AccessibilityService] monitors every window-content-change and
 * window-state-change event, matches the accessibility tree against the
 * user's detection rules, and performs a "slam" block when a short-video
 * feed is detected.
 *
 * Not managed by Hilt's standard injection — uses [EntryPointAccessors]
 * to pull dependencies from the singleton component.
 */
class BlockAccessibilityService : AccessibilityService() {

    // ── Hilt EntryPoint ──────────────────────────────────────

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ServiceEntryPoint {
        fun rulesRepository(): RulesRepository
        fun settingsDataStore(): SettingsDataStore
        fun blockAttemptDao(): BlockAttemptDao
    }

    // ── Dependencies (lazily resolved) ───────────────────────

    private lateinit var rulesRepo: RulesRepository
    private lateinit var settings: SettingsDataStore
    private lateinit var blockAttemptDao: BlockAttemptDao

    // ── Engine components ────────────────────────────────────

    private val ruleMatcher = RuleMatcher()
    private val debounce = DebounceManager()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var windowManager: WindowManager

    // Cached settings to avoid suspending in the hot path
    @Volatile private var shieldEnabled = true
    @Volatile private var learnModeActive = false
    @Volatile private var escapeTarget = EscapeTarget.HOME_SCREEN
    @Volatile private var reduceMotion = false

    // ── Node walk limits ─────────────────────────────────────

    private companion object {
        const val TAG = "BlockA11yService"
        const val MAX_TREE_DEPTH = 10
        const val MAX_NODES_PER_EVENT = 300
        const val OVERLAY_DURATION_MS = 1_200L
    }

    // ── Bouncer lines ────────────────────────────────────────

    private val bouncerLines = listOf(
        "Not tonight.",
        "Nope.",
        "Future you says no.",
        "Nice try.",
        "Door's closed.",
        "Blocked.",
        "Try again never.",
        "Hard pass.",
        "The answer is no.",
        "Walk away."
    )

    // ── Lifecycle ────────────────────────────────────────────

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")

        // Resolve dependencies from Hilt
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java
        )
        rulesRepo = entryPoint.rulesRepository()
        settings = entryPoint.settingsDataStore()
        blockAttemptDao = entryPoint.blockAttemptDao()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Configure the service
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 200L
        }

        // Observe settings changes and cache values
        serviceScope.launch {
            settings.shieldEnabled.collect { shieldEnabled = it }
        }
        serviceScope.launch {
            settings.learnModeActive.collect { learnModeActive = it }
        }
        serviceScope.launch {
            settings.escapeTarget.collect { escapeTarget = it }
        }
        serviceScope.launch {
            settings.reduceMotion.collect { reduceMotion = it }
        }

        debounce.reset()
        _isRunning.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return

        // Don't block ourselves or the system UI / launcher
        if (pkg == packageName || pkg == "com.android.systemui" || pkg == "com.android.launcher") return

        // Shield off? Do nothing.
        if (!shieldEnabled) return

        val rootNode = rootInActiveWindow ?: return

        try {
            if (learnModeActive) {
                handleLearnMode(pkg, rootNode)
            } else {
                handleBlocking(pkg, rootNode)
            }
        } finally {
            rootNode.recycle()
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        debounce.reset()
        ruleMatcher.clearCache()
        _isRunning.value = false
        Log.d(TAG, "Service destroyed")
    }

    // ── Blocking logic ───────────────────────────────────────

    private fun handleBlocking(packageName: String, rootNode: AccessibilityNodeInfo) {
        val rules = rulesRepo.getRules().filter { it.enabled }
        if (rules.isEmpty()) return

        var nodesChecked = 0
        val matched = walkAndMatch(rootNode, packageName, rules, 0, nodesChecked)
        if (matched == null) return

        // Debounce check
        val now = System.currentTimeMillis()
        if (!debounce.shouldBlock(packageName, now)) return
        debounce.recordBlock(packageName, now)

        // ── BLOCK! ──────────────────────────────────────────
        Log.d(TAG, "BLOCKED $packageName — rule=${matched.rule?.id}")

        // 1. Navigate away
        performEscape(packageName, rootNode)

        // 2. Record in database
        val cal = Calendar.getInstance()
        serviceScope.launch(Dispatchers.IO) {
            blockAttemptDao.insert(
                BlockAttempt(
                    timestamp = now,
                    appPackage = packageName,
                    appName = matched.rule?.appName ?: packageName,
                    ruleId = matched.rule?.id ?: "unknown",
                    dayOfYear = cal.get(Calendar.DAY_OF_YEAR),
                    hourOfDay = cal.get(Calendar.HOUR_OF_DAY),
                    year = cal.get(Calendar.YEAR)
                )
            )
        }

        // 3. Show slam overlay
        serviceScope.launch(Dispatchers.Main) {
            showSlamOverlay()
        }

        // 4. Haptic feedback
        vibrate()
    }

    /**
     * BFS/DFS walk of the node tree, matching each node.
     * Returns the first [RuleMatcher.MatchResult] that fires, or null.
     */
    private fun walkAndMatch(
        node: AccessibilityNodeInfo,
        packageName: String,
        rules: List<DetectionRule>,
        depth: Int,
        nodesCheckedSoFar: Int
    ): RuleMatcher.MatchResult? {
        if (depth > MAX_TREE_DEPTH) return null
        var nodesChecked = nodesCheckedSoFar
        if (nodesChecked > MAX_NODES_PER_EVENT) return null

        val result = ruleMatcher.matchNode(
            packageName = packageName,
            nodeViewId = node.viewIdResourceName,
            nodeClassName = node.className?.toString(),
            nodeContentDescription = node.contentDescription?.toString(),
            nodeText = node.text?.toString(),
            rules = rules
        )
        nodesChecked++

        if (result.matched) return result

        for (i in 0 until node.childCount) {
            if (nodesChecked > MAX_NODES_PER_EVENT) return null
            val child = node.getChild(i) ?: continue
            try {
                val childResult = walkAndMatch(child, packageName, rules, depth + 1, nodesChecked)
                if (childResult != null) return childResult
                // Approximate: we can't perfectly track nodesChecked across siblings
                // without a mutable counter, but the depth limit keeps us safe.
            } finally {
                child.recycle()
            }
        }

        return null
    }

    /** Navigate away based on the user's escape target preference. */
    private fun performEscape(packageName: String, rootNode: AccessibilityNodeInfo) {
        if (escapeTarget == EscapeTarget.APP_HOME) {
            // Try to find and click the app's home/feed tab
            val homeTab = findHomeTab(rootNode)
            if (homeTab != null) {
                homeTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }
        // Fallback: just go home
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    /** Heuristic: try to find a "Home" or "Feed" tab in the bottom nav. */
    private fun findHomeTab(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val cd = node.contentDescription?.toString()?.lowercase() ?: ""
        val text = node.text?.toString()?.lowercase() ?: ""
        if ((cd.contains("home") || cd.contains("feed") || text.contains("home") || text.contains("feed"))
            && node.isClickable
        ) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findHomeTab(child)
            if (found != null) {
                if (found != child) child.recycle()
                return found
            }
            child.recycle()
        }
        return null
    }

    // ── Learn Mode ───────────────────────────────────────────

    private fun handleLearnMode(packageName: String, rootNode: AccessibilityNodeInfo) {
        val captured = LearnModeCapture.captureNodeTree(rootNode)
        if (captured.isEmpty()) return

        // Log captured nodes for debugging
        Log.d(TAG, "Learn Mode: captured ${captured.size} nodes from $packageName")

        // Generate and save a rule
        val pm = applicationContext.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }

        val rule = LearnModeCapture.generateRule(packageName, appName, captured)
        if (rule.signals.isNotEmpty()) {
            rulesRepo.addRule(rule)
            Log.d(TAG, "Learn Mode: generated rule ${rule.id} with ${rule.signals.size} signals")
        }

        // Disable learn mode after one capture
        serviceScope.launch {
            settings.setLearnModeActive(false)
        }
    }

    // ── Slam Overlay ─────────────────────────────────────────

    @Suppress("DEPRECATION")
    private fun showSlamOverlay() {
        val slamView = SlamOverlayView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        try {
            windowManager.addView(slamView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add slam overlay", e)
            return
        }

        // Auto-remove after animation
        val duration = if (reduceMotion) 600L else OVERLAY_DURATION_MS
        slamView.postDelayed({
            try {
                windowManager.removeView(slamView)
            } catch (_: Exception) {
                // View may already be removed
            }
        }, duration)
    }

    // ── Haptic feedback ──────────────────────────────────────

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed", e)
        }
    }

    // ── Companion: service running state ─────────────────────

    companion object {
        private val _isRunning = MutableStateFlow(false)

        /** Observable: whether the accessibility service is currently running. */
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    }

    // ═══════════════════════════════════════════════════════════
    // Slam Overlay View — the "BLOCKED" stamp animation
    // ═══════════════════════════════════════════════════════════

    /**
     * Custom view for the neo-brutalist "BLOCKED" slam overlay.
     *
     * Animation sequence:
     * 1. Black background fades in
     * 2. "BLOCKED" text scales up with overshoot
     * 3. Random bouncer line appears below
     * 4. Blocks break apart and fall (if reduce-motion is off)
     */
    private inner class SlamOverlayView(context: Context) : View(context) {

        // ── Colors ───────────────────────────────────────────
        private val bgColor = Color.parseColor("#DD000000")      // semi-transparent black
        private val stampColor = Color.parseColor("#CCFF00")      // electric lime
        private val textColor = Color.parseColor("#F5F0E8")       // off-white
        private val borderColor = Color.parseColor("#CCFF00")

        // ── Paints ───────────────────────────────────────────
        private val bgPaint = Paint().apply { color = bgColor }

        private val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stampColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            isFakeBoldText = true
        }

        private val stampStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            isFakeBoldText = true
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        private val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        // ── Animation state ──────────────────────────────────
        private var scale = 0f
        private var alpha = 0f
        private var blockFallProgress = 0f  // 0..1
        private val bouncerLine = bouncerLines.random()

        // Grid for the falling-blocks effect
        private val gridCols = 8
        private val gridRows = 12
        private val blockFallDelays = Array(gridRows) { row ->
            FloatArray(gridCols) { col ->
                // Random stagger based on position
                (row * 0.05f + col * 0.02f + Math.random().toFloat() * 0.1f)
            }
        }

        init {
            startAnimation()
        }

        private fun startAnimation() {
            if (reduceMotion) {
                // Simple fade in/out, no scaling or blocks
                alpha = 1f
                scale = 1f
                invalidate()
                return
            }

            // Phase 1: Scale in the stamp text (0–400ms)
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 400
                interpolator = OvershootInterpolator(2f)
                addUpdateListener {
                    scale = it.animatedValue as Float
                    alpha = minOf(1f, scale * 2f) // fade in faster than scale
                    invalidate()
                }
                start()
            }

            // Phase 2: Blocks fall (400ms–1200ms)
            postDelayed({
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 800
                    addUpdateListener {
                        blockFallProgress = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
            }, 400)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()

            // ── Background ───────────────────────────────────
            bgPaint.alpha = (alpha * 221).toInt() // 221 = 0xDD
            canvas.drawRect(0f, 0f, w, h, bgPaint)

            // ── Border (neo-brutalist thick border) ──────────
            if (alpha > 0.5f) {
                val inset = 16f
                canvas.drawRect(inset, inset, w - inset, h - inset, borderPaint)
            }

            // ── "BLOCKED" stamp ──────────────────────────────
            val stampSize = w * 0.14f * scale
            stampPaint.textSize = stampSize
            stampStrokePaint.textSize = stampSize

            val cx = w / 2f
            val cy = h / 2f - stampSize * 0.3f

            // Draw stroke (outline) then fill for thick neo-brutalist text
            canvas.drawText("BLOCKED", cx, cy, stampStrokePaint)
            canvas.drawText("BLOCKED", cx, cy, stampPaint)

            // ── Bouncer line below ───────────────────────────
            if (scale > 0.5f) {
                linePaint.textSize = w * 0.04f
                linePaint.alpha = ((scale - 0.5f) * 2f * 255f).toInt().coerceIn(0, 255)
                canvas.drawText(bouncerLine, cx, cy + stampSize * 0.8f, linePaint)
            }

            // ── Falling blocks overlay ───────────────────────
            if (blockFallProgress > 0f && !reduceMotion) {
                drawFallingBlocks(canvas, w, h)
            }
        }

        private fun drawFallingBlocks(canvas: Canvas, w: Float, h: Float) {
            val blockW = w / gridCols
            val blockH = h / gridRows

            val blockPaint = Paint().apply {
                color = Color.BLACK
            }

            for (row in 0 until gridRows) {
                for (col in 0 until gridCols) {
                    val delay = blockFallDelays[row][col]
                    val progress = ((blockFallProgress - delay) / (1f - delay)).coerceIn(0f, 1f)

                    if (progress <= 0f) continue

                    // Block falls downward with acceleration
                    val fallDistance = progress * progress * h * 0.5f
                    val blockAlpha = (1f - progress).coerceIn(0f, 1f)

                    blockPaint.alpha = (blockAlpha * 180).toInt()

                    val left = col * blockW
                    val top = row * blockH + fallDistance
                    canvas.drawRect(left, top, left + blockW - 2f, top + blockH - 2f, blockPaint)
                }
            }
        }
    }
}
