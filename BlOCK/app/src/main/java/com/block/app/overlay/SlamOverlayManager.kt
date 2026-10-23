package com.block.app.overlay

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.Gravity
import android.view.WindowManager

/**
 * Manages presentation and teardown of the neo-brutalist "Slam" overlay window.
 */
class SlamOverlayManager(private val context: Context) {

    companion object {
        private const val TAG = "SlamOverlayManager"
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentOverlayView: SlamOverlayView? = null

    /**
     * Slams the BLOCKED overlay over the launcher/current screen.
     * Fires haptic feedback immediately and cleans up upon animation finish.
     */
    fun showSlam() {
        mainHandler.post {
            try {
                // Dismiss any existing overlay
                dismiss()

                val overlay = SlamOverlayView(context)
                currentOverlayView = overlay

                val windowType = if (context is AccessibilityService) {
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    windowType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                }

                overlay.setOnAnimationCompleteListener {
                    dismiss()
                }

                windowManager.addView(overlay, params)
                overlay.startAnimation()
                triggerHapticThud()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to display slam overlay", e)
                dismiss()
            }
        }
    }

    fun dismiss() {
        mainHandler.post {
            currentOverlayView?.let { view ->
                try {
                    windowManager.removeView(view)
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing slam overlay", e)
                }
                currentOverlayView = null
            }
        }
    }

    private fun triggerHapticThud() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(120)
                }
            }
        } catch (_: Exception) {
            // Haptics optional if permission denied
        }
    }
}
