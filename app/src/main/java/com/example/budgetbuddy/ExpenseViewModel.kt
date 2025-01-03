package com.example.budgetbuddy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetbuddy.database.AppDatabase
import com.example.budgetbuddy.database.ExpenseDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseDao: ExpenseDao = AppDatabase.getDatabase(application).expenseDao()

    val expenses: Flow<List<Expense>> = expenseDao.getAllExpenses()


    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.insertExpense(expense)
        }
    }


}

