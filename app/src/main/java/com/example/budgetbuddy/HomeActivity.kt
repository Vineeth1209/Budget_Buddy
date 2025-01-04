package com.example.budgetbuddy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.budgetbuddy.database.Budget
import com.example.budgetbuddy.ui.theme.BudgetBuddyTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : ComponentActivity() {
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetBuddyTheme {
                HomeScreen(expenseViewModel, budgetViewModel)  // Ensure this call matches the function signature
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(expenseViewModel: ExpenseViewModel, budgetViewModel: BudgetViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Budget Buddy") })
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index -> selectedTab = index }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> SummaryScreen(expenseViewModel, budgetViewModel) // Updated to SummaryScreen
                1 -> ExpensesScreen(expenseViewModel)
                2 -> BudgetScreen(budgetViewModel)
                3 -> ProfileScreen()
            }
        }
    }
}

@Composable
fun SummaryScreen(expenseViewModel: ExpenseViewModel, budgetViewModel: BudgetViewModel) {
    val context = LocalContext.current
    val expenses by expenseViewModel.expenses.collectAsState(initial = emptyList())
    val budgets by budgetViewModel.allBudgets.observeAsState(emptyList())

    // Calculate summary values
    val totalExpenses = expenses.sumOf { it.amount }
    val totalBudget = budgets.lastOrNull()?.amount ?: 0.0
    val remainingBudget = totalBudget - totalExpenses

    // Check if daily expenses exceed £150
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayExpenses = expenses.filter { it.date.startsWith(today) }.sumOf { it.amount }

    if (todayExpenses > 150) {
        Toast.makeText(context, "Alert: Your daily expenses exceed £150!", Toast.LENGTH_LONG).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Summary", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Budget: £${String.format("%.2f", totalBudget)}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Expenses: £${String.format("%.2f", totalExpenses)}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Remaining Budget: £${String.format("%.2f", remainingBudget)}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}




    @Composable
    fun ExpensesScreen(expenseViewModel: ExpenseViewModel) {
        val context = LocalContext.current
        var expenseName by remember { mutableStateOf("") }
        var expenseAmount by remember { mutableStateOf("") }
        val expenses by expenseViewModel.expenses.collectAsState(initial = emptyList())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextField(
                value = expenseName,
                onValueChange = { expenseName = it },
                label = { Text("Expense Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = expenseAmount,
                onValueChange = { expenseAmount = it },
                label = { Text("Expense Amount (£)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (expenseName.isNotEmpty() && expenseAmount.isNotEmpty()) {
                        val amount = expenseAmount.toDoubleOrNull()
                        if (amount != null) {
                            val currentDate = System.currentTimeMillis()
                            val expense = Expense(
                                name = expenseName,
                                amount = amount,
                                date = currentDate.toString()
                            )
                            expenseViewModel.addExpense(expense)
                            Toast.makeText(context, "Expense added successfully", Toast.LENGTH_SHORT).show()
                            expenseName = ""
                            expenseAmount = ""
                        } else {
                            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add Expense")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Expense History", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses) { expense ->
                    ExpenseItem(expense)
                }
            }
        }
    }

    @Composable
    fun ExpenseItem(expense: Expense) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = expense.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "£${String.format("%.2f", expense.amount)}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    @Composable
    fun BudgetScreen(budgetViewModel: BudgetViewModel) {
        val context = LocalContext.current
        var budgetAmount by remember { mutableStateOf("") }
        val budgets by budgetViewModel.allBudgets.observeAsState(emptyList())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add Budget", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = budgetAmount,
                onValueChange = { budgetAmount = it },
                label = { Text("Enter Budget Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val amount = budgetAmount.toDoubleOrNull()
                if (amount != null) {
                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    )
                    budgetViewModel.addBudget(amount, currentDate)
                    Toast.makeText(context, "Budget Added", Toast.LENGTH_SHORT).show()
                    budgetAmount = ""
                } else {
                    Toast.makeText(context, "Invalid Amount", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Add")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Budget History", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(budgets) { budget ->
                    BudgetItem(budget)
                }
            }
        }
    }

    @Composable
    fun BudgetItem(budget: Budget) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "£${String.format("%.2f", budget.amount)}", style = MaterialTheme.typography.titleMedium)
                Text(text = budget.date, style = MaterialTheme.typography.bodySmall)
            }
        }
    }



    @Composable
    fun ProfileScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Profile", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Name: Vineeth Kumar", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Student ID: S3117755", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Email: vineethbunny420@gmail.com", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("About", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Budget Buddy is an intuitive application designed to help users manage their personal finances efficiently. Track your expenses, set budgets, and gain insights into your spending habits with ease.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    @Composable
    fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Expenses") },
                label = { Text("Expenses") },
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Budget") },
                label = { Text("Budget") },
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                label = { Text("Profile") },
                selected = selectedTab == 3,
                onClick = { onTabSelected(3) }
            )
        }
    }
