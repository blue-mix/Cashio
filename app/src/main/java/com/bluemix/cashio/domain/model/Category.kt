package com.bluemix.cashio.domain.model

/**
 * Domain model for an expense category.
 *
 * Color is stored as an ARGB [Long] (e.g. 0xFF4CAF50L) to keep the domain layer
 * free of any UI/Compose dependencies. Conversion to
 * [androidx.compose.ui.graphics.Color] happens exclusively in the presentation layer.
 */
data class Category(
    val id: String,
    val name: String,
    val icon: String,           // Emoji or material icon name
    val colorHex: Long,         // ARGB packed as Long, e.g. 0xFF4CAF50L
    val isDefault: Boolean = false
) {
    companion object {

        // ------------------------------------------------------------------ //
        // Palette — reusable color constants keep category definitions DRY    //
        // and make duplicate-color issues visible at a glance.                //
        // ------------------------------------------------------------------ //
        private const val GREEN = 0xFF4CAF50L
        private const val DEEP_ORANGE = 0xFFFF5722L
        private const val BROWN = 0xFF795548L
        private const val ORANGE = 0xFFFF9800L
        private const val BLUE = 0xFF2196F3L
        private const val AMBER = 0xFFFFC107L
        private const val BLUE_GREY = 0xFF607D8BL
        private const val GREY = 0xFF9E9E9EL
        private const val INDIGO = 0xFF3F51B5L
        private const val YELLOW = 0xFFFFEB3BL
        private const val LIGHT_BLUE = 0xFF03A9F4L
        private const val CYAN = 0xFF00BCD4L
        private const val LIGHT_GREEN = 0xFF8BC34AL
        private const val PINK = 0xFFE91E63L
        private const val DEEP_PURPLE = 0xFF673AB7L
        private const val PURPLE = 0xFF9C27B0L
        private const val LIGHT_PINK = 0xFFF06292L
        private const val RED = 0xFFF44336L
        private const val TEAL = 0xFF009688L

        // ------------------------------------------------------------------ //
        // Stable default — looked up from DEFAULT_CATEGORIES, never           //
        // constructed separately to avoid a duplicate "other" entry.          //
        // ------------------------------------------------------------------ //
        fun default(): Category =
            DEFAULT_CATEGORIES.first { it.id == "other" }

        /**
         * Lazily-initialised, immutable list.
         * Constructed once per process lifetime; never rebuilt on repeated calls.
         */
        val DEFAULT_CATEGORIES: List<Category> by lazy {
            listOf(
                // ── Food & Dining ──────────────────────────────────────────
                Category("groceries", "Groceries", "🛒", GREEN, true),
                Category("dining_out", "Dining Out", "🍽️", DEEP_ORANGE, true),
                Category("coffee_snacks", "Coffee & Snacks", "☕", BROWN, true),

                // ── Transportation ─────────────────────────────────────────
                Category("fuel", "Fuel", "⛽", ORANGE, true),
                Category("public_transport", "Public Transport", "🚇", BLUE, true),
                Category("taxi_ride_share", "Taxi & Ride Share", "🚕", AMBER, true),
                Category("vehicle_maintenance", "Vehicle Maintenance", "🔧", BLUE_GREY, true),
                Category("parking", "Parking & Tolls", "🅿️", GREY, true),

                // ── Housing & Utilities ────────────────────────────────────
                Category("rent_mortgage", "Rent/Mortgage", "🏠", INDIGO, true),
                Category("electricity", "Electricity", "💡", YELLOW, true),
                Category("water", "Water", "💧", LIGHT_BLUE, true),
                Category("internet_phone", "Internet & Phone", "📱", CYAN, true),
                Category("home_maintenance", "Home Maintenance", "🛠️", LIGHT_GREEN, true),

                // ── Shopping ───────────────────────────────────────────────
                Category("clothing", "Clothing & Accessories", "👔", PINK, true),
                Category("electronics", "Electronics", "💻", DEEP_PURPLE, true),
                Category("home_goods", "Home & Furniture", "🛋️", PURPLE, true),
                Category("personal_care", "Personal Care", "💄", LIGHT_PINK, true),

                // ── Entertainment & Lifestyle ──────────────────────────────
                Category("movies_shows", "Movies & Shows", "🎬", DEEP_PURPLE, true),
                Category("sports_fitness", "Sports & Fitness", "⚽", TEAL, true),
                Category("hobbies", "Hobbies", "🎨", ORANGE, true),
                Category("books_music", "Books & Music", "📚", PURPLE, true),
                Category("gaming", "Gaming", "🎮", INDIGO, true),
                Category("subscriptions", "Subscriptions", "📺", DEEP_ORANGE, true),

                // ── Health & Wellness ──────────────────────────────────────
                Category("medical", "Medical & Doctor", "⚕️", RED, true),
                Category("pharmacy", "Pharmacy & Medicine", "💊", PINK, true),
                Category("dental", "Dental", "🦷", CYAN, true),
                Category("insurance", "Insurance", "🛡️", TEAL, true),

                // ── Education & Development ────────────────────────────────
                Category("tuition", "Tuition & Courses", "🎓", INDIGO, true),
                Category("learning_materials", "Learning Materials", "📖", DEEP_PURPLE, true),

                // ── Financial ──────────────────────────────────────────────
                Category("savings", "Savings", "💰", GREEN, true),
                Category("investments", "Investments", "📈", TEAL, true),
                Category("loan_payment", "Loan Payment", "💳", DEEP_ORANGE, true),
                Category("taxes", "Taxes", "📋", BROWN, true),

                // ── Family & Pets ──────────────────────────────────────────
                Category("childcare", "Childcare & Education", "👶", YELLOW, true),
                Category("pets", "Pets", "🐾", LIGHT_GREEN, true),

                // ── Travel ─────────────────────────────────────────────────
                Category("vacation", "Vacation & Travel", "✈️", BLUE, true),
                Category("hotel", "Hotel & Accommodation", "🏨", CYAN, true),

                // ── Giving ─────────────────────────────────────────────────
                Category("donations", "Donations & Charity", "🤲", PINK, true),
                Category("gifts", "Gifts", "🎁", LIGHT_PINK, true),

                // ── Income ─────────────────────────────────────────────────
                Category("salary", "Salary", "💼", GREEN, true),
                Category("freelance", "Freelance", "🖥️", TEAL, true),
                Category("business", "Business", "🏢", INDIGO, true),
                Category("rental_income", "Rental Income", "🏘️", LIGHT_GREEN, true),

                // ── Miscellaneous ──────────────────────────────────────────
                Category("other", "Other", "📦", GREY, true),
            )
        }

        /** Convenience map for O(1) lookup by id. */
        val DEFAULT_CATEGORIES_BY_ID: Map<String, Category> by lazy {
            DEFAULT_CATEGORIES.associateBy { it.id }
        }
    }
}