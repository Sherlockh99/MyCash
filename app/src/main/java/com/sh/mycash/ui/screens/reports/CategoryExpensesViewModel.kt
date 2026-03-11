package com.sh.mycash.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sh.mycash.data.local.dao.SubcategoryDao
import com.sh.mycash.data.local.entity.CategoryType
import com.sh.mycash.data.local.entity.TransactionEntity
import com.sh.mycash.data.local.entity.TransactionType
import com.sh.mycash.data.repository.AccountRepository
import com.sh.mycash.data.repository.CategoryRepository
import com.sh.mycash.data.repository.TransactionRepository
import com.sh.mycash.data.repository.TransactionWithDetails
import com.sh.mycash.ui.screens.transactions.TransactionEditState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryExpensesViewModel(
    private val subcategoryId: Long,
    private val startDate: Long,
    private val endDate: Long,
    private val transactionRepository: TransactionRepository,
    private val subcategoryDao: SubcategoryDao,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categoryName = MutableStateFlow("")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    val accounts = accountRepository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeSubcategories = categoryRepository.getCategoriesWithSubcategories(CategoryType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseSubcategories = categoryRepository.getCategoriesWithSubcategories(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionWithDetails>> = transactionRepository
        .getExpensesWithDetailsBySubcategoryAndDateRange(subcategoryId, startDate, endDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionWithDetails?>(null)
    val editingTransaction: StateFlow<TransactionWithDetails?> = _editingTransaction.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow<TransactionWithDetails?>(null)
    val showDeleteConfirm: StateFlow<TransactionWithDetails?> = _showDeleteConfirm.asStateFlow()

    init {
        viewModelScope.launch {
            _categoryName.value = subcategoryDao.getById(subcategoryId)?.name ?: "-"
        }
    }

    fun onEditClick(transaction: TransactionWithDetails) {
        _editingTransaction.value = transaction
        _showEditDialog.value = true
    }

    fun onDeleteClick(transaction: TransactionWithDetails) {
        _showDeleteConfirm.value = transaction
    }

    fun saveTransaction(state: TransactionEditState) {
        viewModelScope.launch {
            if (state.amount <= 0) return@launch
            when (state.type) {
                TransactionType.INCOME, TransactionType.EXPENSE -> {
                    if (state.accountId == null || state.subcategoryId == null) return@launch
                }
                TransactionType.TRANSFER -> {
                    if (state.accountId == null || state.targetAccountId == null) return@launch
                    if (state.accountId == state.targetAccountId) return@launch
                }
            }
            val entity = TransactionEntity(
                id = state.id ?: 0,
                type = state.type,
                amount = state.amount,
                accountId = state.accountId ?: 0,
                targetAccountId = state.targetAccountId,
                subcategoryId = state.subcategoryId,
                date = state.date,
                comment = state.comment.ifBlank { null }
            )
            if (state.id != null) {
                transactionRepository.update(entity)
            } else {
                transactionRepository.insert(entity)
            }
            dismissDialog()
        }
    }

    fun confirmDelete(transaction: TransactionWithDetails) {
        viewModelScope.launch {
            transactionRepository.delete(transaction.transaction)
            _showDeleteConfirm.value = null
        }
    }

    fun dismissDialog() {
        _showEditDialog.value = false
        _editingTransaction.value = null
    }

    fun dismissDeleteConfirm() {
        _showDeleteConfirm.value = null
    }
}

class CategoryExpensesViewModelFactory(
    private val subcategoryId: Long,
    private val startDate: Long,
    private val endDate: Long,
    private val transactionRepository: TransactionRepository,
    private val subcategoryDao: SubcategoryDao,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryExpensesViewModel(
            subcategoryId, startDate, endDate,
            transactionRepository, subcategoryDao,
            accountRepository, categoryRepository
        ) as T
    }
}
