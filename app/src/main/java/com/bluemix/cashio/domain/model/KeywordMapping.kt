package com.bluemix.cashio.domain.model

/**
 * Domain model for keyword to category mapping
 * Used for auto-categorizing expenses from SMS
 */
data class KeywordMapping(
    val id: String,
    val keyword: String,
    val categoryId: String,
    val priority: Int = 0  // Higher priority = checked first
) {
    companion object {
        // Default keyword mappings for common merchants
        fun getDefaultKeywordMappings(): List<KeywordMapping> = listOf(
            // Groceries
            KeywordMapping("kw_g1", "bigbasket", "groceries", 10),
            KeywordMapping("kw_g2", "blinkit", "groceries", 10),
            KeywordMapping("kw_g3", "zepto", "groceries", 10),
            KeywordMapping("kw_g4", "instamart", "groceries", 10),
            KeywordMapping("kw_g5", "dmart", "groceries", 10),
            KeywordMapping("kw_g6", "reliance fresh", "groceries", 10),
            KeywordMapping("kw_g7", "more megastore", "groceries", 10),
            KeywordMapping("kw_g8", "star bazaar", "groceries", 10),
            KeywordMapping("kw_g9", "spencer", "groceries", 10),
            KeywordMapping("kw_g10", "grocery", "groceries", 5),
            KeywordMapping("kw_g11", "supermarket", "groceries", 5),

            // Dining Out
            KeywordMapping("kw_d1", "swiggy", "dining_out", 10),
            KeywordMapping("kw_d2", "zomato", "dining_out", 10),
            KeywordMapping("kw_d3", "dominos", "dining_out", 10),
            KeywordMapping("kw_d4", "mcdonald", "dining_out", 10),
            KeywordMapping("kw_d5", "kfc", "dining_out", 10),
            KeywordMapping("kw_d6", "burger king", "dining_out", 10),
            KeywordMapping("kw_d7", "pizza hut", "dining_out", 10),
            KeywordMapping("kw_d8", "subway", "dining_out", 10),
            KeywordMapping("kw_d9", "restaurant", "dining_out", 5),
            KeywordMapping("kw_d10", "food delivery", "dining_out", 5),
            KeywordMapping("kw_d11", "hotel", "dining_out", 3),
            KeywordMapping("kw_d12", "dine", "dining_out", 3),

            // Coffee & Snacks
            KeywordMapping("kw_c1", "starbucks", "coffee_snacks", 10),
            KeywordMapping("kw_c2", "cafe coffee day", "coffee_snacks", 10),
            KeywordMapping("kw_c3", "ccd", "coffee_snacks", 10),
            KeywordMapping("kw_c4", "dunkin", "coffee_snacks", 10),
            KeywordMapping("kw_c5", "cafe", "coffee_snacks", 5),
            KeywordMapping("kw_c6", "bakery", "coffee_snacks", 5),

            // Fuel
            KeywordMapping("kw_f1", "petrol pump", "fuel", 10),
            KeywordMapping("kw_f2", "indian oil", "fuel", 10),
            KeywordMapping("kw_f3", "bharat petroleum", "fuel", 10),
            KeywordMapping("kw_f4", "hp petrol", "fuel", 10),
            KeywordMapping("kw_f5", "shell", "fuel", 10),
            KeywordMapping("kw_f6", "petrol", "fuel", 8),
            KeywordMapping("kw_f7", "diesel", "fuel", 8),
            KeywordMapping("kw_f8", "fuel", "fuel", 8),
            KeywordMapping("kw_f9", "cng", "fuel", 8),

            // Public Transport
            KeywordMapping("kw_pt1", "metro", "public_transport", 10),
            KeywordMapping("kw_pt2", "dmrc", "public_transport", 10),
            KeywordMapping("kw_pt3", "railway", "public_transport", 10),
            KeywordMapping("kw_pt4", "irctc", "public_transport", 10),
            KeywordMapping("kw_pt5", "bus ticket", "public_transport", 10),
            KeywordMapping("kw_pt6", "local train", "public_transport", 8),
            KeywordMapping("kw_pt7", "transport", "public_transport", 3),

            // Taxi & Ride Share
            KeywordMapping("kw_t1", "uber", "taxi_ride_share", 10),
            KeywordMapping("kw_t2", "ola", "taxi_ride_share", 10),
            KeywordMapping("kw_t3", "rapido", "taxi_ride_share", 10),
            KeywordMapping("kw_t4", "namma yatri", "taxi_ride_share", 10),
            KeywordMapping("kw_t5", "meru", "taxi_ride_share", 10),
            KeywordMapping("kw_t6", "taxi", "taxi_ride_share", 5),
            KeywordMapping("kw_t7", "cab", "taxi_ride_share", 5),

            // Parking & Tolls
            KeywordMapping("kw_p1", "fastag", "parking", 10),
            KeywordMapping("kw_p2", "toll", "parking", 10),
            KeywordMapping("kw_p3", "parking", "parking", 10),
            KeywordMapping("kw_p4", "park+", "parking", 10),

            // Vehicle Maintenance
            KeywordMapping("kw_vm1", "service center", "vehicle_maintenance", 10),
            KeywordMapping("kw_vm2", "car wash", "vehicle_maintenance", 10),
            KeywordMapping("kw_vm3", "auto repair", "vehicle_maintenance", 10),
            KeywordMapping("kw_vm4", "tyre", "vehicle_maintenance", 8),
            KeywordMapping("kw_vm5", "battery", "vehicle_maintenance", 8),

            // Rent/Mortgage
            KeywordMapping("kw_r1", "rent", "rent_mortgage", 10),
            KeywordMapping("kw_r2", "housing society", "rent_mortgage", 10),
            KeywordMapping("kw_r3", "maintenance", "rent_mortgage", 5),

            // Electricity
            KeywordMapping("kw_e1", "electricity", "electricity", 10),
            KeywordMapping("kw_e2", "power", "electricity", 8),
            KeywordMapping("kw_e3", "adani electricity", "electricity", 10),
            KeywordMapping("kw_e4", "tata power", "electricity", 10),
            KeywordMapping("kw_e5", "bescom", "electricity", 10),

            // Water
            KeywordMapping("kw_w1", "water", "water", 10),
            KeywordMapping("kw_w2", "municipal", "water", 5),

            // Internet & Phone
            KeywordMapping("kw_i1", "airtel", "internet_phone", 10),
            KeywordMapping("kw_i2", "jio", "internet_phone", 10),
            KeywordMapping("kw_i3", "vi", "internet_phone", 10),
            KeywordMapping("kw_i4", "vodafone", "internet_phone", 10),
            KeywordMapping("kw_i5", "bsnl", "internet_phone", 10),
            KeywordMapping("kw_i6", "broadband", "internet_phone", 10),
            KeywordMapping("kw_i7", "internet", "internet_phone", 8),
            KeywordMapping("kw_i8", "mobile recharge", "internet_phone", 8),
            KeywordMapping("kw_i9", "recharge", "internet_phone", 5),
            KeywordMapping("kw_i10", "postpaid", "internet_phone", 8),
            KeywordMapping("kw_i11", "prepaid", "internet_phone", 8),

            // Home Maintenance
            KeywordMapping("kw_hm1", "plumber", "home_maintenance", 10),
            KeywordMapping("kw_hm2", "electrician", "home_maintenance", 10),
            KeywordMapping("kw_hm3", "carpenter", "home_maintenance", 10),
            KeywordMapping("kw_hm4", "urban company", "home_maintenance", 10),

            // Clothing & Accessories
            KeywordMapping("kw_cl1", "myntra", "clothing", 10),
            KeywordMapping("kw_cl2", "ajio", "clothing", 10),
            KeywordMapping("kw_cl3", "h&m", "clothing", 10),
            KeywordMapping("kw_cl4", "zara", "clothing", 10),
            KeywordMapping("kw_cl5", "lifestyle", "clothing", 10),
            KeywordMapping("kw_cl6", "pantaloons", "clothing", 10),
            KeywordMapping("kw_cl7", "westside", "clothing", 10),
            KeywordMapping("kw_cl8", "max fashion", "clothing", 10),
            KeywordMapping("kw_cl9", "clothing", "clothing", 5),
            KeywordMapping("kw_cl10", "apparel", "clothing", 5),

            // Electronics
            KeywordMapping("kw_el1", "croma", "electronics", 10),
            KeywordMapping("kw_el2", "reliance digital", "electronics", 10),
            KeywordMapping("kw_el3", "vijay sales", "electronics", 10),
            KeywordMapping("kw_el4", "apple store", "electronics", 10),
            KeywordMapping("kw_el5", "samsung", "electronics", 8),
            KeywordMapping("kw_el6", "oneplus", "electronics", 8),
            KeywordMapping("kw_el7", "electronics", "electronics", 5),

            // Shopping (General)
            KeywordMapping("kw_sh1", "amazon", "home_goods", 10),
            KeywordMapping("kw_sh2", "flipkart", "home_goods", 10),
            KeywordMapping("kw_sh3", "meesho", "home_goods", 10),
            KeywordMapping("kw_sh4", "snapdeal", "home_goods", 10),
            KeywordMapping("kw_sh5", "shopclues", "home_goods", 10),

            // Personal Care
            KeywordMapping("kw_pc1", "nykaa", "personal_care", 10),
            KeywordMapping("kw_pc2", "purplle", "personal_care", 10),
            KeywordMapping("kw_pc3", "salon", "personal_care", 10),
            KeywordMapping("kw_pc4", "spa", "personal_care", 10),
            KeywordMapping("kw_pc5", "cosmetics", "personal_care", 8),

            // Movies & Shows
            KeywordMapping("kw_m1", "bookmyshow", "movies_shows", 10),
            KeywordMapping("kw_m2", "pvr", "movies_shows", 10),
            KeywordMapping("kw_m3", "inox", "movies_shows", 10),
            KeywordMapping("kw_m4", "cinepolis", "movies_shows", 10),
            KeywordMapping("kw_m5", "movie ticket", "movies_shows", 10),
            KeywordMapping("kw_m6", "cinema", "movies_shows", 8),

            // Sports & Fitness
            KeywordMapping("kw_sf1", "gym", "sports_fitness", 10),
            KeywordMapping("kw_sf2", "cult fit", "sports_fitness", 10),
            KeywordMapping("kw_sf3", "fitness first", "sports_fitness", 10),
            KeywordMapping("kw_sf4", "gold's gym", "sports_fitness", 10),
            KeywordMapping("kw_sf5", "decathlon", "sports_fitness", 10),
            KeywordMapping("kw_sf6", "yoga", "sports_fitness", 8),
            KeywordMapping("kw_sf7", "sports", "sports_fitness", 5),

            // Gaming
            KeywordMapping("kw_gm1", "steam", "gaming", 10),
            KeywordMapping("kw_gm2", "playstation", "gaming", 10),
            KeywordMapping("kw_gm3", "xbox", "gaming", 10),
            KeywordMapping("kw_gm4", "google play", "gaming", 8),
            KeywordMapping("kw_gm5", "app store", "gaming", 5),

            // Subscriptions
            KeywordMapping("kw_sub1", "netflix", "subscriptions", 10),
            KeywordMapping("kw_sub2", "amazon prime", "subscriptions", 10),
            KeywordMapping("kw_sub3", "hotstar", "subscriptions", 10),
            KeywordMapping("kw_sub4", "disney+", "subscriptions", 10),
            KeywordMapping("kw_sub5", "spotify", "subscriptions", 10),
            KeywordMapping("kw_sub6", "youtube premium", "subscriptions", 10),
            KeywordMapping("kw_sub7", "apple music", "subscriptions", 10),
            KeywordMapping("kw_sub8", "zee5", "subscriptions", 10),
            KeywordMapping("kw_sub9", "sonyliv", "subscriptions", 10),
            KeywordMapping("kw_sub10", "subscription", "subscriptions", 5),

            // Medical & Doctor
            KeywordMapping("kw_md1", "apollo", "medical", 10),
            KeywordMapping("kw_md2", "fortis", "medical", 10),
            KeywordMapping("kw_md3", "max hospital", "medical", 10),
            KeywordMapping("kw_md4", "hospital", "medical", 8),
            KeywordMapping("kw_md5", "doctor", "medical", 8),
            KeywordMapping("kw_md6", "clinic", "medical", 8),
            KeywordMapping("kw_md7", "practo", "medical", 10),

            // Pharmacy & Medicine
            KeywordMapping("kw_ph1", "apollo pharmacy", "pharmacy", 10),
            KeywordMapping("kw_ph2", "netmeds", "pharmacy", 10),
            KeywordMapping("kw_ph3", "1mg", "pharmacy", 10),
            KeywordMapping("kw_ph4", "pharmeasy", "pharmacy", 10),
            KeywordMapping("kw_ph5", "medplus", "pharmacy", 10),
            KeywordMapping("kw_ph6", "pharmacy", "pharmacy", 8),
            KeywordMapping("kw_ph7", "medicine", "pharmacy", 8),

            // Dental
            KeywordMapping("kw_de1", "dental", "dental", 10),
            KeywordMapping("kw_de2", "dentist", "dental", 10),

            // Insurance
            KeywordMapping("kw_in1", "lic", "insurance", 10),
            KeywordMapping("kw_in2", "hdfc life", "insurance", 10),
            KeywordMapping("kw_in3", "icici prudential", "insurance", 10),
            KeywordMapping("kw_in4", "max life", "insurance", 10),
            KeywordMapping("kw_in5", "insurance", "insurance", 8),
            KeywordMapping("kw_in6", "premium", "insurance", 3),

            // Tuition & Courses
            KeywordMapping("kw_tu1", "byju", "tuition", 10),
            KeywordMapping("kw_tu2", "unacademy", "tuition", 10),
            KeywordMapping("kw_tu3", "upgrad", "tuition", 10),
            KeywordMapping("kw_tu4", "coursera", "tuition", 10),
            KeywordMapping("kw_tu5", "udemy", "tuition", 10),
            KeywordMapping("kw_tu6", "tuition", "tuition", 8),
            KeywordMapping("kw_tu7", "course", "tuition", 5),

            // Learning Materials
            KeywordMapping("kw_lm1", "amazon books", "learning_materials", 10),
            KeywordMapping("kw_lm2", "flipkart books", "learning_materials", 10),
            KeywordMapping("kw_lm3", "book", "learning_materials", 5),
            KeywordMapping("kw_lm4", "stationery", "learning_materials", 8),

            // Investments
            KeywordMapping("kw_inv1", "zerodha", "investments", 10),
            KeywordMapping("kw_inv2", "groww", "investments", 10),
            KeywordMapping("kw_inv3", "upstox", "investments", 10),
            KeywordMapping("kw_inv4", "mutual fund", "investments", 10),
            KeywordMapping("kw_inv5", "sip", "investments", 10),
            KeywordMapping("kw_inv6", "stock", "investments", 8),

            // Loan Payment
            KeywordMapping("kw_lp1", "emi", "loan_payment", 10),
            KeywordMapping("kw_lp2", "loan", "loan_payment", 8),
            KeywordMapping("kw_lp3", "credit card bill", "loan_payment", 10),

            // Taxes
            KeywordMapping("kw_tx1", "income tax", "taxes", 10),
            KeywordMapping("kw_tx2", "gst", "taxes", 10),
            KeywordMapping("kw_tx3", "tax", "taxes", 8),

            // Childcare
            KeywordMapping("kw_ch1", "school fee", "childcare", 10),
            KeywordMapping("kw_ch2", "daycare", "childcare", 10),
            KeywordMapping("kw_ch3", "toys", "childcare", 5),

            // Pets
            KeywordMapping("kw_pet1", "pet shop", "pets", 10),
            KeywordMapping("kw_pet2", "veterinary", "pets", 10),
            KeywordMapping("kw_pet3", "pet food", "pets", 10),

            // Vacation & Travel
            KeywordMapping("kw_vt1", "makemytrip", "vacation", 10),
            KeywordMapping("kw_vt2", "goibibo", "vacation", 10),
            KeywordMapping("kw_vt3", "cleartrip", "vacation", 10),
            KeywordMapping("kw_vt4", "yatra", "vacation", 10),
            KeywordMapping("kw_vt5", "flight", "vacation", 8),
            KeywordMapping("kw_vt6", "airline", "vacation", 8),
            KeywordMapping("kw_vt7", "indigo", "vacation", 10),
            KeywordMapping("kw_vt8", "air india", "vacation", 10),
            KeywordMapping("kw_vt9", "spicejet", "vacation", 10),

            // Hotel & Accommodation
            KeywordMapping("kw_ho1", "oyo", "hotel", 10),
            KeywordMapping("kw_ho2", "treebo", "hotel", 10),
            KeywordMapping("kw_ho3", "airbnb", "hotel", 10),
            KeywordMapping("kw_ho4", "booking.com", "hotel", 10),
            KeywordMapping("kw_ho5", "hotel booking", "hotel", 10),

            // Donations & Charity
            KeywordMapping("kw_dc1", "donation", "donations", 10),
            KeywordMapping("kw_dc2", "charity", "donations", 10),
            KeywordMapping("kw_dc3", "ngo", "donations", 8),

            // Gifts
            KeywordMapping("kw_gi1", "gift", "gifts", 8),
            KeywordMapping("kw_gi2", "flowers", "gifts", 5),
            KeywordMapping("kw_gi3", "ferns n petals", "gifts", 10)
        )
    }
}
