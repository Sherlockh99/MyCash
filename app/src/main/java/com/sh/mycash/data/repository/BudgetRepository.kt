package com.sh.mycash.data.repository

import com.sh.mycash.data.local.dao.BudgetDao
import com.sh.mycash.data.local.dao.SubcategoryDao
import com.sh.mycash.data.local.dao.TransactionDao
import com.sh.mycash.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class BudgetRepository(
    private val budgetDao: BudgetDao,
    private val subcategoryDao: SubcategoryDao,
    private val transactionDao: TransactionDao
) {

    fun getBudgetsWithDetails(year: Int, month: Int): Flow<List<BudgetWithDetails>> {
        val (startDate, endDate) = monthRange(year, month)
        return combine(
            budgetDao.getByMonth(year, month),
            subcategoryDao.getAll(),
            transactionDao.getExpensesBySubcategory(startDate, endDate)
        ) { budgets, subcategories, expenseSummaries ->
            val expenseBySubcat = expenseSummaries.associate { it.subcategoryId to it.total }
            budgets.map { budget ->
                val subcategory = subcategories.find { it.id == budget.subcategoryId }
                val spent = expenseBySubcat[budget.subcategoryId] ?: 0.0
                BudgetWithDetails(
                    budget = budget,
                    subcategoryName = subcategory?.name ?: "-",
                    spent = spent
                )
            }.sortedBy { it.subcategoryName }
        }
    }

    suspend fun insert(budget: BudgetEntity): Long = budgetDao.insert(budget)

    suspend fun update(budget: BudgetEntity) = budgetDao.update(budget)

    suspend fun delete(budget: BudgetEntity) = budgetDao.delete(budget)

    suspend fun getById(id: Long) = budgetDao.getById(id)

    private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }
}

data class BudgetWithDetails(
    val budget: BudgetEntity,
    val subcategoryName: String,
    val spent: Double
)
