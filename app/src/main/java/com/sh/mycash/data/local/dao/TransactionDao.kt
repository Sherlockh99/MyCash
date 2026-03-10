package com.sh.mycash.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sh.mycash.data.local.entity.TransactionEntity
import com.sh.mycash.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR targetAccountId = :accountId ORDER BY date DESC")
    fun getByAccountId(accountId: Long): Flow<List<TransactionEntity>>

    @Query(
        """SELECT * FROM transactions 
        WHERE type = 'EXPENSE' 
        AND date >= :startDate AND date <= :endDate 
        ORDER BY date DESC"""
    )
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query(
        """
        SELECT COALESCE(SUM(
            CASE 
                WHEN type = 'INCOME' AND accountId = :accountId THEN amount
                WHEN type = 'EXPENSE' AND accountId = :accountId THEN -amount
                WHEN type = 'TRANSFER' AND accountId = :accountId THEN -amount
                WHEN type = 'TRANSFER' AND targetAccountId = :accountId THEN amount
                ELSE 0
            END
        ), 0) FROM transactions
        """
    )
    suspend fun getBalanceDelta(accountId: Long): Double
}
