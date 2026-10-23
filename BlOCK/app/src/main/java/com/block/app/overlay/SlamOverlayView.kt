package com.block.app.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.SystemClock
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Custom View that renders the "BLOCKED" slam animation.
 *
 * Phase 1 (0–400ms):   "BLOCKED" stamp slams in from 3× scale with overshoot,
 *                       rotates from -30° to -12°, plus a random bouncer line.
 * Phase 2 (400–1200ms): Screen shatters into an 8×16 grid of falling blocks.
 *
 * This is a plain View (not Compose) because it runs inside the
 * AccessibilityService overlay context where Compose is unavailable.
 */
class SlamOverlayView(context: Context) : View(context) {

    // ── Bouncer lines ───────────────────────────────────────────
    companion object {
        private val BOUNCER_LINES = listOf(
            "Not tonight.",
            "Nope.",
            "Future you says no.",
            "Nice try.",
            "Turn around.",
            "Door's closed.",
            "Maybe next time. Actually, no.",
            "Not happening.",
            "You know better."
        )

        // Neo-brutalist palette
        private const val COLOR_LIME = 0xFF_CC_FF_00.toInt()
        private const val COLOR_BLACK = 0xFF_00_00_00.toInt()
        private const val COLOR_DARK = 0xFF_1A_1A_1A.toInt()
        private const val COLOR_OFF_WHITE = 0xFF_F5_F0_E8.toInt()

        // Timing
        private const val PHASE_1_DURATION_MS = 400L
        private const val TOTAL_DURATION_MS = 1200L

        // Shatter grid
        private const val GRID_COLS = 8
        private const val GRID_ROWS = 16
        private const val GRAVITY = 3200f // px/s²
    }

    // ── State ────────────────────────────────────────────────────
    private val bouncerLine = BOUNCER_LINES[Random.nextInt(BOUNCER_LINES.size)]
    private var animationStartMs = 0L
    private var shards: List<BlockShard> = emptyList()
    private var shardsInitialized = false
    private var onAnimationComplete: (() -> Unit)? = null

    // ── Paints ───────────────────────────────────────────────────
    private val overlayPaint = Paint().apply {
        color = Color.BLACK
        alpha = (0.85f * 255).toInt()
        style = Paint.Style.FILL
    }

