package com.bluemix.cashio.data.local.mapper

import com.bluemix.cashio.data.local.entity.CategoryEntity
import com.bluemix.cashio.domain.model.Category

/**
 * Bi-directional mapper between [Category] domain model and [CategoryEntity].
 *
 * Color conversion:
 *  - Domain uses [Long] (e.g. 0xFF4CAF50L) — full ARGB, no sign issues.
 *  - Realm stores [Int] (ARGB packed) — lossy for values > Int.MAX_VALUE when
 *    interpreted as signed, but consistent because we always round-trip through
 *    [Int.toLong] with an unsigned mask.
 */

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    colorHex = colorArgb.toUInt().toLong(),   // unsigned → Long avoids sign-extension
    isDefault = isDefault
)

fun Category.toEntity(sortOrder: Int = 0): CategoryEntity = CategoryEntity().apply {
    id = this@toEntity.id
    name = this@toEntity.name
    icon = this@toEntity.icon
    colorArgb = (this@toEntity.colorHex and 0xFFFFFFFFL).toInt()  // Long → Int, preserving bits
    isDefault = this@toEntity.isDefault
    this.sortOrder = sortOrder
}