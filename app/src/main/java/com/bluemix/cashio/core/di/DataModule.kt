package com.bluemix.cashio.core.di

import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.data.repository.CategoryRepositoryImpl
import com.bluemix.cashio.data.repository.ExpenseRepositoryImpl
import com.bluemix.cashio.data.repository.KeywordMappingRepositoryImpl
import com.bluemix.cashio.data.repository.SeedRepositoryImpl
import com.bluemix.cashio.data.sms.SmsReader
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import com.bluemix.cashio.domain.repository.SeedRepository
import com.bluemix.cashio.domain.repository.UserPreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    // ── Infrastructure ─────────────────────────────────────────────────────

    /**
     * RealmManager is a singleton — multiple instances cause write conflicts.
     * [RealmManager.open] is called explicitly during app startup, not lazily here.
     */
    single { RealmManager() }

    single<UserPreferencesRepository> {
        UserPreferencesDataStore(androidContext())
    }

    single { SmsReader(androidContext()) }

    // ── Repositories ───────────────────────────────────────────────────────

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
            smsReader = get()
        )
    }

    single<SeedRepository> {
        SeedRepositoryImpl(get())
    }


}