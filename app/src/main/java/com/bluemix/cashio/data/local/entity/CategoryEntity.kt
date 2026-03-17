package com.bluemix.cashio.data.local.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Realm persistence entity for [com.bluemix.cashio.domain.model.Category].
 *
 * Color is stored as an ARGB [Int] — conversion to/from [Long] or
 * [androidx.compose.ui.graphics.Color] happens exclusively in the mapper layer.
 * This class has zero UI/Compose dependencies.
 */
class CategoryEntity : RealmObject {
    @PrimaryKey
    var id: String = ""

    var name: String = ""
    var icon: String = ""

    /** ARGB color packed as Int. Use [com.bluemix.cashio.data.local.mapper.toDomain] to convert. */
    var colorArgb: Int = 0

    var isDefault: Boolean = false

    /**
     * Explicit display sort order preserving the semantic grouping from
     * [com.bluemix.cashio.domain.model.Category.DEFAULT_CATEGORIES].
     * Alphabetical sorting destroys the Food / Transport / Housing grouping.
     */
    var sortOrder: Int = 0
}