package com.example.budgetbuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetbuddy.ui.theme.BudgetBuddyTheme

class ConsentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetBuddyTheme {
                ConsentScreen(onConsentGiven = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }, onConsentDenied = {
                    finish()
                })
            }
        }
    }
}

@Composable
fun ConsentScreen(onConsentGiven: () -> Unit, onConsentDenied: () -> Unit) {
    val isAgreed = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Consent Agreement",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "We value your privacy. By using this app, you agree to our terms and conditions, including data collection and processing in compliance with GDPR regulations."
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    isAgreed.value = true
                    onConsentGiven()
                }) {
                    Text("Agree")
                }

                Button(onClick = {
                    isAgreed.value = false
                    onConsentDenied()
                }) {
                    Text("Disagree")
                }
            }
        }
    }
}
