package com.bluemix.cashio.data.local.mapper

import com.bluemix.cashio.data.local.entity.KeywordMappingEntity
import com.bluemix.cashio.domain.model.KeywordMapping

fun KeywordMappingEntity.toDomain(): KeywordMapping = KeywordMapping(
    id = id,
    keyword = keyword,
    categoryId = categoryId,
    priority = priority
)

fun KeywordMapping.toEntity(): KeywordMappingEntity = KeywordMappingEntity().apply {
    id = this@toEntity.id
    keyword = this@toEntity.keyword
    categoryId = this@toEntity.categoryId
    priority = this@toEntity.priority
}