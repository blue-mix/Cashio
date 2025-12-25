//package com.bluemix.cashio.data.sms
//
//import com.bluemix.cashio.domain.model.ParsedSmsTransaction
//import com.bluemix.cashio.domain.model.TransactionType
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//import java.util.Locale
//
///**
// * Parse bank SMS messages to extract transaction data
// * Supports multiple bank formats
// */
//object SmsParser {
//
//    // Common regex patterns for Indian banks
//    private val amountPattern = """(?:Rs\.?|INR|₹)\s*([0-9,]+(?:\.[0-9]{2})?)""".toRegex(RegexOption.IGNORE_CASE)
//    private val debitKeywords = listOf("debited", "debit", "withdrawn", "paid", "spent", "purchase", "txn", "transaction")
//    private val creditKeywords = listOf("credited", "credit", "received", "deposited")
//    private val accountPattern = """(?:A/c|Account|AC)\s*(?:no\.?)?[:\s]*[xX*]*(\d{4})""".toRegex(RegexOption.IGNORE_CASE)
//
//    /**
//     * Parse SMS text to transaction
//     * Returns null if SMS is not a valid bank transaction
//     */
//    fun parseSms(smsBody: String, timestamp: LocalDateTime): ParsedSmsTransaction? {
//        return try {
//            // Extract amount
//            val amount = extractAmount(smsBody) ?: return null
//
//            // Determine transaction type
//            val transactionType = determineTransactionType(smsBody)
//            if (transactionType == TransactionType.UNKNOWN) return null
//
//            // Only process debit transactions (expenses)
//            if (transactionType != TransactionType.DEBIT) return null
//
//            // Extract merchant name
//            val merchantName = extractMerchantName(smsBody)
//
//            // Extract account number
//            val accountNumber = extractAccountNumber(smsBody)
//
//            // Extract bank name
//            val bankName = extractBankName(smsBody)
//
//            ParsedSmsTransaction(
//                amount = amount,
//                transactionType = transactionType,
//                merchantName = merchantName,
//                accountNumber = accountNumber,
//                timestamp = timestamp,
//                rawSmsBody = smsBody,
//                bankName = bankName
//            )
//        } catch (e: Exception) {
//            println("❌ Error parsing SMS: ${e.message}")
//            null  // Return null on error instead of crashing
//        }
//    }
//
//    /**
//     * Extract amount from SMS
//     */
//    private fun extractAmount(smsBody: String): Double? {
//        val match = amountPattern.find(smsBody) ?: return null
//        val amountStr = match.groupValues[1].replace(",", "")
//        return amountStr.toDoubleOrNull()
//    }
//
//    /**
//     * Determine if transaction is debit or credit
//     */
//    private fun determineTransactionType(smsBody: String): TransactionType {
//        val lowerBody = smsBody.lowercase(Locale.getDefault())
//
//        return when {
//            debitKeywords.any { it in lowerBody } -> TransactionType.DEBIT
//            creditKeywords.any { it in lowerBody } -> TransactionType.CREDIT
//            else -> TransactionType.UNKNOWN
//        }
//    }
//
//    /**
//     * Extract merchant/vendor name from SMS
//     */
//    private fun extractMerchantName(smsBody: String): String? {
//        // Try common patterns
//        val patterns = listOf(
//            """(?:at|to|towards)\s+([A-Z][A-Za-z\s&]{2,30})""".toRegex(),
//            """(?:paid to|sent to)\s+([A-Z][A-Za-z\s&]{2,30})""".toRegex(),
//            """(?:merchant|vendor):\s*([A-Za-z\s&]{2,30})""".toRegex(RegexOption.IGNORE_CASE)
//        )
//
//        for (pattern in patterns) {
//            val match = pattern.find(smsBody)
//            if (match != null) {
//                return match.groupValues[1].trim()
//            }
//        }
//
//        return null
//    }
//
//    /**
//     * Extract account number (last 4 digits)
//     */
//    private fun extractAccountNumber(smsBody: String): String? {
//        val match = accountPattern.find(smsBody) ?: return null
//        return match.groupValues[1]
//    }
//
//    /**
//     * Extract bank name from sender ID
//     */
//    private fun extractBankName(smsBody: String): String? {
//        val bankKeywords = mapOf(
//            "HDFC" to "HDFC Bank",
//            "ICICI" to "ICICI Bank",
//            "SBI" to "State Bank of India",
//            "AXIS" to "Axis Bank",
//            "KOTAK" to "Kotak Mahindra Bank",
//            "IDFC" to "IDFC First Bank",
//            "PNB" to "Punjab National Bank",
//            "BOB" to "Bank of Baroda",
//            "CANARA" to "Canara Bank",
//            "UNION" to "Union Bank"
//        )
//
//        val upperBody = smsBody.uppercase(Locale.getDefault())
//        for ((keyword, bankName) in bankKeywords) {
//            if (keyword in upperBody) {
//                return bankName
//            }
//        }
//
//        return null
//    }
//
//    /**
//     * Check if SMS looks like a bank transaction
//     */
//    fun isBankSms(smsBody: String): Boolean {
//        val lowerBody = smsBody.lowercase(Locale.getDefault())
//        val hasAmount = amountPattern.containsMatchIn(smsBody)
//        val hasTransactionKeyword = debitKeywords.any { it in lowerBody } || creditKeywords.any { it in lowerBody }
//
//        return hasAmount && hasTransactionKeyword
//    }
//}
package com.bluemix.cashio.data.sms

