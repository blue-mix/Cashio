//package com.bluemix.cashio.data.sms
//
//import android.util.Log
//import com.bluemix.cashio.domain.model.ParsedSmsTransaction
//import com.bluemix.cashio.domain.model.TransactionType
//import java.time.LocalDateTime
//import java.util.Locale
//
//object SmsParser {
//
//    private const val TAG = "SmsParser"
//
//    private val excludeKeywords = listOf(
//        "unsuccessful",
//        "failed",
//        "declined",
//        "rejected",
//        "refunded",
//        "recharge now",
//        "offer",
//        "will be refunded"
//    )
//
//    fun parseSms(smsBody: String, timestamp: LocalDateTime): ParsedSmsTransaction? {
//        Log.d(TAG, "🔍 Parsing: ${smsBody.take(100)}")
//
//        if (shouldExclude(smsBody)) {
//            return null
//        }
//
//        for ((index, pattern) in bankPatterns.withIndex()) {
//            val match = pattern.regex.find(smsBody.replace("\n", " "))
//            if (match != null) {
//                Log.i(TAG, "✅ Pattern #${index + 1} matched")
//                return pattern.groupExtractor(match, timestamp)
//            }
//        }
//
//        Log.w(TAG, "❌ No pattern matched")
//        return null
//    }
//
//    /**
//     * FIXED: Better amount detection (catches numbers without Rs symbol too)
//     */
//    fun isBankSms(smsBody: String): Boolean {
//        Log.v(TAG, "🔍 Checking SMS: ${smsBody.take(80)}...")
//
//        if (shouldExclude(smsBody)) {
//            return false
//        }
//
//        val lowerBody = smsBody.lowercase(Locale.getDefault())
//
//        // FIXED: Multiple amount patterns
//        val amountPatterns = listOf(
//            Regex("""(?:rs\.?|inr|₹)\s*[\d,]+(?:\.\d+)?""", RegexOption.IGNORE_CASE),
//            Regex("""[\d,]+\.?\d*\s+on\s+date""", RegexOption.IGNORE_CASE), // SBI format
//            Regex("""debited\s+by\s+[\d,]+\.?\d*""", RegexOption.IGNORE_CASE),
//            Regex("""credited\s+by\s+[\d,]+\.?\d*""", RegexOption.IGNORE_CASE)
//        )
//
//        val hasAmount = amountPatterns.any { it.containsMatchIn(smsBody) }
//
//        val transactionKeywords = listOf(
//            "debited", "credited", "paid", "received",
//            "sent", "withdrawn", "transferred", "debit",
//            "credit", "payment", "upi", "a/c", "account"
//        )
//
//        val hasTransaction = transactionKeywords.any { it in lowerBody }
//
//        val isBankSender = Regex("""(sbi|hdfc|icici|axis|kotak|bank)""", RegexOption.IGNORE_CASE)
//            .containsMatchIn(smsBody)
//
//        Log.v(TAG, "  💰 Has amount: $hasAmount")
//        Log.v(TAG, "  📝 Has transaction keyword: $hasTransaction")
//        Log.v(TAG, "  🏦 Is bank sender: $isBankSender")
//
//        val isBankSms = hasAmount && (hasTransaction || isBankSender)
//
//        if (isBankSms) {
//            Log.d(TAG, "🏦 DETECTED as bank SMS")
//        } else {
//            Log.v(TAG, "  ❌ NOT a bank SMS")
//        }
//
//        return isBankSms
//    }
//
//    private fun shouldExclude(smsBody: String): Boolean {
//        val lowerBody = smsBody.lowercase(Locale.getDefault())
//        val excluded = excludeKeywords.any { it in lowerBody }
//
//        if (excluded) {
//            val keyword = excludeKeywords.first { it in lowerBody }
//            Log.d(TAG, "🚫 Excluded: '$keyword'")
//        }
//
//        return excluded
//    }
//}
//
//// Keep your existing bankPatterns list here...
//
///**
// * Bank pattern definition with extractor function
// */
//data class BankPattern(
//    val regex: Regex,
//    val bankName: String,
//    val type: TransactionType,
//    val groupExtractor: (MatchResult, LocalDateTime) -> ParsedSmsTransaction?
//)
//
///**
// * Extensible list of bank patterns
// * Add new patterns here as you encounter new banks
// */
//val bankPatterns = listOf(
//
//    // ========== SBI ==========
//
//    // SBI Debit (your original pattern)
//    BankPattern(
//        regex = Regex(
//            """A/C\s+(\w+)\s+debited\s+by\s+([\d,]+(?:\.\d+)?)\s+on\s+date\s+(\d{1,2}[A-Za-z]{3}\d{2})\s+trf\s+to\s+([\w\s]+)\s+Refno\s+(\d+)""",
//            RegexOption.IGNORE_CASE
//        ),
//        bankName = "State Bank of India",
//        type = TransactionType.EXPENSE,
//        groupExtractor = { match, timestamp ->
//            val (account, amount, dateStr, merchant, refNo) = match.destructured
//            ParsedSmsTransaction(
//                amount = amount.replace(",", "").toDouble(),
//                transactionType = TransactionType.EXPENSE,
//                merchantName = merchant.trim(),
//                accountNumber = account.takeLast(4),
//                timestamp = timestamp,
//                rawSmsBody = match.value,
//                bankName = "State Bank of India"
//            )
//        }
//    ),
//
//// ========== SBI UPI Pattern (Your SMS Format) ==========
//    BankPattern(
//        regex = Regex(
//            """A/C\s+(\w+)\s+debited\s+by\s+([\d,]+(?:\.\d+)?)\s+on\s+date\s+(\w+)\s+trf\s+to\s+([\w\s]+)\s+Refno\s+(\d+)""",
//            RegexOption.IGNORE_CASE
//        ),
//        bankName = "State Bank of India",
//        type = TransactionType.EXPENSE,
//        groupExtractor = { match, timestamp ->
//            val groups = match.groupValues
//            val account = groups[1]
//            val amount = groups[2].replace(",", "").toDouble()
//            val dateStr = groups[3]
//            val merchant = groups[4].trim()
//            val refNo = groups[5]
//
//            ParsedSmsTransaction(
//                amount = amount,
//                transactionType = TransactionType.EXPENSE,
//                merchantName = merchant,
//                accountNumber = account.takeLast(4),
//                timestamp = timestamp,
//                rawSmsBody = match.value,
//                bankName = "State Bank of India"
//            )
//        }
//    ),
//
//    // SBI Credit (Government DBT)
//    BankPattern(
//        regex = Regex(
//            """payment\s+of\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+credited\s+to\s+your\s+Acc\s+No\.?\s*\w*(\d{4,})""",
//            RegexOption.IGNORE_CASE
//        ),
//        bankName = "State Bank of India",
//        type = TransactionType.INCOME,
//        groupExtractor = { match, timestamp ->
//            val (amount, account) = match.destructured
//            ParsedSmsTransaction(
//                amount = amount.replace(",", "").toDouble(),
//                transactionType = TransactionType.INCOME,
//                merchantName = "Govt DBT",
//                accountNumber = account.takeLast(4),
//                timestamp = timestamp,
//                rawSmsBody = match.value,
//                bankName = "State Bank of India"
//            )
//        }
//    ),
//
//    // SBI NEFT Credit
//    BankPattern(
//        regex = Regex(
//            """INR\s+([\d,]+(?:\.\d+)?)\s+credited\s+to\s+your\s+A/c\s+No\s+\w*(\d{4,}).*?by\s+([\w\s.]+)""",
//            RegexOption.IGNORE_CASE
//        ),
//        bankName = "State Bank of India",
//        type = TransactionType.INCOME,
//        groupExtractor = { match, timestamp ->
//            val (amount, account, merchant) = match.destructured
//            ParsedSmsTransaction(
//                amount = amount.replace(",", "").toDouble(),
//                transactionType = TransactionType.INCOME,
//                merchantName = merchant.trim(),
//                accountNumber = account.takeLast(4),
//                timestamp = timestamp,
//                rawSmsBody = match.value,
//                bankName = "State Bank of India"
//            )
//        }
//    ),
//
//    // ========== HDFC ==========
//
//    // HDFC UPI Debit
//    BankPattern(
//        regex = Regex(
//            """Sent\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+From\s+HDFC\s+Bank\s+A/C\s+(\w+).*?To\s+(.*?)\s+On""",
//            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
//        ),
//        bankName = "HDFC Bank",
//        type = TransactionType.EXPENSE,
//        groupExtractor = { match, timestamp ->
//            val (amount, account, merchant) = match.destructured
//            ParsedSmsTransaction(
//                amount = amount.replace(",", "").toDouble(),
//                transactionType = TransactionType.EXPENSE,
//                merchantName = merchant.trim(),
//                accountNumber = account.takeLast(4),
//                timestamp = timestamp,
//                rawSmsBody = match.value,
//                bankName = "HDFC Bank"
//            )
//        }
//    ),
//
//    // HDFC UPI Credit
//    BankPattern(
//        regex = Regex(
//            """Rs\.?\s*([\d,]+(?:\.\d+)?)\s+credited\s+to\s+HDFC\s+Bank\s+A/c\s+(\w+).*?from\s+VPA\s+([\w@.]+)""",
//            RegexOption.IGNORE_CASE
//        ),
//        bankName = "HDFC Bank",
//        type = TransactionType.INCOME,
//        groupExtractor = { match, timestamp ->
//            val (amount, account, merchant) = match.destructured
//            ParsedSmsTransaction(
//                amount = amount.replace(",", "").toDouble(),
//                transactionType = TransactionType.INCOME,
//                merchantName = merchant.trim(),
//                accountNumber = account.takeLast(4),
//                timestamp = timestamp,
//                rawSmsBody = match.value,
//                bankName = "HDFC Bank"
//            )
//        }
//    ),
//
//    // ========== KOTAK ==========
//
//    // Kotak UPI Debit
//    BankPattern(
//        regex = Regex(
//            """Sent\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+from\s+Kotak\s+Bank\s+AC\s+(\w+)\s+to\s+([\w@.]+)""",
//            RegexOption.IGNORE_CASE
//        ),
//        bankName = "Kotak Mahindra Bank",
//        type = TransactionType.EXPENSE,
//        groupExtractor = { match, timestamp ->
//            val (amount, account, merchant) = match.destructured
//            ParsedSmsTransaction(
//                amount = amount.replace(",", "").toDouble(),
//                transactionType = TransactionType.EXPENSE,
//                merchantName = merchant.trim(),
//                accountNumber = account.takeLast(4),
//                timestamp = timestamp,
//                rawSmsBody = match.value,
//                bankName = "Kotak Mahindra Bank"
//            )
//        }
//    ),
//
//    // Kotak UPI Credit
//    BankPattern(
//        regex = Regex(
//            """Received\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+in\s+your\s+Kotak\s+Bank\s+AC\s+(\w+)\s+from\s+([\w@.]+)""",
//            RegexOption.IGNORE_CASE
//        ),
//        bankName = "Kotak Mahindra Bank",
//        type = TransactionType.INCOME,
//        groupExtractor = { match, timestamp ->
//            val (amount, account, merchant) = match.destructured
//            ParsedSmsTransaction(
//                amount = amount.replace(",", "").toDouble(),
//                transactionType = TransactionType.INCOME,
//                merchantName = merchant.trim(),
//                accountNumber = account.takeLast(4),
//                timestamp = timestamp,
//                rawSmsBody = match.value,
//                bankName = "Kotak Mahindra Bank"
//            )
//        }
//    ),
//
//    // ========== GENERIC UPI PATTERNS (Fallback) ==========
//
//    // Generic UPI Debit (catches most formats)
//    BankPattern(
//        regex = Regex(
//            """(?:debited|paid|sent)\s+(?:Rs\.?|INR)?\s*([\d,]+(?:\.\d+)?)\s+(?:from|to)\s+(?:.*?)\s+(?:a/c|account|AC)\s*\w*(\d{4})""",
//            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
//        ),
//        bankName = "Bank",
//        type = TransactionType.EXPENSE,
//        groupExtractor = { match, timestamp ->
//            val (amount, account) = match.destructured
//            ParsedSmsTransaction(
//                amount = amount.replace(",", "").toDouble(),
//                transactionType = TransactionType.EXPENSE,
//                merchantName = "UPI Payment",
//                accountNumber = account,
//                timestamp = timestamp,
//                rawSmsBody = match.value,
//                bankName = null
//            )
//        }
//    )
//)
package com.bluemix.cashio.data.sms

