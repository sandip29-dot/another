package com.block.app.watchdog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.block.app.R
import com.block.app.data.settings.SettingsDataStore
import com.block.app.engine.BlockAccessibilityService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Periodic WorkManager worker that checks whether the accessibility
 * service is still running. If the shield should be up but the service
 * is dead, fires a loud notification to alert the user.
 *
 * Scheduled by [ServiceMonitor.scheduleWatchdog].
 */
@HiltWorker
class WatchdogWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "block_watchdog_channel"
        private const val NOTIFICATION_ID = 9001
    }

    override suspend fun doWork(): Result {
        // Only nag if the user actually wants the shield enabled
        val shieldWanted = settingsDataStore.shieldEnabled.first()
        if (!shieldWanted) return Result.success()

        val serviceRunning = ServiceMonitor.isAccessibilityServiceEnabled(
            context,
            BlockAccessibilityService::class.java
        )

        if (!serviceRunning) {
            fireShieldDownNotification()
        }

        return Result.success()
    }

    /**
     * Creates a high-importance notification warning the user
     * that the shield has gone down.
     */
    private fun fireShieldDownNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (safe to call multiple times)
        createNotificationChannel(notificationManager)

        // Intent to open accessibility settings
        val accessibilityIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            accessibilityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // TODO: replace with proper icon
            .setContentTitle(context.getString(R.string.watchdog_title))
            .setContentText(context.getString(R.string.watchdog_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setVibrate(longArrayOf(0, 300, 200, 300, 200, 300))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.watchdog_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.watchdog_channel_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300, 200, 300)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
