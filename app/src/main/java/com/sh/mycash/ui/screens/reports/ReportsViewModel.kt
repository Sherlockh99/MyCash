package com.sh.mycash.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sh.mycash.data.repository.CategoryExpenseItem
import com.sh.mycash.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val cal = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(cal.get(Calendar.YEAR))
    private val _selectedMonth = MutableStateFlow(cal.get(Calendar.MONTH) + 1)

    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    val expensesByCategory: StateFlow<List<CategoryExpenseItem>> = combine(
        _selectedYear,
        _selectedMonth
    ) { year, month -> Pair(year, month) }
        .flatMapLatest { (year, month) ->
            val (start, end) = monthRange(year, month)
            transactionRepository.getExpensesBySubcategoryWithNames(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    init {
        refreshTotals()
    }

    fun prevMonth() {
        if (_selectedMonth.value <= 1) {
            _selectedMonth.value = 12
            _selectedYear.value = _selectedYear.value - 1
        } else {
            _selectedMonth.value = _selectedMonth.value - 1
        }
        refreshTotals()
    }

    fun nextMonth() {
        if (_selectedMonth.value >= 12) {
            _selectedMonth.value = 1
            _selectedYear.value = _selectedYear.value + 1
        } else {
            _selectedMonth.value = _selectedMonth.value + 1
        }
        refreshTotals()
    }

    private fun refreshTotals() {
        viewModelScope.launch {
            val (start, end) = monthRange(_selectedYear.value, _selectedMonth.value)
            _totalExpenses.value = transactionRepository.getTotalExpenses(start, end)
            _totalIncome.value = transactionRepository.getTotalIncome(start, end)
        }
    }

    private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val c = Calendar.getInstance()
        c.set(year, month - 1, 1, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        val start = c.timeInMillis
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        val end = c.timeInMillis
        return Pair(start, end)
    }
}

class ReportsViewModelFactory(
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReportsViewModel(transactionRepository) as T
    }
}
