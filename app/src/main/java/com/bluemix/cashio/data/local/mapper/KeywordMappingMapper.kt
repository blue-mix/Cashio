package com.bluemix.cashio.data.local.mapper

import com.bluemix.cashio.data.local.entity.KeywordMappingEntity
import com.bluemix.cashio.domain.model.KeywordMapping

/**
 * Convert between KeywordMapping domain model and KeywordMappingEntity
 */
fun KeywordMappingEntity.toDomain(): KeywordMapping {
    return KeywordMapping(
        id = id,
        keyword = keyword,
        categoryId = categoryId,
        priority = priority
    )
}

fun KeywordMapping.toEntity(): KeywordMappingEntity {
    return KeywordMappingEntity().apply {
        id = this@toEntity.id
        keyword = this@toEntity.keyword
        categoryId = this@toEntity.categoryId
        priority = this@toEntity.priority
    }
}
