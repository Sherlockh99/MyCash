package com.sh.mycash.data.repository

import com.sh.mycash.data.local.dao.AccountDao
import com.sh.mycash.data.local.dao.RecurringTransactionDao
import com.sh.mycash.data.local.dao.SubcategoryDao
import com.sh.mycash.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RecurringTransactionRepository(
    private val recurringDao: RecurringTransactionDao,
    private val accountDao: AccountDao,
    private val subcategoryDao: SubcategoryDao
) {

    fun getAllWithDetails(): Flow<List<RecurringTransactionWithDetails>> {
        return combine(
            recurringDao.getAll(),
            accountDao.getAll(),
            subcategoryDao.getAll()
        ) { recurring, accounts, subcategories ->
            recurring.map { r ->
                val account = accounts.find { it.id == r.accountId }
                val subcategory = subcategories.find { it.id == r.subcategoryId }
                RecurringTransactionWithDetails(
                    recurring = r,
                    accountName = account?.name ?: "-",
                    subcategoryName = subcategory?.name ?: "-"
                )
            }
        }
    }

    suspend fun insert(recurring: RecurringTransactionEntity): Long =
        recurringDao.insert(recurring)

    suspend fun update(recurring: RecurringTransactionEntity) =
        recurringDao.update(recurring)

    suspend fun delete(recurring: RecurringTransactionEntity) =
        recurringDao.delete(recurring)

    suspend fun getById(id: Long) = recurringDao.getById(id)
}

data class RecurringTransactionWithDetails(
    val recurring: RecurringTransactionEntity,
    val accountName: String,
    val subcategoryName: String
)
