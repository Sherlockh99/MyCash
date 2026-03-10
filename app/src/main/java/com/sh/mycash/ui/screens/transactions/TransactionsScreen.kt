package com.sh.mycash.ui.screens.transactions

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
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
import com.sh.mycash.data.local.entity.AccountEntity
import com.sh.mycash.data.local.entity.SubcategoryEntity
import com.sh.mycash.data.local.entity.TransactionType
import com.sh.mycash.data.repository.AccountRepository
import com.sh.mycash.data.repository.CategoryRepository
import com.sh.mycash.data.repository.TransactionRepository
import com.sh.mycash.data.repository.TransactionWithDetails
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = viewModel(
        factory = TransactionsViewModelFactory(
            TransactionRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.transactionDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.accountDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.subcategoryDao()
            ),
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
    val transactions by viewModel.transactions.collectAsState()
    val showDialog by viewModel.showAddDialog.collectAsState()
    val editingTransaction by viewModel.editingTransaction.collectAsState()
    val deleteConfirm by viewModel.showDeleteConfirm.collectAsState()

    Scaffold(
        topBar = {
            Text(
                text = stringResource(R.string.screen_transactions),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
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
        if (transactions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.transactions_empty),
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
                    TransactionCard(
                        transactionWithDetails = item,
                        onEditClick = { viewModel.onEditClick(item) },
                        onDeleteClick = { viewModel.onDeleteClick(item) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        TransactionEditDialog(
            initialState = editingTransaction,
            viewModel = viewModel,
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
private fun TransactionCard(
    transactionWithDetails: TransactionWithDetails,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val t = transactionWithDetails.transaction
    val amountColor = when (t.type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
    }
    val amountPrefix = when (t.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.TRANSFER -> ""
    }
    val description = when (t.type) {
        TransactionType.INCOME -> transactionWithDetails.subcategoryName ?: "-"
        TransactionType.EXPENSE -> transactionWithDetails.subcategoryName ?: "-"
        TransactionType.TRANSFER -> "${transactionWithDetails.accountName} → ${transactionWithDetails.targetAccountName ?: "-"}"
    }
    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        .format(Date(t.date))

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
                    text = "$amountPrefix${String.format("%.2f", t.amount)} грн",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
                Text(
                    text = description,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionEditDialog(
    initialState: TransactionWithDetails?,
    viewModel: TransactionsViewModel,
    onDismiss: () -> Unit,
    onSave: (TransactionEditState) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accounts by viewModel.accounts.collectAsState()
    val incomeSubs by viewModel.incomeSubcategories.collectAsState()
    val expenseSubs by viewModel.expenseSubcategories.collectAsState()

    var type by remember(initialState) {
        mutableStateOf(initialState?.transaction?.type ?: TransactionType.EXPENSE)
    }
    var amount by remember(initialState) {
        mutableStateOf(
            initialState?.transaction?.amount?.toString() ?: ""
        )
    }
    var accountId by remember(initialState) {
        mutableStateOf(initialState?.transaction?.accountId)
    }
    var targetAccountId by remember(initialState) {
        mutableStateOf(initialState?.transaction?.targetAccountId)
    }
    var subcategoryId by remember(initialState) {
        mutableStateOf(initialState?.transaction?.subcategoryId)
    }
    var date by remember(initialState) {
        mutableStateOf(initialState?.transaction?.date ?: System.currentTimeMillis())
    }
    var comment by remember(initialState) {
        mutableStateOf(initialState?.transaction?.comment ?: "")
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val subcategories = when (type) {
        TransactionType.INCOME -> incomeSubs
        TransactionType.EXPENSE -> expenseSubs
        TransactionType.TRANSFER -> emptyList()
    }
    val flatSubcategories = subcategories.flatMap { it.subcategories }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialState != null) stringResource(R.string.transactions_edit_title)
                    else stringResource(R.string.transactions_add_title),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.transactions_type),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TransactionType.entries.forEachIndexed { index, t ->
                    SegmentedButton(
                        selected = type == t,
                        onClick = {
                            type = t
                            if (t == TransactionType.TRANSFER) subcategoryId = null
                            else targetAccountId = null
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, TransactionType.entries.size)
                    ) {
                        Text(
                            when (t) {
                                TransactionType.INCOME -> stringResource(R.string.transaction_type_income)
                                TransactionType.EXPENSE -> stringResource(R.string.transaction_type_expense)
                                TransactionType.TRANSFER -> stringResource(R.string.transaction_type_transfer)
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
                label = { Text(stringResource(R.string.transactions_amount)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            AccountDropdown(
                label = if (type == TransactionType.TRANSFER) stringResource(R.string.transactions_from_account)
                else stringResource(R.string.transactions_account),
                accounts = accounts,
                selectedId = accountId,
                onSelect = { accountId = it }
            )

            if (type == TransactionType.TRANSFER) {
                AccountDropdown(
                    label = stringResource(R.string.transactions_to_account),
                    accounts = accounts.filter { it.id != accountId },
                    selectedId = targetAccountId,
                    onSelect = { targetAccountId = it }
                )
            } else {
                SubcategoryDropdown(
                    subcategories = flatSubcategories,
                    selectedId = subcategoryId,
                    onSelect = { subcategoryId = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = dateFormat.format(Date(date)),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.transactions_date)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { showDatePicker = true }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = timeFormat.format(Date(date)),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.transactions_time)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { showTimePicker = true }
                    )
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text(stringResource(R.string.transactions_comment)) },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        val amountVal = amount.toDoubleOrNull() ?: 0.0
                        val accId = accountId
                        val tgtId = if (type == TransactionType.TRANSFER) targetAccountId else null
                        val subId = if (type != TransactionType.TRANSFER) subcategoryId else null
                        onSave(
                            TransactionEditState(
                                id = initialState?.transaction?.id,
                                type = type,
                                amount = amountVal,
                                accountId = accId,
                                targetAccountId = tgtId,
                                subcategoryId = subId,
                                date = date,
                                comment = comment.trim()
                            )
                        )
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance().apply { timeInMillis = date }
                            val newCal = Calendar.getInstance().apply { timeInMillis = millis }
                            newCal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                            newCal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
                            newCal.set(Calendar.SECOND, cal.get(Calendar.SECOND))
                            date = newCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.transactions_time)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newCal = Calendar.getInstance().apply { timeInMillis = date }
                        newCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        newCal.set(Calendar.MINUTE, timePickerState.minute)
                        newCal.set(Calendar.SECOND, 0)
                        date = newCal.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AccountDropdown(
    label: String,
    accounts: List<AccountEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val selectedAccount = accounts.find { it.id == selectedId }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedAccount?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { showPicker = true }
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(label) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    accounts.forEach { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(account.id)
                                    showPicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SubcategoryDropdown(
    subcategories: List<SubcategoryEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val selected = subcategories.find { it.id == selectedId }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.transactions_subcategory)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { showPicker = true }
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(stringResource(R.string.transactions_subcategory)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subcategories.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(sub.id)
                                    showPicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
