package com.sh.mycash.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sh.mycash.data.local.entity.AccountEntity
import com.sh.mycash.data.repository.AccountRepository
import com.sh.mycash.data.repository.AccountWithBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    val accounts: StateFlow<List<AccountWithBalance>> = repository.getAllWithBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showEditDialog = MutableStateFlow<AccountEditState?>(null)
    val showEditDialog: StateFlow<AccountEditState?> = _showEditDialog.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow<AccountWithBalance?>(null)
    val showDeleteConfirm: StateFlow<AccountWithBalance?> = _showDeleteConfirm.asStateFlow()

    fun onAddClick() {
        _showEditDialog.value = AccountEditState(
            id = null,
            name = "",
            type = "",
            initialBalance = 0.0
        )
    }

    fun onEditClick(account: AccountWithBalance) {
        _showEditDialog.value = AccountEditState(
            id = account.account.id,
            name = account.account.name,
            type = account.account.type,
            initialBalance = account.account.initialBalance
        )
    }

    fun onDeleteClick(account: AccountWithBalance) {
        _showDeleteConfirm.value = account
    }

    fun saveAccount(state: AccountEditState) {
        viewModelScope.launch {
            if (state.name.isBlank()) return@launch
            val entity = AccountEntity(
                id = state.id ?: 0,
                name = state.name.trim(),
                type = state.type.trim().ifBlank { "-" },
                initialBalance = state.initialBalance,
                createdAt = state.id?.let { repository.getAccountById(it)?.createdAt } ?: System.currentTimeMillis()
            )
            if (state.id != null) {
                repository.updateAccount(entity)
            } else {
                repository.insertAccount(entity)
            }
            _showEditDialog.value = null
        }
    }

    fun confirmDelete(account: AccountWithBalance) {
        viewModelScope.launch {
            repository.deleteAccount(account.account)
            _showDeleteConfirm.value = null
        }
    }

    fun dismissEditDialog() {
        _showEditDialog.value = null
    }

    fun dismissDeleteConfirm() {
        _showDeleteConfirm.value = null
    }
}

data class AccountEditState(
    val id: Long?,
    val name: String,
    val type: String,
    val initialBalance: Double
)

class AccountsViewModelFactory(
    private val repository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AccountsViewModel(repository) as T
    }
}
