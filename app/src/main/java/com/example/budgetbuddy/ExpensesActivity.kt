package com.example.budgetbuddy

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.budgetbuddy.ui.theme.BudgetBuddyTheme
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExpensesActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetBuddyTheme {
                ExpensesScreen()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @Composable
    fun ExpensesScreen() {
        var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }

        // Fetch expenses from Firestore when the screen is first launched
        LaunchedEffect(Unit) {
            getExpenses { fetchedExpenses ->
                expenses = fetchedExpenses
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header for the Expenses screen
            Text("Expenses", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            // List of expenses using LazyColumn
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(expenses) { expense ->
                    ExpenseItem(expense)
                }
            }
        }
    }

    // Function to fetch expenses from Firestore
    private fun getExpenses(callback: (List<Expense>) -> Unit) {
        db.collection("expenses")
            .get()
            .addOnSuccessListener { result ->
                val expensesList = result.map { document ->
                    val name = document.getString("name") ?: ""
                    val amount = document.getDouble("amount") ?: 0.0
                    val timestamp = document.getLong("timestamp") ?: System.currentTimeMillis()
                    val formattedTimestamp = formatDate(timestamp)
                    Expense(name, amount, formattedTimestamp)
                }
                callback(expensesList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch expenses", Toast.LENGTH_SHORT).show()
            }
    }

    // Format the timestamp to a readable date and time format
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Expense data model
    data class Expense(
        val name: String,
        val amount: Double,
        val timestamp: String
    )

    // Composable to display a single expense
    @Composable
    fun ExpenseItem(expense: Expense) {
        // Use a Card without elevation, with background color and rounded corners
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray),
            shape = MaterialTheme.shapes.medium // Rounded corners
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Expense: ${expense.name}", style = MaterialTheme.typography.bodyLarge)
                Text("Amount: £${String.format("%.2f", expense.amount)}", style = MaterialTheme.typography.bodyMedium)
                Text("Added on: ${expense.timestamp}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
