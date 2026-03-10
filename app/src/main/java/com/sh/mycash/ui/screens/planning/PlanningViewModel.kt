package com.sh.mycash.ui.screens.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sh.mycash.data.local.entity.BudgetEntity
import com.sh.mycash.data.local.entity.CategoryType
import com.sh.mycash.data.local.entity.RecurringTransactionEntity
import com.sh.mycash.data.local.entity.TransactionType
import com.sh.mycash.data.repository.BudgetRepository
import com.sh.mycash.data.repository.BudgetWithDetails
import com.sh.mycash.data.repository.CategoryRepository
import com.sh.mycash.data.repository.RecurringTransactionRepository
import com.sh.mycash.data.repository.RecurringTransactionWithDetails
import com.sh.mycash.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class PlanningViewModel(
    private val budgetRepository: BudgetRepository,
    private val recurringRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val cal = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(cal.get(Calendar.YEAR))
    private val _selectedMonth = MutableStateFlow(cal.get(Calendar.MONTH) + 1)

    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    val expenseSubcategories = categoryRepository.getCategoriesWithSubcategories(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeSubcategories = categoryRepository.getCategoriesWithSubcategories(CategoryType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts = accountRepository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetWithDetails>> = combine(
        _selectedYear,
        _selectedMonth
    ) { year, month -> Pair(year, month) }
        .flatMapLatest { (year, month) -> budgetRepository.getBudgetsWithDetails(year, month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringTransactions: StateFlow<List<RecurringTransactionWithDetails>> =
        recurringRepository.getAllWithDetails()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showBudgetSheet = MutableStateFlow(false)
    val showBudgetSheet: StateFlow<Boolean> = _showBudgetSheet.asStateFlow()

    private val _editingBudget = MutableStateFlow<BudgetWithDetails?>(null)
    val editingBudget: StateFlow<BudgetWithDetails?> = _editingBudget.asStateFlow()

    private val _showRecurringSheet = MutableStateFlow(false)
    val showRecurringSheet: StateFlow<Boolean> = _showRecurringSheet.asStateFlow()

    private val _editingRecurring = MutableStateFlow<RecurringTransactionWithDetails?>(null)
    val editingRecurring: StateFlow<RecurringTransactionWithDetails?> = _editingRecurring.asStateFlow()

    private val _deleteBudgetConfirm = MutableStateFlow<BudgetWithDetails?>(null)
    val deleteBudgetConfirm: StateFlow<BudgetWithDetails?> = _deleteBudgetConfirm.asStateFlow()

    private val _deleteRecurringConfirm = MutableStateFlow<RecurringTransactionWithDetails?>(null)
    val deleteRecurringConfirm: StateFlow<RecurringTransactionWithDetails?> = _deleteRecurringConfirm.asStateFlow()

    fun prevMonth() {
        if (_selectedMonth.value <= 1) {
            _selectedMonth.value = 12
            _selectedYear.value = _selectedYear.value - 1
        } else {
            _selectedMonth.value = _selectedMonth.value - 1
        }
    }

    fun nextMonth() {
        if (_selectedMonth.value >= 12) {
            _selectedMonth.value = 1
            _selectedYear.value = _selectedYear.value + 1
        } else {
            _selectedMonth.value = _selectedMonth.value + 1
        }
    }

    fun onAddBudget() {
        _editingBudget.value = null
        _showBudgetSheet.value = true
    }

    fun onEditBudget(budget: BudgetWithDetails) {
        _editingBudget.value = budget
        _showBudgetSheet.value = true
    }

    fun onDeleteBudget(budget: BudgetWithDetails) {
        _deleteBudgetConfirm.value = budget
    }

    fun confirmDeleteBudget(budget: BudgetWithDetails) {
        viewModelScope.launch {
            budgetRepository.delete(budget.budget)
            _deleteBudgetConfirm.value = null
        }
    }

    fun dismissDeleteBudgetConfirm() {
        _deleteBudgetConfirm.value = null
    }

    fun dismissBudgetSheet() {
        _showBudgetSheet.value = false
        _editingBudget.value = null
    }

    fun saveBudget(subcategoryId: Long, amount: Double) {
        viewModelScope.launch {
            if (subcategoryId <= 0 || amount <= 0) return@launch
            val editing = _editingBudget.value
            val entity = BudgetEntity(
                id = editing?.budget?.id ?: 0,
                subcategoryId = subcategoryId,
                amount = amount,
                month = _selectedMonth.value,
                year = _selectedYear.value
            )
            if (editing != null) {
                budgetRepository.update(entity)
            } else {
                budgetRepository.insert(entity)
            }
            dismissBudgetSheet()
        }
    }

    fun onAddRecurring() {
        _editingRecurring.value = null
        _showRecurringSheet.value = true
    }

    fun onEditRecurring(r: RecurringTransactionWithDetails) {
        _editingRecurring.value = r
        _showRecurringSheet.value = true
    }

    fun onDeleteRecurring(r: RecurringTransactionWithDetails) {
        _deleteRecurringConfirm.value = r
    }

    fun confirmDeleteRecurring(r: RecurringTransactionWithDetails) {
        viewModelScope.launch {
            recurringRepository.delete(r.recurring)
            _deleteRecurringConfirm.value = null
        }
    }

    fun dismissDeleteRecurringConfirm() {
        _deleteRecurringConfirm.value = null
    }

    fun dismissRecurringSheet() {
        _showRecurringSheet.value = false
        _editingRecurring.value = null
    }

    fun saveRecurring(type: TransactionType, amount: Double, accountId: Long, subcategoryId: Long, dayOfMonth: Int) {
        viewModelScope.launch {
            if (amount <= 0 || accountId <= 0 || subcategoryId <= 0) return@launch
            val day = dayOfMonth.coerceIn(1, 31)
            val editing = _editingRecurring.value
            val entity = RecurringTransactionEntity(
                id = editing?.recurring?.id ?: 0,
                type = type,
                amount = amount,
                accountId = accountId,
                subcategoryId = subcategoryId,
                dayOfMonth = day,
                lastCreatedDate = editing?.recurring?.lastCreatedDate
            )
            if (editing != null) {
                recurringRepository.update(entity)
            } else {
                recurringRepository.insert(entity)
            }
            dismissRecurringSheet()
        }
    }
}

class PlanningViewModelFactory(
    private val budgetRepository: BudgetRepository,
    private val recurringRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlanningViewModel(
            budgetRepository,
            recurringRepository,
            categoryRepository,
            accountRepository
        ) as T
    }
}
