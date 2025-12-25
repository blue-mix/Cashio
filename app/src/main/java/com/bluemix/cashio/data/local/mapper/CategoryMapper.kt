package com.bluemix.cashio.data.local.mapper

import com.bluemix.cashio.data.local.entity.CategoryEntity
import com.bluemix.cashio.domain.model.Category

/**
 * Convert between Category domain model and CategoryEntity
 */
fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isDefault = isDefault
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity().apply {
        id = this@toEntity.id
        name = this@toEntity.name
        icon = this@toEntity.icon
        color = this@toEntity.color
        isDefault = this@toEntity.isDefault
    }
}
