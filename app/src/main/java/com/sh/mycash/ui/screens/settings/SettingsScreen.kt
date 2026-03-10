package com.sh.mycash.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sh.mycash.MyCashApplication
import com.sh.mycash.R
import androidx.core.os.LocaleListCompat
import com.sh.mycash.data.backup.BackupRepository
import com.sh.mycash.data.preferences.AppLanguage
import com.sh.mycash.data.preferences.UserPreferences
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.sh.mycash.worker.BackupWorker
import java.util.concurrent.TimeUnit

private const val BACKUP_WORK_NAME = "mycash_auto_backup"

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as MyCashApplication
    val userPreferences = UserPreferences(context)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val systemInDarkTheme = isSystemInDarkTheme()

    val darkTheme by userPreferences.darkTheme.collectAsState(initial = null)
    val language by userPreferences.language.collectAsState(initial = AppLanguage.UKRAINIAN)
    val autoBackupEnabled by userPreferences.autoBackupEnabled.collectAsState(initial = false)

    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val backupRepository = remember {
        BackupRepository(
            app.database.accountDao(),
            app.database.categoryDao(),
            app.database.subcategoryDao(),
            app.database.transactionDao(),
            app.database.recurringTransactionDao(),
            app.database.budgetDao(),
            Dispatchers.IO
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val json = backupRepository.exportToJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    snackbarHostState.showSnackbar(context.getString(R.string.backup_export_success))
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(context.getString(R.string.backup_export_error, e.message))
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    // When null, follow system; display switch reflects current effective state
    val isDarkTheme = darkTheme ?: systemInDarkTheme

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
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

        Spacer(modifier = Modifier.height(24.dp))

        // Backup section
        Text(
            text = stringResource(R.string.settings_backup),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { exportLauncher.launch("mycash_backup_${System.currentTimeMillis()}.json") }) {
                Text(stringResource(R.string.backup_export))
            }
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }) {
                Text(stringResource(R.string.backup_import))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.backup_auto),
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = autoBackupEnabled,
                onCheckedChange = { enabled ->
                    coroutineScope.launch {
                        userPreferences.setAutoBackupEnabled(enabled)
                        if (enabled) {
                            val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
                                .build()
                            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                                BACKUP_WORK_NAME,
                                ExistingPeriodicWorkPolicy.KEEP,
                                request
                            )
                        } else {
                            WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME)
                        }
                    }
                }
            )
        }
    }
    }

    if (showImportConfirm && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false; pendingImportUri = null },
            title = { Text(stringResource(R.string.backup_import_confirm_title)) },
            text = { Text(stringResource(R.string.backup_import_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingImportUri!!
                        pendingImportUri = null
                        showImportConfirm = false
                        coroutineScope.launch {
                            try {
                                val json = withContext(Dispatchers.IO) {
                                    context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                                        ?: throw IllegalStateException("Cannot read file")
                                }
                                backupRepository.importFromJson(json).getOrThrow()
                                snackbarHostState.showSnackbar(context.getString(R.string.backup_import_success))
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(context.getString(R.string.backup_import_error, e.message))
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.backup_import_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false; pendingImportUri = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
