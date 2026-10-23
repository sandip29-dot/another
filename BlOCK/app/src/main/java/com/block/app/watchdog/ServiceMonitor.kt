package com.block.app.watchdog

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Utility to check accessibility service status and schedule/cancel
 * the periodic watchdog worker.
 */
object ServiceMonitor {

    private const val WATCHDOG_WORK_NAME = "block_watchdog"

    /**
     * Checks whether the given accessibility service class is currently enabled
     * by inspecting both AccessibilityManager and Settings.Secure.
     */
    fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        // Method 1: AccessibilityManager query
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        if (am != null) {
            val enabledServices = am.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )
            val expectedComponent = ComponentName(context, serviceClass)
            for (info in enabledServices) {
                val enabledComponent = ComponentName.unflattenFromString(info.id)
                if (enabledComponent == expectedComponent) {
                    return true
                }
            }
        }

        // Method 2: Settings.Secure fallback
        val expectedId = ComponentName(context, serviceClass).flattenToString()
        val enabledSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledSetting.split(':').any { it.equals(expectedId, ignoreCase = true) }
    }

    /**
     * Enqueues a periodic watchdog that checks every 15 minutes
     * whether the accessibility service is still alive.
     */
    fun scheduleWatchdog(context: Context) {
        val request = PeriodicWorkRequestBuilder<WatchdogWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag(WATCHDOG_WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WATCHDOG_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Cancels the periodic watchdog work.
     */
    fun cancelWatchdog(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WATCHDOG_WORK_NAME)
    }
}
