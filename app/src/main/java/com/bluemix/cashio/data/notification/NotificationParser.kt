package com.bluemix.cashio.data.notification

import com.bluemix.cashio.domain.model.ParsedNotificationTransaction
import com.bluemix.cashio.domain.model.TransactionType
import java.time.LocalDateTime
import java.util.Locale

object NotificationParser {

    // Private — implementation detail of [isUpiApp]. Not part of the public API.
    private val UPI_APP_PACKAGES: Set<String> = setOf(
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "net.one97.paytm",
        "in.org.npci.upiapp",
        "com.amazon.mShop.android.shopping",
        "com.mobikwik_new",
        "com.freecharge.android"
    )

    private val AMOUNT_PATTERN = Regex(
        pattern = """(?:(?:₹)|(?:rs\.?)|(?:inr))\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
        option = RegexOption.IGNORE_CASE
    )

    private val STRONG_INCOME_PHRASES = listOf(
        "paid you", "you received", "received money", "money received",
        "payment received", "credited to you", "credited in your",
        "added to your", "refund received"
    )

    private val STRONG_EXPENSE_PHRASES = listOf(
        "you paid", "you sent", "payment made", "debited from", "spent on"
    )

    private val EXPENSE_KEYWORDS = listOf(
        "paid", "sent", "debited", "debit", "spent",
        "payment to", "paid to", "sent to", "txn to", "transfer to", "to vpa", "to upi"
    )

    private val INCOME_KEYWORDS = listOf(
        "received", "credited", "credit", "refund",
        "payment from", "received from", "from vpa", "from upi"
    )

    private val NEGATIVE_KEYWORDS = listOf(
        "otp", "one time password", "verification",
        "cashback offer", "offer", "promo", "discount",
        "bill due", "due date", "statement", "reminder",
        "failed", "declined", "unsuccessful"
    )

    // ── Public API ─────────────────────────────────────────────────────────

    fun isUpiApp(packageName: String): Boolean = packageName in UPI_APP_PACKAGES

    fun isTransactionNotification(title: String, text: String): Boolean {
        val full = "$title $text".lowercase(Locale.getDefault())
        if (!AMOUNT_PATTERN.containsMatchIn(full)) return false
        if (NEGATIVE_KEYWORDS.any { it in full }) return false
        if (STRONG_INCOME_PHRASES.any { it in full } || STRONG_EXPENSE_PHRASES.any { it in full }) return true
        return EXPENSE_KEYWORDS.any { it in full } || INCOME_KEYWORDS.any { it in full }
    }

    fun parseNotification(
        title: String,
        text: String,
        packageName: String,
        postTimeMillis: Long,
        sbnKey: String?,
        timestamp: LocalDateTime = LocalDateTime.now()
    ): ParsedNotificationTransaction? {
        return try {
            val fullText = "$title $text".trim()
            if (fullText.isBlank()) return null

            val amountPaise = extractAmountPaise(fullText) ?: return null
            val transactionType = determineTransactionType(fullText) ?: return null
            val merchantName = extractMerchantName(fullText, transactionType)
            val appName = extractAppName(packageName)

            val notifId = NotificationFingerprint.build(
                packageName = packageName,
                amountPaise = amountPaise,
                merchant = merchantName,
                postTimeMillis = postTimeMillis,
                sbnKey = sbnKey,
                bucketSeconds = 30
            )

            ParsedNotificationTransaction(
                notifId = notifId,
                packageName = packageName,
                postTimeMillis = postTimeMillis,
                title = title,
                text = text,
                amountPaise = amountPaise,
                transactionType = transactionType,
                merchantName = merchantName,
                timestamp = timestamp,
                rawBody = fullText,
                appName = appName
            )
        } catch (_: Throwable) {
            null
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Extracts the monetary amount from notification text and returns it as paise.
     * Returns `null` if no amount is found or the value is non-positive.
     */
    private fun extractAmountPaise(text: String): Long? {
        val match = AMOUNT_PATTERN.find(text) ?: return null
        val raw = match.groupValues[1].replace(",", "")
        val value = raw.toDoubleOrNull() ?: return null
        if (value <= 0.0) return null
        return (value * 100).toLong()
    }

    /**
     * Determines transaction direction from notification text.
     *
     * Returns `null` in ambiguous cases (both income and expense keywords present)
     * rather than guessing — the notification is dropped to avoid miscategorisation.
     *
     * Strong phrase signals (e.g. "you paid", "paid you") take precedence over
     * generic keywords.
     */
    private fun determineTransactionType(text: String): TransactionType? {
        val lower = text.lowercase(Locale.getDefault())
        if (NEGATIVE_KEYWORDS.any { it in lower }) return null
        if (STRONG_INCOME_PHRASES.any { it in lower }) return TransactionType.INCOME
        if (STRONG_EXPENSE_PHRASES.any { it in lower }) return TransactionType.EXPENSE

        val hasIncome = INCOME_KEYWORDS.any { it in lower }
        val hasExpense = EXPENSE_KEYWORDS.any { it in lower }

        return when {
            hasIncome && !hasExpense -> TransactionType.INCOME
            hasExpense && !hasIncome -> TransactionType.EXPENSE
            // Both or neither — too ambiguous, drop the notification.
            else -> null
        }
    }

    private fun extractMerchantName(text: String, type: TransactionType): String? {
        val patterns = when (type) {
            TransactionType.EXPENSE -> listOf(
                Regex(
                    """(?:paid|payment|sent|txn|transfer)\s+to\s+([A-Za-z0-9@._\-\s]{2,40})""",
                    RegexOption.IGNORE_CASE
                ),
                Regex("""\bto\s+([A-Za-z0-9@._\-\s]{2,40})""", RegexOption.IGNORE_CASE),
                Regex("""(?:at)\s+([A-Za-z0-9@._\-\s]{2,40})""", RegexOption.IGNORE_CASE)
            )

            TransactionType.INCOME -> listOf(
                Regex("""([A-Za-z0-9@._\-\s]{2,40})\s+paid\s+you\b""", RegexOption.IGNORE_CASE),
                Regex(
                    """(?:received|credited|payment)\s+from\s+([A-Za-z0-9@._\-\s]{2,40})""",
                    RegexOption.IGNORE_CASE
                ),
                Regex("""\bfrom\s+([A-Za-z0-9@._\-\s]{2,40})""", RegexOption.IGNORE_CASE),
                Regex("""refund\s+from\s+([A-Za-z0-9@._\-\s]{2,40})""", RegexOption.IGNORE_CASE)
            )
        }

        for (p in patterns) {
            val m = p.find(text) ?: continue
            val raw = sanitizeMerchant(m.groupValues[1]) ?: continue
            if (raw.isNotBlank()) return raw
        }
        return extractUpiId(text)
    }

    private fun extractUpiId(text: String): String? {
        val upiRegex = Regex("""\b([a-zA-Z0-9.\-_]{2,}@[a-zA-Z0-9.\-_]{2,})\b""")
        return upiRegex.find(text)?.groupValues?.get(1)
    }

    /**
     * Cleans up a raw merchant name extracted by regex.
     * Returns `null` if the result is blank after sanitization.
     */
    private fun sanitizeMerchant(raw: String): String? {
        val cleaned = raw.trim()
            .replace(Regex("""[\n\r\t]+"""), " ")
            .replace(Regex("""\s{2,}"""), " ")
            .replace(Regex("""[.,;:)\]]+$"""), "")
            // Strip common legal suffixes that pollute keyword matching
            .replace(
                Regex(
                    """\s*(private limited|pvt\.? ltd\.?|ltd\.?|india)\s*$""",
                    RegexOption.IGNORE_CASE
                ), ""
            )
            .trim()
            .take(60)
        return cleaned.ifBlank { null }
    }

    private fun extractAppName(packageName: String): String = when (packageName) {
        "com.phonepe.app" -> "PhonePe"
        "com.google.android.apps.nbu.paisa.user" -> "Google Pay"
        "net.one97.paytm" -> "Paytm"
        "in.org.npci.upiapp" -> "BHIM UPI"
        "com.amazon.mShop.android.shopping" -> "Amazon Pay"
        "com.mobikwik_new" -> "MobiKwik"
        "com.freecharge.android" -> "FreeCharge"
        else -> "UPI Payment"
    }
}