    private val stampStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_BLACK
        textSize = 120f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        textAlign = Paint.Align.CENTER
    }

    private val stampFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_LIME
        textSize = 120f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_OFF_WHITE
        textSize = 40f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    private val shardPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    // Interpolators
    private val overshootInterp = OvershootInterpolator(2.5f)
    private val accelInterp = AccelerateInterpolator(1.5f)

    // Animator for frame ticking
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = TOTAL_DURATION_MS
        addUpdateListener { invalidate() }
    }

    // ── Lifecycle ────────────────────────────────────────────────

    fun setOnAnimationCompleteListener(listener: () -> Unit) {
        onAnimationComplete = listener
    }

    fun startAnimation() {
        animationStartMs = SystemClock.elapsedRealtime()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    // ── Drawing ──────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (animationStartMs == 0L) return

        val elapsed = SystemClock.elapsedRealtime() - animationStartMs
        val w = width.toFloat()
        val h = height.toFloat()

        if (elapsed >= TOTAL_DURATION_MS) {
            // Animation complete — signal and stop
            animator.cancel()
            onAnimationComplete?.invoke()
            return
        }

        if (elapsed <= PHASE_1_DURATION_MS) {
            drawPhase1(canvas, elapsed, w, h)
        } else {
            drawPhase2(canvas, elapsed, w, h)
        }
    }

    /**
     * Phase 1: Semi-transparent overlay + "BLOCKED" stamp slamming in.
     */
    private fun drawPhase1(canvas: Canvas, elapsed: Long, w: Float, h: Float) {
        val progress = (elapsed.toFloat() / PHASE_1_DURATION_MS).coerceIn(0f, 1f)
        val interpProgress = overshootInterp.getInterpolation(progress)

        // Background overlay — fade in
        overlayPaint.alpha = (0.85f * 255 * progress.coerceAtMost(1f)).toInt()
        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        // Scale: 3× → 1×
        val scale = 3f - (2f * interpProgress)
        // Rotation: -30° → -12°
        val rotation = -30f + (18f * interpProgress)
        // Alpha
        val alpha = (progress * 255).toInt().coerceIn(0, 255)

        canvas.save()
        canvas.translate(w / 2f, h / 2f)
        canvas.rotate(rotation)
        canvas.scale(scale, scale)

        // Draw "BLOCKED" stamp
        stampStrokePaint.alpha = alpha
        stampFillPaint.alpha = alpha
        canvas.drawText("BLOCKED", 0f, 0f, stampStrokePaint)
        canvas.drawText("BLOCKED", 0f, 0f, stampFillPaint)

        // Draw bouncer line below
        subTextPaint.alpha = alpha
        canvas.drawText(bouncerLine, 0f, 60f, subTextPaint)

        canvas.restore()
    }

    /**
     * Phase 2: Screen shatters into falling blocks.
     */
    private fun drawPhase2(canvas: Canvas, elapsed: Long, w: Float, h: Float) {
        if (!shardsInitialized) {
            initializeShards(w, h)
            shardsInitialized = true
        }

        val phase2Elapsed = elapsed - PHASE_1_DURATION_MS
        val phase2Duration = TOTAL_DURATION_MS - PHASE_1_DURATION_MS
        val phase2Progress = (phase2Elapsed.toFloat() / phase2Duration).coerceIn(0f, 1f)

        // Fade the overlay background out
        val bgAlpha = ((1f - phase2Progress) * 0.85f * 255).toInt()
        overlayPaint.alpha = bgAlpha
        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        // Delta time for physics (in seconds)
        val dt = phase2Elapsed / 1000f

        for (shard in shards) {
            // Update physics
            val currentVelY = shard.initialVelocityY + GRAVITY * dt
            val currentX = shard.originX + shard.velocityX * dt
            val currentY = shard.originY + shard.initialVelocityY * dt + 0.5f * GRAVITY * dt * dt
            val currentRotation = shard.initialRotation + shard.rotationSpeed * dt

            // Alpha fade out
            val shardAlpha = ((1f - phase2Progress * 0.8f) * 255).toInt().coerceIn(0, 255)

            canvas.save()
            canvas.translate(currentX + shard.width / 2f, currentY + shard.height / 2f)
            canvas.rotate(currentRotation)

            shardPaint.color = shard.color
            shardPaint.alpha = shardAlpha
            canvas.drawRect(
                -shard.width / 2f,
                -shard.height / 2f,
                shard.width / 2f,
                shard.height / 2f,
                shardPaint
            )
            canvas.restore()
        }
    }

    /**
     * Initialize the shatter grid with randomized velocities.
     */
    private fun initializeShards(w: Float, h: Float) {
        val shardWidth = w / GRID_COLS
        val shardHeight = h / GRID_ROWS
        val colors = intArrayOf(COLOR_LIME, COLOR_DARK, COLOR_BLACK, COLOR_LIME, COLOR_DARK)

        shards = buildList {
            for (row in 0 until GRID_ROWS) {
                for (col in 0 until GRID_COLS) {
                    add(
                        BlockShard(
                            originX = col * shardWidth,
                            originY = row * shardHeight,
                            width = shardWidth,
                            height = shardHeight,
                            velocityX = Random.nextFloat() * 400f - 200f,       // -200..+200
                            initialVelocityY = -(Random.nextFloat() * 600f + 200f), // upward burst
                            initialRotation = Random.nextFloat() * 360f,
                            rotationSpeed = Random.nextFloat() * 720f - 360f,   // -360..+360 deg/s
                            color = colors[Random.nextInt(colors.size)]
                        )
                    )
                }
            }
        }
    }

    /**
     * Data for a single falling shard in the shatter animation.
     */
    private data class BlockShard(
        val originX: Float,
        val originY: Float,
        val width: Float,
        val height: Float,
        val velocityX: Float,
        val initialVelocityY: Float,
        val initialRotation: Float,
        val rotationSpeed: Float,
        val color: Int
    )
}
