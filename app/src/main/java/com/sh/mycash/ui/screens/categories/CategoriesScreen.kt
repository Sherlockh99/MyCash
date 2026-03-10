package com.sh.mycash.ui.screens.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.sh.mycash.data.local.entity.CategoryEntity
import com.sh.mycash.data.local.entity.CategoryType
import com.sh.mycash.data.local.entity.SubcategoryEntity
import com.sh.mycash.data.repository.CategoryRepository
import com.sh.mycash.data.repository.CategoryWithSubcategories

@Composable
fun CategoriesScreen(
    onBackClick: () -> Unit,
    viewModel: CategoriesViewModel = viewModel(
        factory = CategoriesViewModelFactory(
            CategoryRepository(
                (LocalContext.current.applicationContext as MyCashApplication).database.categoryDao(),
                (LocalContext.current.applicationContext as MyCashApplication).database.subcategoryDao()
            )
        )
    )
) {
    val categories by viewModel.categories.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val categoryDialog by viewModel.showCategoryDialog.collectAsState()
    val subcategoryDialog by viewModel.showSubcategoryDialog.collectAsState()
    val deleteCategoryConfirm by viewModel.showDeleteCategoryConfirm.collectAsState()
    val deleteSubcategoryConfirm by viewModel.showDeleteSubcategoryConfirm.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Text(
                        text = stringResource(R.string.screen_categories),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    SegmentedButton(
                        selected = selectedType == CategoryType.EXPENSE,
                        onClick = { viewModel.setType(CategoryType.EXPENSE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(stringResource(R.string.categories_type_expense))
                    }
                    SegmentedButton(
                        selected = selectedType == CategoryType.INCOME,
                        onClick = { viewModel.setType(CategoryType.INCOME) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(stringResource(R.string.categories_type_income))
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddCategoryClick() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        if (categories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.categories_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.category.id }) { categoryWithSub ->
                    CategoryCard(
                        categoryWithSub = categoryWithSub,
                        onAddSubcategory = { viewModel.onAddSubcategoryClick(categoryWithSub.category) },
                        onEditCategory = { viewModel.onEditCategoryClick(categoryWithSub.category) },
                        onDeleteCategory = { viewModel.onDeleteCategoryClick(categoryWithSub.category) },
                        onEditSubcategory = { sub -> viewModel.onEditSubcategoryClick(sub, categoryWithSub.category.name) },
                        onDeleteSubcategory = { viewModel.onDeleteSubcategoryClick(it) }
                    )
                }
            }
        }
    }

    categoryDialog?.let { state ->
        CategoryEditDialog(
            state = state,
            onDismiss = { viewModel.dismissCategoryDialog() },
            onSave = { viewModel.saveCategory(it) }
        )
    }

    subcategoryDialog?.let { state ->
        SubcategoryEditDialog(
            state = state,
            onDismiss = { viewModel.dismissSubcategoryDialog() },
            onSave = { viewModel.saveSubcategory(it) }
        )
    }

    deleteCategoryConfirm?.let { category ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteCategoryConfirm() },
            title = { Text(stringResource(R.string.categories_delete_category_title)) },
            text = { Text(stringResource(R.string.categories_delete_message, category.name)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteCategory(category) }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteCategoryConfirm() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    deleteSubcategoryConfirm?.let { subcategory ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteSubcategoryConfirm() },
            title = { Text(stringResource(R.string.categories_delete_subcategory_title)) },
            text = { Text(stringResource(R.string.categories_delete_message, subcategory.name)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteSubcategory(subcategory) }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteSubcategoryConfirm() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryCard(
    categoryWithSub: CategoryWithSubcategories,
    onAddSubcategory: () -> Unit,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit,
    onEditSubcategory: (SubcategoryEntity) -> Unit,
    onDeleteSubcategory: (SubcategoryEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
                Text(
                    text = categoryWithSub.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onAddSubcategory) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
                IconButton(onClick = onEditCategory) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
                IconButton(onClick = onDeleteCategory) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(start = 48.dp, end = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categoryWithSub.subcategories.forEach { sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onEditSubcategory(sub) },
                                modifier = Modifier.padding(0.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                            IconButton(
                                onClick = { onDeleteSubcategory(sub) },
                                modifier = Modifier.padding(0.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryEditDialog(
    state: CategoryDialogState,
    onDismiss: () -> Unit,
    onSave: (CategoryDialogState) -> Unit
) {
    var name by remember(state) { mutableStateOf(state.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (state.id != null) stringResource(R.string.categories_edit_title)
                else stringResource(R.string.categories_add_title)
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.categories_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(state.copy(name = name)) }) {
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

@Composable
private fun SubcategoryEditDialog(
    state: SubcategoryDialogState,
    onDismiss: () -> Unit,
    onSave: (SubcategoryDialogState) -> Unit
) {
    var name by remember(state) { mutableStateOf(state.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (state.id != null) stringResource(R.string.categories_edit_subcategory_title)
                else stringResource(R.string.categories_add_subcategory_title, state.categoryName)
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.categories_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(state.copy(name = name)) }) {
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