import com.bluemix.cashio.domain.model.ParsedSmsTransaction
import com.bluemix.cashio.domain.model.TransactionType
import java.time.LocalDateTime
import java.util.Locale

object SmsParser {

    private const val TAG = "SmsParser"

    private val excludeKeywords = listOf(
        "unsuccessful", "failed", "declined", "rejected",
        "refunded", "recharge now", "offer", "will be refunded",
        "request", "otp", "login"
    )

    fun parseSms(smsBody: String, timestamp: LocalDateTime): ParsedSmsTransaction? {
        if (shouldExclude(smsBody)) return null

        // Try specific bank patterns first
        for (pattern in bankPatterns) {
            val match = pattern.regex.find(smsBody.replace("\n", " "))
            if (match != null) {
                return pattern.groupExtractor(match, timestamp)
            }
        }
        return null
    }

    fun isBankSms(smsBody: String): Boolean {
        if (shouldExclude(smsBody)) return false

        val lowerBody = smsBody.lowercase(Locale.getDefault())

        // 1. Must contain an amount
        val amountPatterns = listOf(
            Regex("""(?:rs\.?|inr|₹)\s*[\d,]+(?:\.\d+)?""", RegexOption.IGNORE_CASE),
            Regex("""debited\s+by\s+[\d,]+\.?\d*""", RegexOption.IGNORE_CASE),
            Regex("""credited\s+by\s+[\d,]+\.?\d*""", RegexOption.IGNORE_CASE)
        )
        if (amountPatterns.none { it.containsMatchIn(smsBody) }) return false

        // 2. Must contain transaction keywords OR be from a known Bank Sender ID
        val transactionKeywords = listOf(
            "debited", "credited", "spent", "received", "sent",
            "withdrawn", "transferred", "payment", "upi", "a/c", "account"
        )

        // Sender ID check (e.g. "HDFCBK", "SBIUPS") usually happens in Reader,
        // but checking body for bank names helps too.
        val isBankSender = Regex("""(sbi|hdfc|icici|axis|kotak|bank)""", RegexOption.IGNORE_CASE)
            .containsMatchIn(smsBody)

        val hasKeyword = transactionKeywords.any { it in lowerBody }

        return hasKeyword || isBankSender
    }

    private fun shouldExclude(smsBody: String): Boolean {
        val lowerBody = smsBody.lowercase(Locale.getDefault())
        return excludeKeywords.any { it in lowerBody }
    }
}

data class BankPattern(
    val regex: Regex,
    val bankName: String,
    val type: TransactionType,
    val groupExtractor: (MatchResult, LocalDateTime) -> ParsedSmsTransaction?
)

// Define your list of patterns (SBI, HDFC, Generic UPI)
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
    BankPattern(
        regex = Regex(
            """(?:debited|paid|sent)\s+(?:Rs\.?|INR)?\s*([\d,]+(?:\.\d+)?)\s+(?:from|to)\s+(?:.*?)\s+(?:a/c|account|AC)\s*\w*(\d{4})""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ),
        bankName = "UPI",
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