package com.sh.mycash

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.sh.mycash.data.local.AppDatabase
import com.sh.mycash.data.preferences.AppLanguage
import com.sh.mycash.data.preferences.PreferenceKeys
import com.sh.mycash.data.preferences.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MyCashApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        applySavedLanguage()
    }

    private fun applySavedLanguage() {
        runBlocking {
            val languageCode = applicationContext.dataStore.data.first()[PreferenceKeys.LANGUAGE]
                ?: AppLanguage.UKRAINIAN.code
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        }
    }
}
