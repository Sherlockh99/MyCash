package com.sh.mycash.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sh.mycash.data.local.entity.CategoryEntity
import com.sh.mycash.data.local.entity.CategoryType
import com.sh.mycash.data.local.entity.SubcategoryEntity
import com.sh.mycash.data.repository.CategoryRepository
import com.sh.mycash.data.repository.CategoryWithSubcategories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _selectedType = MutableStateFlow(CategoryType.EXPENSE)
    val selectedType: StateFlow<CategoryType> = _selectedType.asStateFlow()

    val categories: StateFlow<List<CategoryWithSubcategories>> = _selectedType
        .flatMapLatest { type -> repository.getCategoriesWithSubcategories(type) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showCategoryDialog = MutableStateFlow<CategoryDialogState?>(null)
    val showCategoryDialog: StateFlow<CategoryDialogState?> = _showCategoryDialog.asStateFlow()

    private val _showSubcategoryDialog = MutableStateFlow<SubcategoryDialogState?>(null)
    val showSubcategoryDialog: StateFlow<SubcategoryDialogState?> = _showSubcategoryDialog.asStateFlow()

    private val _showDeleteCategoryConfirm = MutableStateFlow<CategoryEntity?>(null)
    val showDeleteCategoryConfirm: StateFlow<CategoryEntity?> = _showDeleteCategoryConfirm.asStateFlow()

    private val _showDeleteSubcategoryConfirm = MutableStateFlow<SubcategoryEntity?>(null)
    val showDeleteSubcategoryConfirm: StateFlow<SubcategoryEntity?> = _showDeleteSubcategoryConfirm.asStateFlow()

    fun setType(type: CategoryType) {
        _selectedType.value = type
    }

    fun onAddCategoryClick() {
        _showCategoryDialog.value = CategoryDialogState(
            id = null,
            name = "",
            type = _selectedType.value
        )
    }

    fun onEditCategoryClick(category: CategoryEntity) {
        _showCategoryDialog.value = CategoryDialogState(
            id = category.id,
            name = category.name,
            type = category.type
        )
    }

    fun onDeleteCategoryClick(category: CategoryEntity) {
        _showDeleteCategoryConfirm.value = category
    }

    fun onAddSubcategoryClick(category: CategoryEntity) {
        _showSubcategoryDialog.value = SubcategoryDialogState(
            id = null,
            categoryId = category.id,
            categoryName = category.name,
            name = ""
        )
    }

    fun onEditSubcategoryClick(subcategory: SubcategoryEntity, categoryName: String) {
        _showSubcategoryDialog.value = SubcategoryDialogState(
            id = subcategory.id,
            categoryId = subcategory.categoryId,
            categoryName = categoryName,
            name = subcategory.name
        )
    }

    fun onDeleteSubcategoryClick(subcategory: SubcategoryEntity) {
        _showDeleteSubcategoryConfirm.value = subcategory
    }

    fun saveCategory(state: CategoryDialogState) {
        viewModelScope.launch {
            if (state.name.isBlank()) return@launch
            val entity = CategoryEntity(
                id = state.id ?: 0,
                name = state.name.trim(),
                type = state.type
            )
            if (state.id != null) {
                repository.updateCategory(entity)
            } else {
                repository.insertCategory(entity)
            }
            _showCategoryDialog.value = null
        }
    }

    fun saveSubcategory(state: SubcategoryDialogState) {
        viewModelScope.launch {
            if (state.name.isBlank()) return@launch
            val entity = SubcategoryEntity(
                id = state.id ?: 0,
                categoryId = state.categoryId,
                name = state.name.trim()
            )
            if (state.id != null) {
                repository.updateSubcategory(entity)
            } else {
                repository.insertSubcategory(entity)
            }
            _showSubcategoryDialog.value = null
        }
    }

    fun confirmDeleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _showDeleteCategoryConfirm.value = null
        }
    }

    fun confirmDeleteSubcategory(subcategory: SubcategoryEntity) {
        viewModelScope.launch {
            repository.deleteSubcategory(subcategory)
            _showDeleteSubcategoryConfirm.value = null
        }
    }

    fun dismissCategoryDialog() {
        _showCategoryDialog.value = null
    }

    fun dismissSubcategoryDialog() {
        _showSubcategoryDialog.value = null
    }

    fun dismissDeleteCategoryConfirm() {
        _showDeleteCategoryConfirm.value = null
    }

    fun dismissDeleteSubcategoryConfirm() {
        _showDeleteSubcategoryConfirm.value = null
    }
}

data class CategoryDialogState(
    val id: Long?,
    val name: String,
    val type: CategoryType
)

data class SubcategoryDialogState(
    val id: Long?,
    val categoryId: Long,
    val categoryName: String,
    val name: String
)

class CategoriesViewModelFactory(
    private val repository: CategoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoriesViewModel(repository) as T
    }
}
