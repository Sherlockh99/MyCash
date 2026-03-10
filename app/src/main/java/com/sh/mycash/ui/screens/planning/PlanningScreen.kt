package com.sh.mycash.ui.screens.planning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.sh.mycash.data.local.entity.CategoryType
import com.sh.mycash.data.local.entity.SubcategoryEntity
import com.sh.mycash.data.local.entity.TransactionType
import com.sh.mycash.data.repository.AccountRepository
import com.sh.mycash.data.repository.BudgetRepository
import com.sh.mycash.data.repository.BudgetWithDetails
import com.sh.mycash.data.repository.CategoryRepository
import com.sh.mycash.data.repository.RecurringTransactionRepository
import com.sh.mycash.data.repository.RecurringTransactionWithDetails
import com.sh.mycash.data.local.entity.AccountEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    viewModel: PlanningViewModel = viewModel(
        factory = PlanningViewModelFactory(
            BudgetRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.budgetDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.subcategoryDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.transactionDao()
            ),
            RecurringTransactionRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.recurringTransactionDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.accountDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.subcategoryDao()
            ),
            CategoryRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.categoryDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.subcategoryDao()
            ),
            AccountRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.accountDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.transactionDao()
            )
        )
    )
) {
    var selectedTab by remember { mutableStateOf(0) }
    val budgets by viewModel.budgets.collectAsState()
    val recurring by viewModel.recurringTransactions.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val showBudgetSheet by viewModel.showBudgetSheet.collectAsState()
    val editingBudget by viewModel.editingBudget.collectAsState()
    val showRecurringSheet by viewModel.showRecurringSheet.collectAsState()
    val editingRecurring by viewModel.editingRecurring.collectAsState()
    val deleteBudgetConfirm by viewModel.deleteBudgetConfirm.collectAsState()
    val deleteRecurringConfirm by viewModel.deleteRecurringConfirm.collectAsState()
    val expenseSubcategories by viewModel.expenseSubcategories.collectAsState()
    val incomeSubcategories by viewModel.incomeSubcategories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    val monthNames = listOf(
        "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.screen_planning),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text(stringResource(R.string.planning_budgets)) }
                    )
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text(stringResource(R.string.planning_recurring)) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) viewModel.onAddBudget()
                    else viewModel.onAddRecurring()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.prevMonth() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                    }
                    Text(
                        text = "${monthNames[selectedMonth - 1]}.$selectedYear",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
                if (budgets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.planning_empty_budgets),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(budgets, key = { it.budget.id }) { item ->
                            BudgetCard(
                                item = item,
                                onEditClick = { viewModel.onEditBudget(item) },
                                onDeleteClick = { viewModel.onDeleteBudget(item) }
                            )
                        }
                    }
                }
            }
        } else {
            if (recurring.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.planning_empty_recurring),
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
                    items(recurring, key = { it.recurring.id }) { item ->
                        RecurringCard(
                            item = item,
                            onEditClick = { viewModel.onEditRecurring(item) },
                            onDeleteClick = { viewModel.onDeleteRecurring(item) }
                        )
                    }
                }
            }
        }
    }

    if (showBudgetSheet) {
        BudgetSheet(
            editing = editingBudget,
            year = selectedYear,
            month = selectedMonth,
            expenseSubcategories = expenseSubcategories,
            onDismiss = { viewModel.dismissBudgetSheet() },
            onSave = { subId, amount -> viewModel.saveBudget(subId, amount) }
        )
    }

    if (showRecurringSheet) {
        RecurringSheet(
            editing = editingRecurring,
            accounts = accounts,
            incomeSubcategories = incomeSubcategories,
            expenseSubcategories = expenseSubcategories,
            onDismiss = { viewModel.dismissRecurringSheet() },
            onSave = { type, amount, accId, subId, day -> viewModel.saveRecurring(type, amount, accId, subId, day) }
        )
    }

    deleteBudgetConfirm?.let { budget ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteBudgetConfirm() },
            title = { Text(stringResource(R.string.planning_delete_budget_title)) },
            text = { Text(stringResource(R.string.planning_delete_budget_message, budget.subcategoryName)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteBudget(budget) }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteBudgetConfirm() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    deleteRecurringConfirm?.let { r ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteRecurringConfirm() },
            title = { Text(stringResource(R.string.planning_delete_recurring_title)) },
            text = { Text(stringResource(R.string.planning_delete_recurring_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteRecurring(r) }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteRecurringConfirm() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun BudgetCard(
    item: BudgetWithDetails,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val amountStr = stringResource(R.string.planning_spent, item.spent)
    val ofStr = stringResource(R.string.planning_of, item.budget.amount)
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
                    text = item.subcategoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$amountStr $ofStr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun RecurringCard(
    item: RecurringTransactionWithDetails,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val r = item.recurring
    val amountColor = when (r.type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
    }
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
                    text = "${String.format("%.2f", r.amount)} грн · ${item.subcategoryName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
                Text(
                    text = "${item.accountName} · ${stringResource(R.string.planning_day_of_month)}: ${r.dayOfMonth}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetSheet(
    editing: BudgetWithDetails?,
    year: Int,
    month: Int,
    expenseSubcategories: List<com.sh.mycash.data.repository.CategoryWithSubcategories>,
    onDismiss: () -> Unit,
    onSave: (Long, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allSubcats = expenseSubcategories.flatMap { it.subcategories }
    var subcategoryId by remember { mutableStateOf(editing?.budget?.subcategoryId ?: 0L) }
    var amountText by remember { mutableStateOf(editing?.budget?.amount?.toString() ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (editing != null) stringResource(R.string.planning_edit_budget)
                else stringResource(R.string.planning_add_budget),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            SubcategoryDropdown(
                subcategories = allSubcats,
                selectedId = subcategoryId,
                onSelect = { subcategoryId = it }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(stringResource(R.string.transactions_amount)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (subcategoryId > 0 && amt > 0) onSave(subcategoryId, amt)
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringSheet(
    editing: RecurringTransactionWithDetails?,
    accounts: List<AccountEntity>,
    incomeSubcategories: List<com.sh.mycash.data.repository.CategoryWithSubcategories>,
    expenseSubcategories: List<com.sh.mycash.data.repository.CategoryWithSubcategories>,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Double, Long, Long, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var type by remember { mutableStateOf(editing?.recurring?.type ?: TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf(editing?.recurring?.amount?.toString() ?: "") }
    var accountId by remember { mutableStateOf(editing?.recurring?.accountId ?: 0L) }
    var subcategoryId by remember { mutableStateOf(editing?.recurring?.subcategoryId ?: 0L) }
    var dayText by remember { mutableStateOf(editing?.recurring?.dayOfMonth?.toString() ?: "1") }

    val subcategories = when (type) {
        TransactionType.INCOME -> incomeSubcategories.flatMap { it.subcategories }
        TransactionType.EXPENSE -> expenseSubcategories.flatMap { it.subcategories }
        TransactionType.TRANSFER -> emptyList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (editing != null) stringResource(R.string.planning_edit_recurring)
                else stringResource(R.string.planning_add_recurring),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == TransactionType.EXPENSE,
                    onClick = {
                        if (type != TransactionType.EXPENSE) { type = TransactionType.EXPENSE; subcategoryId = 0L }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = { Text(stringResource(R.string.transaction_type_expense)) }
                )
                SegmentedButton(
                    selected = type == TransactionType.INCOME,
                    onClick = {
                        if (type != TransactionType.INCOME) { type = TransactionType.INCOME; subcategoryId = 0L }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = { Text(stringResource(R.string.transaction_type_income)) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(stringResource(R.string.transactions_amount)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            AccountDropdown(
                label = stringResource(R.string.transactions_account),
                accounts = accounts,
                selectedId = accountId,
                onSelect = { accountId = it }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SubcategoryDropdown(
                subcategories = subcategories,
                selectedId = subcategoryId,
                onSelect = { subcategoryId = it }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = dayText,
                onValueChange = { dayText = it.filter { c -> c.isDigit() }.take(2) },
                label = { Text(stringResource(R.string.planning_day_of_month)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        val day = dayText.toIntOrNull()?.coerceIn(1, 31) ?: 1
                        if (accountId > 0 && subcategoryId > 0 && amt > 0) {
                            onSave(type, amt, accountId, subcategoryId, day)
                        }
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
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
                Icon(Icons.Default.ExpandMore, contentDescription = null)
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
                            Text(text = sub.name, style = MaterialTheme.typography.bodyLarge)
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
private fun AccountDropdown(
    label: String,
    accounts: List<AccountEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val selected = accounts.find { it.id == selectedId }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(Icons.Default.ExpandMore, contentDescription = null)
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
                    accounts.forEach { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(acc.id)
                                    showPicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = acc.name, style = MaterialTheme.typography.bodyLarge)
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
