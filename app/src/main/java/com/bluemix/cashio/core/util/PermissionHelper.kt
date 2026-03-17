package com.bluemix.cashio.core.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Stateless helpers for checking runtime permission status.
 *
 * ## Source of truth
 * Permission state is read **directly from the system** on every call.
 * It is never cached or stored in DataStore/SharedPreferences — doing so
 * creates a second source of truth that drifts when the user revokes
 * permissions from the Settings app without reopening Cashio.
 *
 * Call these helpers immediately before any operation that requires a
 * permission, not once at startup.
 */
object PermissionHelper {

    // ── SMS ────────────────────────────────────────────────────────────────

    /**
     * Returns `true` if both [Manifest.permission.READ_SMS] and
     * [Manifest.permission.RECEIVE_SMS] are granted.
     */
    fun isSmsPermissionGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
                PackageManager.PERMISSION_GRANTED

    // ── Notification listener ──────────────────────────────────────────────

    /**
     * Returns `true` if Cashio's NotificationListenerService is enabled.
     */
    fun isNotificationListenerEnabled(context: Context): Boolean =
        NotificationManagerCompat
            .getEnabledListenerPackages(context)
            .contains(context.packageName)

    // ── POST_NOTIFICATIONS (Android 13+) ───────────────────────────────────

    /**
     * Returns `true` if the app can post notifications.
     * On API < 33 this always returns `true` (permission did not exist).
     */
    fun canPostNotifications(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    // ── Convenience ───────────────────────────────────────────────────────

    /**
     * Returns `true` if all permissions required for automatic SMS import are granted.
     */
    fun canAutoImportSms(context: Context): Boolean = isSmsPermissionGranted(context)

    /**
     * Returns `true` if all permissions required for notification-based import are granted.
     */
    fun canAutoImportNotifications(context: Context): Boolean =
        isNotificationListenerEnabled(context)

    // ── Navigation to Settings ────────────────────────────────────────────

    /**
     * Opens the app's settings page where users can manage permissions.
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }

    /**
     * Opens the notification listener settings page.
     */
    fun openNotificationAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        context.startActivity(intent)
    }
}

/**
 * Permission status result for Settings screen.
 */
data class PermissionStatus(
    val smsGranted: Boolean,
    val notificationGranted: Boolean
)

/**
 * Get current permission status for all app permissions.
 */
fun PermissionHelper.getPermissionStatus(context: Context): PermissionStatus {
    return PermissionStatus(
        smsGranted = isSmsPermissionGranted(context),
        notificationGranted = isNotificationListenerEnabled(context)
    )
}