package com.block.app.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Device admin receiver for optional uninstall protection.
 * When enabled, the user must first deactivate device admin before
 * uninstalling BlOCK — adds friction against impulsive removal.
 */
class BlockDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(
            context,
            "BlOCK uninstall protection enabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(
            context,
            "BlOCK uninstall protection disabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Disabling device admin will make BlOCK easier to uninstall. Are you sure?"
    }
}
