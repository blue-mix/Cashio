package com.bluemix.cashio.core.di

import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.data.repository.CategoryRepositoryImpl
import com.bluemix.cashio.data.repository.ExpenseRepositoryImpl
import com.bluemix.cashio.data.repository.KeywordMappingRepositoryImpl
import com.bluemix.cashio.data.sms.SmsReader
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { RealmManager() }
    single { UserPreferencesDataStore(androidContext()) }
    single { SmsReader(androidContext()) }
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class
    singleOf(::KeywordMappingRepositoryImpl) bind KeywordMappingRepository::class
    singleOf(::ExpenseRepositoryImpl) bind ExpenseRepository::class
}