package com.example.budgetbuddy.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.budgetbuddy.Expense

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}


