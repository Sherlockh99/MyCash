package com.sh.mycash.data.backup

import com.sh.mycash.data.local.entity.AccountEntity
import com.sh.mycash.data.local.entity.BudgetEntity
import com.sh.mycash.data.local.entity.CategoryEntity
import com.sh.mycash.data.local.entity.RecurringTransactionEntity
import com.sh.mycash.data.local.entity.SubcategoryEntity
import com.sh.mycash.data.local.entity.TransactionEntity

data class BackupData(
    val version: Int = 1,
    val exportedAt: Long,
    val appVersion: String,
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val subcategories: List<SubcategoryEntity>,
    val transactions: List<TransactionEntity>,
    val recurringTransactions: List<RecurringTransactionEntity>,
    val budgets: List<BudgetEntity>
)
