package com.sh.mycash.data.repository

import com.sh.mycash.data.local.dao.AccountDao
import com.sh.mycash.data.local.dao.SubcategoryDao
import com.sh.mycash.data.local.dao.TransactionDao
import com.sh.mycash.data.local.entity.TransactionEntity
import com.sh.mycash.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val subcategoryDao: SubcategoryDao
) {

    fun getAllWithDetails(): Flow<List<TransactionWithDetails>> {
        return combine(
            transactionDao.getAll(),
            accountDao.getAll(),
            subcategoryDao.getAll()
        ) { transactions, accounts, subcategories ->
            transactions.map { t ->
                val account = accounts.find { it.id == t.accountId }
                val targetAccount = t.targetAccountId?.let { id -> accounts.find { it.id == id } }
                val subcategory = t.subcategoryId?.let { id -> subcategories.find { it.id == id } }
                TransactionWithDetails(
                    transaction = t,
                    accountName = account?.name ?: "-",
                    targetAccountName = targetAccount?.name,
                    subcategoryName = subcategory?.name
                )
            }
        }
    }

    suspend fun insert(transaction: TransactionEntity): Long = transactionDao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) = transactionDao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    suspend fun getById(id: Long) = transactionDao.getById(id)
}

data class TransactionWithDetails(
    val transaction: TransactionEntity,
    val accountName: String,
    val targetAccountName: String?,
    val subcategoryName: String?
)
