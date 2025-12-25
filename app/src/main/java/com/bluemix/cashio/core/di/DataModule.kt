package com.bluemix.cashio.core.di

import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.data.repository.CategoryRepositoryImpl
import com.bluemix.cashio.data.repository.ExpenseRepositoryImpl
import com.bluemix.cashio.data.repository.KeywordMappingRepositoryImpl
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    // Realm Database
    single { RealmManager() }

    // DataStore
    single { UserPreferencesDataStore(androidContext()) }

    // Repositories
    single<CategoryRepository> {
        CategoryRepositoryImpl(get())
    }

    single<KeywordMappingRepository> {
        KeywordMappingRepositoryImpl(get())
    }

    single<ExpenseRepository> {
        ExpenseRepositoryImpl(
            realmManager = get(),
            categoryRepository = get(),
            keywordMappingRepository = get(),
            context = androidContext()
        )
    }
}
