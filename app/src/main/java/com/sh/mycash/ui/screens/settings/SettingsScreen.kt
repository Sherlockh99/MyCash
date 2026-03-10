package com.sh.mycash.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sh.mycash.R
import androidx.core.os.LocaleListCompat
import com.sh.mycash.data.preferences.AppLanguage
import com.sh.mycash.data.preferences.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val userPreferences = UserPreferences(context)
    val coroutineScope = rememberCoroutineScope()
    val systemInDarkTheme = isSystemInDarkTheme()

    val darkTheme by userPreferences.darkTheme.collectAsState(initial = null)
    val language by userPreferences.language.collectAsState(initial = AppLanguage.UKRAINIAN)

    // When null, follow system; display switch reflects current effective state
    val isDarkTheme = darkTheme ?: systemInDarkTheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.screen_settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Dark theme switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_dark_theme),
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { enabled ->
                    coroutineScope.launch {
                        userPreferences.setDarkTheme(enabled)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language selector
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        AppLanguage.entries.forEach { lang ->
            val isSelected = language == lang
            val languageName = when (lang) {
                AppLanguage.UKRAINIAN -> stringResource(R.string.settings_language_ukrainian)
                AppLanguage.RUSSIAN -> stringResource(R.string.settings_language_russian)
                AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        coroutineScope.launch {
                            userPreferences.setLanguage(lang)
                            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(lang.code)
                            )
                        }
                    }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = languageName,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (isSelected) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
