package com.block.app.watchdog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Listens for BOOT_COMPLETED to re-schedule the watchdog worker
 * after device restart. If the shield should be up, the watchdog
 * will catch a missing accessibility service and fire a notification.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ServiceMonitor.scheduleWatchdog(context)
        }
    }
}
