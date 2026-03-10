package com.sh.mycash.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sh.mycash.MyCashApplication
import com.sh.mycash.data.backup.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val app = applicationContext.applicationContext as MyCashApplication
            val backupDir = File(applicationContext.filesDir, "backups").apply { mkdirs() }
            val file = File(backupDir, "mycash_auto_${System.currentTimeMillis()}.json")

            val repository = BackupRepository(
                app.database.accountDao(),
                app.database.categoryDao(),
                app.database.subcategoryDao(),
                app.database.transactionDao(),
                app.database.recurringTransactionDao(),
                app.database.budgetDao(),
                Dispatchers.IO
            )

            val json = repository.exportToJson()
            file.writeText(json, Charsets.UTF_8)

            // Keep only last 5 auto-backups
            backupDir.listFiles()?.sortedBy { it.lastModified() }?.let { files ->
                if (files.size > 5) {
                    files.take(files.size - 5).forEach { it.delete() }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
