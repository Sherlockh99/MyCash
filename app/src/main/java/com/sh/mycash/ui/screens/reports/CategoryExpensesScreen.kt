package com.sh.mycash.ui.screens.reports

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.sh.mycash.data.repository.CategoryRepository
import com.sh.mycash.data.repository.TransactionRepository
import com.sh.mycash.data.repository.TransactionWithDetails
import com.sh.mycash.ui.screens.transactions.TransactionEditDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val CATEGORY_EXPENSES_ROUTE = "category_expenses/{subcategoryId}/{startDate}/{endDate}"

fun categoryExpensesRoute(subcategoryId: Long, startDate: Long, endDate: Long) =
    "category_expenses/$subcategoryId/$startDate/$endDate"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExpensesScreen(
    subcategoryId: Long,
    startDate: Long,
    endDate: Long,
    onBackClick: () -> Unit,
    viewModel: CategoryExpensesViewModel = viewModel(
        factory = CategoryExpensesViewModelFactory(
            subcategoryId,
            startDate,
            endDate,
            TransactionRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.transactionDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.accountDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.subcategoryDao()
            ),
            (LocalContext.current.applicationContext as MyCashApplication).database.subcategoryDao(),
            AccountRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.accountDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.transactionDao()
            ),
            CategoryRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.categoryDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.subcategoryDao()
            )
        )
    )
) {
    val categoryName by viewModel.categoryName.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val incomeSubcategories by viewModel.incomeSubcategories.collectAsState()
    val expenseSubcategories by viewModel.expenseSubcategories.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val editingTransaction by viewModel.editingTransaction.collectAsState()
    val deleteConfirm by viewModel.showDeleteConfirm.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reports_category_expenses_title, categoryName),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.reports_category_expenses_empty),
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
                items(transactions, key = { it.transaction.id }) { item ->
                    CategoryExpenseTransactionCard(
                        transactionWithDetails = item,
                        onEditClick = { viewModel.onEditClick(item) },
                        onDeleteClick = { viewModel.onDeleteClick(item) }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        TransactionEditDialog(
            initialState = editingTransaction,
            accounts = accounts,
            incomeSubcategories = incomeSubcategories,
            expenseSubcategories = expenseSubcategories,
            onDismiss = { viewModel.dismissDialog() },
            onSave = { viewModel.saveTransaction(it) }
        )
    }

    deleteConfirm?.let { transaction ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirm() },
            title = { Text(stringResource(R.string.transactions_delete_title)) },
            text = { Text(stringResource(R.string.transactions_delete_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(transaction) }) {
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
private fun CategoryExpenseTransactionCard(
    transactionWithDetails: TransactionWithDetails,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val t = transactionWithDetails.transaction
    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(t.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
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
                    text = "-${String.format("%.2f", t.amount)} грн",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = transactionWithDetails.accountName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                t.comment?.let { comment ->
                    if (comment.isNotBlank()) {
                        Text(
                            text = comment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
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
