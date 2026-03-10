package com.sh.mycash.ui.screens.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sh.mycash.MyCashApplication
import com.sh.mycash.R
import com.sh.mycash.data.repository.AccountRepository
import com.sh.mycash.data.repository.AccountWithBalance

@Composable
fun AccountsScreen(
    onBackClick: () -> Unit,
    viewModel: AccountsViewModel = viewModel(
        factory = AccountsViewModelFactory(
            AccountRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.accountDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.transactionDao()
            )
        )
    )
) {
    val accounts by viewModel.accounts.collectAsState()
    val editState by viewModel.showEditDialog.collectAsState()
    val deleteConfirm by viewModel.showDeleteConfirm.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Text(
                    text = stringResource(R.string.screen_accounts),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddClick() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.accounts_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts, key = { it.account.id }) { item ->
                    AccountCard(
                        accountWithBalance = item,
                        onEditClick = { viewModel.onEditClick(item) },
                        onDeleteClick = { viewModel.onDeleteClick(item) }
                    )
                }
            }
        }
    }

    editState?.let { state ->
        AccountEditDialog(
            state = state,
            onDismiss = { viewModel.dismissEditDialog() },
            onSave = { viewModel.saveAccount(it) }
        )
    }

    deleteConfirm?.let { account ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirm() },
            title = { Text(stringResource(R.string.accounts_delete_title)) },
            text = {
                Text(stringResource(R.string.accounts_delete_message, account.account.name))
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(account) }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirm() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AccountCard(
    accountWithBalance: AccountWithBalance,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = accountWithBalance.account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = accountWithBalance.account.type.ifBlank { "-" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.accounts_balance_format, accountWithBalance.balance),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun AccountEditDialog(
    state: AccountEditState,
    onDismiss: () -> Unit,
    onSave: (AccountEditState) -> Unit
) {
    var name by remember(state) { mutableStateOf(state.name) }
    var type by remember(state) { mutableStateOf(state.type) }
    var initialBalance by remember(state) { mutableStateOf(state.initialBalance.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (state.id != null) stringResource(R.string.accounts_edit_title)
                else stringResource(R.string.accounts_add_title)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.accounts_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text(stringResource(R.string.accounts_type)) },
                    placeholder = { Text(stringResource(R.string.accounts_type_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialBalance,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) initialBalance = it },
                    label = { Text(stringResource(R.string.accounts_initial_balance)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val balance = initialBalance.toDoubleOrNull() ?: 0.0
                    onSave(state.copy(name = name, type = type, initialBalance = balance))
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
