package com.bluemix.cashio.data.sms

import com.bluemix.cashio.data.sms.SmsParser.BANK_PATTERNS
import com.bluemix.cashio.domain.model.ParsedSmsTransaction
import com.bluemix.cashio.domain.model.TransactionType
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.Locale

object SmsParser {

    // ── Exclusion filter ───────────────────────────────────────────────────

    private val EXCLUDE_KEYWORDS = listOf(
        "unsuccessful", "failed", "declined", "rejected",
        "refunded", "recharge now", "offer", "will be refunded",
        "request", "otp", "login"
    )

    // ── Amount patterns compiled once ──────────────────────────────────────

    private val AMOUNT_PATTERNS = listOf(
        Regex("""(?:rs\.?|inr|₹)\s*[\d,]+(?:\.\d+)?""", RegexOption.IGNORE_CASE),
        Regex("""debited\s+by\s+[\d,]+\.?\d*""", RegexOption.IGNORE_CASE),
        Regex("""credited\s+by\s+[\d,]+\.?\d*""", RegexOption.IGNORE_CASE)
    )

    private val TRANSACTION_KEYWORDS = listOf(
        "debited", "credited", "spent", "received", "sent",
        "withdrawn", "transferred", "payment", "upi", "a/c", "account"
    )

    private val BANK_SENDER_REGEX = Regex(
        """(sbi|hdfc|icici|axis|kotak|bank)""", RegexOption.IGNORE_CASE
    )

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Returns `true` if [smsBody] looks like a bank transaction notification.
     * Used as a fast pre-filter before attempting full regex parsing.
     */
    fun isBankSms(smsBody: String): Boolean {
        if (shouldExclude(smsBody)) return false
        val lower = smsBody.lowercase(Locale.getDefault())
        if (AMOUNT_PATTERNS.none { it.containsMatchIn(smsBody) }) return false
        return TRANSACTION_KEYWORDS.any { it in lower } || BANK_SENDER_REGEX.containsMatchIn(smsBody)
    }

    /**
     * Attempts to parse [smsBody] into a [ParsedSmsTransaction].
     *
     * Iterates [BANK_PATTERNS] in order — specific patterns must precede generic
     * fallback patterns (see ordering note on [BANK_PATTERNS]).
     *
     * Returns `null` if no pattern matches or if the body should be excluded.
     */
    fun parseSms(
        smsBody: String,
        timestamp: LocalDateTime,
        smsDateMillis: Long,
        smsAddress: String?
    ): ParsedSmsTransaction? {
        if (shouldExclude(smsBody)) return null
        val cleanBody = smsBody.replace("\n", " ")

        for (pattern in BANK_PATTERNS) {
            val match = pattern.regex.find(cleanBody) ?: continue
            return pattern.groupExtractor(match, timestamp, smsDateMillis, smsAddress)
                ?: continue   // extractor can return null on parse failure — try next pattern
        }
        return null
    }

    // ── Deduplication helpers ──────────────────────────────────────────────

    internal fun normalizeMerchant(input: String?): String {
        if (input.isNullOrBlank()) return "unknown"
        return input
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[^a-z0-9 @._-]"), "")
            .take(80)
    }

    /**
     * Builds a stable fingerprint used as the expense primary key.
     * Buckets [dateMillis] into 2-minute windows to tolerate delivery jitter.
     *
     * CAVEAT: Two genuinely different transactions to the same merchant for the
     * same amount within 2 minutes will collide. This is an accepted trade-off
     * documented here explicitly.
     */
    internal fun makeStrongSmsId(
        amountPaise: Long,
        merchantName: String?,
        dateMillis: Long,
        bucketMs: Long = 2 * 60 * 1_000L
    ): String {
        val merchant = normalizeMerchant(merchantName)
        val bucket = (dateMillis / bucketMs) * bucketMs
        return sha256("sms|${amountPaise}|${merchant}|${bucket}")
    }

    /**
     * SHA-256 hex digest. [MessageDigest] instances are not thread-safe so a
     * new one is obtained per call — [getInstance] uses a pool internally and
     * is fast relative to the digest computation itself.
     */
    internal fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private fun shouldExclude(smsBody: String): Boolean {
        val lower = smsBody.lowercase(Locale.getDefault())
        return EXCLUDE_KEYWORDS.any { it in lower }
    }

    /**
     * Parses an amount string like "1,23,456.78" into paise.
     * Returns `null` on any parse failure so callers can skip the pattern safely.
     */
    internal fun parseAmountToPaise(raw: String): Long? {
        val value = raw.replace(",", "").toDoubleOrNull() ?: return null
        if (value <= 0.0) return null
        return (value * 100).toLong()
    }

    // ── Bank patterns ──────────────────────────────────────────────────────

