package com.sh.mycash.data.repository

import com.sh.mycash.data.local.dao.CategoryDao
import com.sh.mycash.data.local.dao.SubcategoryDao
import com.sh.mycash.data.local.entity.CategoryEntity
import com.sh.mycash.data.local.entity.CategoryType
import com.sh.mycash.data.local.entity.SubcategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val subcategoryDao: SubcategoryDao
) {

    fun getCategoriesWithSubcategories(type: CategoryType): Flow<List<CategoryWithSubcategories>> {
        return combine(
            categoryDao.getByType(type),
            subcategoryDao.getAll()
        ) { categories, allSubcategories ->
            categories.map { category ->
                val subcats = allSubcategories.filter { it.categoryId == category.id }
                    .sortedBy { it.name }
                CategoryWithSubcategories(category, subcats)
            }
        }
    }

    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun getCategoryById(id: Long) = categoryDao.getById(id)

    suspend fun insertSubcategory(subcategory: SubcategoryEntity): Long =
        subcategoryDao.insert(subcategory)

    suspend fun updateSubcategory(subcategory: SubcategoryEntity) =
        subcategoryDao.update(subcategory)

    suspend fun deleteSubcategory(subcategory: SubcategoryEntity) =
        subcategoryDao.delete(subcategory)

    suspend fun getSubcategoryById(id: Long) = subcategoryDao.getById(id)
}

data class CategoryWithSubcategories(
    val category: CategoryEntity,
    val subcategories: List<SubcategoryEntity>
)
