
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
//package com.bluemix.cashio
//
//import android.app.Application
//import android.util.Log
//import com.bluemix.cashio.core.di.appModule
//import com.bluemix.cashio.core.di.dataModule
//import com.bluemix.cashio.core.di.domainModule
//import com.bluemix.cashio.data.local.database.DatabaseSeeder
//import com.bluemix.cashio.data.local.database.RealmManager
//import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//import org.koin.android.ext.koin.androidContext
//import org.koin.android.ext.koin.androidLogger
//import org.koin.core.context.GlobalContext
//import org.koin.core.context.startKoin
//import org.koin.core.logger.Level
//import kotlinx.coroutines.withContext
//
//
//class CashioApplication : Application() {
//
//    /**
//     * App-wide scope for background one-off tasks that should live as long as the process.
//     * - SupervisorJob: one failure won't cancel other tasks.
//     * - Default: lightweight coordinator thread pool; we switch to IO for disk work.
//     */
//    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//
//    override fun onCreate() {
//        super.onCreate()
//
//        initKoin()
//
//        // Seed DB in background (only once).
//        appScope.launch {
//            seedDatabaseIfNeeded()
//        }
//    }
//
//    private fun initKoin() {
//        startKoin {
//            androidLogger(Level.ERROR) // bump to INFO/DEBUG during development if needed
//            androidContext(this@CashioApplication)
//            modules(appModule, dataModule, domainModule)
//        }
//    }
//
//    /**
//     * Seeds Realm with default data exactly once.
//     * Uses a DataStore flag to skip seeding on future launches.
//     */
//    private suspend fun seedDatabaseIfNeeded() = withContext(Dispatchers.IO) {
//        val koin = GlobalContext.get()
//
//        val prefs = koin.get<UserPreferencesDataStore>()
//        val isAlreadySeeded = prefs.isDbSeeded.first()
//        if (isAlreadySeeded) return@withContext
//
//        try {
//            val realmManager = koin.get<RealmManager>()
//            val didSeed = DatabaseSeeder.seedAll(realmManager.realm)
//
//            if (didSeed) {
//                prefs.setDbSeeded(true)
//                Log.i(TAG, "✅ Database seeded successfully")
//            } else {
//                Log.w(TAG, "⚠️ Database seeding returned false (nothing seeded)")
//            }
//        } catch (t: Throwable) {
//            Log.e(TAG, "❌ Database seeding failed", t)
//        }
//    }
//
//    private companion object {
//        const val TAG = "CashioApplication"
//    }
//}