    /**
     * Ordered list of [BankPattern]s.
     *
     * ORDERING RULES:
     * 1. More-specific patterns before more-general ones.
     * 2. Where two patterns cover the same bank, the stricter date-format variant
     *    comes first (e.g. SBI "ddMMMYY" before the relaxed "\w+" variant).
     * 3. Generic UPI fallback is always last.
     *
     * This is a private val inside the object — it was previously a file-level val
     * accessible from the entire package, which was unintentional.
     */
    private val BANK_PATTERNS: List<BankPattern> = buildBankPatterns()
}

// ── Data class for pattern definitions ─────────────────────────────────────

data class BankPattern(
    val regex: Regex,
    val bankName: String,
    val type: TransactionType,
    val groupExtractor: (
        match: MatchResult,
        timestamp: LocalDateTime,
        smsDateMillis: Long,
        smsAddress: String?
    ) -> ParsedSmsTransaction?
)

// ── Pattern builder — keeps SmsParser object readable ──────────────────────

private fun buildBankPatterns(): List<BankPattern> = listOf(

    // ════════════════════════ SBI ═════════════════════════════════════════

    // SBI Debit — strict date format (ddMMMYY), must come BEFORE the relaxed variant.
    BankPattern(
        regex = Regex(
            """A/C\s+(\w+)\s+debited\s+by\s+([\d,]+(?:\.\d+)?)\s+on\s+date\s+(\d{1,2}[A-Za-z]{3}\d{2})\s+trf\s+to\s+([\w\s]+?)\s+Refno\s+(\d+)""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "State Bank of India",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp, smsDateMillis, smsAddress ->
            val account = match.groupValues[1]
            val amountPaise =
                SmsParser.parseAmountToPaise(match.groupValues[2]) ?: return@BankPattern null
            val merchant = match.groupValues[4].trim()
            ParsedSmsTransaction(
                smsId = SmsParser.makeStrongSmsId(amountPaise, merchant, smsDateMillis),
                smsDateMillis = smsDateMillis,
                smsAddress = smsAddress,
                amountPaise = amountPaise,
                transactionType = TransactionType.EXPENSE,
                merchantName = merchant,
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "State Bank of India"
            )
        }
    ),

    // SBI Debit — relaxed date format (\w+ matches any date token).
    // Intentionally after the strict variant — only catches what the strict one misses.
    BankPattern(
        regex = Regex(
            """A/C\s+(\w+)\s+debited\s+by\s+([\d,]+(?:\.\d+)?)\s+on\s+date\s+(\w+)\s+trf\s+to\s+([\w\s]+?)\s+Refno\s+(\d+)""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "State Bank of India",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp, smsDateMillis, smsAddress ->
            val account = match.groupValues[1]
            val amountPaise =
                SmsParser.parseAmountToPaise(match.groupValues[2]) ?: return@BankPattern null
            val merchant = match.groupValues[4].trim()
            ParsedSmsTransaction(
                smsId = SmsParser.makeStrongSmsId(amountPaise, merchant, smsDateMillis),
                smsDateMillis = smsDateMillis,
                smsAddress = smsAddress,
                amountPaise = amountPaise,
                transactionType = TransactionType.EXPENSE,
                merchantName = merchant,
                accountNumber = account.takeLast(4),
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "State Bank of India"
            )
        }
    ),

    // SBI Credit — government DBT
    BankPattern(
        regex = Regex(
            """payment\s+of\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+credited\s+to\s+your\s+Acc\s+No\.?\s*\w*(\d{4,})""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "State Bank of India",
        type = TransactionType.INCOME,
        groupExtractor = { match, timestamp, smsDateMillis, smsAddress ->
            val amountPaise =
                SmsParser.parseAmountToPaise(match.groupValues[1]) ?: return@BankPattern null
            val account = match.groupValues[2].takeLast(4)
            val merchant = "Govt DBT"
            ParsedSmsTransaction(
                smsId = SmsParser.makeStrongSmsId(amountPaise, merchant, smsDateMillis),
                smsDateMillis = smsDateMillis,
                smsAddress = smsAddress,
                amountPaise = amountPaise,
                transactionType = TransactionType.INCOME,
                merchantName = merchant,
                accountNumber = account,
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
        groupExtractor = { match, timestamp, smsDateMillis, smsAddress ->
            val amountPaise =
                SmsParser.parseAmountToPaise(match.groupValues[1]) ?: return@BankPattern null
            val account = match.groupValues[2].takeLast(4)
            val merchant = match.groupValues[3].trim()
            ParsedSmsTransaction(
                smsId = SmsParser.makeStrongSmsId(amountPaise, merchant, smsDateMillis),
                smsDateMillis = smsDateMillis,
                smsAddress = smsAddress,
                amountPaise = amountPaise,
                transactionType = TransactionType.INCOME,
                merchantName = merchant,
                accountNumber = account,
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "State Bank of India"
            )
        }
    ),

    // ════════════════════════ HDFC ════════════════════════════════════════

    // HDFC UPI Debit
    // NOTE: DOT_MATCHES_ALL used with non-greedy .*? — test against long/malformed SMS
    // bodies to verify no catastrophic backtracking.
    BankPattern(
        regex = Regex(
            """Sent\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+From\s+HDFC\s+Bank\s+A/C\s+(\w+).*?To\s+(.*?)\s+On""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ),
        bankName = "HDFC Bank",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp, smsDateMillis, smsAddress ->
            val amountPaise =
                SmsParser.parseAmountToPaise(match.groupValues[1]) ?: return@BankPattern null
            val account = match.groupValues[2].takeLast(4)
            val merchant = match.groupValues[3].trim()
            ParsedSmsTransaction(
                smsId = SmsParser.makeStrongSmsId(amountPaise, merchant, smsDateMillis),
                smsDateMillis = smsDateMillis,
                smsAddress = smsAddress,
                amountPaise = amountPaise,
                transactionType = TransactionType.EXPENSE,
                merchantName = merchant,
                accountNumber = account,
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
        groupExtractor = { match, timestamp, smsDateMillis, smsAddress ->
            val amountPaise =
                SmsParser.parseAmountToPaise(match.groupValues[1]) ?: return@BankPattern null
            val account = match.groupValues[2].takeLast(4)
            val merchant = match.groupValues[3].trim()
            ParsedSmsTransaction(
                smsId = SmsParser.makeStrongSmsId(amountPaise, merchant, smsDateMillis),
                smsDateMillis = smsDateMillis,
                smsAddress = smsAddress,
                amountPaise = amountPaise,
                transactionType = TransactionType.INCOME,
                merchantName = merchant,
                accountNumber = account,
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "HDFC Bank"
            )
        }
    ),

    // ════════════════════════ KOTAK ═══════════════════════════════════════

    // Kotak UPI Debit
    BankPattern(
        regex = Regex(
            """Sent\s+Rs\.?\s*([\d,]+(?:\.\d+)?)\s+from\s+Kotak\s+Bank\s+AC\s+(\w+)\s+to\s+([\w@.]+)""",
            RegexOption.IGNORE_CASE
        ),
        bankName = "Kotak Mahindra Bank",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp, smsDateMillis, smsAddress ->
            val amountPaise =
                SmsParser.parseAmountToPaise(match.groupValues[1]) ?: return@BankPattern null
            val account = match.groupValues[2].takeLast(4)
            val merchant = match.groupValues[3].trim()
            ParsedSmsTransaction(
                smsId = SmsParser.makeStrongSmsId(amountPaise, merchant, smsDateMillis),
                smsDateMillis = smsDateMillis,
                smsAddress = smsAddress,
                amountPaise = amountPaise,
                transactionType = TransactionType.EXPENSE,
                merchantName = merchant,
                accountNumber = account,
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
        groupExtractor = { match, timestamp, smsDateMillis, smsAddress ->
            val amountPaise =
                SmsParser.parseAmountToPaise(match.groupValues[1]) ?: return@BankPattern null
            val account = match.groupValues[2].takeLast(4)
            val merchant = match.groupValues[3].trim()
            ParsedSmsTransaction(
                smsId = SmsParser.makeStrongSmsId(amountPaise, merchant, smsDateMillis),
                smsDateMillis = smsDateMillis,
                smsAddress = smsAddress,
                amountPaise = amountPaise,
                transactionType = TransactionType.INCOME,
                merchantName = merchant,
                accountNumber = account,
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = "Kotak Mahindra Bank"
            )
        }
    ),

    // ════════════════════════ Generic UPI fallback ════════════════════════

    // Must be LAST — catches any UPI debit not matched above.
    BankPattern(
        regex = Regex(
            """(?:debited|paid|sent)\s+(?:Rs\.?|INR)?\s*([\d,]+(?:\.\d+)?)\s+(?:from|to)\s+(?:.*?)\s+(?:a/c|account|AC)\s*\w*(\d{4})""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ),
        bankName = "UPI",
        type = TransactionType.EXPENSE,
        groupExtractor = { match, timestamp, smsDateMillis, smsAddress ->
            val amountPaise =
                SmsParser.parseAmountToPaise(match.groupValues[1]) ?: return@BankPattern null
            val account = match.groupValues[2]
            val merchant = "UPI Payment"
            ParsedSmsTransaction(
                smsId = SmsParser.makeStrongSmsId(amountPaise, merchant, smsDateMillis),
                smsDateMillis = smsDateMillis,
                smsAddress = smsAddress,
                amountPaise = amountPaise,
                transactionType = TransactionType.EXPENSE,
                merchantName = merchant,
                accountNumber = account,
                timestamp = timestamp,
                rawSmsBody = match.value,
                bankName = null
            )
        }
    )
)