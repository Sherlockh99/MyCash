package com.sh.mycash.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sh.mycash.data.local.dao.AccountDao
import com.sh.mycash.data.local.dao.BudgetDao
import com.sh.mycash.data.local.dao.CategoryDao
import com.sh.mycash.data.local.dao.RecurringTransactionDao
import com.sh.mycash.data.local.dao.SubcategoryDao
import com.sh.mycash.data.local.dao.TransactionDao
import com.sh.mycash.data.local.entity.AccountEntity
import com.sh.mycash.data.local.entity.BudgetEntity
import com.sh.mycash.data.local.entity.CategoryEntity
import com.sh.mycash.data.local.entity.RecurringTransactionEntity
import com.sh.mycash.data.local.entity.SubcategoryEntity
import com.sh.mycash.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        SubcategoryEntity::class,
        TransactionEntity::class,
        RecurringTransactionEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subcategoryDao(): SubcategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        private const val DATABASE_NAME = "mycash.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
