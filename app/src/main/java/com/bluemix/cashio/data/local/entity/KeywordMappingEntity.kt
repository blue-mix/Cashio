package com.bluemix.cashio.data.local.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Realm entity for Keyword Mapping
 */
class KeywordMappingEntity : RealmObject {
    @PrimaryKey
    var id: String = ""

    @Index  // Index for faster keyword searches
    var keyword: String = ""

    var categoryId: String = ""
    var priority: Int = 0
}
