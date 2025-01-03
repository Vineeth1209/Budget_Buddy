package com.example.budgetbuddy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.budgetbuddy.ui.theme.BudgetBuddyTheme

class HomeActivity : ComponentActivity() {
    private val expenseViewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetBuddyTheme {
                HomeScreen(expenseViewModel)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomeScreen(expenseViewModel: ExpenseViewModel) {
        val expenses by expenseViewModel.expenses.collectAsState(initial = emptyList())
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
                    0 -> HomeContent()
                    1 -> ExpensesScreen(expenses, expenseViewModel)
                    2 -> BudgetSummaryScreen(expenses)
                    3 -> ProfileScreen()
                }
            }
        }
    }

    @Composable
    fun HomeContent() {
        // Sample data for overall budget summary
        val totalBudget = 1000.0
        val totalSpending = 400.0
        val remainingBudget = totalBudget - totalSpending

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Overall Summary", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            PieChart(
                data = listOf(totalSpending.toFloat(), remainingBudget.toFloat()),
                colors = listOf(Color.Red, Color.Green),
                labels = listOf("Spent", "Remaining"),
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp)) // Adjusted the height to move the pie chart higher

            // Adding description with more colors
            Text(
                text = "Total Budget: £${String.format("%.2f", totalBudget)}",
                color = Color.Blue,  // Blue color for Total Budget
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Total Spending: £${String.format("%.2f", totalSpending)}",
                color = Color.Red,  // Red color for Total Spending
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Remaining Budget: £${String.format("%.2f", remainingBudget)}",
                color = Color.Green,  // Green color for Remaining Budget
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }


    @Composable
    fun ExpensesScreen(expenses: List<Expense>, expenseViewModel: ExpenseViewModel) {
        val context = LocalContext.current
        var expenseName by remember { mutableStateOf("") }
        var expenseAmount by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Input fields for adding expenses
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

            // Expense history
            Text("Expense History", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses) { expense ->
                    ExpenseItem(expense)
                }
            }
        }
    }

    @Composable
    fun BudgetSummaryScreen(expenses: List<Expense>) {
        val totalBudget = 1000.0
        val totalSpending = expenses.sumOf { it.amount }
        val remainingBudget = totalBudget - totalSpending

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Overall Summary", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            PieChart(
                data = listOf(totalSpending.toFloat(), remainingBudget.toFloat()),
                colors = listOf(Color.Red, Color.Green),
                labels = listOf("Spent", "Remaining"),
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp)) // Adjusted the height to move the pie chart higher

            // Adding description with more colors
            Text(
                text = "Total Budget: £${String.format("%.2f", totalBudget)}",
                color = Color.Blue,  // Blue color for Total Budget
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Total Spending: £${String.format("%.2f", totalSpending)}",
                color = Color.Red,  // Red color for Total Spending
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Remaining Budget: £${String.format("%.2f", remainingBudget)}",
                color = Color.Green,  // Green color for Remaining Budget
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    @Composable
    fun PieChart(
        data: List<Float>,
        colors: List<Color>,
        labels: List<String>,
        modifier: Modifier = Modifier
    ) {
        Canvas(modifier = modifier) {
            val total = data.sum()
            var startAngle = 0f

            data.forEachIndexed { index, value ->
                val sweepAngle = (value / total) * 360f
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }

        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            labels.forEachIndexed { index, label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colors[index])
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
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
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "£${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    @Composable
    fun ProfileScreen() {
        // Start with a Column layout to structure the profile screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Profile Information
            Text("Profile", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Name: Vineeth Kumar", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Student ID: S3117755", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Email: vineethbunny420@gmail.com", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            // About Section
            Spacer(modifier = Modifier.height(16.dp))
            Text("About", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Budget Buddy is an intuitive application designed to help users manage their personal finances efficiently. Track your expenses, set budgets, and gain insights into your spending habits with ease.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Settings and Privacy Section
            Spacer(modifier = Modifier.height(16.dp))
            Text("Options", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))
            // Call the composable function for each option
            SettingsOption(optionText = "Settings")
            SettingsOption(optionText = "Privacy Policy (GDPR Compliant)")
            SettingsOption(optionText = "Terms and Conditions")
        }
    }

    @Composable
    fun SettingsOption(optionText: String) {
        val context = LocalContext.current
        // Provide a clickable row for each option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable {
                    // Trigger Toast within the composable context
                    Toast.makeText(context, "$optionText clicked", Toast.LENGTH_SHORT).show()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(optionText, style = MaterialTheme.typography.bodyLarge)
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
}
