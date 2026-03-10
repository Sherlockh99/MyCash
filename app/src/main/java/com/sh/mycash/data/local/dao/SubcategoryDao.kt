package com.sh.mycash.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sh.mycash.data.local.entity.SubcategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubcategoryDao {

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY name")
    fun getByCategoryId(categoryId: Long): Flow<List<SubcategoryEntity>>

    @Query("SELECT * FROM subcategories ORDER BY name")
    fun getAll(): Flow<List<SubcategoryEntity>>

    @Query("SELECT * FROM subcategories WHERE id = :id")
    suspend fun getById(id: Long): SubcategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subcategory: SubcategoryEntity): Long

    @Update
    suspend fun update(subcategory: SubcategoryEntity)

    @Delete
    suspend fun delete(subcategory: SubcategoryEntity)

    @Query("DELETE FROM subcategories")
    suspend fun deleteAll()
}
