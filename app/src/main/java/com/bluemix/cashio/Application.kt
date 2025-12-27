package com.bluemix.cashio

import android.app.Application
import android.util.Log
import com.bluemix.cashio.core.di.appModule
import com.bluemix.cashio.core.di.dataModule
import com.bluemix.cashio.core.di.domainModule
import com.bluemix.cashio.data.local.database.DatabaseSeeder
import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.getKoin

class CashioApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@CashioApplication)
            modules(appModule, dataModule, domainModule)
        }

        applicationScope.launch(Dispatchers.IO) {
            val koin = getKoin()
            val prefs = koin.get<UserPreferencesDataStore>()
            val alreadySeeded = prefs.isDbSeeded.first()
            if (alreadySeeded) return@launch

            try {
                val realmManager = koin.get<RealmManager>()
                val didSeed = DatabaseSeeder.seedAll(realmManager.realm)
                if (didSeed) prefs.setDbSeeded(true)
            } catch (e: Exception) {
                Log.e("CashioApplication", "Seeding failed", e)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

}