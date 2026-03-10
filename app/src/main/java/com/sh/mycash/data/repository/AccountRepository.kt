package com.sh.mycash.data.repository

import com.sh.mycash.data.local.dao.AccountDao
import com.sh.mycash.data.local.dao.TransactionDao
import com.sh.mycash.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {

    fun getAllWithBalance(): Flow<List<AccountWithBalance>> = accountDao.getAll().map { accounts ->
        accounts.map { account ->
            val delta = transactionDao.getBalanceDelta(account.id)
            AccountWithBalance(account = account, balance = account.initialBalance + delta)
        }
    }

    fun getAccounts(): Flow<List<AccountEntity>> = accountDao.getAll()

    suspend fun getAccountWithBalance(id: Long): AccountWithBalance? {
        val account = accountDao.getById(id) ?: return null
        val delta = transactionDao.getBalanceDelta(id)
        return AccountWithBalance(account = account, balance = account.initialBalance + delta)
    }

    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insert(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)

    suspend fun deleteAccount(account: AccountEntity) = accountDao.delete(account)

    suspend fun getAccountById(id: Long) = accountDao.getById(id)
}

data class AccountWithBalance(
    val account: AccountEntity,
    val balance: Double
)
