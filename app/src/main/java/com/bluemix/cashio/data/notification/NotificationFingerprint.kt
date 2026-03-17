package com.bluemix.cashio.data.notification

import java.security.MessageDigest

/**
 * Builds a stable fingerprint used as the [com.bluemix.cashio.domain.model.ParsedNotificationTransaction.notifId].
 *
 * ## Canonical amount representation
 * Amount is represented as paise ([Long]) — no floating-point round-trips that could
 * produce non-deterministic strings (e.g. "500.0" vs "500.00").
 *
 * ## Bucket size
 * [bucketSeconds] (default 30) collapses duplicate notification re-deliveries
 * within the same 30-second window into a single fingerprint. UPI apps frequently
 * re-post or update the same notification within seconds.
 */
object NotificationFingerprint {

    fun build(
        packageName: String,
        amountPaise: Long,          // Use paise — no floating-point ambiguity
        merchant: String?,
        postTimeMillis: Long,
        sbnKey: String?,
        bucketSeconds: Long = 30
    ): String {
        val merchantNorm = merchant.orEmpty()
            .trim()
            .lowercase()
            .replace(Regex("""\s+"""), " ")

        val bucket = (postTimeMillis / 1_000L) / bucketSeconds

        // Format: "notif|<pkg>|<paise>|<merchant>|<bucket>|<sbnKey>"
        val base = "notif|$packageName|$amountPaise|$merchantNorm|$bucket|${sbnKey.orEmpty()}"
        return "notif_${sha256(base).take(24)}"
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}