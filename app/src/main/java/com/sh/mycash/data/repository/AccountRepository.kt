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
            val balance = account.initialBalance + delta
            val limit = account.creditLimit ?: 0.0
            val isCreditCard = limit > 0
            // For reporting: balance < limit → debt, balance > limit → own money on card
            val debt = if (isCreditCard && balance < limit) limit - balance else 0.0
            val ownMoneyOnCard = if (isCreditCard && balance > limit) balance - limit else 0.0
            AccountWithBalance(
                account = account,
                balance = balance,
                creditLimit = if (isCreditCard) limit else null,
                debt = debt,
                ownMoneyOnCard = ownMoneyOnCard
            )
        }
    }

    fun getAccounts(): Flow<List<AccountEntity>> = accountDao.getAll()

    suspend fun getAccountWithBalance(id: Long): AccountWithBalance? {
        val account = accountDao.getById(id) ?: return null
        val delta = transactionDao.getBalanceDelta(id)
        val balance = account.initialBalance + delta
        val limit = account.creditLimit ?: 0.0
        val isCreditCard = limit > 0
        val debt = if (isCreditCard && balance < limit) limit - balance else 0.0
        val ownMoneyOnCard = if (isCreditCard && balance > limit) balance - limit else 0.0
        return AccountWithBalance(
            account = account,
            balance = balance,
            creditLimit = if (isCreditCard) limit else null,
            debt = debt,
            ownMoneyOnCard = ownMoneyOnCard
        )
    }

    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insert(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)

    suspend fun deleteAccount(account: AccountEntity) = accountDao.delete(account)

    suspend fun getAccountById(id: Long) = accountDao.getById(id)

    /** Total balance across all accounts. For credit cards: balance - creditLimit. */
    fun getTotalBalanceWithCredit(): Flow<Double> = getAllWithBalance().map { accounts ->
        accounts.sumOf { acc ->
            val limit = acc.creditLimit ?: 0.0
            if (limit > 0) acc.balance - limit else acc.balance
        }
    }
}

data class AccountWithBalance(
    val account: AccountEntity,
    val balance: Double,
    /** For credit cards: credit limit. Null for regular accounts. */
    val creditLimit: Double? = null,
    /** For reporting: when balance < limit, debt = limit - balance. Zero otherwise. */
    val debt: Double = 0.0,
    /** For reporting: when balance > limit, own money on card = balance - limit. Zero otherwise. */
    val ownMoneyOnCard: Double = 0.0
)
