package com.example.budgetbuddy

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.budgetbuddy.database.Budget
import com.example.budgetbuddy.ui.theme.BudgetBuddyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : ComponentActivity() {
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(application)
    }

    private var userFullName by mutableStateOf("")
    private var capturedImageUri by mutableStateOf<Uri?>(null)

    private lateinit var imageFileUri: Uri

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                capturedImageUri = imageFileUri
                Toast.makeText(this, "Image captured successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch user full name from Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("fullName")) {
                        userFullName = document.getString("fullName") ?: "User"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                }
        }

        setContent {
            BudgetBuddyTheme {
                HomeScreen(
                    expenseViewModel,
                    budgetViewModel,
                    userFullName,
                    capturedImageUri,
                    onCameraClick = { openCamera() }
                )
            }
        }
    }

    private fun openCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                val imageFile = createImageFile()
                imageFileUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    imageFile
                )
                cameraLauncher.launch(imageFileUri)
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
            }
        }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    expenseViewModel: ExpenseViewModel,
    budgetViewModel: BudgetViewModel,
    userFullName: String,
    capturedImageUri: Uri?,
    onCameraClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Budget Buddy",
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                },
                actions = {
                    IconButton(onClick = onCameraClick) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Capture Image")
                    }
                }
            )
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
                0 -> SummaryScreen(expenseViewModel, budgetViewModel, userFullName, capturedImageUri)
                1 -> ExpensesScreen(expenseViewModel)
                2 -> BudgetScreen(budgetViewModel)
                3 -> ProfileScreen(userFullName)
            }
        }
    }
}

@Composable
fun SummaryScreen(
    expenseViewModel: ExpenseViewModel,
    budgetViewModel: BudgetViewModel,
    userFullName: String,
    capturedImageUri: Uri?
) {
    val context = LocalContext.current
    val expenses by expenseViewModel.expenses.collectAsState(initial = emptyList())
    val budgets by budgetViewModel.allBudgets.observeAsState(emptyList())

    var showDailyExpenseAlert by remember { mutableStateOf(false) }
    var showRemainingBudgetWarning by remember { mutableStateOf(false) }
    var alertChecked by remember { mutableStateOf(false) }

    val totalExpenses = expenses.sumOf { it.amount }
    val totalBudget = budgets.lastOrNull()?.amount ?: 0.0
    val remainingBudget = totalBudget - totalExpenses
    val budgetProgress = (totalExpenses / totalBudget).coerceIn(0.0, 1.0).toFloat()

    val currentDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    val recentExpense = expenses.lastOrNull()?.let { "${it.name}: £${String.format("%.2f", it.amount)}" }
        ?: "No recent expenses"
    val highestExpense = expenses.maxByOrNull { it.amount }?.let { "${it.name}: £${String.format("%.2f", it.amount)}" }
        ?: "No data"

    if (!alertChecked) {
        if (expenses.filter { it.date == currentDate }.sumOf { it.amount } > 150) showDailyExpenseAlert = true
        if (remainingBudget < 10) showRemainingBudgetWarning = true
        alertChecked = true
    }

    BackgroundWrapper {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display captured image if available
            capturedImageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Captured Image",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text("Hello, $userFullName!", style = MaterialTheme.typography.headlineMedium)
            Text("Today is $currentDate", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Budget: £${String.format("%.2f", totalBudget)}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Expenses: £${String.format("%.2f", totalExpenses)}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Remaining Budget: £${String.format("%.2f", remainingBudget)}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = budgetProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (budgetProgress < 0.5) Color.Green else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Transactions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recent Transaction", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(recentExpense, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Budget Insights Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Budget Insights", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Highest Expense: $highestExpense", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (showDailyExpenseAlert) {
            AlertDialog(
                onDismissRequest = { showDailyExpenseAlert = false },
                confirmButton = {
                    Button(onClick = { showDailyExpenseAlert = false }) { Text("OK") }
                },
                title = { Text("Daily Expense Alert") },
                text = { Text("Your daily expenses have exceeded £150!") }
            )
        }

        if (showRemainingBudgetWarning) {
            AlertDialog(
                onDismissRequest = { showRemainingBudgetWarning = false },
                confirmButton = {
                    Button(onClick = { showRemainingBudgetWarning = false }) { Text("OK") }
                },
                title = { Text("Remaining Budget Warning") },
                text = { Text("Your remaining budget is below £10!") }
            )
        }
    }
}


@Composable
fun BackgroundWrapper(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background), // Replace with your actual background resource ID
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80FFFFFF)) // 50% opacity white overlay to lighten
        )
        content()
    }
}





@Composable
fun ExpensesScreen(expenseViewModel: ExpenseViewModel) {
    BackgroundWrapper {
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
                            val currentDate =
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val expense = Expense(
                                name = expenseName,
                                amount = amount,
                                date = currentDate,

                                )
                            expenseViewModel.addExpense(expense)
                            Toast.makeText(
                                context,
                                "Expense added successfully",
                                Toast.LENGTH_SHORT
                            ).show()
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
        BackgroundWrapper {
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
                        val currentDate =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
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
    fun ProfileScreen(userFullName: String) {
        BackgroundWrapper {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text("Profile", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Name: $userFullName", style = MaterialTheme.typography.bodyLarge)
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


