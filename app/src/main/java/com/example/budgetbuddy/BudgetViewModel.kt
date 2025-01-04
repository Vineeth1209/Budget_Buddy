package com.example.budgetbuddy

import android.app.Application
import androidx.lifecycle.*
import com.example.budgetbuddy.database.AppDatabase
import com.example.budgetbuddy.database.Budget
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val budgetDao = AppDatabase.getDatabase(application).budgetDao()
    val allBudgets: LiveData<List<Budget>> = budgetDao.getAllBudgets().asLiveData()

    fun addBudget(amount: Double, date: String) {
        val budget = Budget(amount = amount, date = date)
        viewModelScope.launch {
            budgetDao.insertBudget(budget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget)
        }
    }
}
