package com.sh.mycash.data.backup

import com.sh.mycash.data.local.dao.AccountDao
import com.sh.mycash.data.local.dao.BudgetDao
import com.sh.mycash.data.local.dao.CategoryDao
import com.sh.mycash.data.local.dao.RecurringTransactionDao
import com.sh.mycash.data.local.dao.SubcategoryDao
import com.sh.mycash.data.local.dao.TransactionDao
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class BackupRepository(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val subcategoryDao: SubcategoryDao,
    private val transactionDao: TransactionDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val budgetDao: BudgetDao,
    private val ioContext: CoroutineContext
) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToJson(): String = withContext(ioContext) {
        val accounts = accountDao.getAll().first()
        val categories = categoryDao.getAllSync()
        val subcategories = subcategoryDao.getAll().first()
        val transactions = transactionDao.getAll().first()
        val recurring = recurringTransactionDao.getAll().first()
        val budgets = budgetDao.getAllSync()

        val data = BackupData(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            appVersion = "1.0",
            accounts = accounts,
            categories = categories,
            subcategories = subcategories,
            transactions = transactions,
            recurringTransactions = recurring,
            budgets = budgets
        )
        gson.toJson(data)
    }

    suspend fun importFromJson(json: String): Result<Unit> = withContext(ioContext) {
        runCatching {
            val data = gson.fromJson(json, BackupData::class.java)
                ?: throw IllegalArgumentException("Invalid backup format")

            if (data.version != 1) {
                throw IllegalArgumentException("Unsupported backup version: ${data.version}")
            }

            // Delete in order (children first due to FKs)
            budgetDao.deleteAll()
            recurringTransactionDao.deleteAll()
            transactionDao.deleteAll()
            subcategoryDao.deleteAll()
            accountDao.deleteAll()
            categoryDao.deleteAll()

            // Insert in order (parents first)
            data.categories.forEach { categoryDao.insert(it) }
            data.subcategories.forEach { subcategoryDao.insert(it) }
            data.accounts.forEach { accountDao.insert(it) }
            data.transactions.forEach { transactionDao.insert(it) }
            data.recurringTransactions.forEach { recurringTransactionDao.insert(it) }
            data.budgets.forEach { budgetDao.insert(it) }
        }
    }
}
