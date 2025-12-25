package com.bluemix.cashio.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model for expense category
 */
data class Category(
    val id: String,
    val name: String,
    val icon: String,  // Material icon name or emoji
    val color: Color,
    val isDefault: Boolean = false
) {
    companion object {
        // Default categories for seeding
        fun getDefaultCategories(): List<Category> = listOf(
            // Food & Dining
            Category(
                id = "groceries",
                name = "Groceries",
                icon = "🛒",
                color = Color(0xFF4CAF50), // Green
                isDefault = true
            ),
            Category(
                id = "dining_out",
                name = "Dining Out",
                icon = "🍽️",
                color = Color(0xFFFF5722), // Deep Orange
                isDefault = true
            ),
            Category(
                id = "coffee_snacks",
                name = "Coffee & Snacks",
                icon = "☕",
                color = Color(0xFF795548), // Brown
                isDefault = true
            ),

            // Transportation
            Category(
                id = "fuel",
                name = "Fuel",
                icon = "⛽",
                color = Color(0xFFFF9800), // Orange
                isDefault = true
            ),
            Category(
                id = "public_transport",
                name = "Public Transport",
                icon = "🚇",
                color = Color(0xFF2196F3), // Blue
                isDefault = true
            ),
            Category(
                id = "taxi_ride_share",
                name = "Taxi & Ride Share",
                icon = "🚕",
                color = Color(0xFFFFC107), // Amber
                isDefault = true
            ),
            Category(
                id = "vehicle_maintenance",
                name = "Vehicle Maintenance",
                icon = "🔧",
                color = Color(0xFF607D8B), // Blue Grey
                isDefault = true
            ),
            Category(
                id = "parking",
                name = "Parking & Tolls",
                icon = "🅿️",
                color = Color(0xFF9E9E9E), // Grey
                isDefault = true
            ),

            // Housing & Utilities
            Category(
                id = "rent_mortgage",
                name = "Rent/Mortgage",
                icon = "🏠",
                color = Color(0xFF3F51B5), // Indigo
                isDefault = true
            ),
            Category(
                id = "electricity",
                name = "Electricity",
                icon = "💡",
                color = Color(0xFFFFEB3B), // Yellow
                isDefault = true
            ),
            Category(
                id = "water",
                name = "Water",
                icon = "💧",
                color = Color(0xFF03A9F4), // Light Blue
                isDefault = true
            ),
            Category(
                id = "internet_phone",
                name = "Internet & Phone",
                icon = "📱",
                color = Color(0xFF00BCD4), // Cyan
                isDefault = true
            ),
            Category(
                id = "home_maintenance",
                name = "Home Maintenance",
                icon = "🛠️",
                color = Color(0xFF8BC34A), // Light Green
                isDefault = true
            ),

            // Shopping
            Category(
                id = "clothing",
                name = "Clothing & Accessories",
                icon = "👔",
                color = Color(0xFFE91E63), // Pink
                isDefault = true
            ),
            Category(
                id = "electronics",
                name = "Electronics",
                icon = "💻",
                color = Color(0xFF673AB7), // Deep Purple
                isDefault = true
            ),
            Category(
                id = "home_goods",
                name = "Home & Furniture",
                icon = "🛋️",
                color = Color(0xFF9C27B0), // Purple
                isDefault = true
            ),
            Category(
                id = "personal_care",
                name = "Personal Care",
                icon = "💄",
                color = Color(0xFFF06292), // Light Pink
                isDefault = true
            ),

            // Entertainment & Lifestyle
            Category(
                id = "movies_shows",
                name = "Movies & Shows",
                icon = "🎬",
                color = Color(0xFFE91E63), // Pink
                isDefault = true
            ),
            Category(
                id = "sports_fitness",
                name = "Sports & Fitness",
                icon = "⚽",
                color = Color(0xFF4CAF50), // Green
                isDefault = true
            ),
            Category(
                id = "hobbies",
                name = "Hobbies",
                icon = "🎨",
                color = Color(0xFFFF9800), // Orange
                isDefault = true
            ),
            Category(
                id = "books_music",
                name = "Books & Music",
                icon = "📚",
                color = Color(0xFF9C27B0), // Purple
                isDefault = true
            ),
            Category(
                id = "gaming",
                name = "Gaming",
                icon = "🎮",
                color = Color(0xFF3F51B5), // Indigo
                isDefault = true
            ),
            Category(
                id = "subscriptions",
                name = "Subscriptions",
                icon = "📺",
                color = Color(0xFFFF5722), // Deep Orange
                isDefault = true
            ),

            // Health & Wellness
            Category(
                id = "medical",
                name = "Medical & Doctor",
                icon = "⚕️",
                color = Color(0xFFF44336), // Red
                isDefault = true
            ),
            Category(
                id = "pharmacy",
                name = "Pharmacy & Medicine",
                icon = "💊",
                color = Color(0xFFE91E63), // Pink
                isDefault = true
            ),
            Category(
                id = "dental",
                name = "Dental",
                icon = "🦷",
                color = Color(0xFF00BCD4), // Cyan
                isDefault = true
            ),
            Category(
                id = "insurance",
                name = "Insurance",
                icon = "🛡️",
                color = Color(0xFF009688), // Teal
                isDefault = true
            ),

            // Education & Development
            Category(
                id = "tuition",
                name = "Tuition & Courses",
                icon = "🎓",
                color = Color(0xFF3F51B5), // Indigo
                isDefault = true
            ),
            Category(
                id = "learning_materials",
                name = "Learning Materials",
                icon = "📖",
                color = Color(0xFF673AB7), // Deep Purple
                isDefault = true
            ),

            // Financial
            Category(
                id = "savings",
                name = "Savings",
                icon = "💰",
                color = Color(0xFF4CAF50), // Green
                isDefault = true
            ),
            Category(
                id = "investments",
                name = "Investments",
                icon = "📈",
                color = Color(0xFF009688), // Teal
                isDefault = true
            ),
            Category(
                id = "loan_payment",
                name = "Loan Payment",
                icon = "💳",
                color = Color(0xFFFF5722), // Deep Orange
                isDefault = true
            ),
            Category(
                id = "taxes",
                name = "Taxes",
                icon = "📋",
                color = Color(0xFF795548), // Brown
                isDefault = true
            ),

            // Family & Pets
            Category(
                id = "childcare",
                name = "Childcare & Education",
                icon = "👶",
                color = Color(0xFFFFEB3B), // Yellow
                isDefault = true
            ),
            Category(
                id = "pets",
                name = "Pets",
                icon = "🐾",
                color = Color(0xFF8BC34A), // Light Green
                isDefault = true
            ),

            // Travel
            Category(
                id = "vacation",
                name = "Vacation & Travel",
                icon = "✈️",
                color = Color(0xFF2196F3), // Blue
                isDefault = true
            ),
            Category(
                id = "hotel",
                name = "Hotel & Accommodation",
                icon = "🏨",
                color = Color(0xFF00BCD4), // Cyan
                isDefault = true
            ),

            // Giving
            Category(
                id = "donations",
                name = "Donations & Charity",
                icon = "🤲",
                color = Color(0xFFE91E63), // Pink
                isDefault = true
            ),
            Category(
                id = "gifts",
                name = "Gifts",
                icon = "🎁",
                color = Color(0xFFF06292), // Light Pink
                isDefault = true
            ),

            // Miscellaneous
            Category(
                id = "other",
                name = "Other",
                icon = "📦",
                color = Color(0xFF9E9E9E), // Grey
                isDefault = true
            )
        )
    }
}
