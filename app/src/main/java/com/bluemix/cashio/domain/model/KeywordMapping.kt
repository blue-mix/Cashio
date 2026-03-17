package com.bluemix.cashio.domain.model

/**
 * Domain model for a keyword-to-category mapping used for auto-categorising expenses.
 *
 * [keyword] matching is case-insensitive substring matching performed at the use-site
 * (see [KeywordMappingRepository.findCategoryForMerchant]).
 * Higher [priority] values are evaluated first; ties are broken by insertion order.
 */
data class KeywordMapping(
    val id: String,
    val keyword: String,
    val categoryId: String,
    val priority: Int = 5   // 1-10 scale; default mid-range
) {
    companion object {

        /**
         * Lazily-initialised default rules.
         *
         * ID scheme: "kw_<categoryId>_<descriptor>" — stable, human-readable,
         * immune to re-ordering unlike sequential numeric suffixes.
         *
         * Priority guide:
         *  10 — exact brand / app name (highest confidence)
         *   8 — strong generic keyword
         *   5 — medium generic (may match broadly)
         *   3 — weak/ambiguous (use sparingly)
         */
        val DEFAULT_KEYWORD_MAPPINGS: List<KeywordMapping> by lazy {
            listOf(
                // ── Groceries ─────────────────────────────────────────────
                KeywordMapping("kw_groceries_bigbasket", "bigbasket", "groceries", 10),
                KeywordMapping("kw_groceries_blinkit", "blinkit", "groceries", 10),
                KeywordMapping("kw_groceries_zepto", "zepto", "groceries", 10),
                KeywordMapping("kw_groceries_instamart", "instamart", "groceries", 10),
                KeywordMapping("kw_groceries_dmart", "dmart", "groceries", 10),
                KeywordMapping("kw_groceries_reliancefresh", "reliance fresh", "groceries", 10),
                KeywordMapping("kw_groceries_more", "more megastore", "groceries", 10),
                KeywordMapping("kw_groceries_starbazaar", "star bazaar", "groceries", 10),
                KeywordMapping("kw_groceries_spencer", "spencer", "groceries", 10),
                KeywordMapping("kw_groceries_generic", "grocery", "groceries", 5),
                KeywordMapping("kw_groceries_supermarket", "supermarket", "groceries", 5),

                // ── Dining Out ────────────────────────────────────────────
                KeywordMapping("kw_dining_swiggy", "swiggy", "dining_out", 10),
                KeywordMapping("kw_dining_zomato", "zomato", "dining_out", 10),
                KeywordMapping("kw_dining_dominos", "dominos", "dining_out", 10),
                KeywordMapping("kw_dining_mcdonalds", "mcdonald", "dining_out", 10),
                KeywordMapping("kw_dining_kfc", "kfc", "dining_out", 10),
                KeywordMapping("kw_dining_burgerking", "burger king", "dining_out", 10),
                KeywordMapping("kw_dining_pizzahut", "pizza hut", "dining_out", 10),
                KeywordMapping("kw_dining_subway", "subway", "dining_out", 10),
                KeywordMapping("kw_dining_restaurant", "restaurant", "dining_out", 5),
                KeywordMapping("kw_dining_fooddelivery", "food delivery", "dining_out", 5),

                // ── Coffee & Snacks ───────────────────────────────────────
                KeywordMapping("kw_coffee_starbucks", "starbucks", "coffee_snacks", 10),
                KeywordMapping("kw_coffee_ccd", "cafe coffee day", "coffee_snacks", 10),
                KeywordMapping("kw_coffee_ccd2", "ccd", "coffee_snacks", 10),
                KeywordMapping("kw_coffee_dunkin", "dunkin", "coffee_snacks", 10),
                KeywordMapping("kw_coffee_cafe", "cafe", "coffee_snacks", 5),
                KeywordMapping("kw_coffee_bakery", "bakery", "coffee_snacks", 5),

                // ── Fuel ──────────────────────────────────────────────────
                KeywordMapping("kw_fuel_petrolpump", "petrol pump", "fuel", 10),
                KeywordMapping("kw_fuel_indianoil", "indian oil", "fuel", 10),
                KeywordMapping("kw_fuel_bharatpetroleum", "bharat petroleum", "fuel", 10),
                KeywordMapping("kw_fuel_hp", "hp petrol", "fuel", 10),
                KeywordMapping("kw_fuel_shell", "shell", "fuel", 10),
                KeywordMapping("kw_fuel_petrol", "petrol", "fuel", 8),
                KeywordMapping("kw_fuel_diesel", "diesel", "fuel", 8),
                KeywordMapping("kw_fuel_cng", "cng", "fuel", 8),

                // ── Public Transport ──────────────────────────────────────
                KeywordMapping("kw_transport_metro", "metro", "public_transport", 10),
                KeywordMapping("kw_transport_dmrc", "dmrc", "public_transport", 10),
                KeywordMapping("kw_transport_railway", "railway", "public_transport", 10),
                KeywordMapping("kw_transport_irctc", "irctc", "public_transport", 10),
                KeywordMapping("kw_transport_busticket", "bus ticket", "public_transport", 10),
                KeywordMapping("kw_transport_localtrain", "local train", "public_transport", 8),

                // ── Taxi & Ride Share ─────────────────────────────────────
                KeywordMapping("kw_taxi_uber", "uber", "taxi_ride_share", 10),
                KeywordMapping("kw_taxi_ola", "ola", "taxi_ride_share", 10),
                KeywordMapping("kw_taxi_rapido", "rapido", "taxi_ride_share", 10),
                KeywordMapping("kw_taxi_nammayatri", "namma yatri", "taxi_ride_share", 10),
                KeywordMapping("kw_taxi_meru", "meru", "taxi_ride_share", 10),
                KeywordMapping("kw_taxi_generic", "taxi", "taxi_ride_share", 5),
                KeywordMapping("kw_taxi_cab", "cab", "taxi_ride_share", 5),

                // ── Parking & Tolls ───────────────────────────────────────
                KeywordMapping("kw_parking_fastag", "fastag", "parking", 10),
                KeywordMapping("kw_parking_toll", "toll", "parking", 10),
                KeywordMapping("kw_parking_generic", "parking", "parking", 10),
                KeywordMapping("kw_parking_parkplus", "park+", "parking", 10),

                // ── Vehicle Maintenance ───────────────────────────────────
                KeywordMapping(
                    "kw_vehicle_servicecenter",
                    "service center",
                    "vehicle_maintenance",
                    10
                ),
                KeywordMapping("kw_vehicle_carwash", "car wash", "vehicle_maintenance", 10),
                KeywordMapping("kw_vehicle_autorepair", "auto repair", "vehicle_maintenance", 10),
                KeywordMapping("kw_vehicle_tyre", "tyre", "vehicle_maintenance", 8),
                KeywordMapping("kw_vehicle_battery", "battery", "vehicle_maintenance", 8),

                // ── Rent / Mortgage ───────────────────────────────────────
                KeywordMapping("kw_rent_generic", "rent", "rent_mortgage", 10),
                KeywordMapping("kw_rent_housingsociety", "housing society", "rent_mortgage", 10),

                // ── Electricity ───────────────────────────────────────────
                KeywordMapping("kw_electricity_generic", "electricity", "electricity", 10),
                KeywordMapping("kw_electricity_adani", "adani electricity", "electricity", 10),
                KeywordMapping("kw_electricity_tata", "tata power", "electricity", 10),
                KeywordMapping("kw_electricity_bescom", "bescom", "electricity", 10),

                // ── Water ─────────────────────────────────────────────────
                KeywordMapping("kw_water_generic", "water bill", "water", 10),
                KeywordMapping("kw_water_municipal", "municipal", "water", 5),

                // ── Internet & Phone ──────────────────────────────────────
                KeywordMapping("kw_internet_airtel", "airtel", "internet_phone", 10),
                KeywordMapping("kw_internet_jio", "jio", "internet_phone", 10),
                KeywordMapping("kw_internet_vodafoneidea", "vodafone idea", "internet_phone", 10),
                KeywordMapping("kw_internet_bsnl", "bsnl", "internet_phone", 10),
                KeywordMapping("kw_internet_broadband", "broadband", "internet_phone", 10),
                KeywordMapping(
                    "kw_internet_mobilerecharge",
                    "mobile recharge",
                    "internet_phone",
                    8
                ),
                KeywordMapping("kw_internet_postpaid", "postpaid", "internet_phone", 8),
                KeywordMapping("kw_internet_prepaid", "prepaid", "internet_phone", 8),

                // ── Home Maintenance ──────────────────────────────────────
                KeywordMapping("kw_home_plumber", "plumber", "home_maintenance", 10),
                KeywordMapping("kw_home_electrician", "electrician", "home_maintenance", 10),
                KeywordMapping("kw_home_carpenter", "carpenter", "home_maintenance", 10),
                KeywordMapping("kw_home_urbancompany", "urban company", "home_maintenance", 10),

                // ── Clothing & Accessories ────────────────────────────────
                KeywordMapping("kw_clothing_myntra", "myntra", "clothing", 10),
                KeywordMapping("kw_clothing_ajio", "ajio", "clothing", 10),
                KeywordMapping("kw_clothing_hm", "h&m", "clothing", 10),
                KeywordMapping("kw_clothing_zara", "zara", "clothing", 10),
                KeywordMapping("kw_clothing_lifestyle", "lifestyle", "clothing", 10),
                KeywordMapping("kw_clothing_pantaloons", "pantaloons", "clothing", 10),
                KeywordMapping("kw_clothing_westside", "westside", "clothing", 10),
                KeywordMapping("kw_clothing_maxfashion", "max fashion", "clothing", 10),
                KeywordMapping("kw_clothing_generic", "clothing", "clothing", 5),

                // ── Electronics ───────────────────────────────────────────
                KeywordMapping("kw_electronics_croma", "croma", "electronics", 10),
                KeywordMapping(
                    "kw_electronics_reliancedigital",
                    "reliance digital",
                    "electronics",
                    10
                ),
                KeywordMapping("kw_electronics_vijaysales", "vijay sales", "electronics", 10),
                KeywordMapping("kw_electronics_apple", "apple store", "electronics", 10),
                KeywordMapping("kw_electronics_samsung", "samsung store", "electronics", 8),
                KeywordMapping("kw_electronics_generic", "electronics", "electronics", 5),

                // ── Personal Care ─────────────────────────────────────────
                KeywordMapping("kw_care_nykaa", "nykaa", "personal_care", 10),
                KeywordMapping("kw_care_purplle", "purplle", "personal_care", 10),
                KeywordMapping("kw_care_salon", "salon", "personal_care", 10),
                KeywordMapping("kw_care_spa", "spa", "personal_care", 10),

                // ── Movies & Shows ────────────────────────────────────────
                KeywordMapping("kw_movies_bookmyshow", "bookmyshow", "movies_shows", 10),
                KeywordMapping("kw_movies_pvr", "pvr", "movies_shows", 10),
                KeywordMapping("kw_movies_inox", "inox", "movies_shows", 10),
                KeywordMapping("kw_movies_cinepolis", "cinepolis", "movies_shows", 10),
                KeywordMapping("kw_movies_ticket", "movie ticket", "movies_shows", 10),
                KeywordMapping("kw_movies_cinema", "cinema", "movies_shows", 8),

                // ── Sports & Fitness ──────────────────────────────────────
                KeywordMapping("kw_fitness_gym", "gym", "sports_fitness", 10),
                KeywordMapping("kw_fitness_cultfit", "cult fit", "sports_fitness", 10),
                KeywordMapping("kw_fitness_fitnessfirst", "fitness first", "sports_fitness", 10),
                KeywordMapping("kw_fitness_goldsgym", "gold's gym", "sports_fitness", 10),
                KeywordMapping("kw_fitness_decathlon", "decathlon", "sports_fitness", 10),
                KeywordMapping("kw_fitness_yoga", "yoga", "sports_fitness", 8),

                // ── Gaming ────────────────────────────────────────────────
                KeywordMapping("kw_gaming_steam", "steam", "gaming", 10),
                KeywordMapping("kw_gaming_playstation", "playstation", "gaming", 10),
                KeywordMapping("kw_gaming_xbox", "xbox", "gaming", 10),
                KeywordMapping("kw_gaming_googleplay", "google play games", "gaming", 8),

                // ── Subscriptions ─────────────────────────────────────────
                KeywordMapping("kw_sub_netflix", "netflix", "subscriptions", 10),
                KeywordMapping("kw_sub_amazonprime", "amazon prime", "subscriptions", 10),
                KeywordMapping("kw_sub_hotstar", "hotstar", "subscriptions", 10),
                KeywordMapping("kw_sub_disneyplus", "disney+", "subscriptions", 10),
                KeywordMapping("kw_sub_spotify", "spotify", "subscriptions", 10),
                KeywordMapping("kw_sub_youtubepremium", "youtube premium", "subscriptions", 10),
                KeywordMapping("kw_sub_applemusic", "apple music", "subscriptions", 10),
                KeywordMapping("kw_sub_zee5", "zee5", "subscriptions", 10),
                KeywordMapping("kw_sub_sonyliv", "sonyliv", "subscriptions", 10),
                KeywordMapping("kw_sub_generic", "subscription", "subscriptions", 5),

                // ── Medical ───────────────────────────────────────────────
                KeywordMapping("kw_medical_apollo", "apollo hospital", "medical", 10),
                KeywordMapping("kw_medical_fortis", "fortis", "medical", 10),
                KeywordMapping("kw_medical_max", "max hospital", "medical", 10),
                KeywordMapping("kw_medical_hospital", "hospital", "medical", 8),
                KeywordMapping("kw_medical_doctor", "doctor", "medical", 8),
                KeywordMapping("kw_medical_clinic", "clinic", "medical", 8),
                KeywordMapping("kw_medical_practo", "practo", "medical", 10),

                // ── Pharmacy ──────────────────────────────────────────────
                KeywordMapping("kw_pharmacy_apollo", "apollo pharmacy", "pharmacy", 10),
                KeywordMapping("kw_pharmacy_netmeds", "netmeds", "pharmacy", 10),
                KeywordMapping("kw_pharmacy_1mg", "1mg", "pharmacy", 10),
                KeywordMapping("kw_pharmacy_pharmeasy", "pharmeasy", "pharmacy", 10),
                KeywordMapping("kw_pharmacy_medplus", "medplus", "pharmacy", 10),
                KeywordMapping("kw_pharmacy_generic", "pharmacy", "pharmacy", 8),
                KeywordMapping("kw_pharmacy_medicine", "medicine", "pharmacy", 8),

                // ── Dental ────────────────────────────────────────────────
                KeywordMapping("kw_dental_generic", "dental", "dental", 10),
                KeywordMapping("kw_dental_dentist", "dentist", "dental", 10),

                // ── Insurance ─────────────────────────────────────────────
                KeywordMapping("kw_insurance_lic", "lic", "insurance", 10),
                KeywordMapping("kw_insurance_hdfclife", "hdfc life", "insurance", 10),
                KeywordMapping("kw_insurance_icici", "icici prudential", "insurance", 10),
                KeywordMapping("kw_insurance_maxlife", "max life", "insurance", 10),
                KeywordMapping("kw_insurance_generic", "insurance", "insurance", 8),

                // ── Tuition & Courses ─────────────────────────────────────
                KeywordMapping("kw_tuition_byju", "byju", "tuition", 10),
                KeywordMapping("kw_tuition_unacademy", "unacademy", "tuition", 10),
                KeywordMapping("kw_tuition_upgrad", "upgrad", "tuition", 10),
                KeywordMapping("kw_tuition_coursera", "coursera", "tuition", 10),
                KeywordMapping("kw_tuition_udemy", "udemy", "tuition", 10),
                KeywordMapping("kw_tuition_generic", "tuition", "tuition", 8),

                // ── Learning Materials ────────────────────────────────────
                KeywordMapping("kw_learning_stationery", "stationery", "learning_materials", 8),

                // ── Investments ───────────────────────────────────────────
                KeywordMapping("kw_invest_zerodha", "zerodha", "investments", 10),
                KeywordMapping("kw_invest_groww", "groww", "investments", 10),
                KeywordMapping("kw_invest_upstox", "upstox", "investments", 10),
                KeywordMapping("kw_invest_mutualfund", "mutual fund", "investments", 10),
                KeywordMapping("kw_invest_sip", "sip", "investments", 10),

                // ── Loan Payment ──────────────────────────────────────────
                KeywordMapping("kw_loan_emi", "emi", "loan_payment", 10),
                KeywordMapping("kw_loan_generic", "loan repayment", "loan_payment", 8),
                KeywordMapping("kw_loan_creditcardbill", "credit card bill", "loan_payment", 10),

                // ── Taxes ─────────────────────────────────────────────────
                KeywordMapping("kw_tax_incometax", "income tax", "taxes", 10),
                KeywordMapping("kw_tax_gst", "gst payment", "taxes", 10),

                // ── Childcare ─────────────────────────────────────────────
                KeywordMapping("kw_childcare_schoolfee", "school fee", "childcare", 10),
                KeywordMapping("kw_childcare_daycare", "daycare", "childcare", 10),

                // ── Pets ──────────────────────────────────────────────────
                KeywordMapping("kw_pets_petshop", "pet shop", "pets", 10),
                KeywordMapping("kw_pets_veterinary", "veterinary", "pets", 10),
                KeywordMapping("kw_pets_petfood", "pet food", "pets", 10),

                // ── Vacation & Travel ─────────────────────────────────────
                KeywordMapping("kw_travel_makemytrip", "makemytrip", "vacation", 10),
                KeywordMapping("kw_travel_goibibo", "goibibo", "vacation", 10),
                KeywordMapping("kw_travel_cleartrip", "cleartrip", "vacation", 10),
                KeywordMapping("kw_travel_yatra", "yatra", "vacation", 10),
                KeywordMapping("kw_travel_indigo", "indigo", "vacation", 10),
                KeywordMapping("kw_travel_airindia", "air india", "vacation", 10),
                KeywordMapping("kw_travel_spicejet", "spicejet", "vacation", 10),
                KeywordMapping("kw_travel_flight", "flight booking", "vacation", 8),

                // ── Hotel & Accommodation ─────────────────────────────────
                KeywordMapping("kw_hotel_oyo", "oyo", "hotel", 10),
                KeywordMapping("kw_hotel_treebo", "treebo", "hotel", 10),
                KeywordMapping("kw_hotel_airbnb", "airbnb", "hotel", 10),
                KeywordMapping("kw_hotel_bookingcom", "booking.com", "hotel", 10),

                // ── Donations ─────────────────────────────────────────────
                KeywordMapping("kw_donation_generic", "donation", "donations", 10),
                KeywordMapping("kw_donation_charity", "charity", "donations", 10),
                KeywordMapping("kw_donation_ngo", "ngo", "donations", 8),

                // ── Gifts ─────────────────────────────────────────────────
                KeywordMapping("kw_gifts_generic", "gift", "gifts", 8),
                KeywordMapping("kw_gifts_fnp", "ferns n petals", "gifts", 10),
            )
        }

        /** Convenience accessor sorted by descending priority — ready for matching. */
        val DEFAULT_KEYWORD_MAPPINGS_SORTED: List<KeywordMapping> by lazy {
            DEFAULT_KEYWORD_MAPPINGS.sortedByDescending { it.priority }
        }
    }
}