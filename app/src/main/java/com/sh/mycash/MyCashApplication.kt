package com.sh.mycash

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sh.mycash.data.local.AppDatabase
import com.sh.mycash.data.preferences.AppLanguage
import com.sh.mycash.data.preferences.PreferenceKeys
import com.sh.mycash.data.preferences.dataStore
import com.sh.mycash.worker.BackupWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class MyCashApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        applySavedLanguage()
        scheduleAutoBackupIfEnabled()
    }

    private fun scheduleAutoBackupIfEnabled() {
        runBlocking {
            val enabled = applicationContext.dataStore.data.first()[PreferenceKeys.AUTO_BACKUP_ENABLED] ?: false
            if (enabled) {
                val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS).build()
                WorkManager.getInstance(this@MyCashApplication).enqueueUniquePeriodicWork(
                    "mycash_auto_backup",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            }
        }
    }

    private fun applySavedLanguage() {
        runBlocking {
            val languageCode = applicationContext.dataStore.data.first()[PreferenceKeys.LANGUAGE]
                ?: AppLanguage.UKRAINIAN.code
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        }
    }
}
