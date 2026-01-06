package com.bluemix.cashio

import android.app.Application
import com.bluemix.cashio.core.di.appModule
import com.bluemix.cashio.core.di.dataModule
import com.bluemix.cashio.core.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Main Application class for Cashio.
 *
 * This class is the first entry point of the app process. Its primary responsibility
 * is to initialize the Koin dependency injection framework. This ensures that
 * all singletons (Database, Repositories) and factories (UseCases, ViewModels)
 * are registered and ready before any Activity or Service attempts to inject them.
 */
class CashioApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Level.ERROR prevents Koin from flooding Logcat with debug info in production
            androidLogger(Level.ERROR)

            androidContext(this@CashioApplication)

            modules(
                appModule,
                dataModule,
                domainModule
            )
        }
    }
}