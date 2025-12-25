package com.bluemix.cashio.core.util

import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import kotlin.reflect.KClass

/**
 * Realm utility extensions
 */

/**
 * Delete all objects of a type
 */
suspend fun <T : RealmObject> Realm.deleteAll(type: KClass<T>) {
    write {
        val objects = query(type).find()
        delete(objects)
    }
}

/**
 * Check if any object of type exists
 */
suspend fun <T : RealmObject> Realm.exists(type: KClass<T>): Boolean {
    return query(type).count().find() > 0
}

/**
 * Get count of objects
 */
suspend fun <T : RealmObject> Realm.count(type: KClass<T>): Long {
    return query(type).count().find()
}
