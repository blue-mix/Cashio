package com.bluemix.cashio.data.local.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Realm persistence entity for [com.bluemix.cashio.domain.model.KeywordMapping].
 *
 * [keyword] is indexed for faster case-insensitive substring searches via
 * Realm's CONTAINS[c] operator. Matching logic lives in the repository layer.
 */
class KeywordMappingEntity : RealmObject {

    @PrimaryKey
    var id: String = ""

    @Index
    var keyword: String = ""

    /** FK reference to [CategoryEntity.id]. */
    var categoryId: String = ""

    /**
     * Matching priority. Higher values are evaluated first.
     * Range: 1–10. Default seed value: 5.
     */
    var priority: Int = 5
}