package com.example.budgetbuddy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetbuddy.ui.theme.BudgetBuddyTheme
import com.google.firebase.firestore.FirebaseFirestore

class SetBudgetActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetBuddyTheme {
                SetBudgetScreen()
            }
        }
    }

    @Composable
    fun SetBudgetScreen() {
        var budgetAmount by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = budgetAmount,
                onValueChange = { budgetAmount = it },
                label = { Text("Set Budget Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (budgetAmount.isNotEmpty()) {
                        setBudget(budgetAmount.toDouble())
                    } else {
                        Toast.makeText(this@SetBudgetActivity, "Field cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Set Budget")
            }
        }
    }

    private fun setBudget(amount: Double) {
        val budget = mapOf("totalBudget" to amount)
        db.collection("budget")
            .document("currentBudget")
            .set(budget)
            .addOnSuccessListener {
                Toast.makeText(this, "Budget set successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity after success
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to set budget", Toast.LENGTH_SHORT).show()
            }
    }
}
