package com.sh.mycash.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sh.mycash.data.local.entity.CategoryType
import com.sh.mycash.data.local.entity.TransactionEntity
import com.sh.mycash.data.local.entity.TransactionType
import com.sh.mycash.data.repository.AccountRepository
import com.sh.mycash.data.repository.CategoryRepository
import com.sh.mycash.data.repository.TransactionRepository
import com.sh.mycash.data.repository.TransactionWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val accounts = accountRepository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeSubcategories = categoryRepository.getCategoriesWithSubcategories(CategoryType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseSubcategories = categoryRepository.getCategoriesWithSubcategories(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionWithDetails>> = transactionRepository
        .getAllWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionWithDetails?>(null)
    val editingTransaction: StateFlow<TransactionWithDetails?> = _editingTransaction.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow<TransactionWithDetails?>(null)
    val showDeleteConfirm: StateFlow<TransactionWithDetails?> = _showDeleteConfirm.asStateFlow()

    fun onAddClick() {
        _editingTransaction.value = null
        _showAddDialog.value = true
    }

    fun onEditClick(transaction: TransactionWithDetails) {
        _editingTransaction.value = transaction
        _showAddDialog.value = true
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
        _showAddDialog.value = false
        _editingTransaction.value = null
    }

    fun dismissDeleteConfirm() {
        _showDeleteConfirm.value = null
    }
}

data class TransactionEditState(
    val id: Long?,
    val type: TransactionType,
    val amount: Double,
    val accountId: Long?,
    val targetAccountId: Long?,
    val subcategoryId: Long?,
    val date: Long,
    val comment: String
)

class TransactionsViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TransactionsViewModel(
            transactionRepository,
            accountRepository,
            categoryRepository
        ) as T
    }
}
