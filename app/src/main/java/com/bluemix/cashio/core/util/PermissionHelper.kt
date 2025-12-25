package com.bluemix.cashio.core.util

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Unified helper for all app permissions
 */
object PermissionHelper {

    // ==================== SMS PERMISSIONS ====================

    /**
     * Check if SMS permission is granted
     */
    fun isSmsPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECEIVE_SMS
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Granted by default on older versions
        }
    }

    /**
     * Get SMS permissions to request
     */
    fun getSmsPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS
        )
    }

    /**
     * Open app settings for manual permission grant
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ==================== NOTIFICATION ACCESS ====================

    /**
     * Check if notification listener permission is granted
     */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )

        if (enabledListeners.isNullOrEmpty()) {
            return false
        }

        val packageName = context.packageName
        return enabledListeners.split(":").any { componentString ->
            val componentName = ComponentName.unflattenFromString(componentString)
            componentName?.packageName == packageName
        }
    }

    /**
     * Open notification listener settings
     */
    fun openNotificationAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Check if notification listener service is running
     */
    fun isNotificationServiceRunning(context: Context): Boolean {
        return isNotificationAccessGranted(context)
    }

    // ==================== PERMISSION STATUS ====================

    /**
     * Get comprehensive permission status for display
     */
    data class PermissionStatus(
        val smsGranted: Boolean,
        val notificationGranted: Boolean
    )

    /**
     * Get all permission statuses at once
     */
    fun getPermissionStatus(context: Context): PermissionStatus {
        return PermissionStatus(
            smsGranted = isSmsPermissionGranted(context),
            notificationGranted = isNotificationAccessGranted(context)
        )
    }
}
