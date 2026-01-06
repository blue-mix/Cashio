package com.bluemix.cashio.data.local.entity

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Realm entity for Category
 * Optimized: color stored as Int (ARGB), not String
 */
class CategoryEntity : RealmObject {
    @PrimaryKey
    var id: String = ""

    var name: String = ""
    var icon: String = ""

    var colorArgb: Int = 0

    var isDefault: Boolean = false

    @Ignore
    var color: Color
        get() = Color(colorArgb)
        set(value) {
            colorArgb = value.toArgb()
        }
}
