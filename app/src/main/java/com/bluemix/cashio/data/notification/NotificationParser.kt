//package com.bluemix.cashio.data.notification
//
//import com.bluemix.cashio.domain.model.ParsedSmsTransaction
//import com.bluemix.cashio.domain.model.TransactionType
//import java.time.LocalDateTime
//import java.util.Locale
//
///**
// * Parses UPI/banking notifications from payment apps (PhonePe, GPay, Paytm, BHIM, Amazon Pay etc.)
// *
// * NOTE:
// * - This is heuristic parsing; keep it conservative to reduce false positives.
// * - If you later add a persistent dedupe key, include a stable "sourceId" for notifications.
// */
//object NotificationParser {
//
//    /**
//     * UPI app package names to monitor.
//     * Keep this list tight to reduce noise.
//     */
//    val UPI_APP_PACKAGES: Set<String> = setOf(
//        "com.phonepe.app",                         // PhonePe
//        "com.google.android.apps.nbu.paisa.user",  // Google Pay
//        "net.one97.paytm",                         // Paytm
//        "in.org.npci.upiapp",                      // BHIM
//        "com.amazon.mShop.android.shopping",       // Amazon Pay (inside Amazon app)
//        "com.mobikwik_new",                        // MobiKwik
//        "com.freecharge.android"                   // FreeCharge
//    )
//
//    // Amount patterns: ₹123, Rs. 123.45, INR 1,234.00
//    private val amountPattern = Regex(
//        pattern = """(?:(?:₹)|(?:rs\.?)|(?:inr))\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
//        option = RegexOption.IGNORE_CASE
//    )
//
//    private val expenseKeywords = listOf(
//        "paid", "sent", "debited", "debit", "spent",
//        "payment to", "paid to", "sent to", "txn to", "transfer to", "to vpa", "to upi"
//    )
//
//    private val incomeKeywords = listOf(
//        "received", "credited", "credit", "refund",
//        "payment from", "received from", "from vpa", "from upi"
//    )
//
//    // Things that often contain amount but are NOT transactions
//    private val negativeKeywords = listOf(
//        "otp", "one time password", "verification",
//        "cashback offer", "offer", "promo", "discount",
//        "bill due", "due date", "statement", "reminder",
//        "failed", "declined", "unsuccessful"
//    )
//
//    fun isUpiApp(packageName: String): Boolean = packageName in UPI_APP_PACKAGES
//
//    /**
//     * Fast pre-check: amount + a transaction keyword, and not a promo/otp.
//     */
//    fun isTransactionNotification(title: String, text: String): Boolean {
//        val full = "$title $text".lowercase(Locale.getDefault())
//
//        if (!amountPattern.containsMatchIn(full)) return false
//        if (negativeKeywords.any { it in full }) return false
//
//        val hasTypeKeyword = expenseKeywords.any { it in full } || incomeKeywords.any { it in full }
//        return hasTypeKeyword
//    }
//
//    /**
//     * Parse a notification into ParsedSmsTransaction. Returns null if unknown / too noisy.
//     */
//    fun parseNotification(
//        title: String,
//        text: String,
//        packageName: String,
//        timestamp: LocalDateTime = LocalDateTime.now()
//    ): ParsedSmsTransaction? {
//        return try {
//            val fullText = "$title $text".trim()
//            if (fullText.isBlank()) return null
//
//            val amount = extractAmount(fullText) ?: return null
//
//            val transactionType = determineTransactionType(fullText) ?: return null
//
//            val merchantName = extractMerchantName(fullText, transactionType)
//            val bankName = extractAppName(packageName)
//
//            ParsedSmsTransaction(
//                amount = amount,
//                transactionType = transactionType,
//                merchantName = merchantName,
//                accountNumber = null,
//                timestamp = timestamp,
//                rawSmsBody = fullText,
//                bankName = bankName
//            )
//        } catch (_: Throwable) {
//            null
//        }
//    }
//
//    /**
//     * If you want *only* expenses from notifications.
//     */
//    fun parseExpenseNotification(
//        title: String,
//        text: String,
//        packageName: String,
//        timestamp: LocalDateTime = LocalDateTime.now()
//    ): ParsedSmsTransaction? {
//        val tx = parseNotification(title, text, packageName, timestamp)
//        return if (tx?.transactionType == TransactionType.EXPENSE) tx else null
//    }
//
//    fun getTransactionType(text: String): TransactionType? = determineTransactionType(text)
//
//    private fun extractAmount(text: String): Double? {
//        val match = amountPattern.find(text) ?: return null
//        val raw = match.groupValues[1].replace(",", "")
//        return raw.toDoubleOrNull()
//    }
//
//    private fun determineTransactionType(text: String): TransactionType? {
//        val lower = text.lowercase(Locale.getDefault())
//
//        // If explicitly failed/declined -> ignore
//        if (negativeKeywords.any { it in lower }) return null
//
//        return when {
//            expenseKeywords.any { it in lower } -> TransactionType.EXPENSE
//            incomeKeywords.any { it in lower } -> TransactionType.INCOME
//            else -> null
//        }
//    }
//
//    /**
//     * Merchant extraction is best-effort. We keep it conservative:
//     * - Try "to ..." or "from ..."
//     * - Try "VPA/UPI ID"
//     * - Clean up noise
//     */
//    private fun extractMerchantName(text: String, type: TransactionType): String? {
//        val patterns = when (type) {
//            TransactionType.EXPENSE -> listOf(
//                // paid to XYZ / payment to XYZ / sent to XYZ
//                Regex(
//                    """(?:paid|payment|sent|txn|transfer)\s+(?:to)\s+([A-Za-z0-9@._\-\s]{2,60})""",
//                    RegexOption.IGNORE_CASE
//                ),
//                // to XYZ (common in some notifications)
//                Regex("""\bto\s+([A-Za-z0-9@._\-\s]{2,60})""", RegexOption.IGNORE_CASE),
//                // at XYZ
//                Regex("""(?:at)\s+([A-Za-z0-9@._\-\s]{2,60})""", RegexOption.IGNORE_CASE)
//            )
//
//            TransactionType.INCOME -> listOf(
//                Regex(
//                    """(?:received|credited|payment)\s+(?:from)\s+([A-Za-z0-9@._\-\s]{2,60})""",
//                    RegexOption.IGNORE_CASE
//                ),
//                Regex("""\bfrom\s+([A-Za-z0-9@._\-\s]{2,60})""", RegexOption.IGNORE_CASE),
//                Regex(
//                    """(?:refund)\s+(?:from)\s+([A-Za-z0-9@._\-\s]{2,60})""",
//                    RegexOption.IGNORE_CASE
//                )
//            )
//        }
//
//        // 1) Direct phrase patterns
//        for (p in patterns) {
//            val m = p.find(text) ?: continue
//            return sanitizeMerchant(m.groupValues[1])
//        }
//
//        // 2) VPA/UPI ID patterns
//        val vpa = extractUpiId(text)
//        if (vpa != null) return vpa
//
//        return null
//    }
//
//    private fun extractUpiId(text: String): String? {
//        // Typical VPAs: name@bank, phone@upi, xyz@okaxis, etc.
//        val upiRegex = Regex("""\b([a-zA-Z0-9.\-_]{2,}@[a-zA-Z0-9.\-_]{2,})\b""")
//        return upiRegex.find(text)?.groupValues?.get(1)
//    }
//
//    private fun sanitizeMerchant(raw: String): String {
//        return raw
//            .trim()
//            .replace(Regex("""[\n\r\t]+"""), " ")
//            .replace(Regex("""\s{2,}"""), " ")
//            .replace(Regex("""[.,;:)\]]+$"""), "")   // trailing punctuation
//            .take(60)
//    }
//
//    private fun extractAppName(packageName: String): String {
//        return when (packageName) {
//            "com.phonepe.app" -> "PhonePe"
//            "com.google.android.apps.nbu.paisa.user" -> "Google Pay"
//            "net.one97.paytm" -> "Paytm"
//            "in.org.npci.upiapp" -> "BHIM UPI"
//            "com.amazon.mShop.android.shopping" -> "Amazon Pay"
//            "com.mobikwik_new" -> "MobiKwik"
//            "com.freecharge.android" -> "FreeCharge"
//            else -> "UPI Payment"
//        }
//    }
//}
package com.bluemix.cashio.data.notification