import android.util.Log
import com.bluemix.cashio.domain.model.ParsedSmsTransaction
import com.bluemix.cashio.domain.model.TransactionType
import java.time.LocalDateTime
import java.util.Locale

object SmsParser {

    private const val TAG = "SmsParser"

    private val excludeKeywords = listOf(
        "unsuccessful",
        "failed",
        "declined",
        "rejected",
        "refunded",
        "recharge now",
        "offer",
        "will be refunded"
    )

    fun parseSms(smsBody: String, timestamp: LocalDateTime): ParsedSmsTransaction? {
        Log.d(TAG, "🔍 Parsing: ${smsBody.take(100)}")

        if (shouldExclude(smsBody)) {
            return null
        }

        for ((index, pattern) in bankPatterns.withIndex()) {
            val match = pattern.regex.find(smsBody.replace("\n", " "))
            if (match != null) {
                Log.i(TAG, "✅ Pattern #${index + 1} matched")
                return pattern.groupExtractor(match, timestamp)
            }
        }

        Log.w(TAG, "❌ No pattern matched")
        return null
    }

    /**
     * FIXED: Better amount detection (catches numbers without Rs symbol too)
     */
    fun isBankSms(smsBody: String): Boolean {
        Log.v(TAG, "🔍 Checking SMS: ${smsBody.take(80)}...")

        if (shouldExclude(smsBody)) {
            return false
        }

        val lowerBody = smsBody.lowercase(Locale.getDefault())

        // FIXED: Multiple amount patterns
        val amountPatterns = listOf(
            Regex("""(?:rs\.?|inr|₹)\s*[\d,]+(?:\.\d+)?""", RegexOption.IGNORE_CASE),
            Regex("""[\d,]+\.?\d*\s+on\s+date""", RegexOption.IGNORE_CASE), // SBI format
            Regex("""debited\s+by\s+[\d,]+\.?\d*""", RegexOption.IGNORE_CASE),
            Regex("""credited\s+by\s+[\d,]+\.?\d*""", RegexOption.IGNORE_CASE)
        )

        val hasAmount = amountPatterns.any { it.containsMatchIn(smsBody) }

        val transactionKeywords = listOf(
            "debited", "credited", "paid", "received",
            "sent", "withdrawn", "transferred", "debit",
            "credit", "payment", "upi", "a/c", "account"
        )

        val hasTransaction = transactionKeywords.any { it in lowerBody }

        val isBankSender = Regex("""(sbi|hdfc|icici|axis|kotak|bank)""", RegexOption.IGNORE_CASE)
            .containsMatchIn(smsBody)

        Log.v(TAG, "  💰 Has amount: $hasAmount")
        Log.v(TAG, "  📝 Has transaction keyword: $hasTransaction")
        Log.v(TAG, "  🏦 Is bank sender: $isBankSender")

        val isBankSms = hasAmount && (hasTransaction || isBankSender)

        if (isBankSms) {
            Log.d(TAG, "🏦 DETECTED as bank SMS")
        } else {
            Log.v(TAG, "  ❌ NOT a bank SMS")
        }

        return isBankSms
    }

    private fun shouldExclude(smsBody: String): Boolean {
        val lowerBody = smsBody.lowercase(Locale.getDefault())
        val excluded = excludeKeywords.any { it in lowerBody }

        if (excluded) {
            val keyword = excludeKeywords.first { it in lowerBody }
            Log.d(TAG, "🚫 Excluded: '$keyword'")
        }

        return excluded
    }
}

// Keep your existing bankPatterns list here...

/**
 * Bank pattern definition with extractor function
 */
data class BankPattern(
    val regex: Regex,
    val bankName: String,
    val type: TransactionType,
    val groupExtractor: (MatchResult, LocalDateTime) -> ParsedSmsTransaction?
)

/**
 * Extensible list of bank patterns
 * Add new patterns here as you encounter new banks
 */