import com.bluemix.cashio.domain.model.ParsedSmsTransaction
import com.bluemix.cashio.domain.model.TransactionType
import java.time.LocalDateTime
import java.util.Locale

/**
 * Parses UPI/banking notifications from payment apps (PhonePe, GPay, Paytm, BHIM, Amazon Pay etc.)
 *
 * NOTE:
 * - This is heuristic parsing; keep it conservative to reduce false positives.
 * - If you later add a persistent dedupe key, include a stable "sourceId" for notifications.
 */
object NotificationParser {

    val UPI_APP_PACKAGES: Set<String> = setOf(
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "net.one97.paytm",
        "in.org.npci.upiapp",
        "com.amazon.mShop.android.shopping",
        "com.mobikwik_new",
        "com.freecharge.android"
    )

    // Amount patterns: ₹123, Rs. 123.45, INR 1,234.00
    private val amountPattern = Regex(
        pattern = """(?:(?:₹)|(?:rs\.?)|(?:inr))\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
        option = RegexOption.IGNORE_CASE
    )

    /**
     * Strong phrases that should override ambiguity.
     * These are common in GPay and should be treated as INCOME.
     */
    private val strongIncomePhrases = listOf(
        "paid you",
        "you received",
        "received money",
        "money received",
        "payment received",
        "credited to you",
        "credited in your",
        "added to your",
        "refund received"
    )

    /**
     * Strong phrases for EXPENSE.
     */
    private val strongExpensePhrases = listOf(
        "you paid",
        "you sent",
        "payment made",
        "debited from",
        "spent on"
    )

    private val expenseKeywords = listOf(
        "paid", "sent", "debited", "debit", "spent",
        "payment to", "paid to", "sent to", "txn to", "transfer to", "to vpa", "to upi"
    )

    private val incomeKeywords = listOf(
        "received", "credited", "credit", "refund",
        "payment from", "received from", "from vpa", "from upi"
    )

    private val negativeKeywords = listOf(
        "otp", "one time password", "verification",
        "cashback offer", "offer", "promo", "discount",
        "bill due", "due date", "statement", "reminder",
        "failed", "declined", "unsuccessful"
    )

    fun isUpiApp(packageName: String): Boolean = packageName in UPI_APP_PACKAGES

    fun isTransactionNotification(title: String, text: String): Boolean {
        val full = "$title $text".lowercase(Locale.getDefault())

        if (!amountPattern.containsMatchIn(full)) return false
        if (negativeKeywords.any { it in full }) return false

        // If it matches strong phrases, it’s definitely a transaction.
        if (strongIncomePhrases.any { it in full } || strongExpensePhrases.any { it in full }) return true

        val hasTypeKeyword = expenseKeywords.any { it in full } || incomeKeywords.any { it in full }
        return hasTypeKeyword
    }

    fun parseNotification(
        title: String,
        text: String,
        packageName: String,
        timestamp: LocalDateTime = LocalDateTime.now()
    ): ParsedSmsTransaction? {
        return try {
            val fullText = "$title $text".trim()
            if (fullText.isBlank()) return null

            val amount = extractAmount(fullText) ?: return null
            val transactionType = determineTransactionType(fullText) ?: return null

            val merchantName = extractMerchantName(fullText, transactionType)
            val bankName = extractAppName(packageName)

            ParsedSmsTransaction(
                amount = amount,
                transactionType = transactionType,
                merchantName = merchantName,
                accountNumber = null,
                timestamp = timestamp,
                rawSmsBody = fullText,
                bankName = bankName
            )
        } catch (_: Throwable) {
            null
        }
    }

    fun parseExpenseNotification(
        title: String,
        text: String,
        packageName: String,
        timestamp: LocalDateTime = LocalDateTime.now()
    ): ParsedSmsTransaction? {
        val tx = parseNotification(title, text, packageName, timestamp)
        return if (tx?.transactionType == TransactionType.EXPENSE) tx else null
    }

    fun getTransactionType(text: String): TransactionType? = determineTransactionType(text)

    private fun extractAmount(text: String): Double? {
        val match = amountPattern.find(text) ?: return null
        val raw = match.groupValues[1].replace(",", "")
        return raw.toDoubleOrNull()
    }

    private fun determineTransactionType(text: String): TransactionType? {
        val lower = text.lowercase(Locale.getDefault())

        if (negativeKeywords.any { it in lower }) return null

        //  Strong overrides first
        if (strongIncomePhrases.any { it in lower }) return TransactionType.INCOME
        if (strongExpensePhrases.any { it in lower }) return TransactionType.EXPENSE

        /**
         *  Key fix:
         * "paid you" contains "paid" so we must not let "paid" auto-map to EXPENSE.
         * Also in general, for notifications, prefer INCOME when both signals appear,
         * because "paid" often appears in credit messages.
         */
        val hasIncome = incomeKeywords.any { it in lower }
        val hasExpense = expenseKeywords.any { it in lower }

        return when {
            hasIncome && !hasExpense -> TransactionType.INCOME
            hasExpense && !hasIncome -> TransactionType.EXPENSE
            hasIncome && hasExpense -> {
                // Prefer INCOME when ambiguous (better for GPay phrasing).
                TransactionType.INCOME
            }
            else -> null
        }
    }

    private fun extractMerchantName(text: String, type: TransactionType): String? {
        val patterns = when (type) {
            TransactionType.EXPENSE -> listOf(
                Regex(
                    """(?:paid|payment|sent|txn|transfer)\s+(?:to)\s+([A-Za-z0-9@._\-\s]{2,60})""",
                    RegexOption.IGNORE_CASE
                ),
                Regex("""\bto\s+([A-Za-z0-9@._\-\s]{2,60})""", RegexOption.IGNORE_CASE),
                Regex("""(?:at)\s+([A-Za-z0-9@._\-\s]{2,60})""", RegexOption.IGNORE_CASE)
            )

            TransactionType.INCOME -> listOf(
                // ✅ NEW: "<merchant> paid you ..."
                Regex(
                    """([A-Za-z0-9@._\-\s]{2,60})\s+paid\s+you\b""",
                    RegexOption.IGNORE_CASE
                ),
                Regex(
                    """(?:received|credited|payment)\s+(?:from)\s+([A-Za-z0-9@._\-\s]{2,60})""",
                    RegexOption.IGNORE_CASE
                ),
                Regex("""\bfrom\s+([A-Za-z0-9@._\-\s]{2,60})""", RegexOption.IGNORE_CASE),
                Regex(
                    """(?:refund)\s+(?:from)\s+([A-Za-z0-9@._\-\s]{2,60})""",
                    RegexOption.IGNORE_CASE
                )
            )
        }

        for (p in patterns) {
            val m = p.find(text) ?: continue
            return sanitizeMerchant(m.groupValues[1])
        }

        val vpa = extractUpiId(text)
        if (vpa != null) return vpa

        return null
    }

    private fun extractUpiId(text: String): String? {
        val upiRegex = Regex("""\b([a-zA-Z0-9.\-_]{2,}@[a-zA-Z0-9.\-_]{2,})\b""")
        return upiRegex.find(text)?.groupValues?.get(1)
    }

    private fun sanitizeMerchant(raw: String): String {
        return raw
            .trim()
            .replace(Regex("""[\n\r\t]+"""), " ")
            .replace(Regex("""\s{2,}"""), " ")
            .replace(Regex("""[.,;:)\]]+$"""), "")
            .take(60)
    }

    private fun extractAppName(packageName: String): String {
        return when (packageName) {
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
}