val bankPatterns = listOf(

    // ========== SBI ==========

    // SBI Debit (your original pattern)
    BankPattern(
        regex = Regex(
            """A/C\s+(\w+)\s+debited\s+by\s+([\d,]+(?:\.\d+)?)\s+on\s+date\s+(\d{1,2}[A-Za-z]{3}\d{2})\s+trf\s+to\s+([\w\s]+)\s+Refno\s+(\d+)""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "State Bank of India",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp ->
            val (account, amount, dateStr, merchant, refNo) = match.destructured
            ParsedSmsTransaction(
                amount = amount.replace(",", "").toDouble(),
                transactionType = TransactionType.EXPENSE,
                merchantName = merchant.trim(),
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "State Bank of India"
            )
        }
    ),

// ========== SBI UPI Pattern (Your SMS Format) ==========
    BankPattern(
        regex = Regex(
            """A/C\s+(\w+)\s+debited\s+by\s+([\d,]+(?:\.\d+)?)\s+on\s+date\s+(\w+)\s+trf\s+to\s+([\w\s]+)\s+Refno\s+(\d+)""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "State Bank of India",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp ->
            val groups = match.groupValues
            val account = groups[1]
            val amount = groups[2].replace(",", "").toDouble()
            val dateStr = groups[3]
            val merchant = groups[4].trim()
            val refNo = groups[5]

            ParsedSmsTransaction(
                amount = amount,
                transactionType = TransactionType.EXPENSE,
                merchantName = merchant,
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "State Bank of India"
            )
        }
    ),

    // SBI Credit (Government DBT)
    BankPattern(
        regex = Regex(
            """payment\s+of\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+credited\s+to\s+your\s+Acc\s+No\.?\s*\w*(\d{4,})""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "State Bank of India",
        type = TransactionType.INCOME,
        groupExtractor = { match, timestamp ->
            val (amount, account) = match.destructured
            ParsedSmsTransaction(
                amount = amount.replace(",", "").toDouble(),
                transactionType = TransactionType.INCOME,
                merchantName = "Govt DBT",
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "State Bank of India"
            )
        }
    ),

    // SBI NEFT Credit
    BankPattern(
        regex = Regex(
            """INR\s+([\d,]+(?:\.\d+)?)\s+credited\s+to\s+your\s+A/c\s+No\s+\w*(\d{4,}).*?by\s+([\w\s.]+)""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "State Bank of India",
        type = TransactionType.INCOME,
        groupExtractor = { match, timestamp ->
            val (amount, account, merchant) = match.destructured
            ParsedSmsTransaction(
                amount = amount.replace(",", "").toDouble(),
                transactionType = TransactionType.INCOME,
                merchantName = merchant.trim(),
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "State Bank of India"
            )
        }
    ),

    // ========== HDFC ==========

    // HDFC UPI Debit
    BankPattern(
        regex = Regex(
            """Sent\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+From\s+HDFC\s+Bank\s+A/C\s+(\w+).*?To\s+(.*?)\s+On""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ),
        bankName = "HDFC Bank",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp ->
            val (amount, account, merchant) = match.destructured
            ParsedSmsTransaction(
                amount = amount.replace(",", "").toDouble(),
                transactionType = TransactionType.EXPENSE,
                merchantName = merchant.trim(),
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "HDFC Bank"
            )
        }
    ),

    // HDFC UPI Credit
    BankPattern(
        regex = Regex(
            """Rs\.?\s*([\d,]+(?:\.\d+)?)\s+credited\s+to\s+HDFC\s+Bank\s+A/c\s+(\w+).*?from\s+VPA\s+([\w@.]+)""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "HDFC Bank",
        type = TransactionType.INCOME,
        groupExtractor = { match, timestamp ->
            val (amount, account, merchant) = match.destructured
            ParsedSmsTransaction(
                amount = amount.replace(",", "").toDouble(),
                transactionType = TransactionType.INCOME,
                merchantName = merchant.trim(),
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "HDFC Bank"
            )
        }
    ),

    // ========== KOTAK ==========

    // Kotak UPI Debit
    BankPattern(
        regex = Regex(
            """Sent\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+from\s+Kotak\s+Bank\s+AC\s+(\w+)\s+to\s+([\w@.]+)""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "Kotak Mahindra Bank",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp ->
            val (amount, account, merchant) = match.destructured
            ParsedSmsTransaction(
                amount = amount.replace(",", "").toDouble(),
                transactionType = TransactionType.EXPENSE,
                merchantName = merchant.trim(),
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "Kotak Mahindra Bank"
            )
        }
    ),

    // Kotak UPI Credit
    BankPattern(
        regex = Regex(
            """Received\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+in\s+your\s+Kotak\s+Bank\s+AC\s+(\w+)\s+from\s+([\w@.]+)""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "Kotak Mahindra Bank",
        type = TransactionType.INCOME,
        groupExtractor = { match, timestamp ->
            val (amount, account, merchant) = match.destructured
            ParsedSmsTransaction(
                amount = amount.replace(",", "").toDouble(),
                transactionType = TransactionType.INCOME,
                merchantName = merchant.trim(),
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "Kotak Mahindra Bank"
            )
        }
    ),

    // ========== GENERIC UPI PATTERNS (Fallback) ==========

    // Generic UPI Debit (catches most formats)
    BankPattern(
        regex = Regex(
            """(?:debited|paid|sent)\s+(?:Rs\.?|INR)?\s*([\d,]+(?:\.\d+)?)\s+(?:from|to)\s+(?:.*?)\s+(?:a/c|account|AC)\s*\w*(\d{4})""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ),
        bankName = "Bank",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp ->
            val (amount, account) = match.destructured
            ParsedSmsTransaction(
                amount = amount.replace(",", "").toDouble(),
                transactionType = TransactionType.EXPENSE,
                merchantName = "UPI Payment",
                accountNumber = account,
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = null
            )
        }
    )
)
